package com.cleo.services.jsonToVersaLexRestAPI;

import com.cleo.services.jsonToVersaLexRestAPI.POJO.VersalexCollectionResponse;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class JsonVersalexRestAPI {
  private REST restClient;
  private Gson gson;

  private boolean generatePass;

  public enum  ConnectionType {
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
          restClient.createAction(gson.toJson(actionJson));
        }
      }
    }
  }

  public static LinkedTreeMap generatePasswordForUser(LinkedTreeMap connection) {
    Object accept = connection.get("accept");
    if (accept != null) {
      String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789~!@#$%^&*()-_=+[{]}\\|;:\'\",<.>/?";
      SecureRandom random = new SecureRandom();
      String pass = random.ints(0, characters.length())
                          .limit(20)
                          .mapToObj(i -> String.valueOf(characters.charAt(i)))
                          .collect(Collectors.joining());
      ((LinkedTreeMap)accept).put("password", pass);
      connection.put("accept", accept);
    }
    return connection;
  }

  public static void writePassFile(LinkedTreeMap connection) throws IOException {
    String username = (String)connection.get("username");
    String password = (String)getSubElement(connection, "accept.password");
    if (username != null && password != null) {
      String lineToWrite = username + "," + password + System.lineSeparator();
      Files.write(Paths.get("userPasswords.csv"), lineToWrite.getBytes(), StandardOpenOption.APPEND, StandardOpenOption.CREATE);
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
      LinkedTreeMap action = (LinkedTreeMap)actions.get(i);
      String href = (String)action.get("href");
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
    authTreeMap.put("href", userHref.substring(0,userHref.indexOf("/users/")));
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
    for (LinkedTreeMap connection : connectionEntries) {
      try {
        LinkedTreeMap actions = (LinkedTreeMap)connection.get("actions");
        if (actions != null) {
          connection.remove("actions");
        }
        if (connection.get("type") == null) {
          String authId = null;
          if (connection.get("host") != null) {
            VersalexCollectionResponse authenticatorsResponse = restClient.getAuthenticators("alias eq \"" + connection.get("host") + "\"");
            if (authenticatorsResponse.getCount() == 0) {
              // Make authenticator because one did not exists
              LinkedTreeMap authFromFile = fixIntDouble(gson.fromJson(Resources.toString(Resources.getResource("authenticator_bare.txt"), Charsets.UTF_8), LinkedTreeMap.class));
              authFromFile.put("alias", connection.get("host"));
              LinkedTreeMap newAuth = restClient.createAuthenticator(gson.toJson(authFromFile));
              authId = (String)newAuth.get("id");
            }
            if (authenticatorsResponse.getCount() == 1) {
              authId = (String)((LinkedTreeMap)authenticatorsResponse.getResources().get(0)).get("id");
            }
            if (authId != null) {
              // Make user
              connection.remove("host");  // This is added just for this tool, not part of actual VersaLex JSON
              if (this.generatePass) {
                connection = generatePasswordForUser(connection);
                writePassFile(connection);
              }
              LinkedTreeMap newUser = restClient.createUser(gson.toJson(connection), authId);
              idHref = (String)((LinkedTreeMap)((LinkedTreeMap)newUser.get("_links")).get("self")).get("href");
              if (actions != null)
                createActions(actions, idHref, ConnectionType.authenticator);
              System.out.println("Created " + newUser.get("username") + " with ID: " + newUser.get("id"));
            } else {
              System.err.println("Host (" + connection.get("host") + ") that was specified in " + connection.get("alias") + " could not be found");
            }
          } else {
            System.err.println("Unable to find host value for: " + connection.get("alias"));
          }
        } else if (connection.get("type").equals("nativeUser")) {
          LinkedTreeMap newAuth = restClient.createAuthenticator(gson.toJson(fixIntDouble(connection)));
          idHref = (String)((LinkedTreeMap)((LinkedTreeMap)newAuth.get("_links")).get("self")).get("href");
          if (actions != null)
            createActions(actions, idHref, ConnectionType.authenticator);
          System.out.println("Created " + newAuth.get("alias") + " with ID: " + newAuth.get("id"));
        } else {
          //Make connection (normal host)
          LinkedTreeMap newconnection = restClient.createConnection(gson.toJson(connection));
          idHref = (String)((LinkedTreeMap)((LinkedTreeMap)newconnection.get("_links")).get("self")).get("href");
          deleteActions(newconnection);
          System.out.println("Created " + newconnection.get("alias") + " with ID: " + newconnection.get("id"));
          if (actions != null)
            createActions(actions, idHref, ConnectionType.connection);
        }
      } catch (Exception ex) {
        ex.printStackTrace();
        System.out.println("Failed to create host : " + ex.getMessage());
      }
    }
  }

  /**
   * Helper method to convert doubles to ints so the scheme matches
   * @param linkedTreeMap
   * @return
   */
  public static LinkedTreeMap<String, Object> fixIntDouble(LinkedTreeMap<String, Object>  linkedTreeMap) {
    LinkedTreeMap newTreeMap = new LinkedTreeMap();
    for(Map.Entry<String, Object> entry: linkedTreeMap.entrySet()) {
      if (entry.getValue().getClass().getName().equals("com.google.gson.internal.LinkedTreeMap")) {
        newTreeMap.put(entry.getKey(), fixIntDouble((LinkedTreeMap<String, Object>) entry.getValue()));
      } else if (entry.getValue().getClass().getName().equals("java.lang.Double")) {
        newTreeMap.put(entry.getKey(), ((Double)entry.getValue()).intValue());
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
