package edu.gbcg.dbInteraction.dbSelector.reddit.submissions;

import edu.gbcg.dbInteraction.dbSelector.RSMapper;
import edu.gbcg.dbInteraction.dbSelector.SelectionWorker;
import edu.gbcg.dbInteraction.dbSelector.Selector;
import edu.gbcg.configs.DBLocator;
import edu.gbcg.utils.c;

import java.util.ArrayList;
import java.util.List;

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
        String over_18 = "over_18";
        for(int i = 0; i < res.size(); ++i){
            c.writeln(ID + ": " + res.get(i).getString(ID));
            c.writeln(author + ": " + res.get(i).getString(author));
            c.writeln(title + ": " + res.get(i).getString(title));
            c.writeln(sub_name + ": " + res.get(i).getString(sub_name));
            c.writeln(score + ": " + res.get(i).getString(score));
            c.writeln(over_18 + ": " + res.get(i).getBoolean(over_18));
            c.writeln("");
            c.writeln("----------");
            c.writeln("");
            //c.writeln("sub_name: " + res.get(i).getString("subreddit_name"));
        }
    }

    /**
     * Return a list of RSMapper objects from the DB query
     * @param SQLStatement
     * @return The list of RSMappers, null if query returned 0 results
     */
    public List<RSMapper> generalSelection(String SQLStatement){
        List<String> DBs = DBLocator.redditSubsAbsolutePaths();
        verifyDBsExist(DBs);

        // I can give this the type of RSMapper in the c'tor and get rid of RedditSubSelectorWorker and implement it
        // all in SelectionWorker!!!!!
        List<SelectionWorker> workers = new ArrayList<>();
        for(int i = 0; i < DBs.size(); ++i)
            workers.add(new SelectionWorker(DBs.get(i), SQLStatement, new SubmissionSetMapper()));
        return genericSelect(workers, SQLStatement);
    }
}
