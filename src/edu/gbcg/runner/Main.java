package edu.gbcg.runner;

import edu.gbcg.configs.columnsAndKeys.RedditComments;
import edu.gbcg.configs.columnsAndKeys.RedditSubmissions;
import edu.gbcg.dbInteraction.dbSelector.RSMapper;
import edu.gbcg.dbInteraction.dbSelector.reddit.comments.RedditComSelector;
import edu.gbcg.dbInteraction.dbSelector.reddit.submissions.RedditSubSelector;
import edu.gbcg.dbInteraction.dbSelector.Selector;
import edu.gbcg.configs.StateVars;
import edu.gbcg.dbInteraction.dbcreator.reddit.Facilitator;
import edu.gbcg.dbInteraction.dbcreator.reddit.comments.CommentsFacilitator;
import edu.gbcg.dbInteraction.dbcreator.reddit.submissions.SubmissionsFacilitator;
import edu.gbcg.utils.TSL;
import edu.gbcg.utils.c;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception{
        // Final test commit from machine
        TSL.get().log("Program starting");

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
    /*
        Facilitator pusher = new SubmissionsFacilitator();
        pusher.createDBs();
        pusher.pushJSONDataIntoDBs();
    */

        //String author = "keen75";
        String select_all = "select * from "+StateVars.SUB_TABLE_NAME+" where score = 2500;";

        Selector rss = new RedditSubSelector();
        //List<RSMapper> results = rss.selectAllFromAuthor(author);
        //List<RSMapper> results = rss.selectAllWhereColumnEquals("subreddit_name", "apple");
        //List<RSMapper> results = rss.selectAllFromAuthorOrderBy(author, "post_title");
        //List<RSMapper> results = rss.selectAllWhereColumnEqualsAndColumnAboveValue("author", "a4k04", "score", "2");
        List<RSMapper> results = rss.selectAllWhereColumnEquals("pid", "7a1shj");
        printInfo(results, false);

    }

    public static void doComs(){
    /*
        Facilitator pusher = new CommentsFacilitator();
        pusher.createDBs();
        pusher.pushJSONDataIntoDBs();
    */

        String author = "a4k04";
        //String author = "----root";
        String select_aut = "select * from "+StateVars.COM_TABLE_NAME+" where author = "+"'"+author+"' and score < -5;";
        String select_all = "select * from "+StateVars.COM_TABLE_NAME+" where score > 100;";
        String sub_search = "select * from "+StateVars.COM_TABLE_NAME+" where subreddit_name = 'The_Donald' and score > 1500 limit 10;";
        String cont_search = "select * from "+StateVars.COM_TABLE_NAME+" where subreddit_name = 'The_Donald' and controversial_score > 0 limit 5;";
        Selector rcs = new RedditComSelector();
        //List<RSMapper> results = rcs.selectAllFromAuthor(author);
        List<RSMapper> results = rcs.selectAllWhereColumnEquals("link_id", "t3_7a1shj");
        printInfo(results, true);
    }

    public static void printInfo(List<RSMapper> results, boolean is_comms){
        if(results == null) {
            c.writeln("**----- NO RESULTS -----**");
            return;
        }

        List<String> cols = is_comms ? RedditComments.columnNames() : RedditSubmissions.columnNames();

        for(RSMapper res : results){
            for(String col : cols){
                c.writeln(col + ": " + res.getString(col));
            }
            c.writeln("");
            c.writeln("----------");
            c.writeln("");
        }

        c.writeln_err("Returned " + results.size() + " results.");
    }

    public static void outputToCSV(List<RSMapper> results, boolean is_comms, String csvFilePath){
        if(results == null)
            return;
        List<String> cols = is_comms ? RedditComments.columnNames() : RedditSubmissions.columnNames();
        StringBuilder csv = new StringBuilder();
        // Build the header
        for(int i = 0; i < cols.size() - 1; ++i){
            csv.append(cols.get(i));
            csv.append(",");
        }
        csv.append(cols.get(cols.size() - 1));
        csv.append("\n");

        for(RSMapper res : results){
            for(int i = 0; i < cols.size() - 1; ++i){
                csv.append(res.getString(cols.get(i)));
                csv.append(",");
            }
            csv.append(res.getString(cols.get(cols.size() - 1)));
            csv.append("\n");
        }

        // Get the csv string and write it to the file...

    }
}


























