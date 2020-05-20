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
- Schedule_Collect - Can either be "polling", "none/no/empty", or in timed schedule format (for example to run every 5 minutes: `on Su-Sa @00:00/00:05-24:00`
- Schedule_Receive - Can either be "polling", "none/no/empty", or in timed schedule format (for example to run every 5 minutes: `on Su-Sa @00:00/00:05-24:00`
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
