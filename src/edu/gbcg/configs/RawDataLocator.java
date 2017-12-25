package edu.gbcg.configs;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class RawDataLocator {
    public static List<String> redditJsonSubmissionAbsolutePaths(){
        List<String> allFiles = null;
        String data_dir = getRedditDataPath();
        return getAllFilesWithPrefix("RS", data_dir);
    }

    public static List<String> redditJsonCommentAbsolutePaths(){
        List<String> allFiles = null;
        String data_dir = getRedditDataPath();
        return getAllFilesWithPrefix("RC", data_dir);
    }

    private static String getRedditDataPath(){
        // Testing mode means we're running on my MBP with limited data
        return StateVars.TESTING_MODE ? DataPaths.local_data_path : DataPaths.data_path;
    }

    private static List<String> getAllFilesWithPrefix(String prefix, String path){
        List<String> filepaths = new ArrayList<>();

        File[] files = new File(path).listFiles();
        for(File f : files){
            if(f.isFile() && f.getName().startsWith(prefix))
                filepaths.add(f.getAbsolutePath());
        }
        return filepaths;
    }

}
