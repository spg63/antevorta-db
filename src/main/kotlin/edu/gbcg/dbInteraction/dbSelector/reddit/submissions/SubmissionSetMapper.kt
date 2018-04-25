/*
 * Copyright (c) 2018 Sean Grimes. All Rights Reserved.
 * License: MIT
 */

package edu.gbcg.dbInteraction.dbSelector.reddit.submissions

import edu.gbcg.configs.columnsAndKeys.RedditSubs
import edu.gbcg.dbInteraction.dbSelector.RSMapper
import org.json.JSONObject
import java.sql.ResultSet

class SubmissionSetMapper: RSMapper {
    constructor(map: Map<String, String>): super(map)
    constructor(jsonObject: JSONObject): super(jsonObject)
    constructor(): super()
    override fun buildMappers(rs: ResultSet): MutableList<RSMapper>? {
        return buildMappersImpl(rs, RedditSubs.columnNames())
    }
}

