package edu.gbcg.runner;

import com.google.common.base.Stopwatch;
import edu.gbcg.configs.columnsAndKeys.RedditComments;
import edu.gbcg.configs.columnsAndKeys.RedditSubmissions;
import edu.gbcg.dbInteraction.RSMapperOutput;
import edu.gbcg.dbInteraction.dbSelector.RSMapper;
import edu.gbcg.dbInteraction.dbSelector.reddit.comments.RedditComSelector;
import edu.gbcg.dbInteraction.dbSelector.reddit.submissions.RedditSubSelector;
import edu.gbcg.dbInteraction.dbSelector.Selector;
import edu.gbcg.configs.Finals;
import edu.gbcg.dbInteraction.dbcreator.reddit.Facilitator;
import edu.gbcg.dbInteraction.dbcreator.reddit.comments.CommentsFacilitator;
import edu.gbcg.dbInteraction.dbcreator.reddit.submissions.SubmissionsFacilitator;
import edu.gbcg.utils.Out;
import edu.gbcg.utils.TSL;

import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception{

        TSL logger = TSL.get();
        Out out = Out.get();

        Finals.START_FRESH = false;

        // Check and create them if they don't exist
        if(Finals.isWindows() && Finals.START_FRESH) {
            logger.err("isWindows() was true while trying to start fresh");
            logger.shutDownAndKill();
        }

        Stopwatch sw = Stopwatch.createStarted();

        doSubs();
        //doComs();

        sw.stop();

        logger.info("Execution took " + out.timer_millis(sw));

        // Tell the logger to close up the queue
        logger.shutDown();

    }

    public static void doSubs(){
        /*
        Facilitator fac = new SubmissionsFacilitator();
        fac.createDBs();
        fac.pushJSONDataIntoDBs();
        */

        Selector rss = new RedditSubSelector();
        //List<RSMapper> results = rss.selectAllFromAuthor("a4k04");
        //List<RSMapper> results = rss.selectAllWhereColumnEquals("subreddit_name", "apple");
        //List<RSMapper> results = rss.selectAllFromAuthorOrderBy(author, "post_title");
        //List<RSMapper> results = rss.selectAllWhereColumnEqualsAndColumnAboveValue("author", "a4k04", "score", "2");
        //List<RSMapper> results = rss.selectAllWhereColumnEquals("pid", "7a1shj");
        //List<RSMapper> results = rss.selectAllWhereColumnEqualsAndColumnAboveValue("subreddit_name", "cars",
        //        "score", "500");
        //List<RSMapper> results = rss.selectAllWhereColumnGreaterThan("gilded", "50");
        //List<RSMapper> results = rss.selectAllWhereColumnLessThan("created_dt", "2007-11-10 22:22:22");

        //List<RSMapper> results = rss.selectAllAfterDate(2017, 11, 30, 23, 59, 58);
        //List<RSMapper> results = rss.selectAllBeforeDate(2017, 11, 01, 00, 00, 1);
        List<RSMapper> results = rss.selectAllBetweenDates(2017, 11, 30, 23, 59, 55, 2017, 12, 1, 0, 0, 0);

        RSMapperOutput.printAllColumnsFromRSMappers(results, RedditSubmissions.columnsForPrinting());
        //RSMapperOutput.RSMappersToCSV(results, RedditSubmissions.columnsForPrinting(), "output.csv");
    }

    public static void doComs(){
        /*
        Facilitator fac = new CommentsFacilitator();
        fac.createDBs();
        fac.pushJSONDataIntoDBs();
        */

        Selector rcs = new RedditComSelector();
        String author = "a4k04";
        //String author = "----root";
        //String select_aut = "select * from "+ Finals.COM_TABLE_NAME+" where author = "+"'"+author+"' and score < -5;";
        //String select_all = "select * from "+ Finals.COM_TABLE_NAME+" where score > 100;";
        //String sub_search = "select * from "+ Finals.COM_TABLE_NAME+" where subreddit_name = 'The_Donald' and score > 1500 limit 10;";
        //String cont_search = "select * from "+ Finals.COM_TABLE_NAME+" where subreddit_name = 'The_Donald' and controversial_score > 0 limit 5;";
        //List<RSMapper> results = rcs.selectAllFromAuthor(author);
        //List<RSMapper> results = rcs.selectAllWhereColumnEqualsAndColumnAboveValue("author", author, "score", "0");
        List<RSMapper> results = rcs.selectAllAfterDate(2017, 11, 30, 23, 59, 00);
        //RSMapperOutput.printAllColumnsFromRSMappers(results, RedditComments.columnsForPrinting());
    }
}
