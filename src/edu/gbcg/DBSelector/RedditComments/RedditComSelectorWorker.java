package edu.gbcg.DBSelector.RedditComments;

import edu.gbcg.DBSelector.RSMapper;
import edu.gbcg.DBSelector.SelectionWorker;
import edu.gbcg.dbcreator.DBCommon;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.List;

public class RedditComSelectorWorker implements SelectionWorker {
    String db;
    String SQLStatement;

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
