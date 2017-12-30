package edu.gbcg.dbcreator;

import edu.gbcg.utils.c;

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
