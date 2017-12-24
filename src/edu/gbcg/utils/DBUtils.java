package edu.gbcg.utils;

import org.sqlite.SQLiteConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

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
        String connString = "jdbc:sqlite:"+db;
        Connection conn = null;
        try{
            Class.forName("org.sqlite.JDBC");
            SQLiteConfig config = new SQLiteConfig();
            // This is necessary to enforce foreign keys, has to happen on *every* connection
            config.enforceForeignKeys(true);
            conn = DriverManager.getConnection(connString, config.toProperties());
        }
        catch(SQLException | ClassNotFoundException e){
            e.printStackTrace();
            if(conn != null)
                try {
                    conn.close();
                }
                catch(SQLException ex){
                    ex.printStackTrace();
                }
        }
        return conn;
    }
}
