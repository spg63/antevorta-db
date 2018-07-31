/*
 * Copyright (c) 2018 Sean Grimes. All Rights Reserved.
 * License: MIT
 */

package edu.antevorta.dbInteraction.dbSelector.hollywood.movies

import edu.antevorta.dbInteraction.columnsAndKeys.MovielensGenomeTags
import edu.antevorta.dbInteraction.dbSelector.RSMapper
import org.json.JSONObject
import java.sql.ResultSet

@Suppress("unused")
class MLGenomeTagsSetMapper: RSMapper {
    constructor(map: Map<String, String>): super(map)
    constructor(jsonObject: JSONObject): super(jsonObject)
    constructor(): super()
    override fun buildMappers(rs: ResultSet): MutableList<RSMapper> {
        return buildMappersImpl(rs, MovielensGenomeTags.columnNames())
    }
}
