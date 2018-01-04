package edu.gbcg.dbInteraction;

import edu.gbcg.dbInteraction.dbSelector.RSMapper;
import edu.gbcg.utils.FileUtils;
import edu.gbcg.utils.c;

import java.util.List;

public class RSMapperOutput {
    public static void printAllColumnsFromRSMappers(List<RSMapper> mappers, List<String> columnNames){
        if(mappers == null){
            c.writeln("**----- NO RESULTS -----**");
            return;
        }

        for(RSMapper mapper : mappers){
            for(String col : columnNames){
                String out = mapper.getString(col);
                c.writef("%-20s: %s\n", col, out);
            }
            c.write("\n----------------------------------------------------------------------------------------------------\n\n");
        }
        c.writeln("Returned " + mappers.size() + " results.");
    }

    public static void RSMappersToCSV(List<RSMapper> mappers, List<String> columnNames, String csvFilePath){
        if(mappers == null){
            c.writeln("**----- NO RESULTS -----**");
            return;
        }
        c.writeln("Returned " + mappers.size() + " results.");

        StringBuilder sb = new StringBuilder();

        // Build the header
        for(int i = 0; i < columnNames.size() - 1; ++i){
            sb.append(columnNames.get(i));
            sb.append(",");
        }
        sb.append(columnNames.get(columnNames.size() - 1));
        sb.append("\n");

        for(RSMapper mapper : mappers){
            for(int i = 0; i < columnNames.size() - 1; ++i){
                String result = mapper.getString(columnNames.get(i));
                if(result != null)
                    result = result.replace(',', '\'');
                sb.append(result);
                sb.append(",");
            }
            sb.append(mapper.getString(columnNames.get(columnNames.size() - 1).replace(',', '\'')));
            sb.append("\n");
        }
        FileUtils.get().writeNewFile(csvFilePath, sb.toString());
    }

}
