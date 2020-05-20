cd /c/git/repo/Cleo_Tools
git pull
cd /c/git/repo/Cleo_Tools/VersaLex/jsonVersalexRestAPI
mvn
cd /c/git/repo/Cleo_Tools/VersaLex/jsonVersalexRestAPI/tools/HarmonyCSVToJSON/
mvn
cp /c/git/repo/Cleo_Tools/VersaLex/jsonVersalexRestAPI/tools/HarmonyCSVToJSON/target/HarmonyCSVToJSON*.jar /c/profilebuildertool/HarmonyCSVToJSON.jar
cp /c/git/repo/Cleo_Tools/VersaLex/jsonVersalexRestAPI/tools/HarmonyCSVToJSON/templates/UserGroup-template.csv /c/profilebuildertool/UserGroup.csv
cp /c/git/repo/Cleo_Tools/VersaLex/jsonVersalexRestAPI/tools/HarmonyCSVToJSON/templates/UserMailbox-template.csv /c/profilebuildertool/UserMailbox.csv
cp /c/git/repo/Cleo_Tools/VersaLex/jsonVersalexRestAPI/tools/HarmonyCSVToJSON/app.properties /c/profilebuildertool/app.properties
cp /c/git/repo/Cleo_Tools/VersaLex/jsonVersalexRestAPI/target/JsonToVersalexRestAPI*.jar /c/profilebuildertool/JsonToVersalexRestAPI.jar