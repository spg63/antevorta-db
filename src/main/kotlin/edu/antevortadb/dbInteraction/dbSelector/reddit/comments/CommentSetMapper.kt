/*
 * Copyright (c) 2018 Sean Grimes. All Rights Reserved.
 * License: MIT
 */

package edu.antevortadb.dbInteraction.dbSelector.reddit.comments

import edu.antevortadb.dbInteraction.columnsAndKeys.RedditComs
import edu.antevortadb.dbInteraction.dbSelector.RSMapper
import org.json.JSONObject
import java.sql.ResultSet

@Suppress("unused")
class CommentSetMapper: RSMapper {
    constructor(map: Map<String, String>): super(map)
    constructor(jsonObject: JSONObject): super(jsonObject)
    constructor(): super()
    override fun buildMappers(rs: ResultSet): MutableList<RSMapper> {
        return buildMappersImpl(rs, RedditComs.columnNames())
    }
}
