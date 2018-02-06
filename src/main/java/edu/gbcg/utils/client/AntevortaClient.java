/*
 * Copyright (c) 2018 Sean Grimes. All Rights Reserved.
 * License: MIT
 */

package edu.gbcg.utils.client;

//import edu.gbcg.configs.columnsAndKeys.RedditComs;
//import edu.gbcg.configs.columnsAndKeys.RedditSubs;
//import edu.gbcg.dbInteraction.RSMapperOutput;
//import edu.gbcg.dbInteraction.dbSelector.BaseMapper;
//import edu.gbcg.dbInteraction.dbSelector.RSMapper;
//import edu.gbcg.dbInteraction.dbSelector.reddit.comments.CommentSetMapper;
import edu.gbcg.dbInteraction.dbSelector.DBSelector;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class AntevortaClient {
    private String configPath;
    private String hostname;
    private int hostport;
    private String user;
    private String pass;
    private final String USER = "USER";
    private final String PASS = "PASS";
    private final String QUERY = "QUERY";
    private final String NOOP_FLAG = "=*=";

    // The config file will need to include the server hostname, the server port, the client username and the client
    // password. The idea here really isn't for some secure login system, it's just a very basic attempt to stop
    // people from scraping github and hitting my server with a bunch of requests.
    // NOTE: Realistically I have no way to enforce usage of a config file, but not using it means you're a dick.
    AntevortaClient(String configFilePath){
        this.configPath = configFilePath;
        verifyGitIgnore();
        parseConfigFile();
    }

    /**
     * Query the server with your sql string. The server will determine which DB to query based on table name in the
     * sql string.
     * @param SQLQuery, the sql compatible query string to use to query the DB
     * @return
     */
    public JSONArray queryServer(String SQLQuery){
        String emptyArray = "[]";
        JSONArray results = null;
        try {
            // Open the socket to the
            Socket sock = new Socket("seanpgrimes.com", 3383);
            DataOutputStream serverWriter = new DataOutputStream(sock.getOutputStream());
            BufferedReader serverReader = new BufferedReader(new InputStreamReader(sock.getInputStream()));

            // Build JSONObject, consisting of user, pass, and query string
            JSONObject queryObject = buildJSONObject(SQLQuery);

            // Server reads line by line; need to ensure the string ends with a newline char or server will hang
            serverWriter.writeBytes(queryObject.toString() + "\n");
            serverWriter.flush();

            // Sit here and wait for the server to respond. JSONArray will be returned in one line for easy parsing
            // by the client. This seems to work fine for large results running over TCP, if problems arise this can
            // be revised to transfer raw bytes with better error handling.
            String jsonArrayString = serverReader.readLine();

            // String could perhaps be null if a failure occurs, however in practice it should just hang on the
            // readLine() call
            if(jsonArrayString == null) jsonArrayString = emptyArray;

            // Response starts with NOOP_FLAG if the server didn't perform the request
            if(jsonArrayString.startsWith(NOOP_FLAG)){
                System.out.println("Server failed to return results: " + jsonArrayString.substring(NOOP_FLAG.length()));
                return new JSONArray(emptyArray);
            }

            // NOTE: A query returning no results will return [], a valid (empty) JSONArray. RSMappersOutput knows
            // how to handle empty results
            results = new JSONArray(jsonArrayString);
        }
        catch(IOException e){
            // Add whatever error handling the user wants for a failed connection to the server
            return new JSONArray(emptyArray);
        }

        return results;
    }

// ---------------------------------------------------------------------------------------------------------------------

    private void verifyGitIgnore(){
        //TODO: Check that config file is in .gitignore and auth info isn't being pushed to github
    }

    private void parseConfigFile(){
        //TODO: Read the config file, get hostname, port, and auth information for user
    }

    private JSONObject buildJSONObject(String SQLQuery){
        JSONObject json = new JSONObject();
        json.put(USER, "tmp_user");
        json.put(PASS, "tmp_pass");
        json.put(QUERY, SQLQuery);

        return json;
    }

// ---------------------------------------------------------------------------------------------------------------------

    public static void main(String[] args){
        // Table names for the 2 DBs
        final String redditComTable = "comment_attrs";
        final String redditSubTable = "submission_attrs";

        // Column name for author
        final String authorColName = "author";
        final String author = "----root";

        // Create a new client, in the future the c'tor will need to take a config file path that holds host and
        // ipaddr as well as authentication information
        AntevortaClient av = new AntevortaClient("ignore_me_for_now");

        // Create a DBSelector to create an SQL query string. This thing is more helpful for more complex string, but
        // here's a basic example selecting all comments or submissions from an author
        DBSelector dbsql = new DBSelector()
                .from(redditSubTable)
                .where(authorColName + "='"+ author + "'");

        // Get the sql string from DBSelector
        String sql = dbsql.sql();

        // Make the selection
        JSONArray res = av.queryServer(sql);

        // This *really* shouldn't ever happen. NOOP returns empty array and no results returns an empty array
        if(res == null)
            return;

        // Get the objects out of the JSONArray
        List<JSONObject> resultObjects = new ArrayList<>();
        for(int i = 0; i < res.length(); ++i)
            resultObjects.add(res.getJSONObject(i));

        // Print the JSONObjects
        for(JSONObject obj : resultObjects)
            System.out.println(obj);


        // NOTE: The below is only if you want to convert back to RSMapper object for each JSON object. Definitely
        // not a necessity
        //List<RSMapper> mappers = new ArrayList<>();
        //for(JSONObject obj : resultObjects)
        //    mappers.add(new BaseMapper(obj));

        // Print the RSMapper objects
        //RSMapperOutput.printAllColumnsFromRSMappers(mappers, RedditComs.columnsForPrinting(), RedditComsk
        //        .dataTypesForPrinting());
    }

}
