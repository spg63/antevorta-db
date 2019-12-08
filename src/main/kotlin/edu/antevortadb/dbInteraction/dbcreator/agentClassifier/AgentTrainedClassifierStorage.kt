package edu.antevortadb.dbInteraction.dbcreator.agentClassifier

import edu.antevortadb.configs.DBLocator
import edu.antevortadb.configs.Finals
import edu.antevortadb.dbInteraction.DBCommon
import edu.antevortadb.dbInteraction.columnsAndKeys.PreTrainedClassifiers
import javalibs.FileUtils
import javalibs.Logic
import javalibs.TSL
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.sql.Blob
import java.util.*

object AgentTrainedClassifierStorage {
    private const val tableName = Finals.TRAINED_MODELS_TABLE
    private val log_: TSL = TSL.get()
    private val dbDirPath = DBLocator.preTrainedClassifierDirPath()
    private val dbFilePath = DBLocator.preTrainedClassifierDB()

    fun initTable(dropOldData: Boolean) {
        // Drop the old table if it exists and the user wants it dropped
        if(dropOldData) {
            val tableExists = File(dbFilePath).exists()
            if(tableExists){
                val sql = "drop table if exists $tableName"
                DBCommon.delete(dbFilePath, sql)
            }
        }

        // Create the necessary dirs if they don't yet exist
        FileUtils.get().checkAndCreateDir(dbDirPath)

        // Create the DB file
        val conn = DBCommon.connect(dbFilePath)

        // The list of column names and associated data types for the columns
        val colNames = PreTrainedClassifiers.columnNames()
        val colTypes = PreTrainedClassifiers.dataTypes()

        // Now create the sql statement for table building
        val sb = StringBuilder()
        sb.append("create table if not exists $tableName(")
        for(i in 0 until colNames.size){
            sb.append(colNames[i])
            sb.append(colTypes[i])
        }
        sb.append(");")
        val sql = sb.toString()

        // Create the table and close the connection
        DBCommon.insert(conn, sql)
        DBCommon.disconnect(conn)
    }

    /**
     * Insert a trained classifier into the DB
     * @param agentName Name of agent this classifier belongs to
     * @param filePath Path where the classifier currently resides
     */
    fun insertTrainedClassifier(agentName: String, filePath: String) {
        val timestamp = Date().time / 1000
        val sql = "INSERT INTO $tableName (${Finals.CREATED_DT}, AgentName, BlobData) " +
                "VALUES (?, ?, ?)"
        val conn = DBCommon.connect(dbFilePath)
        conn.autoCommit = false
        val stmt = conn.prepareStatement(sql)
        stmt.setLong(1, timestamp)
        stmt.setString(2, agentName)

        val trainedClassifier = File(filePath)
        val fis = FileInputStream(trainedClassifier)
        stmt.setBinaryStream(3, fis, trainedClassifier.length().toInt())
        stmt.execute()
        conn.commit()
        fis.close()
        DBCommon.disconnect(conn)
    }

    /**
     * Get an already trained classifier from the DB
     * @param agentName Name of the agent this classifier belongs to
     * @return The input stream for the blob of data that will become a classifier
     */
    fun getTrainedClassifier(agentName: String): InputStream {
        var istream: InputStream? = null
        val conn = DBCommon.connect(dbFilePath)
        conn.autoCommit = false
        val sql = "SELECT BlobData from classifier_models where AgentName = '$agentName';"
        val stmt = conn.prepareStatement(sql)
        val rs = stmt.executeQuery()
        // Use if here, we're only expecting a single return and if there's more we
        // only want one
        if(rs.next()){
            log_.debug("I don't think there's a guarantee that this contains *all* data" +
                    " for a big stream")
            istream = rs.getBinaryStream("BlobData")
        }
        else{
            Logic.get().dieFrom("rs.next() was not true!")
        }

        rs.close()
        stmt.close()
        DBCommon.disconnect(conn)

        Logic.get().require(istream != null, "classifier was null from DB for $agentName")
        return istream!!    // The above check guarantees we'll never return null
    }

    fun getMostRecentClassifier(agentName: String): InputStream? {
        Logic.get().dieFrom("These function needs some implementing")
        return null
    }

}
