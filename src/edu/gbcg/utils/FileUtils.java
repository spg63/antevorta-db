package edu.gbcg.utils;

import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * @author Sean Grimes, spg63@cs.drexel.edu
 * @since 6/5/15
 */
public class FileUtils{
    private static volatile FileUtils _instance;
    private Out out = Out.get();

    private FileUtils(){
    }

    public static FileUtils get(){
        if(_instance == null){
            synchronized(FileUtils.class){
                if(_instance == null){
                    _instance = new FileUtils();
                }
            }
        }
        return _instance;
    }

    /**
     * Get the current working directory
     * @return The path to the current working directory
     */
    public String getWorkingDir(){
        Path WD = Paths.get("");
        return WD.toAbsolutePath().toString();
    }

    /**
     * Create a directory if it doesn't exist
     * @param dirName Path to the directory
     */
    public void checkAndCreateDir(String dirName){
        String path = getWorkingDir();
        File tmp = new File(path + File.separator + dirName + File.separator);
        if(!tmp.exists())
            tmp.mkdirs();
    }

    /**
     * Read a file in as a string
     * @param filepath The path to the file
     * @return The file, as a string, if it's found and read successfully
     */
    public String readFullFile(String filepath) {
        BufferedReader br = null;
        String all = null;
        try{
            br = new BufferedReader(new FileReader(filepath));
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
            out.writeln_err(filepath + " not found.");
        }
        catch(IOException e){
            out.writeln_err("IOException in FileUtils.readFullFile");
        }
        finally{
            if(br != null){
                try{
                    br.close();
                }
                catch(IOException e){
                    out.writeln_err("Couldn't close the br | FileUtils.readFullFile");
                }
            }
        }
        return all;
    }

    /**
     * Read a file line by line
     * @param filepath The path to the file
     * @return A list of lines of the file (as strings) if file is found and readable
     */
    public List<String> readLineByLine(String filepath) {
        List<String> lines = new ArrayList<>();
        BufferedReader br = null;
        try{
            br = new BufferedReader(new FileReader(filepath));
            String line = br.readLine();
            while(line != null){
                lines.add(line);
                line = br.readLine();
            }
        }
        catch(Exception e){
            e.printStackTrace();
            out.writeln_err("Problem with readLineByLine");
        }
        finally{
            if(br != null){
                try{
                    br.close();
                }
                catch(IOException e){
                    out.writeln_err("Couldn't close the br | FileUtils.readLineByLine");
                }
            }
        }
        return lines;
    }

    /**
     * Returns a list of absolute file paths to files in a directory that start with the supplied
     * prefix
     * @param prefix The prefix string that a file should match with
     * @param path The path to the directory containing the files
     * @return The list of absolute paths, null if nothing matching in the directory
     */
    public List<String> getAllFilePathsInDirWithPrefix(String prefix, String path){
        List<String> filepaths = new ArrayList<>();

        File[] files = new File(path).listFiles();
        if(files == null)
            return null;
        for(File f : files){
            if(f.isFile() && f.getName().startsWith(prefix))
                filepaths.add(f.getAbsolutePath());
        }
        if(filepaths.isEmpty()) return null;
        Comparator<String> comparator = Comparator.comparing((String x) -> x);
        filepaths.sort(comparator);
        return filepaths;
    }

    /**
     * Returns a list of absolute file paths to files in a directory
     * @param path The path to the directory
     * @return The list of absolute paths, null if no files in the directory
     */
    public List<String> getAllFilePathsInDir(String path){
        List<String> filepaths = new ArrayList<>();
        File[] files = new File(path).listFiles();
        if(files == null)
            return null;
        for(File f : files)
            if(f.isFile())
                filepaths.add(f.getAbsolutePath());
        if(filepaths.isEmpty()) return null;
        Comparator<String> comparator = Comparator.comparing((String x) -> x);
        filepaths.sort(comparator);
        return filepaths;
    }

    /**
     * Returns java File objects for all Files in a directory
     * @param path Path to the directory
     * @return The list of File objects, null if no files in the directoryu
     */
    public List<File> getAllFileObjectsInDir(String path){
        List<File> files = new ArrayList<>();
        File[] fs = new File(path).listFiles();
        for(File f : fs)
            if(f.isFile())
                files.add(f);
        if(files.isEmpty()) return null;

        return files;
    }

    public void writeNewFile(String fileName, String str){

        BufferedWriter writer;
        try {
            writer = new BufferedWriter(new FileWriter(fileName));
            writer.write(str);
            writer.close();
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    public BufferedReader getBufferedReaderForBZ2FileType(String filePath){
        BufferedReader br = null;
        try{
            FileInputStream fin = new FileInputStream(filePath);
            BufferedInputStream bis = new BufferedInputStream(fin);
            CompressorInputStream cis = new CompressorStreamFactory().createCompressorInputStream(bis);
            br = new BufferedReader(new InputStreamReader(cis));

        }
        catch(IOException | CompressorException e){
            e.printStackTrace();
            return null;
        }
        return br;
    }
}
