package edu.gbcg.runner;

import edu.gbcg.DBSelector.RedditSubSelector;
import edu.gbcg.configs.DBLocator;
import edu.gbcg.configs.RawDataLocator;
import edu.gbcg.configs.StateVars;
import edu.gbcg.dbcreator.RedditComments;
import edu.gbcg.dbcreator.RedditSubmissions;
import edu.gbcg.utils.FileUtils;
import edu.gbcg.utils.TSL;
import edu.gbcg.utils.TimeFormatter;
import edu.gbcg.utils.c;
import org.json.JSONObject;

import java.io.File;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception{
        // Final test commit from machine
        TSL.get().log("Program starting");

        //String path = "A:/Data/Uncompressed/Reddit/Submissions/testing/";

        //List<String> files = FileUtils.get().getAllFilePathsInDir(path);

        //for(String file : files)
        //    c.writeln("file: " + file);


        // Log only the errors
        TSL.LOG_NON_ERRORS = false;

        // Check and create them if they don't exist
        RedditSubmissions.createDBs();

        long start = System.currentTimeMillis();

        // Read the json files into the DBs
        RedditSubmissions.pushJSONDataIntoDBs();
        String author = "----root";
        //RedditSubSelector.testItOut("select * from submission_attrs where author =
        // '"+author+"';");

        long end = System.currentTimeMillis();

        NumberFormat formatter = new DecimalFormat("#0.00000");
        c.writeln_err("Execution took " + formatter.format((end - start) / 1000d) + " seconds");


        // Tell the logger to close up the queue
        TSL.get().shutDown();

    }
}
