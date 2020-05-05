package com.cleo.services.harmony;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.gson.internal.LinkedTreeMap;
import com.opencsv.CSVReader;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.HeaderColumnNameMappingStrategy;
import com.opencsv.bean.HeaderColumnNameTranslateMappingStrategy;
import com.opencsv.exceptions.CsvException;


public class ConvertCSVToHarmonyJSON {

	public static Gson gson = new Gson();
	
	public static void main(String[] args) throws FileNotFoundException, IOException, CsvException {



		new ApplicationProperties().readProperties();
		System.out.println("Finished reading the app config file...");
		 /*Map<String, String> values =  new CSVReaderHeaderAware(new FileReader(ApplicationProperties.appProps.getProperty("csvFile"))).readMap();
		 for (Map.Entry<String, String> entry : values.entrySet()) {
			    System.out.println(entry.getKey() + "/" + entry.getValue());
		}*/
		
        JsonArray jsonArr = new JsonArray();
        
		CSVReader grpReader = new CSVReader(new FileReader(ApplicationProperties.appProps.getProperty("groupFile")));
		List<String[]> grpElements = grpReader.readAll();
		for(String[] line: grpElements) {
			System.out.println(line[0]);
        	if(!line[0].equals("UserAlias"))
        		jsonArr.add(contructUsrGrpJSON(line));
        }

		HeaderColumnNameMappingStrategy mailboxStrategy = new HeaderColumnNameMappingStrategy<>();
		mailboxStrategy.setType(MailboxCSV.class);
		CSVReader reader2 = new CSVReader(new FileReader(ApplicationProperties.appProps.getProperty("mailBoxFile")));
		CsvToBean csvToBean = new CsvToBean();
		csvToBean.setCsvReader(reader2);
		csvToBean.setMappingStrategy(mailboxStrategy);
		List<MailboxCSV> mailboxCSVList = csvToBean.parse();

        for(MailboxCSV mailboxCSV: mailboxCSVList) {
			jsonArr.add(contructMailboxJSON(mailboxCSV));
        }
        
        FileWriter writer = new FileWriter(new File(ApplicationProperties.appProps.getProperty("jsonFile")));
        gson.toJson(jsonArr, writer);
        writer.close();
        //System.out.println(gson.toJson(jsonArr));
	}

	private static JsonObject  contructMailboxJSON(MailboxCSV mailboxCSV) throws JsonSyntaxException, IOException {
		
        LinkedTreeMap authFromFile = gson.fromJson(Resources.toString(Resources.getResource("mailboxTemplate.json"), Charsets.UTF_8), LinkedTreeMap.class);
        authFromFile.put("host", mailboxCSV.getHost());
        authFromFile.put("username", mailboxCSV.getUserID());
        ((LinkedTreeMap)authFromFile.get("accept")).put("password", mailboxCSV.getPassword());

        //authFromFile.put("accept/password", line[13]);
        if(mailboxCSV.getDefaultHomeDir().equals("Yes")) {
        	((LinkedTreeMap)((LinkedTreeMap)authFromFile.get("home")).get("dir")).put("default", mailboxCSV.getCustomHomeDir());
        }
        else {
        	((LinkedTreeMap)((LinkedTreeMap)authFromFile.get("home")).get("dir")).put("override", mailboxCSV.getCustomHomeDir());
        }
        System.out.println(mailboxCSV.getWhitelistIP().isEmpty());
        if(!mailboxCSV.getWhitelistIP().isEmpty()) {
        	ArrayList wl = new ArrayList();
			for(String ipaddr : mailboxCSV.getWhitelistIP().split(";")) {
				LinkedTreeMap tr = new LinkedTreeMap();
				tr.put("ipAddress", ipaddr);
				wl.add(tr);
			}
			((LinkedTreeMap)authFromFile.get("accept")).put("whitelist",wl);
		}
        authFromFile.put("notes", mailboxCSV.getHostNotes());
		if(!mailboxCSV.getOtherFolder().isEmpty()) {
			for(String path : mailboxCSV.getOtherFolder().split(";")) {
				LinkedTreeMap tr = new LinkedTreeMap();
				tr.put("usage", "other");
				tr.put("path", path);
				((ArrayList)((LinkedTreeMap)((LinkedTreeMap)authFromFile.get("home")).get("subfolders")).get("default")).add(tr);
			}
		}
        authFromFile.put("email", mailboxCSV.getEmail());

		authFromFile.put("actions", createActions(mailboxCSV.getCreateCollectName(), mailboxCSV.getActionCollect().split("\\|"), mailboxCSV.getCreateReceiveName(), mailboxCSV.getActionReceive().split("\\|")));
        
        System.out.println(gson.toJson(authFromFile));
        return gson.toJsonTree(authFromFile).getAsJsonObject();
	}

	private static LinkedTreeMap createActions(String collectAlias, String[] collectCommands, String receiveAlias, String[] receiveCommands) {
		LinkedTreeMap actions = new LinkedTreeMap();
		if (!collectAlias.equalsIgnoreCase("NA")) {
			LinkedTreeMap collect = new LinkedTreeMap();
			collect.put("alias", collectAlias);
			collect.put("commands", collectCommands);
			actions.put(collectAlias, collect);
		}

		if (!receiveAlias.equalsIgnoreCase("NA")) {
			LinkedTreeMap receive = new LinkedTreeMap();
			receive.put("alias", receiveAlias);
			receive.put("commands", receiveCommands);
			actions.put(receiveAlias, receive);
		}

		return actions;
	}

	private static JsonObject contructUsrGrpJSON(String[] line) throws JsonSyntaxException, IOException {
		LinkedTreeMap authFromFile = gson.fromJson(Resources.toString(Resources.getResource("groupTemplate.json"), Charsets.UTF_8), LinkedTreeMap.class);
		authFromFile.put("alias", line[0]);
		if(!line[1].isEmpty())
			authFromFile.put("resourceFolder", line[0]);
		if(!line[2].isEmpty())
			((LinkedTreeMap)((LinkedTreeMap)authFromFile.get("home")).get("dir")).put("default", line[2]);
		if(!line[3].isEmpty()) {
			LinkedTreeMap tr = new LinkedTreeMap();
			tr.put("usage", "download");
			tr.put("path", line[3]);
			((ArrayList)((LinkedTreeMap)((LinkedTreeMap)authFromFile.get("home")).get("subfolders")).get("default")).add(tr);
			//((ArrayList)((LinkedTreeMap)((LinkedTreeMap)authFromFile.get("home")).get("subfolders")).get("default")).add(new LinkedTreeMap().put("path", line[3]));
		}
		if(!line[4].isEmpty()) {
			LinkedTreeMap tr = new LinkedTreeMap();
			tr.put("usage", "upload");
			tr.put("path", line[4]);
			((ArrayList)((LinkedTreeMap)((LinkedTreeMap)authFromFile.get("home")).get("subfolders")).get("default")).add(tr);
		}
		if(!line[5].isEmpty()) {
			for(String path :line[5].split(";")) {
				LinkedTreeMap tr = new LinkedTreeMap();
				tr.put("usage", "other");
				tr.put("path", path);
				((ArrayList)((LinkedTreeMap)((LinkedTreeMap)authFromFile.get("home")).get("subfolders")).get("default")).add(tr);
			}
		}
		if(!line[6].isEmpty()) {
			((LinkedTreeMap)((LinkedTreeMap)authFromFile.get("outgoing")).get("storage")).put("sentbox", line[6]);
		}
		if(!line[7].isEmpty()) {
			((LinkedTreeMap)((LinkedTreeMap)authFromFile.get("incoming")).get("storage")).put("receivedbox", line[7]);
		}
		if(line[8].equals("Yes")) {
			((LinkedTreeMap)((LinkedTreeMap)authFromFile.get("accept")).get("ftp")).put("enabled", true);
		}
		if(line[9].equals("Yes")) {
			((LinkedTreeMap)((LinkedTreeMap)authFromFile.get("accept")).get("sftp")).put("enabled", true);
		}
		if(line[10].equals("Yes")) {
			((LinkedTreeMap)((LinkedTreeMap)authFromFile.get("accept")).get("http")).put("enabled", true);
		}
		if(!line[11].isEmpty()) {
			((LinkedTreeMap)authFromFile.get("home")).put("access", line[11].toLowerCase());
		}
		return gson.toJsonTree(authFromFile).getAsJsonObject();
	}
}
