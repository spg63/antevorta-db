package edu.gbcg.runner;

import edu.gbcg.DBSelector.RedditSubmission.RedditSubSelector;
import edu.gbcg.configs.StateVars;
import edu.gbcg.dbcreator.RedditSubmissions;
import edu.gbcg.utils.TSL;
import edu.gbcg.utils.c;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception{
        // Final test commit from machine
        TSL.get().log("Program starting");

        StateVars.START_FRESH = false;

        // Log only the errors
        TSL.LOG_NON_ERRORS = false;

        // Check and create them if they don't exist
        if(StateVars.isWindows() && StateVars.START_FRESH)
            System.exit(0);
        RedditSubmissions.createDBs();

        long start = System.currentTimeMillis();

        // Read the json files into the DBs
        //RedditSubmissions.pushJSONDataIntoDBs();

        String author = "a4k04";
        String select_aut = "select selftext from "+StateVars.SUB_TABLE_NAME+" where author = " + "'"+author+"';";
        String select_all = "select * from "+StateVars.SUB_TABLE_NAME+" where score = 5;";
        RedditSubSelector.testItOut(select_aut);

        long end = System.currentTimeMillis();

        NumberFormat formatter = new DecimalFormat("#0.00000");
        c.writeln_err("Execution took " + formatter.format((end - start) / 1000d) + " seconds");
        TSL.get().err("We're Done");

        // Tell the logger to close up the queue
        TSL.get().shutDown();

    }
}
