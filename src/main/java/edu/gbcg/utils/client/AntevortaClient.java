/*
 * Copyright (c) 2018 Sean Grimes. All Rights Reserved.
 * License: MIT
 */

package edu.gbcg.utils.client;

import edu.gbcg.configs.columnsAndKeys.RedditComs;
import edu.gbcg.dbInteraction.RSMapperOutput;
import edu.gbcg.dbInteraction.dbSelector.RSMapper;
import edu.gbcg.dbInteraction.dbSelector.Selector;
import edu.gbcg.dbInteraction.dbSelector.reddit.comments.CommentSetMapper;
import edu.gbcg.utils.TSL;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class AntevortaClient {
    private String configPath;
    private List<String> methodNames;
    private Class selectorClass;
    private TSL logger_ = TSL.get();
    private final String DB_KEY = "dataBase";
    private final String METHOD_KEY = "methodName";

    AntevortaClient(String configFilePath){
        this.configPath = configFilePath;
        verifyGitIgnore();
        parseConfigFile();
        selectorClass = Selector.class;
        populateMethods();
    }

    /**
     *
     * @param dataBase
     * @param functionName
     * @param functionParameters
     * @return
     */
    public JSONArray queryServer(String dataBase, String functionName, String...functionParameters){
        JSONObject toServer = buildJSONObject(dataBase, functionName, functionParameters);
        JSONArray results = null;
        try {
            Socket sock = new Socket("seanpgrimes.com", 3383);
            DataOutputStream serverWriter = new DataOutputStream(sock.getOutputStream());
            BufferedReader serverReader = new BufferedReader(new InputStreamReader(sock.getInputStream()));

            String jsonString = toServer.toString();
            serverWriter.writeBytes(jsonString + "\n");
            serverWriter.flush();
            String jsonArrayString = serverReader.readLine();
            results = new JSONArray(jsonArrayString);
        }
        catch(IOException e){
            return null;
        }

        return results;
    }

// ---------------------------------------------------------------------------------------------------------------------

    private void verifyGitIgnore(){

    }

    private void parseConfigFile(){

    }

    private void populateMethods(){
        // Grab this from server for more up-to-date methods list
        return;
    /*
        Method[] methods = selectorClass.getDeclaredMethods();
        for(Method m : methods)
            methodNames.add(m.toString());
    */
    }

    private void verifyMethodExists(){
        return;
    }

    private JSONObject buildJSONObject(String dataBase, String functionName, String...params){
        JSONObject json = new JSONObject();
        json.put(DB_KEY, dataBase);
        json.put(METHOD_KEY, functionName);

        int counter = 0;
        for(String param : params) {
            json.put("argument_" + Integer.toString(counter), param);
            ++counter;
        }

        return json;
    }

    public static void main(String[] args){
        AntevortaClient av = new AntevortaClient("ignore_me_for_now");
        JSONArray res = av.queryServer("RedditComs", "generalSelect", "select * from comment_attrs where author='----root'");

        // NOTE: The below is only if you want to convert back to RSMapper object for each JSON object. Definitely
        // not a necessity
        List<RSMapper> mappers = new ArrayList<>();
        for(int i = 0; i < res.length(); ++i){
            JSONObject obj = res.getJSONObject(i);
            mappers.add(new CommentSetMapper(obj));
        }
        RSMapperOutput.printAllColumnsFromRSMappers(mappers, RedditComs.columnsForPrinting(), RedditComs.dataTypesForPrinting());
    }

}
