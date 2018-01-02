package edu.gbcg.dbInteraction.dbSelector.reddit.submissions;

import edu.gbcg.dbInteraction.dbSelector.RSMapper;
import edu.gbcg.dbInteraction.dbcreator.reddit.submissions.SubmissionsFacilitator;

import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

/**
 * Specific RSMapper object for a specific DB type. The implementation for buildMappers is handled in the parent
 * abstract class, however it needs to know the column names for this specific DB so pass it the column names and let
 * it work
 */
public class SubmissionSetMapper extends RSMapper {
    public SubmissionSetMapper(Map<String, String> map){ super(map); }
    public SubmissionSetMapper(){ super(); }
    public List<RSMapper> buildMappers(ResultSet rs){
        return buildMappers_impl(rs, SubmissionsFacilitator.getColumnsForDB());
    }
}
