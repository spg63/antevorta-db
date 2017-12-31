package edu.gbcg.DBSelector;

import edu.gbcg.DBSelector.RedditSubmission.SubmissionSetMapper;
import edu.gbcg.dbcreator.RedditSubmissions;
import edu.gbcg.utils.TSL;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class RSMapper {
    protected Map<String, String> map = new HashMap<>();

    public RSMapper(Map<String, String> map){ this.map = map; }
    public RSMapper(){};

    public String getString(String key){
        return getItem(key);
    }

    public int getInt(String key){
        String stringVal = getItem(key);
        if(stringVal == null || stringVal == "") return 0;
        int val;
        try{
            val = Integer.parseInt(key);
        }
        catch(NumberFormatException e){
            TSL.get().log("NFE RSMapper.getInt");
            return 0;
        }
        return val;
    }

    public double getDouble(String key){
        String stringVal = getItem(key);
        if(stringVal == null || stringVal == "") return 0.0d;
        double val;
        try{
            val = Double.parseDouble(stringVal);
        }
        catch(NumberFormatException e){
            TSL.get().log("NFE RSMapper.getDouble");
            return 0.0d;
        }
        return val;
    }

    public boolean getBoolean(String key){
        return getInt(key) == 1 ? true : false;
    }

    public String getItem(String key){
        return this.map.getOrDefault(key, "");
    }

    public List<String> getAllItemsAsStrings(){
        List<String> vals = new ArrayList<>();
        vals.addAll(this.map.values());
        return vals;
    }

    public Map<String, String> getMapAsStrings(){
        return this.map;
    }

    public abstract List<RSMapper> buildMappers(ResultSet rs);
}
