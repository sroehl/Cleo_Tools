# Profile Builder Tool Instructions for VersaLex 
(for Harmony or VLTrader, not Gen2)

##### Instructions written for Windows

## Preparation

Install and configure Java and Maven
https://howtodoinjava.com/maven/how-to-install-maven-on-windows/

- test by running `java -version`

- test by running `mvn -version`

Install Git For Windows (for Git Bash)
https://www.atlassian.com/git/tutorials/install-git#windows

- test by launching Git Bash

Install & license Harmony

- Build the config on a local Harmony, then export and import into the Cloud or Agent
- Start Harmony service
- In VLNavigator add the administrators group to have system privelege so you have to login to Harmony as administrator with Admin password
- Start Harmony native UI to open it up
- Configure %HarmonyRoot% as custom directory macro variable

Repository for code:
https://github.com/sroehl/Cleo_Tools/tree/master/VersaLex/jsonVersalexRestAPI

copy repo.sh to C:\

copy pbt.sh to C:\

within Git Bash (just need to run once):
```
cd /c/
chmod +x repo.sh
chmod +x pbt.sh
./repo.sh
```

## Build the tool

Run the pbt.sh script to get the latest updates of the tool

within Git Bash:
```
./pbt.sh
```


## Preparing the .csv

Modify the app.properties file and put in the following fields:
- groupFile: the name of the group file CSV
- mailboxFile: the name of the mailbox file CSV
- jsonFile: the name of the resulting JSON file

You don't have to use the userGroup.csv. You can instead just use the userMailbox.csv and if the user group (Users host, parent level) doesn't exist it will create it. Otherwise, you can create the User group with one pass, and then run the tool again for the mailboxes.

You could take host from the customer installation and plug it in to your local Harmony, and use the tool to add mailboxes to it.

Going the route of just using the UserMailbox.csv - edit the userGroup.csv by removing all rows except the first (headers).

Edit userMailbox.csv, leaving the first row (headers) alone, and populate the columns and rows per your needs.

You can leave the Password column blank and the program can generate random strong passwords for you, outputting a userPasswords.csv so you can know the User group, mailbox, and password (to be able to share to customer and trading partners).

Make sure the email address is unique for each row.

## Running the tool

Run the 1st tool to convert the csv to json (from C:\profilebuildertool\):

`java -jar HarmonyCSVToJSON.jar`

Run the 2nd tool to build the connections in the local Harmony:

`java -jar JsonToVersalexRestAPI.jar --file result.json -h localhost --port 5080 -u administrator -p Admin --generate-pass`

Launch Harmony native UI, spot check to look to see that Users and mailboxes and actions were created.

Find userPasswords.csv for info on the passwords for the user mailboxes that were created, share with customer and/or trading partners.

Export host(s) and import into customer environment.
