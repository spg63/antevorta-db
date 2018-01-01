package edu.gbcg.DBSelector.RedditSubmission;

import edu.gbcg.DBSelector.RSMapper;
import edu.gbcg.DBSelector.SelectionWorker;
import edu.gbcg.DBSelector.Selector;
import edu.gbcg.configs.DBLocator;
import edu.gbcg.configs.StateVars;
import edu.gbcg.dbcreator.DBCommon;
import edu.gbcg.utils.TSL;
import edu.gbcg.utils.c;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class RedditSubSelector extends Selector {

    public void testItOut(String SQLStatement){
        List<RSMapper> res = generalSelection(SQLStatement);
        if(res == null) {
            c.writeln("**----- NO RESULTS -----**");
            return;
        }

        String author = "author";
        String title = "post_title";
        String sub_name = "subreddit_name";
        String score = "score";
        String ID = "ID";
        for(int i = 0; i < res.size(); ++i){
            c.writeln(ID + ": " + res.get(i).getString(ID));
            c.writeln(author + ": " + res.get(i).getString(author));
            c.writeln(title + ": " + res.get(i).getString(title));
            c.writeln(sub_name + ": " + res.get(i).getString(sub_name));
            c.writeln(score + ": " + res.get(i).getString(score));
            c.writeln("");
            c.writeln("----------");
            c.writeln("");
            //c.writeln("sub_name: " + res.get(i).getString("subreddit_name"));
        }
    }

    /**
     * Return a list of RSMapper objects from the DB query
     * @param dbs
     * @param SQLStatement
     * @return The list of RSMappers, null if query returned 0 results
     */
    public List<RSMapper> generalSelection(String SQLStatement){
        List<String> DBs = DBLocator.redditSubsAbsolutePaths();
        verifyDBsExist(DBs);

        List<SelectionWorker> workers = new ArrayList<>();
        for(int i = 0; i < DBs.size(); ++i)
            workers.add(new RedditSubSelectorWorker(DBs.get(i), SQLStatement));
        return genericSelect(workers, SQLStatement);
    }
}
