#!/bin/bash

# print out usage of the script if argument is entered incorrectly
function usage() {
    echo $0: usage: sh ./productionGitPush [makemigration, updatewhitelist];
    echo "try again with valid arguments"
    exit 1;
}

# for now, if more than one argument is provided, the command is invalid
if [[ $# -gt 1 ]]; then
   usage
fi

# update this list in the future if necessary when more configuration options are added
VALID_ARGUMENTS=$( [[ $1 == "makemigration" || $1 == updatewhitelist ]] && echo "true" || echo "false" )

if [[ $# -eq 1 && $VALID_ARGUMENTS == "false" ]]; then
   usage
fi

echo -e "\nBefore deploying the code to PythonAnywhere, let's go through a brief checklist to make sure all the configurations are correct! \n"

while true; do
    read -p "Are you currently on the pythonAnywhere branch? If unsure, you can check by running 'git branch': " yn
    case $yn in
        [Yy]* ) 
            echo -e "Cool, moving on ✅ \n";
            break;;
        [Nn]* ) 
            echo "Please switch to pythonAnywhere branch first before deployment 👋";
            exit;;
        * ) echo "Please answer yes(y) or no(n).";;
    esac
done

while true; do
    read -p "Did you sync your branch with the development branch and make sure it's up to date? " yn
    case $yn in
        [Yy]* ) 
            echo -e "Cool, moving on ✅ \n";
            break;;
        [Nn]* ) 
            echo "Please sync the code with the comand 'git pull origin dev' 👋";
            exit;;
        * ) echo "Please answer yes(y) or no(n).";;
    esac
done

while true; do
    read -p "Did you run tests on the application and make sure everything works ok locally? " yn
    case $yn in
        [Yy]* ) 
            echo -e "Cool, moving on ✅ \n";
            break;;
        [Nn]* ) 
            echo "Please run the test first before deploying 👋";
            exit;;
        * ) echo "Please answer yes(y) or no(n).";;
    esac
done

MIGRATION_PROMPT_DONE="false"
MIGRATION_PROMPT_ANS="No"
while [[ $MIGRATION_PROMPT_DONE == "false" ]]; do
    read -p "Have there been any changes made to model.py (adding new fields, etc)? " yn
    case $yn in
        [Yy]* )
            MIGRATION_PROMPT_ANS="Yes"; 
            while true; do
                read -p "Are you running this deployment script with 'makemigration' argument? " yn2
                case $yn2 in
                    [Yy]* ) 
                        echo -e "Cool, moving on ✅\n";
                        MIGRATION_PROMPT_DONE="true";
                        break;;
                    [Nn]* ) 
                        echo "Please rerun this script with the 'makemigration' argument 👋";
                        exit;;
                    * ) echo "Please answer yes(y) or no(n).";;
                esac
            done;;
        [Nn]* ) 
            MIGRATION_PROMPT_DONE="true";
            echo -e "Cool, moving on ✅\n";;
        * ) echo "Please answer yes(y) or no(n).";;
    esac
done

CENSUS_DATA_CHANGE_PROMPT_DONE="false"
CENSUS_DATA_CHANGE_PROMPT_ANS="No"
while [[ $CENSUS_DATA_CHANGE_PROMPT_DONE == "false" ]]; do
    read -p "Have there been any changes made to the census data? " yn
    case $yn in
        [Yy]* ) 
            CENSUS_DATA_CHANGE_PROMPT_ANS="Yes"
            while true; do
                # since the migration script always runs the reset_db scropt, we don't make the distinction of the two
                read -p "Are you running this deployment script with 'makemigration' argument? " yn2
                case $yn2 in
                    [Yy]* ) 
                        echo -e "Cook, moving on ✅\n";
                        CENSUS_DATA_CHANGE_PROMPT_DONE="true";
                        break;;
                    [Nn]* ) 
                        echo "Please rerun this script with the 'makemigration' argument 👋";
                        exit;;
                    * ) echo "Please answer yes(y) or no(n).";;
                esac
            done;;
        [Nn]* ) 
            CENSUS_DATA_CHANGE_PROMPT_DONE="true";
            echo -e "Cool, moving on ✅\n";;
        * ) echo "Please answer yes(y) or no(n).";;
    esac
done

if [[ $MIGRATION_PROMPT_ANS == "No" && $CENSUS_DATA_CHANGE_PROMPT_ANS == "No" ]]
then
    WHITELIST_DATA_CHANGE_PROMPT_DONE="false"
    while [[ $WHITELIST_DATA_CHANGE_PROMPT_DONE == "false" ]]; do
        read -p "Have there been any updates made to the whitelisting data (new phone numbers added) ? " yn
        case $yn in
            [Yy]* ) 
                while true; do
                    # since the migration script always runs the reset_db scropt, we don't make the distinction of the two
                    read -p "Are you running this deployment script with the 'updatewhitelist' argument? " yn2
                    case $yn2 in
                        [Yy]* ) 
                            echo -e "Cool, moving on ✅\n";
                            WHITELIST_DATA_CHANGE_PROMPT_DONE="true";
                            break;;
                        [Nn]* ) 
                            echo "Please rerun this script with the 'updatewhitelist' argument? 👋";
                            exit;;
                        * ) echo "Please answer yes(y) or no(n).";;
                    esac
                done;;
            [Nn]* ) 
                WHITELIST_DATA_CHANGE_PROMPT_DONE="true";
                echo -e "Cool, moving on ✅ \n";;
            * ) echo "Please answer yes(y) or no(n).";;
        esac
    done
fi

GIT_HOOK_UPDATE_PROMPT_DONE="false"
while [[ $GIT_HOOK_UPDATE_PROMPT_DONE == "false" ]]; do
    read -p "Have there been any changes made to git hook? " yn
    case $yn in
        [Yy]* ) 
            while true; do
                read -p "Ok, did you run the gitHookUpdate script already? " yn2
                case $yn2 in
                    [Yy]* )
                        GIT_HOOK_UPDATE_PROMPT_DONE="true";
                        echo -e "Cool, moving on ✅ \n";
                        break;;
                    [Nn]* ) 
                        echo "Please run gitHookUpdate script locally to sync the hook in PythonAnywhere! 👋";
                        exit;;
                    * ) echo "Please answer yes(y) or no(n).";;
                esac
            done;;
        [Nn]* )    
            GIT_HOOK_UPDATE_PROMPT_DONE="true";;
        * ) echo "Please answer yes(y) or no(n).";;
    esac
done

echo -e "\nThanks for going through the checklist! 😎\n"

# reflush local database and regenerate the pickled screen before deplpyment
echo "Resetting local database and regenerating pickled screen for deployment"
cd ..
cd ..
python manage.py generate_screens
echo "Done! ✅" 

# assume that your Gitlab remote should be named origin
# assume that you already have a local pythonAnywhere branch tracking master branch on pythonAnywhere
# your local pythonAnywhere should already be merged with the latest origin/dev
echo "Pushing to deployment..."
if [[ $# -eq 1 && $1 == "makemigration" ]]; then
    git push -u --push-option="MIGRATION" pythonanywhere pythonAnywhere:master
elif [[ $# -eq 1 && $1 == "updatewhitelist" ]]; then
    git push -u --push-option="WHITELIST" pythonanywhere pythonAnywhere:master
else    
    git push -u pythonanywhere pythonAnywhere:master 
fi

# force this branch to be in sync with dev branch again in case the user makes any change to the deployment branch locally
echo -e "\n----------------------------- Back on local -----------------------------"
echo -e "Resetting local pythonAnywhere branch to be in sync with origin/dev \n✅"
git reset --hard origin/dev

# if everything's ok, then we update gitlab's remote in the end
echo -e "\nPutting progress made in origin/dev to origin/pythonAnywhere on Gitlab ✅"
git push origin pythonAnywhere

echo -e "\n\nAll done. Make sure to check any command outputs that might indicate an error. Till next time 🎉🎉🎉"

