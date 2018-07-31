/*
 * Copyright (c) 2018 Sean Grimes. All Rights Reserved.
 * License: MIT
 */

package edu.antevorta.utils.client;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.Socket;

@SuppressWarnings("ALL")
public class AntevortaClient {
    private static final String USER = "USER";
    private static final String PASS = "PASS";
    private static final String QUERY = "QUERY";
    private static final String HOST_NAME = "HOSTNAME";
    private static final String HOST_PORT = "HOSTPORT";
    private static final String NOOP_FLAG = "=*=";

    /**
     * Write a config file with the required information
     * @param configFile Path where you want to write the config file, and the config file name
     * @param hostname The host
     * @param hostport The port
     * @param user Your username
     * @param pass Your password -- NOTE: THIS IS NOT SECURE, DON'T USE A PASSWORD YOU CURRENTLY OR
     *             WILL USE ELSEWHERE!!
     */
    public static void writeConfigFile(String configFile, String hostname, Integer hostport,
                                       String user, String pass){
        JSONObject json = new JSONObject();
        json.put(HOST_NAME, hostname);
        json.put(HOST_PORT, hostport);
        json.put(USER, user);
        json.put(PASS, pass);

        // Make the directory if it doesn't exist yet
        File f = new File(configFile);
        f.getParentFile().mkdirs();

        // Write JSON string to file
        try(FileWriter fw = new FileWriter(configFile)){
            fw.write(json.toString());
        }
        catch(IOException e){
            System.err.println("Failed to write " + configFile);
        }
    }

    private String configPath;
    private String hostname;
    private int hostport;
    private String user;
    private String pass;

    // The config file will need to include the server hostname, the server port, the client username and the
    // client password. The idea here really isn't for some secure login system, it's just a very basic
    // attempt to stop people from scraping github and hitting my server with a bunch of requests.
    // NOTE: Realistically I have no way to enforce usage of a config file, but not using it means you're
    // a jerk.
    public AntevortaClient(String configFilePath){
        this.configPath = configFilePath;
        parseConfigFile();
    }

    /**
     * Query the server with your sql string. The server will determine which DB to query based on table name
     * in the sql string.
     * @param SQLQuery, the sql compatible query string to use to query the DB
     * @return
     */
    public JSONArray queryServer(String SQLQuery){
        String emptyArray = "[]";
        JSONArray results = null;
        try {
            // Open the socket to the server
            Socket sock = new Socket(hostname, hostport);
            DataOutputStream serverWriter = new DataOutputStream(sock.getOutputStream());
            BufferedReader serverReader = new BufferedReader(new InputStreamReader(sock.getInputStream()));

            // Build JSONObject, consisting of user, pass, and query string
            JSONObject queryObject = buildJSONObject(SQLQuery);

            // Server reads line by line; need to ensure the string ends with a newline char or server
            // will hang
            serverWriter.writeBytes(queryObject.toString() + "\n");
            serverWriter.flush();

            // Sit here and wait for the server to respond. JSONArray will be returned in one line for easy
            // parsing by the client. This seems to work fine for large results running over TCP, if problems
            // arise this can be revised to transfer raw bytes with better error handling.
            String jsonArrayString = serverReader.readLine();

            // String could perhaps be null if a failure occurs, however in practice it should just hang on
            // the readLine() call -- Might be smart to add a timeout for that call that is reasonable to
            // accomodate large requests.
            if(jsonArrayString == null) jsonArrayString = emptyArray;

            // Response starts with NOOP_FLAG if the server didn't perform the request, a reason for the
            // failure will come after the flag
            if(jsonArrayString.startsWith(NOOP_FLAG)){
                System.out.println("Server failed to return results: " +
                        jsonArrayString.substring(NOOP_FLAG.length()));
                return new JSONArray(emptyArray);
            }

            // NOTE: A query returning no results will return [], a valid (but empty) JSONArray.
            // RSMappersOutput knows how to handle empty results
            results = new JSONArray(jsonArrayString);
        }
        catch(IOException e){
            // Add whatever error handling the user wants for a failed connection to the server
            return new JSONArray(emptyArray);
        }

        return results;
    }

// -----------------------------------------------------------------------------------------------------------

    private void parseConfigFile(){
        String fullString = null;
        try(BufferedReader br = new BufferedReader(new FileReader(this.configPath))){
            // Read in the JSON file
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();
            while(line != null){
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }
            fullString = sb.toString();
        }
        catch(IOException e){
            e.printStackTrace();
            throw new RuntimeException("Unable to parse config file");
        }

        if(fullString == null){
            throw new RuntimeException("Unable to parse config file");
        }

        // Create a new JSONObject and get the stored values
        JSONObject json = new JSONObject(fullString);
        this.hostname = json.getString(HOST_NAME);
        this.hostport = json.getInt(HOST_PORT);
        this.user = json.getString(USER);
        this.pass = json.getString(PASS);
    }

    private JSONObject buildJSONObject(String SQLQuery){
        JSONObject json = new JSONObject();
        json.put(USER, this.user);
        json.put(PASS, this.pass);
        json.put(QUERY, SQLQuery);

        return json;
    }
}
