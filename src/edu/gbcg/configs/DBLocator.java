package edu.gbcg.configs;

import edu.gbcg.utils.FileUtils;

import javax.xml.crypto.Data;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Similar concept to RawDataLocator. The location of the databases will be different depending
 * on which machine this code is running on and whether or not testing_mode is enabled. This
 * class abstracts away the paths to the DB files.
 */
public class DBLocator {
    /**
     * Given a reddit submission database file name it will return the absolute path
     * @param dbName Name of the submission database
     * @return Absolute path to DB if possible, otherwise null
     */
    public static String redditSubmissionsDBAbsolutePath(String dbName){
        List<String> theDBs = redditSubsAbsolutePaths();
        for(int i = 0; i < theDBs.size(); ++i)
            if(theDBs.get(i).contains(dbName))
                return theDBs.get(i);
        return null;
    }

    /**
     * Given a reddit comments database file name it will return the absolute path
     * @param dbName Name of the comments database
     * @return Absolute path to DB if possible, otherwise null
     */
    public static String redditCommentsDBAbsolutePath(String dbName){
        List<String> theDBs = redditComsAbsolutePaths();
        for(int i = 0; i < theDBs.size(); ++i)
            if(theDBs.get(i).contains(dbName))
                return theDBs.get(i);
        return null;
    }

    /**
     * Get a list of absolute file paths to all reddit submission DBs
     * @return List of file paths to submission DBs if they exist, otherwise null
     */
    public static List<String> redditSubsAbsolutePaths(){
        return FileUtils.getAllFilePathsInDirWithPrefix("RS", getSubDBPath());
    }

    /**
     * Get a list of absolute file paths to all reddit comment DBs
     * @return List of file paths to comment DBs if they exist, otherwise null
     */
    public static List<String> redditComsAbsolutePaths(){
        return FileUtils.getAllFilePathsInDirWithPrefix("RC", getComDBPath());
    }

    /**
     * Get the path to the directory that holds the submission DBs. This path changes depending on
     * which machine this code is running on.
     * @return Absolute file path to the directory holding the submission databases
     */
    public static String getSubDBPath(){
        return StateVars.TESTING_MODE ? DataPaths.LOCAL_SUB_DB_PATH : DataPaths.SUB_DB_PATH;
    }

    /**
     * Get the path to the directory that holds the comment DBs. This path changes depending on
     * which machine this code is running on.
     * @return Absolute file path to the directory holding the submission databases
     */
    public static String getComDBPath(){
        return StateVars.TESTING_MODE ? DataPaths.LOCAL_COM_DB_PATH : DataPaths.COM_DB_PATH;
    }

    /**
     * Build paths to the submission databases. Intended to be used when the DBs don't yet exist
     * but a path to the DBs is needed for creation.
     * @return List of absolute paths to DBs (that don't yet exist)
     */
    public static List<String> buildSubDBPaths(){
        return buildDBPaths(getSubDBPath(), DataPaths.SUB_DB_PREFIX);
    }

    /**
     * Build paths to the comment databases. Intended to be used when the DBs don't yet exist but
     * a path to the DBs is needed for creation.
     * @return List of absolute paths to the DBs (that don't yet exist)
     */
    public static List<String> buildComDBPaths(){
        return buildDBPaths(getComDBPath(), DataPaths.COM_DB_PREFIX);
    }

    /*
        ** NO JAVADOC **
        * Actually builds the paths to the DBs based on requested com/sub dir and their prefix
     */
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
