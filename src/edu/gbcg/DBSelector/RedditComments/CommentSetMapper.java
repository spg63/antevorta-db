package edu.gbcg.DBSelector.RedditComments;

import edu.gbcg.DBSelector.RSMapper;
import edu.gbcg.dbcreator.Reddit.Comments;

import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

public class CommentSetMapper extends RSMapper {
    public CommentSetMapper(Map<String, String> map){ super(map); }
    public CommentSetMapper(){ super(); }
    public List<RSMapper> buildMappers(ResultSet rs){
        return buildMappers_impl(rs, Comments.getColumnsForDB());
    }
}
