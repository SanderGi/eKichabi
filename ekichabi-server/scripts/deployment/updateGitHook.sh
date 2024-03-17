#!/bin/bash

# assume you already have already set up the ssh access to PythonAnywhere host
# run this script to copy over the pythonAnywhereHook to pythonAnywhere
# NOTE: you should run this only when pythonAnywhereHook is updated
scp gitHook.sh SECRET@ssh.pythonanywhere.com:SECRET/hooks/ekichabi.git/hooks/post-receive
echo "successfully update git hook in PythonAnywhere 🎉"

