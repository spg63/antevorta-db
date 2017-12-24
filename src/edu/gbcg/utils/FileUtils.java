package edu.gbcg.utils;

import java.io.*;
import java.nio.Buffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Sean Grimes, spg63@cs.drexel.edu
 * @since 6/5/15
 */
public class FileUtils{
    private static volatile FileUtils _instance;

    private FileUtils(){
    }

    public static FileUtils getInstance(){
        if(_instance == null){
            synchronized(FileUtils.class){
                if(_instance == null){
                    _instance = new FileUtils();
                }
            }
        }
        return _instance;
    }

    public String getWorkingDir(){
        Path WD = Paths.get("");
        return WD.toAbsolutePath().toString();
    }

    public String readFullFile(String filename) {
        BufferedReader br = null;
        String all = null;
        try{
            br = new BufferedReader(new FileReader(filename));
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();
            while(line != null){
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }
            all = sb.toString();
        }
        catch(FileNotFoundException e){
            c.writeln_err(filename + " not found.");
        }
        catch(IOException e){
            c.writeln_err("IOException in FileUtils.readFullFile");
        }
        finally{
            if(br != null){
                try{
                    br.close();
                }
                catch(IOException e){
                    c.writeln_err("Couldn't close the br | FileUtils.readFullFile");
                }
            }
        }
        return all;
    }

    public List<String> readLineByLine(String filename) {
        List<String> lines = new ArrayList<>();
        BufferedReader br = null;
        try{
            br = new BufferedReader(new FileReader(filename));
            String line = br.readLine();
            while(line != null){
                lines.add(line);
                line = br.readLine();
            }
        }
        catch(Exception e){
            e.printStackTrace();
            c.writeln_err("Problem with readLineByLine");
        }
        finally{
            if(br != null){
                try{
                    br.close();
                }
                catch(IOException e){
                    c.writeln_err("Couldn't close the br | FileUtils.readLineByLine");
                }
            }
        }
        return lines;
    }
}
