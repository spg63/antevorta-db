/*
 * Copyright (c) 2018 Sean Grimes. All Rights Reserved.
 * License: MIT
 */

package edu.gbcg.server

import edu.gbcg.configs.DataPaths
import edu.gbcg.configs.Finals
import edu.gbcg.utils.FileUtils
import edu.gbcg.utils.TSL
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

@Suppress("unused")
class ServerConfigHandler {
    private var jsonObject = JSONObject()
    //private var configFileMap = HashMap<String, String>()
    private val myRootDir = DataPaths.DB_CONFIG_PATH
    private val myConfigFile = myRootDir + Finals.SERVER_CONFIG_FILE_NAME
    private val fileUtils_ = FileUtils.get()
    private val USER = "USER"
    private val PASS = "PASS"
    private val BAN = "WORD"
    private val USER_ARR = "USERS"
    private val BANN_ARR = "BANNED"

    init {
        fileUtils_.checkAndCreateDir(myRootDir)
        readInConfigs()
    }

    /**
     * Return list of banned words for SQL sanitization
     */
    fun getBannedSQLWords(): List<String>{
        // There should be an array of JSONObjects where each object is a banned word
        val bannedArray = this.jsonObject.getJSONArray(BANN_ARR)
        val bannedWords = ArrayList<String>()
        for(i in 0 until bannedArray.length())
            bannedWords.add(bannedArray.getJSONObject(i).getString(BAN))

        return bannedWords
    }

    /**
     * Users are stored in the config file with a preceeding USR* so look for USR* + "username" in the configFileMap
     */
    fun isUserAuthorized(username: String, userpass: String): Boolean{
        val usersArray = this.jsonObject.getJSONArray(USER_ARR)
        for(i in 0 until usersArray.length()){
            val user = usersArray.getJSONObject(i)
            if(user.get(USER) == username){
                val pw = user.get(PASS)
                if(pw == userpass)
                    return true
            }
        }

        return false
    }

    /**
     * Creates a config file, no users are added to the file, returns path to the file
     */
    fun createConfigFile(): String{
        if(configExists())
            return getConfigFileAbsolutePath()
        val newFile = File(myConfigFile)
        newFile.createNewFile()

        this.jsonObject = JSONObject()
        val newUsersArray = JSONArray()
        val newBanArray = JSONArray()
        this.jsonObject.put(USER_ARR, newUsersArray)
        this.jsonObject.put(BANN_ARR, newBanArray)
        writeConfigFile()

        return getConfigFileAbsolutePath()
    }

    /**
     * Delete existing config file
     */
    fun deleteExistingConfigFile(): Boolean{
        if(!configExists()) return true
        val file = File(myConfigFile)
        return file.delete()
    }

    /**
     * Adds a user and associated password to the config file
     */
    fun addUserPassToConfigFile(username: String, userpasss: String): Boolean{
        // check if the user already exists
        val usersArrayCheck = this.jsonObject.getJSONArray(USER_ARR)
        for(i in 0 until usersArrayCheck.length())
            if(usersArrayCheck.getJSONObject(i).getString(USER) == username)
                return false

        // New user JSONObject
        val userObj = JSONObject()
        userObj.put(USER, username)
        userObj.put(PASS, userpasss)
        TSL.get().info("PASS: $userpasss")

        // Add the new user to the users array in the main json object
        this.jsonObject.getJSONArray(USER_ARR).put(userObj)

        // Write the new JSONObject to file for persistence
        return writeConfigFile()
    }

    /**
     * Add a banned word to the config file
     */
    fun addBannedSQLWords(word: String): Boolean {
        // Don't add multiples
        val bannedArray = this.jsonObject.getJSONArray(BANN_ARR)
        for(i in 0 until bannedArray.length())
            if(bannedArray.getJSONObject(i).getString(BAN) == word)
                return true

        val banObject = JSONObject()
        banObject.put(BAN, word)

        this.jsonObject.getJSONArray(BANN_ARR).put(banObject)

        return writeConfigFile()
    }

    /**
     * Remove a user and associated password from the config file
     */
    fun removeUserPassFromConfigFile(username: String): Boolean{
        // If it doesn't exist yet we can't remove a user
        if(!configExists()) return false

        // No great way to do this, loop through the users, add to new array, skipping the one we want to remove
        val usersArray = this.jsonObject.getJSONArray(USER_ARR)
        for(i in 0 until usersArray.length()){
            val user = usersArray.getJSONObject(i)
            // Remove the user from the users array
            if(user.getString(USER) == username)
                usersArray.remove(i)
        }

        return writeConfigFile()
    }

    /**
     * Reads the config file into the JSONObject
     */
    private fun readInConfigs(){
        if(!configExists()){
            createConfigFile()
            return
        }

        // Read the file in as a single string
        val fileString = fileUtils_.readFullFile(myConfigFile)

        // Create the JSONObject from the string
        this.jsonObject = JSONObject(fileString)
    }

    /**
     * Check that the config file exists
     */
    private fun configExists(): Boolean{
        // Return false if function call returns null
        val files = fileUtils_.getAllFilePathsInDir(myRootDir) ?: return false
        return files.any { it.contains(myConfigFile) }
    }

    /**
     * Get the absolute file path to the config file
     */
    private fun getConfigFileAbsolutePath(): String{
        val f = File(myConfigFile)
        return f.absolutePath.toString()
    }

    /**
     * Write the JSONObject from memory to disk
     */
    private fun writeConfigFile(): Boolean{
        // Loop through the map and add all key,vals to the StringBuilder
        return fileUtils_.writeNewFile(myConfigFile, this.jsonObject.toString())
    }
}

fun main(args: Array<String>){
    val sc = ServerConfigHandler()
    sc.createConfigFile()
    sc.addUserPassToConfigFile("sean", "PASS")
    sc.addUserPassToConfigFile("USER", "PASS")
    sc.addBannedSQLWords("drop")
    sc.addBannedSQLWords("create")
    sc.addBannedSQLWords("insert")
    sc.addBannedSQLWords("index")
    sc.addBannedSQLWords("rename")
    sc.addBannedSQLWords("pragma")
    sc.addBannedSQLWords("schema")
    sc.addBannedSQLWords("update")
    sc.addBannedSQLWords("dump")
}
