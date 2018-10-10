package edu.antevortadb.dbInteraction.dbSelector.hollywood

import edu.antevortadb.dbInteraction.columnsAndKeys.MovielensMovies
import edu.antevortadb.dbInteraction.dbSelector.RSMapper
import org.json.JSONObject
import java.sql.ResultSet

@Suppress("unused")
class MLMoviesSetMapper: RSMapper {
    constructor(map: Map<String, String>): super(map)
    constructor(jsonObject: JSONObject): super(jsonObject)
    constructor(): super()
    override fun buildMappers(rs: ResultSet): MutableList<RSMapper> {
        return buildMappersImpl(rs, MovielensMovies.columnNames())
    }
}
