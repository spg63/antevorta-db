package edu.gbcg.dbInteraction;

import edu.gbcg.dbInteraction.dbSelector.RSMapper;
import edu.gbcg.utils.FileUtils;
import edu.gbcg.utils.Out;

import java.util.List;

public class RSMapperOutput {
    private static Out out = Out.get();
    /**
     * Print all column names and values from a DB list of RSMappers
     * @param mappers The list of RSMapper objects
     * @param columnNames The list of column names
     */
    public static void printAllColumnsFromRSMappers(List<RSMapper> mappers, List<String> columnNames){
        if(mappers == null){
            out.writeln("**----- NO RESULTS -----**");
            return;
        }

        for(RSMapper mapper : mappers){
            for(String col : columnNames){
                String outmap = mapper.getString(col);
                out.writef("%-20s: %s\n", col, outmap);
            }
            out.write
                    ("\n----------------------------------------------------------------------------------------------------\n\n");
        }
        out.writeln("Returned " + mappers.size() + " results.");
    }

    /**
     * Write all column names as headings and column values to a CSV file
     * @param mappers The list of RSMapper objects
     * @param columnNames The list of column names
     * @param csvFilePath CSV file path + file name
     */
    public static void RSMappersToCSV(List<RSMapper> mappers, List<String> columnNames, String csvFilePath){
        if(mappers == null){
            out.writeln("**----- NO RESULTS -----**");
            return;
        }
        out.writeln("Returned " + mappers.size() + " results.");

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
