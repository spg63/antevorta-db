package edu.gbcg.dbInteraction.dbSelector;

import edu.gbcg.configs.StateVars;
import edu.gbcg.utils.TSL;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * This class implements all shared functionality for DB selections. Based on the type of SelectionWorker passed to
 * the genericSelect function it will query the proper DB files and use the proper RSMapper object
 */
public abstract class Selector {
    protected String tableName;
    public abstract void testItOut(String SQLStatemetn);
    public abstract List<RSMapper> generalSelection(String SQLStatement);

    public Selector(){}

    public List<RSMapper> selectAllFromAuthor(String author){
        DBSelector selector = new DBSelector()
                .from(this.tableName)
                .where("author = '"+author+"'");
        return generalSelection(selector.sql());
    }
























    /*
        Perform a multi-threaded selection against the DB shards. Each shard is given a single thread
     */
    protected List<RSMapper> genericSelect(List<SelectionWorker> workers, String SQLStatement){
        List<Future<List<RSMapper>>> future_results = new ArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(StateVars.DB_SHARD_NUM);
        for(SelectionWorker worker : workers)
            future_results.add(executor.submit(worker));

        List<RSMapper> results = new ArrayList<>();
        try{
            for(int i = 0; i < StateVars.DB_SHARD_NUM; ++i)
                results.addAll(future_results.get(i).get());
        }
        catch(InterruptedException e){
            TSL.get().err("Selector.genericSelect InterrupredException");
        }
        catch(ExecutionException ee){
            TSL.get().err("Selector.genericSelect ExecutionException");
        }

        executor.shutdown();

        if(results.isEmpty())
            return null;
        return results;
    }
    /*
        Kill the program if the DBs don't exist or a shard is missing
     */
    protected void verifyDBsExist(List<String> DBs) {
        if (DBs == null) {
            TSL.get().err("Selector.verifyDBsExist DBs was null");
            throw new RuntimeException("Selector.verifyDBsExist DBs was null");
        }
        if (DBs.size() != StateVars.DB_SHARD_NUM) {
            TSL.get().err("Selector.verifyDBsExist DBs.size() != StateVars.DB_SHARD_NUM");
            throw new RuntimeException("Selector.verifyDBsExist DBs.size() != StateVars.DB_SHARD_NUM");
        }
    }
}
