package edu.gbcg.dbcreator;

import edu.gbcg.configs.DBLocator;
import edu.gbcg.utils.DBUtils;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;

public class RedditSubmissions {
    private static final String db = DBLocator.redditSubmissionsDBAbsolutePath();
    private static final String TABLE_NAME = "submission_attrs";


    public static void createDB(){
        Connection conn = DBUtils.get().connect(db);


    }

    private static ArrayList<String> keysOfInterest = new ArrayList<>(Arrays.asList(
             "archived"
            ,"author"
            ,"brand_safe"
            ,"contest_mode"
            ,"created_utc"
            ,"distinguished"
            ,"domain"
            ,"edited"
            ,"gilded"
            ,"hidden"
            ,"id"
            ,"is_crosspostable"
            ,"hide_score"
            ,"is_reddit_media_domain"
            ,"is_self"
            ,"is_video"
            ,"link_flair_text"
            ,"locked"
            ,"num_comments"
            // Note: Here we have an inner object
            ,"media"
            // The inner object
            ,"oembed"
            // The stuff we care about is inside the inner object
            ,"author_name"
            ,"provider_name"
            ,"title"
            ,"type"
            // END INNER
            ,"num_crossposts"
            ,"over_18"
            ,"permalink"
            ,"pinned"
            ,"retrieved_on"
            ,"score"
            ,"selftext"
            ,"stickied"
            ,"subreddit"
            ,"subreddit_id"
            ,"subreddit_type"
            ,"title"
            ,"url"
    ));

    public static void readJsonIntoDB(){

    }

}
