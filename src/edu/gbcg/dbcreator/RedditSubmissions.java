package edu.gbcg.dbcreator;

import edu.gbcg.configs.DBLocator;
import edu.gbcg.configs.StateVars;
import edu.gbcg.utils.DBUtils;

import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RedditSubmissions {
    private static final String DB = DBLocator.redditSubmissionsDBAbsolutePath();
    private static final String TABLE_NAME = "submission_attrs";


    public static void createDB(){
        // Check if the db exists
        File db_file = new File(DB);
        boolean db_exists = false;
        if(db_file.exists() && db_file.isFile())
            db_exists = true;

        if(StateVars.START_FRESH)



        //Connection conn = DBUtils.get().connect(DB);


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

    public static List<String> getColumnsForDB(){
        ArrayList<String> columns = new ArrayList<>();


        return columns;
    }

    public static List<String> getColumnDataTypesForDB(){
        List<String> data_types = new ArrayList<>(Arrays.asList(
                ""
                ,"this"

        ));
        return data_types;
    }

    public static void readJsonIntoDB(){

    }




}
