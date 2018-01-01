package edu.gbcg.DBSelector;

import edu.gbcg.configs.StateVars;
import edu.gbcg.utils.TSL;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public abstract class Selector {
    public abstract void testItOut(String SQLStatemetn);
    public abstract List<RSMapper> generalSelection(String SQLStatement);

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

    protected void verifyDBsExist(List<String> DBs){
        if(DBs == null){
            TSL.get().err("Selector.verifyDBsExist DBs was null");
            throw new RuntimeException("Selector.verifyDBsExist DBs was null");
        }
        if(DBs.size() != StateVars.DB_SHARD_NUM){
            TSL.get().err("Selector.verifyDBsExist DBs.size() != StateVars.DB_SHARD_NUM");
            throw new RuntimeException("Selector.verifyDBsExist DBs.size() != StateVars.DB_SHARD_NUM");
        }
    }
}
