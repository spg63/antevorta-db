package edu.gbcg.DBSelector;

import edu.gbcg.dbcreator.DBCommon;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * SelectionWorker is designed to work for any DB objects. In order to do this is needs to know the path to the DB,
 * the query to run on the DB, and which time os RSMapper object it will be populating. Each mapper object handles
 * the specifics of pulling data out of a ResultSet for a given DB connection
 */
public class SelectionWorker implements Callable {
    String db;
    String SQLStatement;
    RSMapper rsMapper;

    /**
     * Build a SelectionWorker
     * @param dbPath The path to the DB file
     * @param SQLQuery The query to run on the DB
     * @param rsMapper The mapper object to be populated with data from the ResultSet
     */
    public SelectionWorker(String dbPath, String SQLQuery, RSMapper rsMapper){
        this.db = dbPath;
        this.SQLStatement = SQLQuery;
        this.rsMapper = rsMapper;
    }

    /**
     * A thread running on a single DB shard
     * @return A list of populated RSMapper objects
     */
    public List<RSMapper> call(){
        // Connect to the DB
        Connection conn = DBCommon.connect(this.db);

        // Create the ResultSet
        ResultSet rs = DBCommon.select(conn, this.SQLStatement);

        // Pull all results from the ResultSet
        List<RSMapper> mappers = this.rsMapper.buildMappers(rs);

        // Close the ResultSet, we've got all the data
        DBCommon.closeResultSet(rs);

        // Close the connection, we're done
        DBCommon.disconnect(conn);
        return mappers;
    }
}
