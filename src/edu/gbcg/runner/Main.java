package edu.gbcg.runner;

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
        Facilitator pusher = new SubmissionsFacilitator();
        pusher.createDBs();
        pusher.pushJSONDataIntoDBs();

        //String author = "----root";
        String author = "keen75";
        String select_aut = "select * from "+StateVars.SUB_TABLE_NAME+" where author = "+"'"+author+"';";
        String select_all = "select * from "+StateVars.SUB_TABLE_NAME+" where score = 2500;";
        Selector rss = new RedditSubSelector();
        List<RSMapper> results = rss.selectAllFromAuthor("keen75");
        for(RSMapper res : results){
            String title = "post_title";
            String sub_name = "subreddit_name";
            String score = "score";
            String ID = "ID";
            String author_sel = "author";
            String link = "permalink";
            c.writeln(author_sel + ": " + res.getString(author_sel));
            c.writeln(title + ": " + res.getString(title));
            c.writeln(sub_name + ": " + res.getString(sub_name));
            c.writeln(score + ": " + res.getInt(score));
            c.writeln(ID + ": " + res.getString(ID));
            c.writeln(link + ": " + res.getString(link));
            c.writeln("");
            c.writeln("----------");
            c.writeln("");
        }

        //Selector selector = new RedditSubSelector();
        //selector.testItOut(select_aut);


    }

    public static void doComs(){
        Facilitator pusher = new CommentsFacilitator();
        pusher.createDBs();
        pusher.pushJSONDataIntoDBs();

        //String author = "a4k04";
        String author = "----root";
        String select_aut = "select * from "+StateVars.COM_TABLE_NAME+" where author = "+"'"+author+"' and score < -5;";
        String select_all = "select * from "+StateVars.COM_TABLE_NAME+" where score > 100;";
        String sub_search = "select * from "+StateVars.COM_TABLE_NAME+" where subreddit_name = 'The_Donald' and score > 1500 limit 10;";
        String cont_search = "select * from "+StateVars.COM_TABLE_NAME+" where subreddit_name = 'The_Donald' and controversial_score > 0 limit 5;";
        Selector selector = new RedditComSelector();
        selector.testItOut(select_all);
    }
}


























