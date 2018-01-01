package edu.gbcg.runner;

import edu.gbcg.DBSelector.RedditComments.RedditComSelector;
import edu.gbcg.DBSelector.RedditSubmission.RedditSubSelector;
import edu.gbcg.DBSelector.RedditSubmission.SubmissionSetMapper;
import edu.gbcg.configs.StateVars;
import edu.gbcg.dbcreator.Reddit.Comments;
import edu.gbcg.dbcreator.Reddit.Submissions;
import edu.gbcg.utils.TSL;
import edu.gbcg.utils.c;
import javafx.application.Preloader;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public class Main {
    public static void main(String[] args) throws Exception{
        // Final test commit from machine
        TSL.get().log("Program starting");

        //StateVars.START_FRESH = true;

        // Log only the errors
        TSL.LOG_NON_ERRORS = false;

        // Check and create them if they don't exist
        if(StateVars.isWindows() && StateVars.START_FRESH)
            System.exit(0);

        long start = System.currentTimeMillis();

        //doSubs();
        doComs();

        long end = System.currentTimeMillis();

        NumberFormat formatter = new DecimalFormat("#0.00000");
        c.writeln("Execution took " + formatter.format((end - start) / 1000d) + " seconds");
        TSL.get().err("We're Done");

        // Tell the logger to close up the queue
        TSL.get().shutDown();

    }

    public static void doSubs(){
        Submissions.createDBs();
        Submissions.pushJSONDataIntoDBs();

        //String author = "----root";
        String author = "keen75";
        String select_aut = "select * from "+StateVars.SUB_TABLE_NAME+" where author = "+"'"+author+"';";
        String select_all = "select * from "+StateVars.SUB_TABLE_NAME+" where score = 2500;";
        RedditSubSelector.testItOut(select_aut);
    }

    public static void doComs(){
        Comments.createDBs();
        Comments.pushJSSONDataIntoDBs();

        String author = "adnam";
        String select_aut = "select * from "+StateVars.COM_TABLE_NAME+" where author = "+"'"+author+"';";
        String select_all = "select * from "+StateVars.COM_TABLE_NAME+" where score > 100;";
        RedditComSelector.testItOut(select_aut);
    }
}
