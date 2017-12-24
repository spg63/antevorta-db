package edu.gbcg.utils;

import java.sql.Connection;

public class DBUtils {
    private static volatile DBUtils _instance = null;

    private DBUtils(){}
    public static DBUtils get(){
        if(_instance == null){
            synchronized (DBUtils.class){
                if(_instance == null){
                    _instance = new DBUtils();
                }
            }
        }
        return _instance;
    }

    public Connection connect(String db){
        Connection conn = null;
        try{
            //Class.forName("org.sqlite.JDBC");
            //SQLiteConfig config = new SQLiteConfig();

        }
        catch(Exception e){

        }
        return conn;
    }
}
