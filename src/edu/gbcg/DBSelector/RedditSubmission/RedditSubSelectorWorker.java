package edu.gbcg.DBSelector.RedditSubmission;


import edu.gbcg.dbcreator.DBCommon;
import edu.gbcg.utils.c;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.concurrent.Callable;

public class RedditSubSelectorWorker implements Callable{
    Connection conn;
    String SQLStatement;

    public RedditSubSelectorWorker(Connection conn, String SQLQuery){
        this.conn = conn;
        this.SQLStatement = SQLQuery;
    }

    public ResultSet call(){
        return DBCommon.select(this.conn, this.SQLStatement);
    }
}
