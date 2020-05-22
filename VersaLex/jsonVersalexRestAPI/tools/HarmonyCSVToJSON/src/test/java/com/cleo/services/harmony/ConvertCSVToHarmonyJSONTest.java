package com.cleo.services.harmony;


import com.cleo.services.harmony.ConvertCSVToHarmonyJSON;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class ConvertCSVToHarmonyJSONTest {


  @Test
  public void as2CsvToJsonTest() throws Exception {
    File tempFile = File.createTempFile("as2test", ".csv");
    String as2Csv = "type,alias,inbox,outbox,sentbox,receivedbox,url,AS2From,AS2To,Subject,https,encrypted,signed,receipt,receipt_sign,receipt_type,CreateSendName,CreateReceiveName,ActionSend,ActionReceive,Schedule_Send,Schedule_Receive";
    as2Csv += "AS2,TestAS2,inbox/,outbox,sentbox,recbox,http://localhost:5080/AS2/,myAS2,theirAS2,TestSub,false,false,true,true,true,sync,send,,PUT -DEL test/*,,,,";
    Files.write(Paths.get(tempFile.getPath()), as2Csv.getBytes(), StandardOpenOption.WRITE);

    ConvertCSVToHarmonyJSON.create
  }
}
