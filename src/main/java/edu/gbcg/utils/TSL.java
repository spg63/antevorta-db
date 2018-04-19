/*
 * Copyright (c) 2018 Sean Grimes. All rights reserved.
 * License: MIT License
 */

package edu.gbcg.utils;

import java.io.*;
import java.time.LocalDateTime;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;


/**
 * @author Sean Grimes, spg63@cs.drexel.edu
 * @author Andrew W.E. McDonald
 * @since 6/6/15
 */
@SuppressWarnings("unused")
public class TSL extends Thread{
    private static volatile TSL _instance;
    public static boolean LOG_INFO = true;
    public static boolean LOG_WARN = true;
    public static boolean LOG_TO_CONSOLE = true;

    private String SHUTDOWN_REQ = null;
    private volatile boolean shuttingDown, loggerTerminated;
    private BlockingQueue<Object> itemsToLog = new ArrayBlockingQueue<>(1000000);
    @SuppressWarnings("FieldCanBeLocal")
    private LocalDateTime ldt;
    private String dt;
    private Out out;
    private FileUtils futils;

    private TSL(){
        this.SHUTDOWN_REQ = "SHUTDOWN";
        this.shuttingDown = false;
        this.loggerTerminated = false;
        this.out = Out.get();
        this.futils = FileUtils.get();
        LocalDateTime dt = LocalDateTime.now();
        this.dt = dt.toString().replace("T", "_").replace(":","_");

        // Start the logger
        start();
    }

    /**
     * Get reference to the logger
     * @return
     */
    public static TSL get(){
        if(_instance == null){
            synchronized(TSL.class){
                if(_instance == null){
                    _instance = new TSL();
                }
            }
        }
        return _instance;
    }

    @Override
    public void run(){
        this.futils.checkAndCreateDir("logs");
        PrintWriter pw = null;
        try{
            String item;
            try{
                pw = new PrintWriter(
                        new BufferedWriter(
                                new FileWriter("logs/tslogs_"+dt+".txt", true)));
            }
            catch(IOException e){
                out.writeln_err("*** ThreadSafeLogger IOException");
            }

            while(!(item = (String)itemsToLog.take()).equals(SHUTDOWN_REQ)){
                String label;
                if(item.startsWith("[INFO]"))
                    label = "[INF] ";
                else if(item.startsWith("[WARN]"))
                    label = "[WAR] ";
                else if(item.startsWith("[ERROR]"))
                    label = "[ERR] ";
                else
                    label = "[EXP]";

                // Split the init lable off the string
                String[] splitItem = item.split(" ", 2);

                StringBuilder sb = new StringBuilder();
                sb.append(label);
                sb.append(time_str());
                sb.append(splitItem[1]);
                pw.println(sb.toString());
                pw.flush();
                if(LOG_TO_CONSOLE)
                    this.out.writeln(sb.toString());
            }
        }
        catch(InterruptedException e){
            this.out.writeln_err("ThreadSafeLogger -- I was interrupted");
        }
        finally{
            loggerTerminated = true;
            if(pw != null)
                pw.close();
        }
    }

    /**
     * Log info
     * @param str The log message
     */
    public void info(Object str){
        if(!LOG_INFO || shuttingDown || loggerTerminated)
            return;
        try{
            itemsToLog.put("[INFO] " + str);
        }
        catch(InterruptedException e){
            Thread.currentThread().interrupt();
            throw new RuntimeException("ThreadSafeLogger.info() -- Unexpected interruption");
        }
    }

    /**
     * Log warnings
     * @param str The log message
     */
    public void warn(Object str){
        if(!LOG_WARN || shuttingDown || loggerTerminated)
            return;
        try{
            itemsToLog.put("[WARN] " + str);
        }
        catch(InterruptedException e){
            Thread.currentThread().interrupt();
            throw new RuntimeException("ThreadSafeLogger.warn() -- Unexpected interruption");
        }
    }

    /**
     * Log errors
     * @param str The log message
     */
    public void err(Object str){
        if(shuttingDown || loggerTerminated)
            return;
        try{
            itemsToLog.put("[ERROR] " + str);
        }
        catch(InterruptedException e){
            Thread.currentThread().interrupt();
            throw new RuntimeException("ThreadSafeLogger.err() -- Unexpected interruption");
        }
    }

    private void exception(Object str){
        if(shuttingDown || loggerTerminated)
            return;
        try{
            itemsToLog.put("[EXCEPTION] " + str);
        }
        catch(InterruptedException e){
            Thread.currentThread().interrupt();
            throw new RuntimeException(("ThreadSafeLogger.exception() -- Unexpected interruption"));
        }
    }

    /**
     * Log an exception
     * @param e
     */
    public void exception(Exception e){
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String trace = sw.toString();
        exception("\n" + trace);
    }

    /**
     * Shutdown the logger, thread will sleep for 500ms to allow proper flushing
     */
    public void shutDown() {
        shuttingDown = true;
        try {
            itemsToLog.put(SHUTDOWN_REQ);
            // Force a pause of the main thread to give the logger thread a change to write all data to the file system
            Thread.sleep(500);
        }
        catch(InterruptedException e){
            throw new RuntimeException("ThreadSafeLogger.shutDown() -- Unexpected interruption");
        }
    }

    /**
     * Shutdown the logger, sleep for half a second to allow the logger to finish flushing to disk then kill the program
     * with exit code 6
     */
    public void logAndKill(){
        shuttingDown = true;
        try{
            itemsToLog.put(SHUTDOWN_REQ);
            Thread.sleep(500);
            System.exit(6);
        }
        catch(InterruptedException e){
            System.exit(6);
        }
    }

    /**
     * Shutdown the logger, adding a final log message onto the queue before killing the program
     * @param log_message
     */
    public void logAndKill(String log_message){
        err(log_message);
        logAndKill();
    }

    public void logAndKill(Exception e){
        exception(e);
        logAndKill();
    }

    private String time_str(){
        ldt = LocalDateTime.now();
        int hour = ldt.getHour();
        int min = ldt.getMinute();
        int sec = ldt.getSecond();
        int milli = (ldt.getNano())/(1000000);

        String hour_s = hour < 10 ? ("0" + hour) : Integer.toString(hour);
        String min_s = min < 10 ? ("0" + min) : Integer.toString(min);
        String sec_s = sec < 10 ? ("0" + sec) : Integer.toString(sec);
        String mil_s;
        if(milli > 10 && milli < 100)
            mil_s = "0" + milli;
        else if(milli < 10)
            mil_s = "00" + milli;
        else
            mil_s = Integer.toString(milli);

        return "("+hour_s+":"+min_s+":"+sec_s+"."+mil_s+") > ";
    }
}
