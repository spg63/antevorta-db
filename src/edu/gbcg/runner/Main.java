package edu.gbcg.runner;

import edu.gbcg.configs.columnsAndKeys.RedditComments;
import edu.gbcg.configs.columnsAndKeys.RedditSubmissions;
import edu.gbcg.dbInteraction.RSMapperOutput;
import edu.gbcg.dbInteraction.dbSelector.RSMapper;
import edu.gbcg.dbInteraction.dbSelector.reddit.comments.RedditComSelector;
import edu.gbcg.dbInteraction.dbSelector.reddit.submissions.RedditSubSelector;
import edu.gbcg.dbInteraction.dbSelector.Selector;
import edu.gbcg.configs.StateVars;
import edu.gbcg.utils.TSL;
import edu.gbcg.utils.c;

import java.text.DecimalFormat;
import java.text.NumberFormat;
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

        doSubs();
        //doComs();

        long end = System.currentTimeMillis();

        NumberFormat formatter = new DecimalFormat("#0.00000");
        c.writeln("Execution took " + formatter.format((end - start) / 1000d) + " seconds");
        TSL.get().err("We're Done");

        // Tell the logger to close up the queue
        TSL.get().shutDown();

    }

    public static void doSubs(){
        Selector rss = new RedditSubSelector();
        //List<RSMapper> results = rss.selectAllFromAuthor(author);
        //List<RSMapper> results = rss.selectAllWhereColumnEquals("subreddit_name", "apple");
        //List<RSMapper> results = rss.selectAllFromAuthorOrderBy(author, "post_title");
        //List<RSMapper> results = rss.selectAllWhereColumnEqualsAndColumnAboveValue("author", "a4k04", "score", "2");
        //List<RSMapper> results = rss.selectAllWhereColumnEquals("pid", "7a1shj");
        //List<RSMapper> results = rss.selectAllWhereColumnEqualsAndColumnAboveValue("subreddit_name", "cars",
        //        "score", "500");
        //List<RSMapper> results = rss.selectAllWhereColumnGreaterThan("gilded", "50");
        List<RSMapper> results = rss.selectAllWhereColumnLessThan("created_dt", "2007-11-10 22:22:22");
        RSMapperOutput.printAllColumnsFromRSMappers(results, RedditSubmissions.columnsForPrinting());
        //RSMapperOutput.RSMappersToCSV(results, RedditSubmissions.columnsForPrinting(), "output.csv");
    }

    public static void doComs(){
        String author = "a4k04";
        //String author = "----root";
        String select_aut = "select * from "+StateVars.COM_TABLE_NAME+" where author = "+"'"+author+"' and score < -5;";
        String select_all = "select * from "+StateVars.COM_TABLE_NAME+" where score > 100;";
        String sub_search = "select * from "+StateVars.COM_TABLE_NAME+" where subreddit_name = 'The_Donald' and score > 1500 limit 10;";
        String cont_search = "select * from "+StateVars.COM_TABLE_NAME+" where subreddit_name = 'The_Donald' and controversial_score > 0 limit 5;";
        Selector rcs = new RedditComSelector();
        List<RSMapper> results = rcs.selectAllFromAuthor(author);
        RSMapperOutput.printAllColumnsFromRSMappers(results, RedditComments.columnsForPrinting());
    }
}
