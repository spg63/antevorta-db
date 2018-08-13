/*
 * Copyright (c) 2018 Sean Grimes. All Rights Reserved.
 * License: MIT
 */

@file:Suppress("unused")

package edu.antevortadb.runner

import edu.antevortadb.client.AntevortaClient
import edu.antevortadb.configs.Finals
import edu.antevortadb.configs.RawDataLocator
import edu.antevortadb.dbInteraction.columnsAndKeys.RedditComs
import edu.antevortadb.dbInteraction.columnsAndKeys.RedditSubs
import edu.antevortadb.dbInteraction.dbSelector.BaseMapper
import edu.antevortadb.dbInteraction.dbSelector.DBSelector
import edu.antevortadb.dbInteraction.dbSelector.RSMapper
import edu.antevortadb.dbInteraction.dbSelector.RSMapperOutput
import org.json.JSONObject

object PullFromServer{
    fun doServerComs(){
        val client = AntevortaClient(RawDataLocator.clientConfigFile())
        val author = "a4k04"

        val dbsql = DBSelector()
                .from(Finals.REDDIT_COM_TABLE)
                .where("author = '$author'")
                .orderBy(Finals.CREATED_DT, true)
                .orderBy("subreddit_name")
                .limit(10)

        logger.info(dbsql.sql())

        // If results are null, return
        val jsonResults = client.queryServer(dbsql.sql())

        // Get all objects from the JSONArray
        val objects = ArrayList<JSONObject>()
        for(i in 0 until jsonResults.length())
            objects.add(jsonResults.getJSONObject(i))

        val mappers = ArrayList<RSMapper>()
        for(jsonobj in objects)
            mappers.add(BaseMapper(jsonobj))

        RSMapperOutput.printAllColumnsFromRSMappers(mappers, RedditComs.columnsForPrinting(),
                RedditComs.dataTypesForPrinting())
    }

    fun doServerSubs(){
        val client = AntevortaClient(RawDataLocator.clientConfigFile())
        val author = "SciTroll"

        val dbsql = DBSelector()
                .from(Finals.REDDIT_SUB_TABLE)
                .where("author = '$author'")
                .orderBy("created_dt")

        val jsonResults = client.queryServer(dbsql.sql())

        val objects = ArrayList<JSONObject>()
        for(i in 0 until jsonResults.length())
            objects.add(jsonResults.getJSONObject(i))

        val mappers = ArrayList<RSMapper>()
        for(jsonobj in objects)
            mappers.add(BaseMapper(jsonobj))

        RSMapperOutput.printAllColumnsFromRSMappers(mappers, RedditSubs.columnsForPrinting(),
                RedditSubs.dataTypesForPrinting())
    }
}
