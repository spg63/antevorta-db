package edu.antevorta.dbInteraction.dbSelector.hollywood.movies

import edu.antevorta.dbInteraction.columnsAndKeys.TMDBCredits
import edu.antevorta.dbInteraction.dbSelector.RSMapper
import org.json.JSONObject
import java.sql.ResultSet

class TMDBCreditsSetMapper: RSMapper {
    constructor(map: Map<String, String>): super(map)
    constructor(jsonObject: JSONObject): super(jsonObject)
    constructor(): super()
    override fun buildMappers(rs: ResultSet): MutableList<RSMapper> {
        return buildMappersImpl(rs, TMDBCredits.columnNames())
    }
}
