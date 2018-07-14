package edu.antevorta.dbInteraction.dbSelector.hollywood.movies

import edu.antevorta.dbInteraction.columnsAndKeys.MovielensMovies
import edu.antevorta.dbInteraction.dbSelector.RSMapper
import org.json.JSONObject
import java.sql.ResultSet

class MLMoviesSetMapper: RSMapper {
    constructor(map: Map<String, String>): super(map)
    constructor(jsonObject: JSONObject): super(jsonObject)
    constructor(): super()
    override fun buildMappers(rs: ResultSet): MutableList<RSMapper> {
        return buildMappersImpl(rs, MovielensMovies.columnNames())
    }
}