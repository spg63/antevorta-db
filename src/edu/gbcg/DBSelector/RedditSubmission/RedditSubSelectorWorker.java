package edu.gbcg.DBSelector.RedditSubmission;


import edu.gbcg.DBSelector.RSMapper;
import edu.gbcg.DBSelector.SelectionWorker;
import edu.gbcg.dbcreator.DBCommon;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.List;

public class RedditSubSelectorWorker implements SelectionWorker {
    String db;
    String SQLStatement;

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
