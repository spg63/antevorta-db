package edu.gbcg.configs;

public class DataPaths {
    // File paths when running in 'TESTING_MODE' (i.e. on my MBP with limited data)
    public static String local_data_path = "LocalData/raw/";
    public static String data_path = "NULL_FUCKER";

    private static String local_db_path = "LocalDB/";
    private static String db_path = "NULL_FUCKER:";

    public static String reddit_com_db_fname = "redditcoms.sqlite";
    public static String reddit_sub_db_fname = "redditsubs.sqlite";

    public static String local_rcom_db = local_db_path + reddit_com_db_fname;
    public static String local_rsub_db = local_db_path + reddit_sub_db_fname;
    public static String rcom_db = db_path + reddit_com_db_fname;
    public static String rsub_db = db_path + reddit_sub_db_fname;

}
