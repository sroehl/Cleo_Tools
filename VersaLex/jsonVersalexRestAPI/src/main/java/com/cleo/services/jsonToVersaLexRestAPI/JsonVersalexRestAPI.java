package com.cleo.services.jsonToVersaLexRestAPI;

import com.cleo.services.jsonToVersaLexRestAPI.POJO.VersalexCollectionResponse;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class JsonVersalexRestAPI {
  private REST restClient;
  private Gson gson;

  // Timestamp to be added to filenames when outputting to files
  private String fileTime = new SimpleDateFormat("hhmmss").format(Calendar.getInstance().getTime());

  private boolean generatePass;

  private HashMap<String, String> authCache = new HashMap<>();

  public enum ConnectionType {
    connection,
    authenticator
  }

  public void createActions(LinkedTreeMap actions, String idHref, ConnectionType connectionType) throws Exception {
    if (actions.size() == 0)
      return;
    Set<String> keys = actions.keySet();
    for (String key : keys) {
      LinkedTreeMap action = (LinkedTreeMap) actions.get(key);
      if (action != null) {
        String actionName = (String) action.getOrDefault("alias", "");
        if (!actionName.isEmpty() && !actionName.equals("NA")) {
          ArrayList commands = (ArrayList) action.getOrDefault("commands", "");
          LinkedTreeMap actionJson = gson.fromJson(Resources.toString(Resources.getResource("action_bare.txt"), Charsets.UTF_8), LinkedTreeMap.class);
          actionJson.put("commands", commands.toArray(new String[]{}));
          actionJson.put("alias", actionName);
          if (connectionType == ConnectionType.authenticator)
            actionJson.put(ConnectionType.authenticator.toString(), makeAuthLink(idHref));
          actionJson.put(ConnectionType.connection.toString(), makeConnectionLink(idHref));
          String schedule = (String) action.getOrDefault("schedule", "");
          if (!schedule.isEmpty() && !schedule.equalsIgnoreCase("none") && !schedule.equalsIgnoreCase("no"))
            actionJson.put("schedule", schedule);
          restClient.createAction(gson.toJson(actionJson));
        }
      }
    }
  }

  public static LinkedTreeMap generatePasswordForUser(LinkedTreeMap connection) {
    Object accept = connection.get("accept");
    if (accept != null) {
      String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789~!@#$%^&*()-_=+[{]}\\|;:\'\"<.>/?";
      SecureRandom random = new SecureRandom();
      String pass = random.ints(0, characters.length())
              .limit(20)
              .mapToObj(i -> String.valueOf(characters.charAt(i)))
              .collect(Collectors.joining());
      ((LinkedTreeMap) accept).put("password", pass);
      connection.put("accept", accept);
    }
    return connection;
  }

  public void writePassFile(String host, LinkedTreeMap connection) throws IOException {
    String username = (String) connection.get("username");
    String password = (String) getSubElement(connection, "accept.password");
    String email = (String) connection.get("email");
    if (username != null && password != null) {
      String lineToWrite = host + "," + username + "," + password + "," + email + System.lineSeparator();
      Files.write(Paths.get(String.format("userPasswords_%s.csv", this.fileTime)), lineToWrite.getBytes(), StandardOpenOption.APPEND, StandardOpenOption.CREATE);
    }
  }

  public static Object getSubElement(LinkedTreeMap linkedTreeMap, String jsonKey) {
    String[] keys = jsonKey.split("\\.");
    Object subElement = null;
    if (keys.length > 0) {
      subElement = linkedTreeMap.get(keys[0]);
      for (int i = 1; i < keys.length; i++) {
        if (subElement != null)
          subElement = ((LinkedTreeMap) subElement).get(keys[i]);
      }
    }
    return subElement;
  }

  public void deleteActions(LinkedTreeMap connection) {
    ArrayList actions = (ArrayList) getSubElement(connection, "_links.actions");
    for (int i = 0; i < actions.size(); i++) {
      LinkedTreeMap action = (LinkedTreeMap) actions.get(i);
      String href = (String) action.get("href");
      restClient.delete(href);
    }
  }

  /*
   * This is used to create JSON that looks like:
   * "authenticator": {
   *   "href": <auth_href>,
   *   "user": {
   *     "href": <user_href>
   *    }
   *   }
   */
  public LinkedTreeMap makeAuthLink(String userHref) {
    LinkedTreeMap userTreeMap = new LinkedTreeMap();
    userTreeMap.put("href", userHref);
    LinkedTreeMap authTreeMap = new LinkedTreeMap();
    authTreeMap.put("user", userTreeMap);
    authTreeMap.put("href", userHref.substring(0, userHref.indexOf("/users/")));
    LinkedTreeMap topLevelTreeMap = new LinkedTreeMap();
    topLevelTreeMap.put("authenticator", authTreeMap);
    return authTreeMap;
  }

  /*
   * This is used to create JSON that looks like:
   * "connection": {
   *   "href": <conn_href>
   * }
   */
  public LinkedTreeMap makeConnectionLink(String connHref) {
    LinkedTreeMap connectionTreeMap = new LinkedTreeMap();
    connectionTreeMap.put("href", connHref);
    LinkedTreeMap topLevelTreeMap = new LinkedTreeMap();
    topLevelTreeMap.put("connection", connectionTreeMap);
    return connectionTreeMap;
  }

  public void processFile(String jsonFile) throws IOException {
    LinkedTreeMap[] connectionEntries = gson.fromJson(new String(Files.readAllBytes(Paths.get(jsonFile))), LinkedTreeMap[].class);
    String idHref = null;
    ArrayList<LinkedTreeMap> failedEntries = new ArrayList<>();
    ArrayList<String> failedMessages = new ArrayList<>();
    int successCount = 0;
    for (LinkedTreeMap connection : connectionEntries) {
      LinkedTreeMap origConnection = gson.fromJson(gson.toJson(connection), LinkedTreeMap.class);
      try {
        LinkedTreeMap actions;
        if (connection.get("actions") instanceof ArrayList) {
          ArrayList<LinkedTreeMap> arrayActions = (ArrayList) connection.get("actions");
          actions = new LinkedTreeMap();
          for (LinkedTreeMap arrayActionMap : arrayActions) {
            actions.put(arrayActionMap.get("alias"), arrayActionMap);
          }
        } else {
          actions = (LinkedTreeMap) connection.get("actions");
        }
        if (actions != null) {
          connection.remove("actions");
        }
        if (connection.get("type") == null) {
          String authId = null;
          if (connection.get("host") != null) {
            if (! authCache.containsKey(connection.get("host"))) {
              VersalexCollectionResponse authenticatorsResponse = restClient.getAuthenticators("alias eq \"" + connection.get("host") + "\"");
              if (authenticatorsResponse.getCount() == 0) {
                // Make authenticator because one did not exists
                LinkedTreeMap authFromFile = fixIntDouble(gson.fromJson(Resources.toString(Resources.getResource("authenticator_bare.txt"), Charsets.UTF_8), LinkedTreeMap.class));
                authFromFile.put("alias", connection.get("host"));
                LinkedTreeMap newAuth = restClient.createAuthenticator(gson.toJson(authFromFile));
                authId = (String) newAuth.get("id");
                if (authId != null)
                  authCache.put((String) connection.get("host"), authId);
              }
              if (authenticatorsResponse.getCount() == 1) {
                authId = (String) ((LinkedTreeMap) authenticatorsResponse.getResources().get(0)).get("id");
              }
            } else {
              authId = (String)authCache.get(connection.get("host"));
            }
            if (authId != null) {
              // Make user
              String host = (String) connection.remove("host");  // This is added just for this tool, not part of actual VersaLex JSON
              if (this.generatePass) {
                connection = generatePasswordForUser(connection);
              }
              LinkedTreeMap newUser = restClient.createUser(gson.toJson(connection), authId);
              idHref = (String) ((LinkedTreeMap) ((LinkedTreeMap) newUser.get("_links")).get("self")).get("href");
              if (actions != null)
                createActions(actions, idHref, ConnectionType.authenticator);
              System.out.println("Created " + newUser.get("username") + " with ID: " + newUser.get("id"));
              if (this.generatePass)
                writePassFile(host, connection);
            } else {
              System.err.println("Host (" + connection.get("host") + ") that was specified in " + connection.get("alias") + " could not be found");
            }
          } else {
            System.err.println("Unable to find host value for: " + connection.get("alias"));
          }
        } else if (connection.get("type").equals("nativeUser")) {
          LinkedTreeMap newAuth = restClient.createAuthenticator(gson.toJson(fixIntDouble(connection)));
          idHref = (String) ((LinkedTreeMap) ((LinkedTreeMap) newAuth.get("_links")).get("self")).get("href");
          if (actions != null)
            createActions(actions, idHref, ConnectionType.authenticator);
          System.out.println("Created " + newAuth.get("alias") + " with ID: " + newAuth.get("id"));
        } else {
          //Make connection (normal host)
          connection = fixIntDouble(connection);
          LinkedTreeMap newconnection = restClient.createConnection(gson.toJson(connection));
          newconnection = fixIntDouble(newconnection);
          idHref = (String) ((LinkedTreeMap) ((LinkedTreeMap) newconnection.get("_links")).get("self")).get("href");
          deleteActions(newconnection);
          System.out.println("Created " + newconnection.get("alias") + " with ID: " + newconnection.get("id"));
          if (actions != null)
            createActions(actions, idHref, ConnectionType.connection);
        }
        successCount++;
      } catch (Exception ex) {
        System.out.println("Failed to create host : " + ex.getMessage());
        String alias = "";
        if (connection.get("host") != null)
          alias = (String)connection.get("host");
        else if (connection.get("alias") != null)
          alias = (String)connection.get("alias");
        else
          alias = (String)connection.get("username");
        failedMessages.add(alias + ": " + ex.getMessage());
        failedEntries.add(origConnection);

      }
    }
    if (failedEntries.size() > 0) {
      Gson gsonPretty = new GsonBuilder().setPrettyPrinting().create();
      Files.write(Paths.get(String.format("failed_request_%s.json", this.fileTime)), gsonPretty.toJson(failedEntries).getBytes(), StandardOpenOption.APPEND, StandardOpenOption.CREATE);
    }
    System.out.println(String.format("There were %d successful requests", successCount));
    System.out.println(String.format("There were %d failed requests:", failedMessages.size()));
    for (String msg : failedMessages) {
      System.out.println(msg);
    }
  }

  /**
   * Helper method to convert doubles to ints so the scheme matches
   *
   * @param linkedTreeMap
   * @return
   */
  public static LinkedTreeMap<String, Object> fixIntDouble(LinkedTreeMap<String, Object> linkedTreeMap) {
    LinkedTreeMap newTreeMap = new LinkedTreeMap();
    for (Map.Entry<String, Object> entry : linkedTreeMap.entrySet()) {
      if (entry.getValue().getClass().getName().equals("com.google.gson.internal.LinkedTreeMap")) {
        newTreeMap.put(entry.getKey(), fixIntDouble((LinkedTreeMap<String, Object>) entry.getValue()));
      } else if (entry.getValue().getClass().getName().equals("java.lang.Double")) {
        newTreeMap.put(entry.getKey(), ((Double) entry.getValue()).intValue());
      } else {
        newTreeMap.put(entry.getKey(), entry.getValue());
      }
    }
    return newTreeMap;
  }

  public JsonVersalexRestAPI(REST restClient, boolean generatePass) {
    this.restClient = restClient;
    this.gson = new Gson();
    this.generatePass = generatePass;
  }
}
