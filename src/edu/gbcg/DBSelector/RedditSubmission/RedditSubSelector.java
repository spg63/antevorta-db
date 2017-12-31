package edu.gbcg.DBSelector.RedditSubmission;

import edu.gbcg.DBSelector.RSMapper;
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

public class RedditSubSelector {
    private static List<String> DBs = DBLocator.redditSubsAbsolutePaths();

    public static void testItOut(String SQLStatement){
        if(DBs == null || DBs.isEmpty())
            DBs = DBLocator.redditSubsAbsolutePaths();

        List<RSMapper> res = generalSelection(DBs, SQLStatement);
        if(res == null) {
            c.writeln("**----- NO RESULTS -----**");
            return;
        }

        String score = "score";
        String sub_name = "subreddit_name";
        String title = "post_title";
        String author = "author";
        for(int i = 0; i < res.size(); ++i){
            c.writeln(score + ": " + res.get(i).getString(score));
            c.writeln(sub_name + ": " + res.get(i).getString(sub_name));
            c.writeln(title + ": " + res.get(i).getString(title));
            c.writeln(author + ": " + res.get(i).getString(author));
            c.writeln("");
            c.writeln("----------");
            c.writeln("");
            //c.writeln("sub_name: " + res.get(i).getString("subreddit_name"));
        }
    }

    /**
     * Return a list of RSMapper objects from the DB query
     * @param dbs
     * @param SQLStatement
     * @return The list of RSMappers, null if query returned 0 results
     */
    public static List<RSMapper> generalSelection(List<String> dbs, String SQLStatement){
        verifyDBsExist();
        return genericSelect(dbs, SQLStatement);
    }

    private static List<RSMapper> genericSelect(List<String> dbs, String SQLStatement){
        // List of futures for the threads to return to
        List<Future<List<RSMapper>>> future_results = new ArrayList<>();

        // ThreadPool to handle the DB operations
        ExecutorService executor = Executors.newFixedThreadPool(StateVars.DB_SHARD_NUM);

        for(int i = 0; i < StateVars.DB_SHARD_NUM; ++i){
            RedditSubSelectorWorker worker = new RedditSubSelectorWorker(dbs.get(i), SQLStatement);
            Future<List<RSMapper>> future = executor.submit(worker);
            future_results.add(future);
        }

        // Copy the ResultSets out of the futures. Also handles waiting on the Thread to finish
        List<RSMapper> results = new ArrayList<>();
        try {
            for (int i = 0; i < StateVars.DB_SHARD_NUM; ++i)
                results.addAll(future_results.get(i).get());
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
