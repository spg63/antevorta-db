/*
 * Copyright (c) 2018 Sean Grimes. All Rights Reserved.
 * License: MIT
 */

package edu.gbcg.dbInteraction.dbSelector

import org.json.JSONObject
import java.sql.ResultSet

/**
 * This class implements no functionality regarding building a Mapper object, and doesn't know how to read a
 * ResultSet but is used as a return type from buildMappers to avoid switching over the class type which is become
 * tedious to update as more DB types are added to the project
 */
class BaseMapper: RSMapper {
    constructor(): super()
    constructor(map: Map<String, String>): super(map)
    constructor(jsonObject: JSONObject): super(jsonObject)
    override fun buildMappers(rs: ResultSet): MutableList<RSMapper> {
        logger_.logAndKill("BaseMapper.buildMappers was called, this is not allowed!")
        return ArrayList()
    }
}