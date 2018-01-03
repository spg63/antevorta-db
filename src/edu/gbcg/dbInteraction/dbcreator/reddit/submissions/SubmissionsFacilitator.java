package edu.gbcg.dbInteraction.dbcreator.reddit.submissions;

import edu.gbcg.configs.DBLocator;
import edu.gbcg.configs.RawDataLocator;
import edu.gbcg.configs.StateVars;
import edu.gbcg.configs.columnsAndKeys.RedditSubmissions;
import edu.gbcg.dbInteraction.dbcreator.reddit.Facilitator;
import edu.gbcg.dbInteraction.dbcreator.reddit.JsonPusher;

import java.util.*;

/**
 * Class to interact with the reddit submission data. This class will allow for all required
 * functionality from DB creation, inserting the data, updating the data and selecting the data.
 * This class will automatically determine the number of DB shards and their storage location for
 * the user. The sharding will allow for parallel reads and writes to multiple HDDs / compute nodes.
 */
public class SubmissionsFacilitator extends Facilitator {
    public SubmissionsFacilitator(){ super(); }

    protected List<String> buildDBPaths(){
        return DBLocator.buildSubDBPaths();
    }

    protected List<String> getJsonAbsolutePaths(){
        return RawDataLocator.redditJsonSubmissionAbsolutePaths();
    }

    protected List<String> getDBAbsolutePaths(){
        return DBLocator.redditSubsAbsolutePaths();
    }

    protected List<String> getDBDirectoryPaths(){
        return DBLocator.getSubDBPath();
    }

    protected List<String> getJsonKeysOfInterest(){
        return RedditSubmissions.JSONKeys();
    }

    protected List<String> getColumnNames(){
        return RedditSubmissions.columnNames();
    }

    protected List<String> getDataTypes(){
        return RedditSubmissions.dataTypes();
    }

    protected List<JsonPusher> populateJsonWorkers(){
        List<JsonPusher> workers = new ArrayList<>();
        for(int i = 0; i < StateVars.DB_SHARD_NUM; ++i)
            workers.add(new SubmissionJsonPusher());
        return workers;
    }

    protected String getTableName(){
        return StateVars.SUB_TABLE_NAME;
    }

    protected void createIndices(){
        //***** all but author
        createDBIndex("author", "attrs_author");
        createDBIndex("created_dt", "attrs_created");
        createDBIndex("host_domain", "attrs_host");
        createDBIndex("gilded", "attrs_gilded");
        createDBIndex("pid", "attrs_pid");                          //
        createDBIndex("num_comments", "attrs_comments");
        createDBIndex("media_author_name", "attrs_med_author");     //
        createDBIndex("media_provider_name", "attrs_med_provider");
        createDBIndex("media_title", "attrs_media");
        createDBIndex("score", "attrs_score");
        createDBIndex("subreddit_name", "attrs_sub_name");
        createDBIndex("subreddit_id", "attrs_sub_id");
    }
}
