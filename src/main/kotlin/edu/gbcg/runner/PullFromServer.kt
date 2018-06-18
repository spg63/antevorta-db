/*
 * Copyright (c) 2018 Sean Grimes. All Rights Reserved.
 * License: MIT
 */

package edu.gbcg.runner

import edu.gbcg.client.AntevortaClient
import edu.gbcg.configs.Finals
import edu.gbcg.configs.columnsAndKeys.RedditComs
import edu.gbcg.configs.columnsAndKeys.RedditSubs
import edu.gbcg.dbInteraction.dbSelector.BaseMapper
import edu.gbcg.dbInteraction.dbSelector.DBSelector
import edu.gbcg.dbInteraction.dbSelector.RSMapper
import edu.gbcg.dbInteraction.dbSelector.RSMapperOutput
import org.json.JSONObject

object PullFromServer{
    fun doServerComs(){
        val client = AntevortaClient(Finals.CLIENT_CONFIG)
        val author = "a4k04"

        val dbsql = DBSelector()
                .from(Finals.REDDIT_COM_TABLE)
                .where("author = '$author'")
                .orderBy(Finals.CREATED_DT, true)
                .orderBy("subreddit_name")
                .limit(10)

        logger_.info(dbsql.sql())

        // If results are null, return
        val jsonResults = client.queryServer(dbsql.sql()) ?: return

        // Get all objects from the JSONArray
        val objects = ArrayList<JSONObject>()
        for(i in 0 until jsonResults.length())
            objects.add(jsonResults.getJSONObject(i))

        val mappers = ArrayList<RSMapper>()
        for(jsonobj in objects)
            mappers.add(BaseMapper(jsonobj))

        RSMapperOutput.printAllColumnsFromRSMappers(mappers, RedditComs.columnsForPrinting(), RedditComs.dataTypesForPrinting())
    }

    fun doServerSubs(){
        val client = AntevortaClient(Finals.CLIENT_CONFIG)
        val author = "SciTroll"

        val dbsql = DBSelector()
                .from(Finals.REDDIT_SUB_TABLE)
                .where("author = '$author'")
                .orderBy("created_dt")

        val jsonResults = client.queryServer(dbsql.sql()) ?: return

        val objects = ArrayList<JSONObject>()
        for(i in 0 until jsonResults.length())
            objects.add(jsonResults.getJSONObject(i))

        val mappers = ArrayList<RSMapper>()
        for(jsonobj in objects)
            mappers.add(BaseMapper(jsonobj))

        RSMapperOutput.printAllColumnsFromRSMappers(mappers, RedditSubs.columnsForPrinting(), RedditSubs.dataTypesForPrinting())
    }
}