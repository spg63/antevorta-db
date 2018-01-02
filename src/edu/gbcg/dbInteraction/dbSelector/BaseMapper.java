package edu.gbcg.dbInteraction.dbSelector;

import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

/**
 * This class implements no functionality regarding building a Mapper object, and doesn't know how to read a
 * ResultSet but is used as a return type from buildMappers to avoid switching over the class type which is become
 * tedious to update as more DB types are added to the project
 */
public class BaseMapper extends RSMapper{
    public BaseMapper(){ super(); }
    public BaseMapper(Map<String, String> map){ super(map); }
    public List<RSMapper> buildMappers(ResultSet rs){ return null; }
}
