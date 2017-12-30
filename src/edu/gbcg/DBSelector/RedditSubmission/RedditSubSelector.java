package edu.gbcg.DBSelector.RedditSubmission;

import edu.gbcg.configs.DBLocator;
import edu.gbcg.configs.StateVars;
import edu.gbcg.dbcreator.DBCommon;
import edu.gbcg.utils.TSL;
import edu.gbcg.utils.c;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/*
****************************************************************************************************
    NOTE: I fucking hate how this works.
    Need some fucking classes to deal with getting the data back from the DB and connection
    management needs to be handled in this class or (even better) in each thread that connects to
     the DB in the RedditSubSelectorWorker class. Fuck this noise of returning ResultSets all
     over the fucking place. Fuck it fuck it fuck it.
****************************************************************************************************
 */
public class RedditSubSelector {
    private static List<String> DBs = DBLocator.redditSubsAbsolutePaths();

    public static void testItOut(String SQLStatement){
        List<Connection> conns = new ArrayList<>();
        DBs = DBLocator.redditSubsAbsolutePaths();
        for(int i = 0; i < DBs.size(); ++i){
            conns.add(DBCommon.connect(DBs.get(i)));
        }
        List<ResultSet> res = generalSelection(conns, SQLStatement);
        String score = "score";
        String sub_name = "subreddit_name";
        String title = "post_title";
        for(int i = 0; i < res.size(); ++i){
            try {
                while (res.get(i).next()) {
                    c.writeln(score + ": " + res.get(i).getString(score));
                    c.writeln(sub_name + ": " + res.get(i).getString(sub_name));
                    c.writeln(title + ": " + res.get(i).getString(title));
                    c.writeln("");
                    c.writeln("----------");
                    c.writeln("");
                    //c.writeln("sub_name: " + res.get(i).getString("subreddit_name"));
                }
            }
            catch(SQLException e){
                e.printStackTrace();
            }
        }
    }

    public static List<ResultSet> generalSelection(List<Connection> conns, String SQLStatement){
        verifyDBsExist();
        return genericSelect(conns, SQLStatement);
    }

    private static List<ResultSet> genericSelect(List<Connection> conns, String SQLStatement){
        // List of futures for the threads to return to
        List<Future<ResultSet>> future_results = new ArrayList<>();

        // ThreadPool to handle the DB operations
        ExecutorService executor = Executors.newFixedThreadPool(StateVars.DB_SHARD_NUM);

        for(int i = 0; i < StateVars.DB_SHARD_NUM; ++i){
            RedditSubSelectorWorker worker = new RedditSubSelectorWorker(conns.get(i), SQLStatement);
            Future<ResultSet> future = executor.submit(worker);
            future_results.add(future);
        }

        // Copy the ResultSets out of the futures. Also handles waiting on the Thread to finish
        List<ResultSet> results = new ArrayList<>();
        try {
            for (int i = 0; i < StateVars.DB_SHARD_NUM; ++i)
                results.add(future_results.get(i).get());
        }
        catch(InterruptedException e){
            TSL.get().err("RedditSubSelector.genericSelect InterruptedException");
        }
        catch(ExecutionException ex){
            TSL.get().err("RedditSubSelector.genericSelect ExecutionException");
        }

        // Shutdown the executor threadpool
        executor.shutdown();

        if(results.isEmpty())
            return null;
        return results;
    }

    private static void verifyDBsExist(){
        if(DBs == null) {
            TSL.get().err("RedditSubSelector.DBs was null");
            throw new RuntimeException("RedditSubSelector.DBs was null");
        }

        if(DBs.size() != StateVars.DB_SHARD_NUM){
            TSL.get().err("RedditSubSelector.DBs.size() != StateVars.DB_SHARD_NUM");
            throw new RuntimeException("RedditSubSelector.DBs.size() != StateVars.DB_SHARD_NUM");
        }
    }
}
