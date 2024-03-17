## Branches
* Use the `dev` branch for feature developments
* Use the `pythonAnywhere` branch for actual deployment, please do not make changes directly on this branch. Rather, all changes should be directly incorporated from the dev branch.
* `pythonAnywhere` and `dev` branch should never diverge (in other words, `pythonAnywhere` branch should always be behind or on par with `dev` in terms of commit history)


## Workflow

* For smaller features, such as changing a couple lines, fixing a small bug, or a feature that you can work through in one go, directly make changes to the `dev` branch. Once you finish, run `git push origin dev` to update the Gitlab’s remote `dev` branch.

* For larger features - ones that take at least a couple of days to complete, or a feature that you are working with other teammates or need feedback from others, create a separate branch locally by runnng `git checkout -b <branch>`, and then `git push -u origin <branch>` to create a remote-tracking branch on Gitlab as well. Once you are done with the development and all reviews for the branch are done, you can merge the feature branch back into the `dev` branch. To merge, switch to dev branch locally, do `git merge <branch>`, and once it is successfully merged, `git push origin dev`.

## Collaboration

* Reorganization on Gitlab’s tags and workflow
* New issues should be created after weekly meetings and marked with appropriate tags
* Issues should be closed once done, not change the tag to “closed”
* All changes, including bug fix or small feature, should be documented on Gitlab to improve visibility (when an issue is closed, post on Slack channel)
* When an issue is opened, post a short description + necessary screenshots
* When an issue is resolved, post a short explanation on the changes + commit history link
