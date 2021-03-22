package edu.antevortadb.patient.parsing

import edu.antevortadb.configs.RawDataLocator
import javalibs.CSVExtractor
import javalibs.FileUtils
import javalibs.Logic
import javalibs.TSL
import org.apache.commons.csv.CSVRecord

@Suppress("JoinDeclarationAndAssignment", "MemberVisibilityCanBePrivate")

val log_ = TSL.get()
val logic_ = Logic.get()
val fileUtils_= FileUtils.get()

class BCInitialETL {
    val inputFilePath: String
    val extractor: CSVExtractor
    val records: List<CSVRecord>
    val headersInOrder: List<String>
    val arrHeaders: Array<String>
    val outputDir = RawDataLocator.bcDirPath() + "etlRecords" + fileUtils_.sep()

    init {
        // Use existing code from javalibs to get the records. The file is small enough
        // to read them all into memory at once
        this.inputFilePath = RawDataLocator.bcCSVAbsolutePath()
        this.extractor = CSVExtractor(this.inputFilePath)
        this.records = this.extractor.records
        this.headersInOrder = this.extractor.allInputHeadersInOrder
        this.arrHeaders = this.headersInOrder.toTypedArray()
    }

    fun writeTest() {
        // Always make sure it's been created
        fileUtils_.checkAndCreateDir(outputDir)
        val outFile = outputDir + "record2.csv"
        logic_.require(fileUtils_.fexists(outputDir), "$outputDir does not exist")
        CSVExtractor.writeCSVRecord(outFile, this.records[2], this.arrHeaders)
    }

}

fun main(args: Array<String>) {
    val parsing = BCInitialETL()

    val records = parsing.records
    val headers = parsing.headersInOrder

    parsing.writeTest()


    // Needed for clean threadpool shutdown in the logger
    log_.shutDown()
}









