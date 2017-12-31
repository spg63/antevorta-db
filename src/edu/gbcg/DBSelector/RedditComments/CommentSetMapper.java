package edu.gbcg.DBSelector.RedditComments;

import edu.gbcg.DBSelector.RSMapper;
import edu.gbcg.dbcreator.Reddit.Comments;
import edu.gbcg.utils.TSL;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommentSetMapper extends RSMapper {
    public CommentSetMapper(Map<String, String> map){ super(map); }
    public CommentSetMapper(){ super(); }

    public List<RSMapper> buildMappers(ResultSet rs){
        List<RSMapper> maps = new ArrayList<>();

        if(rs == null)
            return maps;

        List<String> colNames = Comments.getColumnsForDB();
        Map<String, Integer> colIDs = new HashMap<>();

        try{
            if(rs.isClosed())
                return maps;

            for(String col : colNames){
                int colIDX;
                try{
                    colIDX = rs.findColumn(col);
                }
                catch(SQLException e){
                    continue;
                }
                colIDs.put(col, colIDX);
            }

            while(rs.next()){
                Map<String, String> map = new HashMap<>();
                for(String col : colIDs.keySet())
                    map.put(col, rs.getString(colIDs.get(col)));
                maps.add(new CommentSetMapper(map));
            }
        }
        catch(SQLException e){
            TSL.get().err("RSMapper.buildMapper SQLException");
            e.printStackTrace();
        }
        return maps;
    }
}
