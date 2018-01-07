package edu.gbcg.dbInteraction.dbcreator.reddit.comments;

import edu.gbcg.configs.DBLocator;
import edu.gbcg.configs.Finals;
import edu.gbcg.configs.RawDataLocator;
import edu.gbcg.configs.columnsAndKeys.RedditComments;
import edu.gbcg.dbInteraction.dbcreator.reddit.Facilitator;
import edu.gbcg.dbInteraction.dbcreator.reddit.JsonPusher;

import java.util.ArrayList;
import java.util.List;

public class CommentsFacilitator extends Facilitator{
    public CommentsFacilitator(){ super(); }

    protected List<String> buildDBPaths(){
        return DBLocator.buildComDBPaths();
    }

    protected List<String> getJsonAbsolutePaths(){
        return RawDataLocator.redditJsonCommentAbsolutePaths();
    }

    protected List<String> getDBAbsolutePaths(){
        return DBLocator.redditComsAbsolutePaths();
    }

    protected List<String> getDBDirectoryPaths(){
        return DBLocator.getComDBPath();
    }

    protected List<String> getJsonKeysOfInterest(){
        return RedditComments.JSONkeys();
    }

    protected List<String> getColumnNames(){
        return RedditComments.columnNames();
    }

    protected List<String> getDataTypes(){
        return RedditComments.dataTypes();
    }

    protected List<JsonPusher> populateJsonWorkers(){
        List<JsonPusher> workers = new ArrayList<>();
        for(int i = 0; i < Finals.DB_SHARD_NUM; ++i)
            workers.add(new CommentsJsonPusher());
        return workers;
    }

    protected String getTableName(){
        return Finals.COM_TABLE_NAME;
    }

    protected void createIndices(){
        createDBIndex(Finals.AUTHOR, "attrs_author");
        createDBIndex("controversial_score", "attrs_cont_score");
        createDBIndex(Finals.CREATED_DT, "attrs_created");
        createDBIndex("gilded", "attrs_gilded");
        createDBIndex(Finals.POST_ID, "attrs_pid");
        createDBIndex("link_id", "attrs_linkid");
        createDBIndex("parent_id", "attrs_parentid");
        createDBIndex(Finals.SCORE, "attrs_score");
        createDBIndex("subreddit_name", "attrs_sub_name");
        createDBIndex("subreddit_id", "attrs_sub_id");
    }
}
