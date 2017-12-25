package edu.gbcg.configs;

import java.io.File;

public class DBLocator {
    public static String redditSubmissionsDBAbsolutePath(){
        String path = StateVars.TESTING_MODE ? DataPaths.local_rsub_db : DataPaths.rsub_db;
        return new File(path).getAbsolutePath().toString();
    }

    public static String redditCommentsDBAbsolutePath(){
        String path = StateVars.TESTING_MODE ? DataPaths.local_rcom_db : DataPaths.rcom_db;
        return new File(path).getAbsolutePath().toString();
    }

}
