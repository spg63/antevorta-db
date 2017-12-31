package edu.gbcg.DBSelector.RedditSubmission;


import edu.gbcg.DBSelector.RSMapper;
import edu.gbcg.dbcreator.DBCommon;
import edu.gbcg.dbcreator.RedditSubmissions;
import edu.gbcg.utils.TSL;
import edu.gbcg.utils.c;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

public class RedditSubSelectorWorker implements Callable{
    String db;
    String SQLStatement;
    Map<String, Integer> colIDs = new HashMap<>();

    public RedditSubSelectorWorker(String dbPath, String SQLQuery){
        this.db = dbPath;
        this.SQLStatement = SQLQuery;
    }

    public List<RSMapper> call(){
        Connection conn = DBCommon.connect(this.db);

        // Make the selection
        ResultSet rs = DBCommon.select(conn, this.SQLStatement);

        // Populate the hashmap with the resultset data
        RSMapper rsMapper = new SubmissionSetMapper();
        List<RSMapper> maps = rsMapper.buildMappers(rs);

        // Close the resultset and the connection
        DBCommon.closeResultSet(rs);
        DBCommon.disconnect(conn);
        return maps;
    }
}
