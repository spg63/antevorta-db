package edu.gbcg.DBSelector.RedditSubmission;

import edu.gbcg.DBSelector.RSMapper;
import edu.gbcg.dbcreator.RedditSubmissions;
import edu.gbcg.utils.TSL;
import edu.gbcg.utils.c;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SubmissionSetMapper extends RSMapper {

    public SubmissionSetMapper(Map<String, String> map){ super(map); }
    public SubmissionSetMapper(){ super(); }

    public List<RSMapper> buildMappers(ResultSet rs){
        List<RSMapper> maps = new ArrayList<>();

        if(rs == null)
            return maps;

        List<String> colNames = RedditSubmissions.getColumnsForDB();
        Map<String, Integer> colIDs = new HashMap<>();
        try{
            // If there are no results for a search the resultset will appear closed
            if(rs.isClosed())
                return maps;

            // Find the index for each column to prevent a lot of string comparisons
            //***** PROBLEM: If the col doesn't exist
            for (String col : colNames)
                colIDs.put(col, rs.findColumn(col));

            // Loop through all results that were found
            while(rs.next()){
                Map<String, String> map = new HashMap<>();

                // For each column, check to see if we have a value and if so, add it to the map
                for(String col : colIDs.keySet())
                    map.put(col, rs.getString(colIDs.get(col)));

                // Add it to the list
                maps.add(new SubmissionSetMapper(map));
            }

        }catch(SQLException e){
            TSL.get().err("RSMapper.buildMappers SQLException");
            e.printStackTrace();
        }

        return maps;
    }

}
