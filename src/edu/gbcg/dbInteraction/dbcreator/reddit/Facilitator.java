package edu.gbcg.dbInteraction.dbcreator.reddit;

import java.util.List;

public abstract class Facilitator {
    protected List<String> DBAbsolutePaths;
    protected String tableName;
    protected List<String> DBDirectoryPaths;
    protected List<String> columnNames;
    protected List<String> dataTypes;

    

    abstract List<String> buildDBPaths();
    abstract List<String> getJsonAbsolutePaths();
}
