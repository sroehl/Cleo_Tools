#Harmony CSV to Json Tool
This tool will convert a CSV file into the correct JSON format for the JsonVersaLexRestAPI tool.

## Compiling
To compile run: `mvn` 

## Running
To run you need to modify the `app.properties` folder and put in the following fields:
- `groupFile` the name of the group file CSV
- `mailboxFile` the name of the mailbox file CSV
- `jsonFile` the name of the resulting JSON file

The `app.properties` file should be in the same folder as the jar file.

Then you can run by executing: `java -jar HarmonyCSVToJSON.jar`

## Fileds
### Mailbox File fields
- Host
- UserID
- Password
- SSHKeyFileName
- LDAPUser
- OverrideDomain
- BaseDN
- OverrideFilter
- SearchFilter
- ExtendedFilter
- DefaultHomeDir
- CustomHomeDir
- WhitelistIP
- CreateCollectName - Name of collect action
- CreateReceiveName - Name of receive action
- ActionCollect - Collect action commands (use '|' or ';' between commands)
- ActionReceive - Receive action commands (use '|' or ';' between commands)
- Schedule_Send
- Schedule_Receive
- Hours
- Minutes
- Seconds
- HostNotes
- MailboxNotes
- CollectActionNotes
- ReleaseActionNotes
- OtherFolder
- Email

### Group File fields
- UserAlias
- FolderPath
- HomeDir
- DownloadFolder
- UploadFolder
- OtherFolder
- ArchiveSentbox
- ArchiveReceivedbox
- FTP
- SSHFTP
- HTTP
- Access
