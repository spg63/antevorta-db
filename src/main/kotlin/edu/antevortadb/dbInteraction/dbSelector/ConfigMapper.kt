package edu.antevortadb.dbInteraction.dbSelector

import javalibs.Logic
import org.json.JSONObject
import java.sql.ResultSet

class ConfigMapper: RSMapper {
    private var cols: List<String> = ArrayList()

    constructor(columns: List<String>): super() {
        Logic.get().require(columns.isNotEmpty())
        this.cols = columns
    }
    constructor(map: Map<String, String>): super(map)
    constructor(jsonObject: JSONObject): super(jsonObject)

    override fun buildMappers(rs: ResultSet): MutableList<RSMapper> {
        return buildMappersImpl(rs, cols)
    }
}