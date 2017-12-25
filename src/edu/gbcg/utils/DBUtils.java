package edu.gbcg.utils;

import edu.gbcg.configs.StateVars;
import org.sqlite.SQLiteConfig;

import java.sql.*;
import java.util.List;

public class DBUtils {
    private static volatile DBUtils _instance = null;
    private static final int QUERY_TIMEOUT = 60;

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
        String connString = StateVars.DB_URL_PREFIX + db;
        Connection conn = null;
        try{
            Class.forName(StateVars.DB_DRIVER);
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

    public void disconnect(Connection conn){
        try{
            conn.close();
        }
        catch(SQLException e){
            e.printStackTrace();
        }
    }

    public void insert(Connection conn, String SQLStatement){
        executeGenericUpdate(conn, SQLStatement);
    }

    public void insert(String db, String SQLStatement) {
        executeGenericUpdate(db, SQLStatement);
    }

    public void delete(Connection conn, String SQLStatement){
        executeGenericUpdate(conn, SQLStatement);

    }

    public void delete(String db, String SQLStatement){
        executeGenericUpdate(db, SQLStatement);
    }

    public void insertAll(Connection conn, List<String> SQLStatements){
        executeBatchUpdate(conn, SQLStatements);
    }

    public void insertAll(String db, List<String> SQLStatements){
        executeBatchUpdate(db, SQLStatements);
    }

    public void deleteAll(Connection conn, List<String> SQLStatements){
        executeBatchUpdate(conn, SQLStatements);
    }

    public void deleteAll(String db, List<String> SQLStatements){
        executeBatchUpdate(db, SQLStatements);
    }

    public ResultSet select(Connection conn, String SQLStatement){
        return null;
    }

    public List<ResultSet> selectAll(Connection conn, List<String> SQLstatements){
        return null;
    }

    private void executeBatchUpdate(String db, List<String> SQLStatements){
        Connection conn = connect(db);
        executeBatchUpdate(conn, SQLStatements);
        try{
            conn.close();
        }
        catch(SQLException e){
            e.printStackTrace();
        }
    }

    private void executeBatchUpdate(Connection conn, List<String> SQLStatements){
        Statement stmt = null;
        try {
            stmt = conn.createStatement();

            // Disable autocommit
            conn.setAutoCommit(false);

            for(int i = 0; i < SQLStatements.size(); ++i) {
                stmt.addBatch(SQLStatements.get(i));
            }

            stmt.executeBatch();
            conn.commit();
        }
        catch(SQLException e){
            e.printStackTrace();
            try {
                conn.rollback();
            }
            catch(SQLException ex){
                ex.printStackTrace();
            }
        }
        finally{
            // stmt should have been closed in stmtExecuteBatch
            if(stmt != null){
                try{
                    stmt.close();
                }
                catch(SQLException e){
                    e.printStackTrace();
                }
            }
            try {
                if (!conn.getAutoCommit())
                    conn.setAutoCommit(true);
            }
            catch(SQLException ex){
                ex.printStackTrace();
            }
        }
    }

    // Separate function to close the DB connection for user
    private void executeGenericUpdate(String db, String SQLStatement){
        Connection conn = connect(db);
        executeGenericUpdate(conn, SQLStatement);
        try{
            conn.close();
        }
        catch(SQLException e){
            e.printStackTrace();
        }
    }

    private void executeGenericUpdate(Connection conn, String SQLStatement){
        Statement stmt = null;
        try{
            stmt = conn.createStatement();
            stmt.setQueryTimeout(QUERY_TIMEOUT);
            stmt.executeUpdate(SQLStatement);
        }
        catch(SQLException e){
            e.printStackTrace();;
        }
        finally{
            if(stmt != null) {
                try {
                    stmt.close();
                }
                catch(SQLException e){
                    e.printStackTrace();
                }
            }
        }
    }


}
