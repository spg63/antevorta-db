package edu.gbcg.dbInteraction.dbcreator;

import edu.gbcg.dbInteraction.DBCommon;

import java.sql.Connection;

public class IndexWorker implements Runnable{
    Connection conn;
    String SQLStatement;

    public IndexWorker(Connection conn, String SQLStatement){
        this.conn = conn;
        this.SQLStatement = SQLStatement;
    }

    public void run(){
        DBCommon.execute(conn, SQLStatement);
    }
}
