#!/bin/bash

# directory shortcuts
VIRTUAL_ENV=~/.virtualenvs/ekichabi-python3-virtualenv/bin/activate
PROJECT_DIR=~/Ekichabi
TMP_DIR=~/tmp 

echo -e "----------------------------- on PythonAnywhere -----------------------------"
echo -e "Git hook change detected, starting setup script 😎"
echo -e "Please look out for any error messages on your screen during the setup process 🛑\n"

if [[ $PRODUCTION_ENV != "true" ]]
then
    echo "Error, PRODUCTION_ENV environment variable not found. Check the remote server setting 🛑"
    exit 1
fi

if [[ -f "$PROJECT_DIR/requirements.txt" ]]
then
    # move old version of requirement.txt into the temp folder for comparison later
    echo "Copying requirements.txt of old version of the project to temp folder"
    cp $PROJECT_DIR/requirements.txt $TMP_DIR
    echo -e "Copying completed ✅\n"
else
    OLD_REQUIREMENT_MISSING="true"
    echo -e "Old version of requirements.txt missing, moving on...✅\n"
fi

 # overwrite the project directory and checkout the new version of the project
echo "Checking out the code in git repo to the project directory"
mkdir -p $PROJECT_DIR                                                                                                         
GIT_WORK_TREE=$PROJECT_DIR git checkout -f
echo -e "Checking out complete ✅\n"


# path to old and new version of the directory
OLD_PKGS=$TMP_DIR/requirements.txt
NEW_PKGS=$PROJECT_DIR/requirements.txt

# activate python virtual environment
echo "Activating python virtual environment for setup"
source $VIRTUAL_ENV
echo -e "Activation complete ✅\n"
 
# move into the project directory 
cd $PROJECT_DIR

# TODO: check environmental variable exists or not

# create log folder for logging (if it does not exist)
mkdir -p logs


if [[ $OLD_REQUIREMENT_MISSING != "true" ]]
then
    # check whether packages need to be installed or updated by comparing the old and new version of the requirement.txt
    REQUIREMENT_CHANGED=$(diff --ignore-blank-lines --ignore-all-space  $OLD_PKGS $NEW_PKGS 2>/dev/null) 
else
    REQUIREMENT_CHANGED="true"
fi

echo "Comparing old and new version of requirement.txt"
# if change in requirement.txt is detected, run pip install requirement.txt to update packages
if [[ ! -z "$REQUIREMENT_CHANGED" ]]
then
    echo "Changes in requirement.txt detected. Installing new packages..."    
    pip install -r requirements.txt
    echo -e "New packages have been installed successfully ✅"
else 
    echo -e "No changes detected in requirement.txt, moving on... ✅\n"
fi

if [[ $OLD_REQUIREMENT_MISSING != "true" ]]
then
    # remove the requirement.txt in the temp folder
    echo "Cleaning up temporary requirement.txt"
    rm $TMP_DIR/requirements.txt
    echo -e "Clean-up complete ✅\n"
fi
  
# if git_push_option signals migration needs to be run, run the necessary migration scripts
if [[ "$GIT_PUSH_OPTION_0" == "MIGRATION" ]]
then
    echo "Migration flag detected... "
    echo "1. Flushing all tables in database..."
    MIGRATION="TRUE" python manage.py flush --no-input
    echo "1. Done ✅"
    echo "2. Repopulating whitelisted phone number data..."
    python manage.py update_whitelist_db
    echo "2. Done, phone numbers have been updated ✅\n"
    echo "3. Repopulating database from csv census data - this might take a while..."
    MIGRATION="TRUE" python manage.py reset_db ./data/census_data_trimmed.csv
    echo "3. Done ✅"
fi

# if git_push_option signals whitelist needs to be updated, run the whitelist script
if [[ "$GIT_PUSH_OPTION_0" == "WHITELIST" ]]
then
    echo "Whitelist flag detected... "
    echo "Running update_whitelist_db script."
    python manage.py update_whitelist_db
    echo -e "Done, whitelist has been updated ✅\n"
fi

# deactivate the virtual environment
echo "Deactivating python virtual environment for setup"
deactivate
echo -e "Deactivation complete ✅\n"
  
# touch wsgi.py so that PythonAnywhere automatically reloads the website
# see: https://blog.pythonanywhere.com/87
echo -e "Triggering Pythonanywhere redeployment with wsgi ✅\n"
touch SECRET/wsgi.py

echo -e "Setup on pythonAnywhere all done 🎉"
