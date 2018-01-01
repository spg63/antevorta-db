package edu.gbcg.DBSelector.RedditSubmission;

import edu.gbcg.DBSelector.RSMapper;
import edu.gbcg.dbcreator.Reddit.Submissions;

import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

public class SubmissionSetMapper extends RSMapper {
    public SubmissionSetMapper(Map<String, String> map){ super(map); }
    public SubmissionSetMapper(){ super(); }
    public List<RSMapper> buildMappers(ResultSet rs){
        return buildMappers_impl(rs, Submissions.getColumnsForDB());
    }
}
