package com.cleo.services.harmony;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ConvertCSVToHarmonyJSONTest {


  @Test
  public void as2CsvToJsonTest() throws Exception {
    String alias = "TestAS2";
    String inbox = "inbox/";
    String outbox = "outbox/";
    String sentbox = "testsentbox//";
    String recbox = "testrecbox/";
    String url = "http://localhost:5080/AS2/";
    String as2from = "myAS2";
    String as2to = "theirAS2";
    String subject = "TestSub";
    boolean https = false;
    boolean encrypted = false;
    boolean signed = true;
    boolean receipt = true;
    boolean receipt_sign = true;
    String receipt_type = "sync";
    String actionAlias = "send";
    String actionCmd = "PUT -DEL test/*";
    File tempFile = File.createTempFile("as2test", ".csv");
    String as2Csv = Files.readAllLines(Paths.get("templates", "AS2Template.csv")).get(0);
    as2Csv += String.format("\nAS2,%s,%s,%s,%s,%s,%s,%s,%s,%s,%b,%b,%b,%b,%b,%s,%s,,%s,,,", alias, inbox, outbox, sentbox, recbox, url, as2from, as2to,
            subject, https, encrypted, signed, receipt, receipt_sign, receipt_type, actionAlias, actionCmd);
    Files.write(Paths.get(tempFile.getPath()), as2Csv.getBytes(), StandardOpenOption.WRITE);

    try {
      List<JsonElement> hosts = ConvertCSVToHarmonyJSON.createAS2Hosts(tempFile.getPath());
      for (JsonElement element : hosts) {
        JsonObject jsonObject = element.getAsJsonObject();
        assertEquals(alias, jsonObject.get("alias").getAsString());
        assertEquals(inbox, jsonObject.getAsJsonObject("incoming").getAsJsonObject("storage").get("inbox").getAsString());
        assertEquals(outbox, jsonObject.getAsJsonObject("outgoing").getAsJsonObject("storage").get("outbox").getAsString());
        assertEquals(sentbox, jsonObject.getAsJsonObject("outgoing").getAsJsonObject("storage").get("sentbox").getAsString());
        assertEquals(recbox, jsonObject.getAsJsonObject("incoming").getAsJsonObject("storage").get("receivedbox").getAsString());
        assertEquals(url, jsonObject.getAsJsonObject("connect").get("url").getAsString());
        assertEquals(as2from, jsonObject.get("localName").getAsString());
        assertEquals(as2to, jsonObject.get("partnerName").getAsString());
        assertEquals(subject, jsonObject.getAsJsonObject("outgoing").get("subject").getAsString());
        assertEquals(encrypted, jsonObject.getAsJsonObject("outgoing").get("encrypt").getAsBoolean());
        assertEquals(signed, jsonObject.getAsJsonObject("outgoing").get("sign").getAsBoolean());
        assertEquals(receipt_sign, jsonObject.getAsJsonObject("outgoing").getAsJsonObject("receipt").get("sign").getAsBoolean());
        assertEquals(receipt_type, jsonObject.getAsJsonObject("outgoing").getAsJsonObject("receipt").get("type").getAsString());
        assertEquals(actionAlias, jsonObject.getAsJsonArray("actions").get(0).getAsJsonObject().get("alias").getAsString());
        assertEquals(actionCmd, jsonObject.getAsJsonArray("actions").get(0).getAsJsonObject().get("commands").getAsString());
      }
    } finally {
      tempFile.delete();
    }
  }

  @Test
  public void ftpCsvToJsonTest() throws Exception {
    String alias = "TestFTP";
    String inbox = "inbox/";
    String outbox = "outbox/";
    String sentbox = "testsentbox//";
    String recbox = "testrecbox/";
    String host = "localhost";
    int port = 5021;
    String username = "testuser";
    String password = "password";
    String dataType = "Binary";
    String channelMode = "active";
    String activeLowPort = "6000";
    String activeHighPort = "6100";
    String actionAlias = "send";
    String actionCmd = "PUT -DEL test/*";
    File tempFile = File.createTempFile("ftptest", ".csv");
    String ftpCsv = Files.readAllLines(Paths.get("templates", "FTPTemplate.csv")).get(0);
    //String ftpCsv = "type,alias,inbox,outbox,sentbox,receivedbox,host,port,username,password,datatype,channelmode,activelowport,activehighport,CreateSendName,CreateReceiveName,ActionSend,ActionReceive,Schedule_Send,Schedule_Receive";
    ftpCsv += String.format("\nFTP,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,,%s,,,", alias, inbox, outbox, sentbox, recbox, host, port, username, password, dataType, channelMode,
            activeLowPort, activeHighPort, actionAlias, actionCmd);
    Files.write(Paths.get(tempFile.getPath()), ftpCsv.getBytes(), StandardOpenOption.WRITE);

    try {
      List<JsonElement> hosts = ConvertCSVToHarmonyJSON.createFTPHosts(tempFile.getPath());
      for (JsonElement element : hosts) {
        JsonObject jsonObject = element.getAsJsonObject();
        assertEquals(alias, jsonObject.get("alias").getAsString());
        assertEquals(inbox, jsonObject.getAsJsonObject("incoming").getAsJsonObject("storage").get("inbox").getAsString());
        assertEquals(outbox, jsonObject.getAsJsonObject("outgoing").getAsJsonObject("storage").get("outbox").getAsString());
        assertEquals(sentbox, jsonObject.getAsJsonObject("outgoing").getAsJsonObject("storage").get("sentbox").getAsString());
        assertEquals(recbox, jsonObject.getAsJsonObject("incoming").getAsJsonObject("storage").get("receivedbox").getAsString());
        assertEquals(host, jsonObject.getAsJsonObject("connect").get("host").getAsString());
        assertEquals(port, jsonObject.getAsJsonObject("connect").get("port").getAsInt());
        assertEquals(username, jsonObject.getAsJsonObject("connect").get("username").getAsString());
        assertEquals(password, jsonObject.getAsJsonObject("connect").get("password").getAsString());
        assertEquals(dataType, jsonObject.getAsJsonObject("connect").get("defaultContentType").getAsString());
        assertEquals(channelMode, jsonObject.getAsJsonObject("connect").getAsJsonObject("dataChannel").get("mode").getAsString());
        assertEquals(activeLowPort, jsonObject.getAsJsonObject("connect").getAsJsonObject("dataChannel").get("lowPort").getAsString());
        assertEquals(activeHighPort, jsonObject.getAsJsonObject("connect").getAsJsonObject("dataChannel").get("highPort").getAsString());
        assertEquals(actionAlias, jsonObject.getAsJsonArray("actions").get(0).getAsJsonObject().get("alias").getAsString());
        assertEquals(actionCmd, jsonObject.getAsJsonArray("actions").get(0).getAsJsonObject().get("commands").getAsString());
      }
    } finally {
      tempFile.delete();
    }
  }

  @Test
  public void sftpCsvToJsonTest() throws Exception {
    String alias = "TestSFTP";
    String inbox = "inbox/";
    String outbox = "outbox/";
    String sentbox = "testsentbox//";
    String recbox = "testrecbox/";
    String host = "localhost";
    int port = 5021;
    String username = "testuser";
    String password = "password";
    String actionAlias = "send";
    String actionCmd = "PUT -DEL test/*";
    File tempFile = File.createTempFile("ftptest", ".csv");
    String sftpCsv = Files.readAllLines(Paths.get("templates", "SFTPTemplate.csv")).get(0);
    //String sftpCsv = "type,alias,inbox,outbox,sentbox,receivedbox,host,port,username,password,CreateSendName,CreateReceiveName,ActionSend,ActionReceive,Schedule_Send,Schedule_Receive";
    sftpCsv += String.format("\nFTP,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,,%s,,,", alias, inbox, outbox, sentbox, recbox, host, port, username, password, actionAlias, actionCmd);
    Files.write(Paths.get(tempFile.getPath()), sftpCsv.getBytes(), StandardOpenOption.WRITE);

    try {
      List<JsonElement> hosts = ConvertCSVToHarmonyJSON.createSFTPHosts(tempFile.getPath());
      for (JsonElement element : hosts) {
        JsonObject jsonObject = element.getAsJsonObject();
        assertEquals(alias, jsonObject.get("alias").getAsString());
        assertEquals(inbox, jsonObject.getAsJsonObject("incoming").getAsJsonObject("storage").get("inbox").getAsString());
        assertEquals(outbox, jsonObject.getAsJsonObject("outgoing").getAsJsonObject("storage").get("outbox").getAsString());
        assertEquals(sentbox, jsonObject.getAsJsonObject("outgoing").getAsJsonObject("storage").get("sentbox").getAsString());
        assertEquals(recbox, jsonObject.getAsJsonObject("incoming").getAsJsonObject("storage").get("receivedbox").getAsString());
        assertEquals(host, jsonObject.getAsJsonObject("connect").get("host").getAsString());
        assertEquals(port, jsonObject.getAsJsonObject("connect").get("port").getAsInt());
        assertEquals(username, jsonObject.getAsJsonObject("connect").get("username").getAsString());
        assertEquals(password, jsonObject.getAsJsonObject("connect").get("password").getAsString());
        assertEquals(actionAlias, jsonObject.getAsJsonArray("actions").get(0).getAsJsonObject().get("alias").getAsString());
        assertEquals(actionCmd, jsonObject.getAsJsonArray("actions").get(0).getAsJsonObject().get("commands").getAsString());
      }
    } finally {
      tempFile.delete();
    }
  }
}
