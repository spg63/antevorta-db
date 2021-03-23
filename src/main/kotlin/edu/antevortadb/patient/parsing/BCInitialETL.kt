package edu.antevortadb.patient.parsing

import edu.antevortadb.configs.RawDataLocator
import javalibs.*
import org.apache.commons.csv.CSVRecord

@Suppress("JoinDeclarationAndAssignment", "MemberVisibilityCanBePrivate")

val log_ = TSL.get()
val logic_ = Logic.get()
val fileUtils_= FileUtils.get()

class BCInitialETL {
    // The file to read from
    val inputFilePath: String
    // The file to write to
    val outputDir = RawDataLocator.bcDirPath() + "etlRecords" + fileUtils_.sep()
    // Using CSVExtractor for reading, getting records / headers, and writing
    val extractor: CSVExtractor
    // The records from the input file
    val records: List<CSVRecord>
    // The headers from the input file
    val headersInOrder: List<String>
    // The headers for the output file, mostly the same as input with some additional
    // columns created from some data manipulation
    var outputHeaders: List<String> = ArrayList()
    // The outputHeaders but as an array to give back to CSVExtractor for writing
    var outputHeadersAsArray: Array<String>? = null

    init {
        // Use existing code from javalibs to get the records. The file is small enough
        // to read them all into memory at once
        this.inputFilePath = RawDataLocator.bcCSVAbsolutePath()
        this.extractor = CSVExtractor(this.inputFilePath)
        this.records = this.extractor.records
        this.headersInOrder = this.extractor.allInputHeadersInOrder
        //this.outputHeadersAsArray = this.headersInOrder.toTypedArray()
    }

    /*
     *  NOTE: There's no easy way to add single columns to an existing CSVRecord object
     *  so the best thing here it to treat each record as a List<String> and then once
     *  all manipulation is complete for a record, use CSVPrinter with default
     *  formatting and the Array form of the output headers to write the List<String> to
     *  a CSV file...
     */
    // Deal with the columns, creating new ones when and manipulating data for each
    // record to make better use of some columns
    fun manipulate() {
        val allRows: MutableList<MutableList<String>> = ArrayList()
        val rowtest = outputDir + "recordTEST.csv"
        // For testing, delete the old one
        FileUtils.get().deleteFile(rowtest)
        var cnt = 0

        // TODO: Move the pNLabel column to the very end of the list
        // For every single record
        for(record in this.records) {
            ++cnt
            val row: MutableList<String> = ArrayList()
            val maniHeaders: MutableList<String> = ArrayList()
            // For every column of every record
            for(header in this.headersInOrder) {
                val cellContent = record.get(header)

                /*
                 * Regardless of which header is being worked on, the original header
                 * and original cell content needs to be written to the CSV. Write this
                 * before the when statement to avoid the same calls multiple times.
                 * The else statement in the when should just do nothing but record a
                 * log of working on a cell that's not being manipulated
                 */
                writeOriginal(row, maniHeaders, header, cellContent)
                when (header) {
                    "Age" -> {
                        ageThreshold(row, maniHeaders, cellContent)
                    }
                    "Body Site Writein" -> {
                        bodySiteWritein(row, maniHeaders, cellContent)
                    }
                    "Diagnosis" -> {
                        diagnosis(row, maniHeaders, cellContent)
                    }

                    else -> {
                        // Nothing to do here manipulation wise. The data was already
                        // written before the when statement
                        log_.trace("No manipulation for: $header")
                    }
                }
            }

            //______ testing
            CSVExtractor.appendRecord(rowtest, row, maniHeaders.toTypedArray())
            //if(cnt >= 25) log_.die("Heyo")

            //______

            // TODO: Re-order columns:
            //       [ ] pNLabel should be the last column
            //       [ ] something else
            //       [ ] something else
            //       [ ] something else
            //       [ ] something else
            //       [ ] something else

            allRows.add(row)
        }

        // TODO: Convert allRows into a List of CSVRecords, or just write allRows
        //       directly to a file
    }

    private fun writeOriginal(
                    row: MutableList<String>,
                    headerList: MutableList<String>,
                    headerName: String,
                    cellContent: String) {
        headerList.add(headerName)
        row.add(cellContent)
    }

    /*
        Looking to see if patient is over or under 45
     */
    private fun ageThreshold(
        row: MutableList<String>, header: MutableList<String>, cellContent: String) {
        header.add("age_under_45")
        if(cellContent.isNotEmpty()) {
            val age = NumUtils.getLongFromStr(cellContent)
            // Good parse
            if(age != null)
                if(age < 45) row.add("1")
            else
                row.add("0")
        }
        // Either empty, bad parse, or <= 45
        else
            row.add("0")
    }

    /*
        Looking for biopsy keyword,
     */
    private fun bodySiteWritein(
        row: MutableList<String>, header: MutableList<String>, cellContent: String) {
        val lowerContent = cellContent.toLowerCase()
        header.add("BSW_biopsy_present")
        var doesContain = false
        if(lowerContent.contains("biopsy")) doesContain = true
        if(lowerContent.contains("bx")) doesContain = true
        if(doesContain) row.add("1") else row.add("0")
    }

    /*
        Looking for "invasive" or "infiltrating"
        Separately, looking for "invasive" or "infiltrating" paired with "ductal"
        Separately, looking for "malignant"
        Separately, looking for "metastatic"
        Separately, looking for "DCIS" or "ductal" WITHOUT "invasive" or "infilitrating"
        Separately, looking for "lobular"
     */
    private fun diagnosis(
        row: MutableList<String>, header: MutableList<String>, cellContent: String) {
        val content = cellContent.toLowerCase()
        // ----- 1st ---------------------------------------------------------------------
        var invasiveOrInfiltrating = false
        header.add("diagnosis_invasive_or_infiltrating")
        if(content.contains("invasive")) invasiveOrInfiltrating = true
        if(content.contains("infiltrating")) invasiveOrInfiltrating = true

        // Reverting the true if it's explicitly non-invasive
        if(content.contains("non-invasive")) invasiveOrInfiltrating = false
        if(content.contains("microinvasive")) invasiveOrInfiltrating = false
        if(content.contains("micro-invasive")) invasiveOrInfiltrating = false

        // Add the value to the column
        if(invasiveOrInfiltrating) row.add("1") else row.add("0")

        // ----- 2nd ---------------------------------------------------------------------
        var malignant = false
        header.add("diagnosis_malignant")
        if(content.contains("malignant")) malignant = true
        if(content.contains("non-malignant")) malignant = false
        if(malignant) row.add("1") else row.add("0")

        // ----- 3rd ---------------------------------------------------------------------
        var metastatic = false
        header.add("diagnosis_metastatic")
        if(content.contains("metastatic")) metastatic = true
        if(content.contains("meta-static")) metastatic = true
        if(content.contains("meta static")) metastatic = true
        if(content.contains("non-metastatic")) metastatic = false
        if(metastatic) row.add("1") else row.add("0")

        // ----- 4th ---------------------------------------------------------------------
        var DCIS = false
        header.add("diagnosis_ductal_WITH_invasive_or_infiltrating")
        if(content.contains("DCIS")) DCIS = true
        if(content.contains("ductal")) DCIS = true
        // Needs to be invasive/infiltrating && ductal to matter
        if(DCIS && invasiveOrInfiltrating) row.add("1") else row.add("0")

        // ----- 5th ---------------------------------------------------------------------
        var lobular = false
        header.add("diagnosis_lobular_type")
        if(content.contains("lobular")) lobular = true
        if(lobular) row.add("1") else row.add("0")

    }

    fun writeTest() {
        // Always make sure it's been created
        fileUtils_.checkAndCreateDir(outputDir)
        val outFile = outputDir + "record2.csv"
        logic_.require(fileUtils_.fexists(outputDir), "$outputDir does not exist")
        CSVExtractor.writeCSVRecord(outFile, this.records[2], this.outputHeadersAsArray)
    }

}

fun main(args: Array<String>) {
    val parsing = BCInitialETL()

    val records = parsing.records
    val headers = parsing.headersInOrder

    //parsing.writeTest()
    parsing.manipulate()


    // Needed for clean threadpool shutdown in the logger
    log_.shutDown()
}









