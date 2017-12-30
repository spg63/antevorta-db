package edu.gbcg.utils;

import edu.gbcg.configs.StateVars;
import org.sqlite.SQLiteConfig;

import java.sql.*;
import java.util.List;

/**
 * Database utility functions. Nonspecific to this project.
 */
public class DBUtils {
    private static volatile DBUtils _instance = null;
    private static final int QUERY_TIMEOUT = 240;

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

    /**
     * Return a connection to the database string, if valid, using the DB driver and DB url
     * prefix set in StateVars
     * @param db Path to your DB
     * @param dbURL The URL for the DB (e.g. jdbc:sqlite:)
     * @param dbDriverClassName The name of your DB driver (e.g. org.sqlite.JDBC)
     * @return The connection
     */
    public Connection connect(String db, String dbURL, String dbDriverClassName){
        String connString = dbURL + db;
        Connection conn = null;
        try{
            Class.forName(dbDriverClassName);
            SQLiteConfig config = new SQLiteConfig();
            // This is necessary to enforce foreign keys, has to happen on *every* connection
            //config.enforceForeignKeys(true);
            conn = DriverManager.getConnection(connString, config.toProperties());
        }
        catch(SQLException | ClassNotFoundException e){
            e.printStackTrace();
            if(conn != null) {
                try {
                    conn.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
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

    /**
     * Perform a DB insertion.
     * NOTE: The connection will not be closed for you
     * @param conn The connection to the DB
     * @param SQLStatement The SQLstatement, as a string
     */
    public void insert(Connection conn, String SQLStatement){
        executeGenericUpdate(conn, SQLStatement);
    }

    /**
     * Perform a DB operation not related to insert, delete, update
     * @param conn
     * @param SQLStatement
     */
    public void execute(Connection conn, String SQLStatement){
        executeGenericUpdate(conn, SQLStatement);
    }

    /**
     * Perform a DB insertion
     * NOTE: There is no connection to close
     * @param db The path to the DB
     * @param SQLStatement The SQLstatement, as a string
     * @param dbURL The URL for the db
     * @param dbDriverClassName The name of your DB driver class
     */
    public void insert(String db, String dbURL, String dbDriverClassName, String SQLStatement) {
        executeGenericUpdate(db, dbURL, dbDriverClassName, SQLStatement);
    }

    /**
     * Perform a DB deletion operation
     * NOTE: The connection will not be closed for you
     * @param conn The connection to the DB
     * @param SQLStatement The SQLStatement, as a string
     */
    public void delete(Connection conn, String SQLStatement){
        executeGenericUpdate(conn, SQLStatement);
    }

    /**
     * Perform a DB deletion operation
     * NOTE: There is no connection to close
     * @param db The path to the DB
     * @param SQLStatement The SQL Statement, as a string
     * @param dbURL The URL for the db
     * @param dbDriverClassName The name of your DB driver class
     */
    public void delete(String db, String dbURL, String dbDriverClassName, String SQLStatement) {
        executeGenericUpdate(db, dbURL, dbDriverClassName, SQLStatement);
    }

    /**
     * Executes a batch insertion. There is no batch size limit. This function assumes the user
     * has properly split the insertion into manageable chunks.
     * NOTE: The connection will not be closed for you
     * @param conn The DB Connection
     * @param SQLStatements A list of SQL statements
     */
    public void insertAll(Connection conn, List<String> SQLStatements){
        executeBatchUpdate(conn, SQLStatements);
    }

    /**
     * Executes a batch insertion. There is no batch size limit. This function assumes the user
     * has properly split the insertion into manageable chunks.
     * NOTE: There is no connection to close
     * @param db The path to the DB
     * @param SQLStatements A list of SQL statements
     * @param dbURL The URL for the db
     * @param dbDriverClassName The name of your DB driver class
     */
    public void insertAll(String db, String dbURL, String dbDriverClassName,
                          List<String> SQLStatements){
        executeBatchUpdate(db, dbURL, dbDriverClassName, SQLStatements);
    }

    /**
     * Executs a batch deletion. There is no batch size limit. This function assumes the user has
     * properly split the deletion into manageable chunks.
     * NOTE: The connection will not be closed for you
     * @param conn The DB connection
     * @param SQLStatements A list of SQL statements
     */
    public void deleteAll(Connection conn, List<String> SQLStatements){
        executeBatchUpdate(conn, SQLStatements);
    }

    /**
     * Executs a batch deletion. There is no batch size limit. This function assumes the user has
     * properly split the deletion into manageable chunks.
     * @param db The path to the DB
     * @param SQLStatements A list of SQL statements
     * @param dbURL The URL for the db
     * @param dbDriverClassName The name of your DB driver class
     */
    public void deleteAll(String db, String dbURL, String dbDriverClassName,
                          List<String> SQLStatements){
        executeBatchUpdate(db, dbURL, dbDriverClassName, SQLStatements);
    }

    /**
     * Get a single result set from a single selection statement
     * NOTE: The connection must remain open while you require access to the ResultSet
     * @param conn The connection to the DB
     * @param SQLStatement SQL select statement
     * @return A ResultSet if the selection was successful
     */
    public ResultSet select(Connection conn, String SQLStatement){
        Statement stmt;
        ResultSet rs = null;
        try {
            stmt = conn.createStatement();
            stmt.setFetchSize(1000);
            rs = stmt.executeQuery(SQLStatement);
        }
        catch(SQLException e){
            e.printStackTrace();
        }

        return rs;
    }

    /**
     * Performs a batch selection
     * NOTE: The connection must remain open while you require access to the ResultSet
     * @param conn The connection to the DB
     * @param SQLStatements SQL select statements
     * @return A list of ResultSet objects if the selections were successful
     */
    public List<ResultSet> selectAll(Connection conn, List<String> SQLStatements){
        return null;
    }

    /*
        ** NO JAVADOC **
        * Executes a batch update for insert / delete. Manages the Connection object itself
     */
    private void executeBatchUpdate(String db, String dbURL, String dbDriverClassName,
                                    List<String> SQLStatements){
        Connection conn = connect(db, dbURL, dbDriverClassName);
        executeBatchUpdate(conn, SQLStatements);
        try{
            conn.close();
        }
        catch(SQLException e){
            e.printStackTrace();
        }
    }

    /*
        ** NO JAVADOC **
        * Excutes a batch update for insert / delete. Does not close the connection object.
     */
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

    /*
        ** NO JAVADOC **
        * Executes a single insert / delete / update. Manages connection object.
     */
    private void executeGenericUpdate(String db, String dbURL, String dbDriverClassName,
                                      String SQLStatement){
        Connection conn = connect(db, dbURL, dbDriverClassName);
        executeGenericUpdate(conn, SQLStatement);
        try{
            conn.close();
        }
        catch(SQLException e){
            e.printStackTrace();
        }
    }

    /*
        ** NO JAVADOC **
        * Executes a single insert / delete / update operation
     */
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
