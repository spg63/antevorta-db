package edu.gbcg.DBSelector.RedditComments;

import edu.gbcg.DBSelector.RSMapper;
import edu.gbcg.dbcreator.DBCommon;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

public class RedditComSelectorWorker implements Callable {
    String db;
    String SQLStatement;
    Map<String, Integer> colIDs = new HashMap<>();

    public RedditComSelectorWorker(String dbPath, String SQLQuery){
        this.db = dbPath;
        this.SQLStatement = SQLQuery;
    }

    public List<RSMapper> call(){
        Connection conn = DBCommon.connect(this.db);

        ResultSet rs = DBCommon.select(conn, this.SQLStatement);

        RSMapper rsMapper = new CommentSetMapper();
        List<RSMapper> maps = rsMapper.buildMappers(rs);

        DBCommon.closeResultSet(rs);
        DBCommon.disconnect(conn);
        return maps;
    }
}
