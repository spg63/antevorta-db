package edu.antevortadb.patient.parsing

import edu.antevortadb.configs.Finals
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
            var pN_number = Finals.BAD_DATA
            // For every column of every record
            log_.trace("Working on ${record[2]}")
            for(header in this.headersInOrder) {
                var cellContent = record.get(header)

                /*
                 * Regardless of which header is being worked on, the original header
                 * and original cell content needs to be written to the CSV. Write this
                 * before the when statement to avoid the same calls multiple times.
                 * The else statement in the when should just do nothing but record a
                 * log of working on a cell that's not being manipulated
                 */
                writeOriginal(row, maniHeaders, header, cellContent)
                cellContent
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
                    "Size" -> {
                        size(row, maniHeaders, cellContent)
                    }
                    "Pos Margin" -> {
                        posMargin(row, maniHeaders, cellContent)
                    }
                    "Angio Lymphatic Invasion" -> {
                        angioLymphaticInvasion(row, maniHeaders, cellContent)
                    }
                    "pT Stage" -> {
                        pT(row, maniHeaders, cellContent)
                    }
                    "pN Stage" -> {
                        pN_number = pN(row, maniHeaders, cellContent)
                    }
                    "Histologic Grade" -> {
                        maniHeaders.add("hist_grade")
                        row.add(validNumber(cellContent))
                    }
                    "Breast: Tubule Formation" -> {
                        tubuleFormation(row, maniHeaders, cellContent)
                    }
                    "Breast: Nuclear Grade" -> {
                        maniHeaders.add("nuclear_grade")
                        row.add(validNumber(cellContent))
                    }
                    "Breast: Mitotic Figure" -> {
                        mitoticFigure(row, maniHeaders, cellContent)
                    }
                    "Breast: Lobular Extension" -> {
                        lobularExtensions(row, maniHeaders, cellContent)
                    }
                    "Breast: Pagetoid Spread" -> {
                        pagetoidSpread(row, maniHeaders, cellContent)
                    }
                    "Breast: Perineureal Invasion" -> {
                        perineurealInvasion(row, maniHeaders, cellContent)
                    }
                    "Breast: Calcifications" -> {
                        calcifications(row, maniHeaders, cellContent)
                    }
                    "ER_percentPosNuclei" -> {
                        // Looking for > 10% positive based on previous Mark paper
                        stainStatus(row, maniHeaders, cellContent, "er", 10.0)
                    }
                    "PR_percentPosNuclei" -> {
                        // Looking for > 10% positive based on previous Mark paper
                        stainStatus(row, maniHeaders, cellContent, "pr", 10.0)
                    }
                    "P53_percentPosNuclei" -> {
                    // Looking for over 5% based on
                    // https://www.sciencedirect.com/science/article/pii/S111003621100046X
                        stainStatus(row, maniHeaders, cellContent, "p53", 5.0)
                    }
                    "KI67_percentPosNuclei" -> {
                        // Looking for over 14% based on previous Mark paper
                        stainStatus(row, maniHeaders, cellContent, "ki67", 14.0)
                    }
                    else -> {
                        // Nothing to do here manipulation wise. The data was already
                        // written before the when statement, perhaps useful in the future
                    }
                }
            }

            // Write a random number for sorting
            writeRandom(row, maniHeaders)

            // We have the pN number, need to re-order the pN label column so it comes
            // in as the last column of the CSV since the date parsing of
            // DeepLearning4J doesn't work otherwise...probably because of something
            // dumb on my end rather than their end
            writePnLabel(row, maniHeaders, pN_number)

            // Appent the row to the output file
            CSVExtractor.appendRecord(rowtest, row, maniHeaders.toTypedArray())
            //if(cnt >= 150) log_.die("dead")

            allRows.add(row)
        }
    }

    private fun writeOriginal(
                    row: MutableList<String>,
                    headerList: MutableList<String>,
                    headerName: String,
                    cellContent: String) {
        headerList.add(headerName)
        // I'm tired of empty cells fucking the agents up. When data doesn't exist,
        // fucking Finals.BAD_DATA
        if(cellContent.isEmpty()) row.add(Finals.BAD_DATA.toString())
        else row.add(cellContent)
    }

    private fun writeRandom(row: MutableList<String>, headers: MutableList<String>){
        headers.add("randomVal")
        val rand = NumUtils.randomBoundedInclusiveInt(0, 1000000)
        row.add(rand.toString())
    }

    private fun writePnLabel(
        row: MutableList<String>,
        headerList: MutableList<String>,
        pNValue: Int) {

        headerList.add("pN_label")
        // If there was no pN value for this record then the pNValue will
        // be Finals.BAD_DATA
        when {
            pNValue == Finals.BAD_DATA  -> row.add("${Finals.BAD_DATA}")
            pNValue == 0                -> row.add("0")
            pNValue > 0                 -> row.add("1")
            else                        -> log_.die("pNValue is impossible: $pNValue")
        }
    }

    /*
        Looking to see if patient is over or under 45
     */
    private fun ageThreshold(
        row: MutableList<String>, header: MutableList<String>, cellContent: String) {
        header.add("age_under_45")
        if(cellContent.isNotEmpty()) {
            val age = NumUtils.getLongFromStr(cellContent)
            if(age.toInt() == Finals.BAD_DATA) {
                row.add("${Finals.BAD_DATA}")
                return
            }
            // Good parse
            if(age != null) {
                // Younger than 45
                if(age < 45) row.add("1")
                // 45 or older
                else row.add("0")
            }
            // It was null, bad parse, throw it in the majority and log it
            else {
                row.add("${Finals.BAD_DATA}")
                log_.warn("Bad parse on age: $cellContent")
            }
        }
        // Empty, throw it in the majority and log it
        else {
            row.add("${Finals.BAD_DATA}")
            log_.warn("Age empty")
        }
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
        Looking for the largest size available
     */
    private fun size(
        row: MutableList<String>, header: MutableList<String>, cellContent: String) {
        header.add("max_dimension_mm")
        var isMM = false
        var isCM = false
        val lower = cellContent.toLowerCase()
        if(lower.contains("mm")) isMM = true
        if(lower.contains("cm")) isCM = true

        val stripped = lower.replace("^\\D+".toRegex(), "").replace("\\D+$", "")
        // Look for the word "core" and if it exist, drop all content to the left of
        // it, it seems some people like to write "5 cores...the important shit"
        val coreGone = stripped.substringAfter("core")
        val fragmentGone = coreGone.substringAfter("fragment")
        val middled = fragmentGone.replace("[a-z]".toRegex(), " ")
        val nocomma = middled.replace(",", "")
        val trimmed = nocomma.trim()
        val splits = trimmed.split(" ")

        // Try parsing whatever is in splits, if some number exists, then great, this
        // can be done better some other time
        // TODO: better, lol
        var max = Finals.BAD_DATA.toDouble()
        for(num in splits) {
            // If it returns null, bad parse, contine the loop
            val parsed = NumUtils.getDoubleFromStr(num) ?: continue
            // Got a valid number, try comparing it, if greater then reset max
            if(parsed > max) max = parsed
        }

        // Data wasn't labeled with CM or MM, unclear on what it is, don't use it
        if(!isMM && !isCM) {
            row.add(Finals.BAD_DATA.toString())
            return
        }

        // If it's in cm, convert to mm, otherwise just put the mm in
        if(isCM) max *= 10
        row.add(max.toString())
    }

    /*
        Determining if stain is positive based on overall % positive nuclei
     */
    private fun stainStatus(
        row: MutableList<String>, header: MutableList<String>, cellContent: String,
        stainName: String, threshold: Double) {
        val headerName = "${stainName}_binary_pos"
        header.add(headerName)

        if(cellContent.isEmpty()) {
            row.add(Finals.BAD_DATA.toString())
            return
        }

        val parsed = NumUtils.getDoubleFromStr(cellContent)
        if(parsed == null) {
            row.add(Finals.BAD_DATA.toString())
            return
        }

        if(parsed >= threshold)
            row.add("1")
        else
            row.add("0")
    }

    /*
        Based on code provided by Mark Zarella to clean up the mitotic scores
     */
    private fun mitoticFigure(
        row: MutableList<String>, header: MutableList<String>, cellContent: String) {
        header.add("mitotic_figure")
        // Only matters for IG14 and up
        // That's the first check
        // Best I can tell, the data has already been transformed to deal with this
        // inconsistency so I'm just going to clean it up
        if(cellContent.isEmpty()) {
            row.add("${Finals.BAD_DATA}")
            return
        }
        // validNumber deals with checking for a valid number, if not it returns
        // Finals.BAD_DATA
        row.add(validNumber(cellContent))
    }

    /*
        If it's a valid num, set it, else set Finals.BAD_DATA
     */
    private fun validNumber(content: String): String {
        return if(NumUtils.getLongFromStr(content) == null) Finals.BAD_DATA.toString()
        else content
    }

    /*
        Just column cleanup
     */
    private fun lobularExtensions(
        row: MutableList<String>, header: MutableList<String>, cellContent: String) {
        header.add("lobular_extension_present")

        if(cellContent.isEmpty()) {
            row.add("${Finals.BAD_DATA}")
            return
        }

        val lobular = cellContent.toLowerCase()
        var contains = false
        if(lobular.contains("present")) contains = true
        if(contains) row.add("1") else row.add("0")
    }

    /*
        Just column cleanup
     */
    private fun pagetoidSpread(
        row: MutableList<String>, header: MutableList<String>, cellContent: String) {
        header.add("pagetoid_spread_present")

        if(cellContent.isEmpty()) {
            row.add("${Finals.BAD_DATA}")
            return
        }

        val pagetoid = cellContent.toLowerCase()
        var contains = false
        if(pagetoid.contains("present")) contains = true
        if(contains) row.add("1") else row.add("0")
    }

    /*
        Just column cleanup
     */
    private fun perineurealInvasion(
        row: MutableList<String>, header: MutableList<String>, cellContent: String) {
        header.add("perineureal_invasion_present")

        if(cellContent.isEmpty()) {
            row.add("${Finals.BAD_DATA}")
            return
        }

        val peri = cellContent.toLowerCase()
        var contains = false
        if(peri.contains("present")) contains = true
        if(contains) row.add("1") else row.add("0")
    }

    /*
        Just column cleanup
     */
    private fun calcifications(
        row: MutableList<String>, header: MutableList<String>, cellContent: String) {
        header.add("calcifications")

        if(cellContent.isEmpty()) {
            row.add("${Finals.BAD_DATA}")
            return
        }

        val califications = cellContent.toLowerCase()
        var present = false
        var dcis = false
        if(califications.contains("present")) present = true
        if(califications.contains("dcis")) dcis = true
        if(califications.contains("ductal")) dcis = true
        if(present && dcis) row.add("2")
        else if(present && !dcis) row.add("1")
        else if(!present && dcis) row.add("-1")
        else if(!present && !dcis) row.add("0")
    }



    /*
        Looking for "absent" in the posMargin col
     */
    private fun posMargin(
        row: MutableList<String>, header: MutableList<String>, cellContent: String) {
        header.add("pos_margin_present")

        // Check if it's empty first
        if(cellContent.isEmpty()) {
            row.add("${Finals.BAD_DATA}")
            return
        }

        val lower = cellContent.toLowerCase()
        var contains = false
        if(lower.contains("pos")) contains = true
        if(lower.contains("pres")) contains = true
        if(lower.contains("dcis")) contains = true
        if(contains) row.add("1") else row.add("0")
    }

    /*
        Looking for "present" in the angio column
     */
    private fun angioLymphaticInvasion(
        row: MutableList<String>, header: MutableList<String>, cellContent: String) {
        header.add("angio_lymphatic_invasion_present")
        val lower = cellContent.toLowerCase()
        var contains = false
        var inderterminate = false
        if(lower.contains("pres")) contains = true
        if(lower.contains("minate")) inderterminate = true
        when {
            contains -> row.add("2")
            inderterminate -> row.add("1")
            else -> row.add("0")
        }
    }

    /*
        Tubule formation has some percents associated with it which aren't helpful to me
        with the basic parsing the agents are setup to do, just grab the leading number
     */
    private fun tubuleFormation(
        row: MutableList<String>, header: MutableList<String>, cellContent: String) {
        header.add("tubule_formation")
        if(cellContent.isEmpty()) {
            row.add("${Finals.BAD_DATA}")
            return
        }

        val tubule = cellContent.toLowerCase()
        val isValid = NumUtils.getLongFromStr(tubule.toCharArray()[0].toString())
        if(isValid == null) {
            row.add("${Finals.BAD_DATA}")
            return
        }
        row.add("$isValid")
    }


    /*
        Looking for the pT stage number, only the number
     */
    private fun pT(
        row: MutableList<String>, header: MutableList<String>, cellContent: String) {
        header.add("pT_numeric")

        val num = parseCharNum(cellContent)

        // Empty or bad parse
        if(num == Finals.BAD_DATA) {
            row.add("${Finals.BAD_DATA}")
            return
        }

        // Make sure num falls between 0 and 5
        logic_.require(num in 0..5, "Impossible pT value")

        row.add(num.toString())
    }

    /*
        Looking for the pN stage number, only the number. This will be used in the
        future for another column.
        @returns the pN stage number or Finals.BAD_DATA if empty
     */
    private fun pN(
        row: MutableList<String>, header: MutableList<String>, cellContent: String)
        : Int
    {
        // Add the header
        header.add("pN_numeric")

        val num = parseCharNum(cellContent)
        // Empty or a bad parse
        if(num == Finals.BAD_DATA) {
            row.add("${Finals.BAD_DATA}")
            return num
        }

        // Make sure the value makes sense
        logic_.require(num in 0..4, "Impossible pN value")

        // Header alrady added, record the value and then return for future use
        row.add(num.toString())

        return num
    }

    private fun parseCharNum(input: String): Int {
        // Nothing here
        if(input.isEmpty()) return Finals.BAD_DATA

        // For each character in the string, look for a number
        var num: Long = Finals.BAD_DATA.toLong()
        for(ch in input) {
            // If null, continue the loop, means bad parse
            num = NumUtils.getLongFromStr(ch.toString()) ?: continue

            // Should have a valid number at this point
        }
        return num.toInt()
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
