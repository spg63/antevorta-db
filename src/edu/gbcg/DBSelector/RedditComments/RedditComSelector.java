package edu.gbcg.DBSelector.RedditComments;

import edu.gbcg.DBSelector.RSMapper;
import edu.gbcg.configs.DBLocator;
import edu.gbcg.configs.StateVars;
import edu.gbcg.utils.TSL;
import edu.gbcg.utils.c;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class RedditComSelector {
    private static List<String> DBs = DBLocator.redditComsAbsolutePaths();

    public static void testItOut(String SQLStatement){
        if(DBs == null || DBs.isEmpty())
            DBs = DBLocator.redditComsAbsolutePaths();

        List<RSMapper> res = generalSelection(DBs, SQLStatement);
        if(res == null){
            c.writeln("**----- NO RESULTS -----**");
            return;
        }

        String author = "author";
        String score = "score";
        String sub_name = "subreddit_name";
        String body = "body";
        String c_score = "controversial_score";
        for(int i = 0; i < res.size(); ++i){
            c.writeln(author + ": " + res.get(i).getString(author));
            c.writeln(score + ": " + res.get(i).getString(score));
            c.writeln(sub_name + ": " + res.get(i).getString(sub_name));
            c.writeln(body + ": " + res.get(i).getString(body));
            c.writeln(c_score + ": " + res.get(i).getString(c_score));
            c.writeln("");
            c.writeln("----------");
            c.writeln("");
        }
    }

    public static List<RSMapper> generalSelection(List<String> dbs, String SQLStatement){
        verifyDBsExist();
        return genericSelect(dbs, SQLStatement);
    }

    private static List<RSMapper> genericSelect(List<String> dbs, String SQLStatement){
        List<Future<List<RSMapper>>> future_results = new ArrayList<>();

        ExecutorService executor = Executors.newFixedThreadPool(StateVars.DB_SHARD_NUM);

        for(int i = 0; i < StateVars.DB_SHARD_NUM; ++i){
            RedditComSelectorWorker worker = new RedditComSelectorWorker(dbs.get(i), SQLStatement);
            Future<List<RSMapper>> future = executor.submit(worker);
            future_results.add(future);
        }

        List<RSMapper> results = new ArrayList<>();
        try{
            for(int i = 0; i < StateVars.DB_SHARD_NUM; ++i)
                results.addAll(future_results.get(i).get());
        }
        catch(InterruptedException e){
            TSL.get().err("RedditComSelector.genericSelect InterrupredException");
        }
        catch(ExecutionException ee){
            TSL.get().err("RedditComSelector.genericSelect ExecutionException");
        }

        executor.shutdown();

        if(results.isEmpty())
            return null;
        return results;
    }

    private static void verifyDBsExist(){
        if(DBs == null){
            TSL.get().err("RedditComSelector.DBs was null");
            throw new RuntimeException("RedditComSelector.DBs was null");
        }
        if(DBs.size() != StateVars.DB_SHARD_NUM){
            TSL.get().err("RedditComSelector.DBs.size() != StateVars.DB_SHARD_NUM");
            throw new RuntimeException("RedditComSelector.DBs.size() != StateVars.DB_SHARD_NUM");
        }
    }

}
