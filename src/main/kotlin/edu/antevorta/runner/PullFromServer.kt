/*
 * Copyright (c) 2018 Sean Grimes. All Rights Reserved.
 * License: MIT
 */

package edu.antevorta.runner

import edu.antevorta.client.AntevortaClient
import edu.antevorta.configs.Finals
import edu.antevorta.configs.columnsAndKeys.RedditComs
import edu.antevorta.configs.columnsAndKeys.RedditSubs
import edu.antevorta.dbInteraction.dbSelector.BaseMapper
import edu.antevorta.dbInteraction.dbSelector.DBSelector
import edu.antevorta.dbInteraction.dbSelector.RSMapper
import edu.antevorta.dbInteraction.dbSelector.RSMapperOutput
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