package edu.gbcg.dbcreator;

import edu.gbcg.configs.StateVars;
import edu.gbcg.utils.DBUtils;
import edu.gbcg.utils.TSL;
import edu.gbcg.utils.c;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static edu.gbcg.utils.c.writeln_err;

/**
 * Class to hold functionality shared among DB classes
 */
public class DBCommon {

    /**
     * Gets a DB Connection object based on db name, the URL to the DB and the type of DB driver in
     * use. The URL prefix and DB Driver class are both found in StateVars
     * @param db Name of the database
     * @return The connection to the db
     */
    public static Connection connect(String db){
        Connection conn = DBUtils.get().connect(db, StateVars.DB_URL_PREFIX, StateVars.DB_DRIVER);
        // Turn off synchronous mode for the reddit DB connections to increase performance
        if(StateVars.SYNC_MODE_OFF) {
            try {
                Statement st = conn.createStatement();
                String sql = "PRAGMA synchronous=OFF";
                st.execute(sql);
                st.close();
            } catch (SQLException e) {
                TSL.get().err("DBCommon.connect exception");
                e.printStackTrace();
            }
        }

        return conn;
    }

    /**
     * Close database connection
     * @param conn The DB connection
     */
    public static void disconnect(Connection conn){
        DBUtils.get().disconnect(conn);
    }

    /**
     * Perform a DB insertion
     * NOTE: The connection will not be closed for you
     * @param conn The connection to the DB
     * @param SQLStatement The SQLstatement, as a string
     */
    public static void insert(Connection conn, String SQLStatement){
        DBUtils.get().insert(conn, SQLStatement);
    }

    /**
     * Perform a DB insertion
     * NOTE: There is no connection to close
     * @param db The path to the DB
     * @param SQLStatement The SQLstatement, as a string
     */
    public static void insert(String db, String SQLStatement){
        DBUtils.get().insert(db, StateVars.DB_URL_PREFIX, StateVars.DB_DRIVER, SQLStatement);
    }

    /**
     * Perform a DB deletion operation
     * NOTE: The connection will not be closed for you
     * @param conn The connection to the DB
     * @param SQLStatement The SQLStatement, as a string
     */
    public static void delete(Connection conn, String SQLStatement){
        DBUtils.get().delete(conn, SQLStatement);
    }

    /**
     * Perform a DB deletion operation
     * NOTE: There is no connection to close
     * @param db The path to the DB
     * @param SQLStatement The SQL Statement, as a string
     */
    public static void delete(String db, String SQLStatement){
        DBUtils.get().delete(db, StateVars.DB_URL_PREFIX, StateVars.DB_DRIVER, SQLStatement);
    }

    /**
     * Executes a batch insertion. There is no batch size limit. This function assumes the user
     * has properly split the insertion into managable chunks.
     * NOTE: The connection will not be closed for you
     * @param conn The DB Connection
     * @param SQLStatements A list of SQL statements
     */
    public static void insertAll(Connection conn, List<String> SQLStatements){
        DBUtils.get().insertAll(conn, SQLStatements);
    }

    /**
     * Executes a batch insertion. There is no batch size limit. This function assumes the user
     * has properly split the insertion into manageable chunks.
     * NOTE: There is no connection to close
     * @param db The path to the DB
     * @param SQLStatements A list of SQL statements
     */
    public static void insertAll(String db, List<String> SQLStatements){
        DBUtils.get().insertAll(db, StateVars.DB_URL_PREFIX, StateVars.DB_DRIVER, SQLStatements);
    }

    /**
     * Executs a batch deletion. There is no batch size limit. This function assumes the user has
     * properly split the deletion into manageable chunks.
     * NOTE: The connection will not be closed for you
     * @param conn The DB connection
     * @param SQLStatements A list of SQL statements
     */
    public static void deleteAll(Connection conn, List<String> SQLStatements){
        DBUtils.get().deleteAll(conn, SQLStatements);
    }

    /**
     * Executs a batch deletion. There is no batch size limit. This function assumes the user has
     * properly split the deletion into manageable chunks.
     * @param db The path ot the DB
     * @param SQLStatements A list of SQL statements
     */
    public static void deleteAll(String db, List<String> SQLStatements){
        DBUtils.get().deleteAll(db, StateVars.DB_URL_PREFIX, StateVars.DB_DRIVER, SQLStatements);
    }

    /**
     * Get a single result set from a single selection statement
     * NOTE: The connection must remain open while you require access to the ResultSet
     * @param conn The connection to the DB
     * @param SQLStatement SQL select statement
     * @return A ResultSet if the selection was successful
     */
    public static ResultSet select(Connection conn, String SQLStatement){
        return DBUtils.get().select(conn, SQLStatement);
    }

    /**
     * Performs a batch selection
     * NOTE: The connection must remain open while you require access to the ResultSet
     * @param conn The connection to the DB
     * @param SQLStatements SQL select statements
     * @return A list of ResultSet objects if the selections were successful
     */
    public static List<ResultSet> selectAll(Connection conn, List<String> SQLStatements){
        return DBUtils.get().selectAll(conn, SQLStatements);
    }

    /**
     * Perform a non insert, delete, update operation
     * @param conn
     * @param SQLStatement
     */
    public static void execute(Connection conn, String SQLStatement){
        DBUtils.get().execute(conn, SQLStatement);
    }

    /**
     * Get a SQL string for index creation
     * @param table Table to create the index for
     * @param columnToIndex Column name the index is being created on
     * @param indexName What to call the index
     * @return The SQL string
     */
    public static String getDBIndexSQLStatement(String table, String columnToIndex, String indexName){
        String idx = "create index "+indexName+" on "+table+"("+columnToIndex+");";
        return idx;
    }
}
