package edu.gbcg.runner;

import edu.gbcg.configs.DBLocator;
import edu.gbcg.configs.RawDataLocator;
import edu.gbcg.configs.StateVars;
import edu.gbcg.dbcreator.RedditComments;
import edu.gbcg.dbcreator.RedditSubmissions;
import edu.gbcg.utils.FileUtils;
import edu.gbcg.utils.TSL;
import edu.gbcg.utils.TimeFormatter;
import edu.gbcg.utils.c;
import org.json.JSONObject;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception{
        TSL.get().log("Program starting");
        StateVars.START_FRESH = true;
        RedditSubmissions.createDBs();
        RedditSubmissions.pushJSONDataIntoDBs();
/*
        List<String> files = RawDataLocator.redditJsonSubmissionAbsolutePaths();
        String tenline = files.get(1);
        List<String> jsonStrs= FileUtils.get().readLineByLine(tenline);

        JSONObject jo = new JSONObject(jsonStrs.get(1));
        //c.writeln("author: " + jo.getJSONObject("media").getJSONObject("oembed").get
        //        ("author_name"));
        c.writeln("created_utc: " + jo.get("created_utc"));
        String utc = jo.get("created_utc").toString();
        LocalDateTime ltd = TimeFormatter.utcToLDT(utc);
        String sql = TimeFormatter.javaDateTimeToSQLDateTime(ltd);
        c.writeln("utc: " + utc);
        c.writeln("ltd: " + ltd);
        c.writeln("sql: " + sql);
        c.writeln("ltd from sql: " + TimeFormatter.SQLDateTimeToJavaDateTime(sql));
*/
        TSL.get().shutDown();

    }
}

// DB path
// List of JSON objects
//