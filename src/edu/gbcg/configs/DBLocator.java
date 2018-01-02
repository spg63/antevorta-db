package edu.gbcg.configs;

import edu.gbcg.utils.FileUtils;

import javax.xml.crypto.Data;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Similar concept to RawDataLocator. The location of the databases will be different depending
 * on which machine this code is running on and whether or not testing_mode is enabled. This
 * class abstracts away the paths to the DB files.
 */
public class DBLocator {
    /**
     * Get a list of absolute file paths to all reddit submission DBs
     * @return List of file paths to submission DBs if they exist, otherwise null
     */
    public static List<String> redditSubsAbsolutePaths(){
        // They all live in one directory, one HDD on my laptop
        if(StateVars.TESTING_MODE)
            return FileUtils.get().getAllFilePathsInDirWithPrefix("RS", getSubDBPath().get(0));

        // They each live on their own drive on the research machine
        else{
            return Arrays.asList(
                "F:/DBs/reddit/Submissions/"+DataPaths.SUB_DB_PREFIX+".sqlite3",
                "G:/DBs/reddit/Submissions/"+DataPaths.SUB_DB_PREFIX+".sqlite3",
                "H:/DBs/reddit/Submissions/"+DataPaths.SUB_DB_PREFIX+".sqlite3",
                "I:/DBs/reddit/Submissions/"+DataPaths.SUB_DB_PREFIX+".sqlite3",
                "J:/DBs/reddit/Submissions/"+DataPaths.SUB_DB_PREFIX+".sqlite3",
                "K:/DBs/reddit/Submissions/"+DataPaths.SUB_DB_PREFIX+".sqlite3"
            );
        }
    }

    /**
     * Get a list of absolute file paths to all reddit comment DBs
     * @return List of file paths to comment DBs if they exist, otherwise null
     */
    public static List<String> redditComsAbsolutePaths(){
        if(StateVars.TESTING_MODE)
            return FileUtils.get().getAllFilePathsInDirWithPrefix("RC", getComDBPath().get(0));
        else{
            return Arrays.asList(
                "F:/DBs/reddit/Comments/"+DataPaths.COM_DB_PREFIX+".sqlite3",
                "G:/DBs/reddit/Comments/"+DataPaths.COM_DB_PREFIX+".sqlite3",
                "H:/DBs/reddit/Comments/"+DataPaths.COM_DB_PREFIX+".sqlite3",
                "I:/DBs/reddit/Comments/"+DataPaths.COM_DB_PREFIX+".sqlite3",
                "J:/DBs/reddit/Comments/"+DataPaths.COM_DB_PREFIX+".sqlite3",
                "K:/DBs/reddit/Comments/"+DataPaths.COM_DB_PREFIX+".sqlite3"
            );
        }
    }

    /**
     * Get the path to the directory that holds the submission DBs. This path changes depending on
     * which machine this code is running on.
     * @return Absolute file path to the directories holding the submission databases
     */
    public static List<String> getSubDBPath(){
        List<String> paths;
        if(StateVars.TESTING_MODE)
            paths = Arrays.asList(DataPaths.LOCAL_SUB_DB_PATH);
        else{
            paths = Arrays.asList(
                "F:/DBs/reddit/Submissions/",
                "G:/DBs/reddit/Submissions/",
                "H:/DBs/reddit/Submissions/",
                "I:/DBs/reddit/Submissions/",
                "J:/DBs/reddit/Submissions/",
                "K:/DBs/reddit/Submissions/"
            );
        }
        return paths;
    }

    /**
     * Get the path to the directory that holds the comment DBs. This path changes depending on
     * which machine this code is running on.
     * @return Absolute file path to the directories holding the submission databases
     */
    public static List<String> getComDBPath(){
        List<String> paths;
        if(StateVars.TESTING_MODE)
            paths = Arrays.asList(DataPaths.LOCAL_COM_DB_PATH);
        else{
            paths = Arrays.asList(
                "F:/DBs/reddit/Comments/",
                "G:/DBs/reddit/Comments/",
                "H:/DBs/reddit/Comments/",
                "I:/DBs/reddit/Comments/",
                "J:/DBs/reddit/Comments/",
                "K:/DBs/reddit/Comments/"
            );
        }
        return paths;
    }

    /**
     * Build paths to the submission databases. Intended to be used when the DBs don't yet exist
     * but a path to the DBs is needed for creation.
     * @return List of absolute paths to DBs (that don't yet exist)
     */
    public static List<String> buildSubDBPaths(){
        if(StateVars.TESTING_MODE)
            return buildDBPaths(getSubDBPath().get(0), DataPaths.SUB_DB_PREFIX);
        else{
            return Arrays.asList(
                    "F:/DBs/reddit/Submissions/"+DataPaths.SUB_DB_PREFIX+".sqlite3",
                    "G:/DBs/reddit/Submissions/"+DataPaths.SUB_DB_PREFIX+".sqlite3",
                    "H:/DBs/reddit/Submissions/"+DataPaths.SUB_DB_PREFIX+".sqlite3",
                    "I:/DBs/reddit/Submissions/"+DataPaths.SUB_DB_PREFIX+".sqlite3",
                    "J:/DBs/reddit/Submissions/"+DataPaths.SUB_DB_PREFIX+".sqlite3",
                    "K:/DBs/reddit/Submissions/"+DataPaths.SUB_DB_PREFIX+".sqlite3"
            );
        }
    }

    /**
     * Build paths to the comment databases. Intended to be used when the DBs don't yet exist but
     * a path to the DBs is needed for creation.
     * @return List of absolute paths to the DBs (that don't yet exist)
     */
    public static List<String> buildComDBPaths(){
        if(StateVars.TESTING_MODE)
            return buildDBPaths(getComDBPath().get(0), DataPaths.COM_DB_PREFIX);
        else{
            return Arrays.asList(
                    "F:/DBs/reddit/Comments/"+DataPaths.COM_DB_PREFIX+".sqlite3",
                    "G:/DBs/reddit/Comments/"+DataPaths.COM_DB_PREFIX+".sqlite3",
                    "H:/DBs/reddit/Comments/"+DataPaths.COM_DB_PREFIX+".sqlite3",
                    "I:/DBs/reddit/Comments/"+DataPaths.COM_DB_PREFIX+".sqlite3",
                    "J:/DBs/reddit/Comments/"+DataPaths.COM_DB_PREFIX+".sqlite3",
                    "K:/DBs/reddit/Comments/"+DataPaths.COM_DB_PREFIX+".sqlite3"
            );
        }
    }

    /*
        ** NO JAVADOC **
        * Actually builds the paths to the DBs based on requested com/sub dir and their prefix
     */

    ///// USE POSTFIX IN TESTING_MODE BUT NOT IN REGULAR MODE!!!!!!!!
    private static List<String> buildDBPaths(String db_dir, String db_prefix){
        List<StringBuilder> sbs = new ArrayList<>();
        for(int i = 0; i < StateVars.DB_SHARD_NUM; ++i){
            StringBuilder sb = new StringBuilder();
            sb.append(db_dir);
            sb.append(db_prefix);
            sb.append(i);
            sb.append(DataPaths.DB_POSTFIX);
            sbs.add(sb);
        }

        List<String> paths = new ArrayList<>();
        for(StringBuilder sb : sbs)
            paths.add(sb.toString());
        return paths;
    }
}
