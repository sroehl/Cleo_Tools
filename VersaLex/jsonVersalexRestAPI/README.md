# Json Versalex REST API tool
Helps with sending JSON to the VersaLex REST API to create Connections and Authenticators

## Compiling
To compile you just need to run `mvn`

## Running
To run it you will need to run `java -jar JsonToVersalexRestAPI` with the following usage
```usage: JsonToVersalexRESTAPI
       --file <FILE>           JSON file containing hosts
       --generate-pass         Generate Passwords for users automatically
    -h,--hostname <HOSTNAME>   VersaLex hostname
       --help
    -p,--password <PASSWORD>   Password
       --port <PORT>           VersaLex HTTP Port
    -u,--username <USERNAME>   Username
    --update                   Updates connections instead of creating new ones
```

### Notes about JSON format
- When creating users, a `host` property is required that will add the User to that User host and create the User host if it does not exist
- To add actions to any User or Connection you use the following format:
```    "actions": {
         "collect":{
           "alias": "connectTest",
           "commands": ["GET -DEL *", "LCOPY -REC %inbox% %inbox%/in"]
         },
         "receive":{
           "alias": "receiveTest",
           "commands": ["GET -DEL *", "LCOPY -REC %inbox% %inbox%/in"]
         }
       }
```

## Tools
In the tools folder are the following useful tools:
- HarmonyCSVToJson - This will convert CSV files into the required JSON files that is needed by this tool