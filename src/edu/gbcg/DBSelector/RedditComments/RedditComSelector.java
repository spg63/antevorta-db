package edu.gbcg.DBSelector.RedditComments;

import edu.gbcg.DBSelector.RSMapper;
import edu.gbcg.DBSelector.SelectionWorker;
import edu.gbcg.DBSelector.Selector;
import edu.gbcg.configs.DBLocator;
import edu.gbcg.utils.c;

import java.util.ArrayList;
import java.util.List;

public class RedditComSelector extends Selector {

    public void testItOut(String SQLStatement){
        List<RSMapper> res = generalSelection(SQLStatement);
        if(res == null){
            c.writeln("**----- NO RESULTS -----**");
            return;
        }

        String author = "author";
        String score = "score";
        String sub_name = "subreddit_name";
        String body = "body";
        String c_score = "controversial_score";
        for(int i = 0; i < res.size(); ++i){
            c.writeln(author + ": " + res.get(i).getString(author));
            c.writeln(score + ": " + res.get(i).getString(score));
            c.writeln(sub_name + ": " + res.get(i).getString(sub_name));
            c.writeln(body + ": " + res.get(i).getString(body));
            c.writeln(c_score + ": " + res.get(i).getString(c_score));
            c.writeln("");
            c.writeln("----------");
            c.writeln("");
        }
    }

    /**
     * Execute a SQL query across all DB shards in parallel
     * @param SQLStatement
     * @return A list of RSMapper objects containing any results
     */
    public List<RSMapper> generalSelection(String SQLStatement){
        List<String> DBs = DBLocator.redditComsAbsolutePaths();
        verifyDBsExist(DBs);

        List<SelectionWorker> workers = new ArrayList<>();
        for(int i = 0; i < DBs.size(); ++i)
            workers.add(new SelectionWorker(DBs.get(i), SQLStatement, new CommentSetMapper()));
        return genericSelect(workers, SQLStatement);
    }
}
