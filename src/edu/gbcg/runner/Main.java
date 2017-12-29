package edu.gbcg.runner;

import edu.gbcg.DBSelector.RedditSubSelector;
import edu.gbcg.configs.StateVars;
import edu.gbcg.dbcreator.RedditSubmissions;
import edu.gbcg.utils.TSL;
import edu.gbcg.utils.c;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public class Main {
    public static void main(String[] args) throws Exception{
        // Final test commit from machine
        TSL.get().log("Program starting");

        // Log only the errors
        TSL.LOG_NON_ERRORS = false;

        // Check and create them if they don't exist
        RedditSubmissions.createDBs();

        long start = System.currentTimeMillis();

        // Read the json files into the DBs
        RedditSubmissions.pushJSONDataIntoDBs();

        String author = "a4k04";
        //RedditSubSelector.testItOut("select * from "+ StateVars.SUB_TABLE_NAME+" where author = '"+author+"';");

        long end = System.currentTimeMillis();

        NumberFormat formatter = new DecimalFormat("#0.00000");
        c.writeln_err("Execution took " + formatter.format((end - start) / 1000d) + " seconds");
        TSL.get().err("We're Done");

        // Tell the logger to close up the queue
        TSL.get().shutDown();

    }
}
