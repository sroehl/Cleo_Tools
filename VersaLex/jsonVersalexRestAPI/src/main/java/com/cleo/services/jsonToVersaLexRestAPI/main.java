package com.cleo.services.jsonToVersaLexRestAPI;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.io.IOException;


public class main {

  static Option helpOption = Option.builder()
          .longOpt("help")
          .build();

  private static Options getOptions() {
    Options options = new Options();

    Option hostnameOption = Option.builder("h")
            .longOpt("hostname")
            .desc("VersaLex hostname")
            .hasArg()
            .argName("HOSTNAME")
            .required()
            .build();
    options.addOption(hostnameOption);

    Option portOption = Option.builder()
            .longOpt("port")
            .desc("VersaLex HTTP Port")
            .hasArg()
            .argName("PORT")
            .required()
            .build();
    options.addOption(portOption);

    Option usernameOption = Option.builder("u")
            .longOpt("username")
            .desc("Username")
            .hasArg()
            .argName("USERNAME")
            .required()
            .build();
    options.addOption(usernameOption);

    Option passwordOption = Option.builder("p")
            .longOpt("password")
            .desc("Password")
            .hasArg()
            .argName("PASSWORD")
            .required()
            .build();
    options.addOption(passwordOption);

    Option jsonFileOption = Option.builder()
            .longOpt("file")
            .desc("JSON file containing hosts")
            .hasArg()
            .argName("FILE")
            .required(false)
            .build();
    options.addOption(jsonFileOption);

    Option genPassOption = Option.builder()
            .longOpt("generate-pass")
            .desc("Generate Passwords for users")
            .required(false)
            .build();
    options.addOption(genPassOption);

    options.addOption(helpOption);

    return options;
  }

  public static void checkHelp(String[] args) {
    Options options = new Options();
    options.addOption(helpOption);
    CommandLineParser parser = new DefaultParser();
    try {
      CommandLine cmd = parser.parse(options, args, false);
      if (cmd.hasOption("help")) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp( "JsonToVersalexRESTAPI", getOptions());
        System.exit(0);
      }
    } catch (ParseException e) {
      ;
    }
  }

  public static void main(String[] args) throws IOException {
    CommandLineParser parser = new DefaultParser();
    CommandLine cmd = null;
    try {
      Options options = getOptions();
      checkHelp(args);
      cmd = parser.parse(options, args);
    } catch (Exception ex) {
      System.out.println("Could not parse command line arguments: " + ex.getMessage());
      System.exit(-1);
    }
    REST restClient = null;
    try {
      restClient = new REST("http://" + cmd.getOptionValue("hostname"), Integer.parseInt(cmd.getOptionValue("port")), cmd.getOptionValue("username"), cmd.getOptionValue("password"));
    } catch (Exception ex) {
      System.out.println("Failed to create REST Client: " + ex.getMessage());
      System.exit(-1);
    }
    JsonVersalexRestAPI jsonVersalexRestAPI = new JsonVersalexRestAPI(restClient, cmd.hasOption("generate-pass"));
    if (cmd.getOptionValue("file") != null) {
      jsonVersalexRestAPI.processFile(cmd.getOptionValue("file"));
    }
  }


}
