package edu.antevortadb.patient

import edu.antevortadb.configs.RawDataLocator
import javalibs.CSVExtractor
import javalibs.TSL
import javalibs.Timer
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVRecord
import java.nio.file.Files
import java.nio.file.Paths

class BCParser {
    val inputCSV = RawDataLocator.bcOriginalCSVAbsolutePath()
    val outputCSV = RawDataLocator.bcCSVAbsolutePath()
    val log_ = TSL.get()

    val standardFeatures = mutableListOf<String>(
        "Age",
        "Gender",
        "Reference ID",
        "Body Site Writein",
        "Specimen Type",
        "Diagnosis",
        "Size",
        "Pos Margin",
        "Angio Lymphatic Invasion",
        "pT Stage",
        "pN Stage",
        "pM Stage",
        "Histologic Grade",
        "Breast: Tubule Formation",
        "Breast: Nuclear Grade",
        "Breast: Mitotic Figure",
        "Breast: Lobular Extension",
        "Breast: Pagetoid Spread",
        "Breast: Perineureal Invasion",
        "Breast: Calcifications",
        "Breast: Sentinel nodes",
        "Breast: Sentinel nodes positive",
        "Breast: Axillary nodes",
        "Breast: Axillary nodes positive",
        "File Location"
    )

    val stainFeatures = mutableListOf<String>(
        "Stain",// The stain name, sometimes a full name but mostly the short name
        "AA",   // [String] Her2 Score or Percent Positive Nuclei
        "AB",   // [Double] Values for the above
        "AC",   // [String] (3+) Percent Cells or Intensity Score
        "AD",   // [Double] Values for the above
        "AE",   // [String] (2+) Percent Cells or (3+) Percent Nuclei
        "AF",   // [Double] Values for the above
        "AG",   // [String] (1+) Percent Cells) or (2+) Percent Nuclei
        "AH",   // [Double] Values for the above
        "AI",   // [String] (0+) Percent Cells or (1+) Percent Nuclei
        "AJ",   // [Double] Values for the above
        "AK",   // [String] Percent Complete or (0+) Percent Nuclei
        "AL",   // [Double] Values for the above
        "AM",   // [String] Membrane Intensity (Average) or Average Positive Intensity
        "AN",   // [Double] Values for the above
        "AO",   // [String] (3+) Cells or Average Negative Intensity
        "AP",   // [Int / Double] values for the above {{ NEED THE LOGIC FOR THIS }}
        "AQ",   // String] (2+) Cells or (3+) Nuclei
        "AR",   // [Int] Count for the above
        "AS",   // [String] (1+) Cells or (2+) Nuclei
        "AT",   // [Int] Count for the above
        "AU",   // [String] (0+) Cells or (1+) Nuclei
        "AV",   // [Int] Count for the above
        "AW",   // [String] Cells (Total) or (0+) Nuclei
        "AX",   // [Int] Count for the above
        "AY",   // [String] Complete Cells or Total Nuclei
        "AZ",   // [Int] Count for the above
        "BA",   // [String] Average Nuclear RGB Intensity {{{ END HER2 Possible }}}
        "BB",   // [Double] Value for the above
        "BC",   // [String] Average Nuclear Size (Pixels)
        "BD",   // [Double] Value for the above
        "BE",   // [String] Average Nuclear Size (um^2)
        "BF",   // [Double] Value for the above
        "BG",   // [String] Area of Analysis (Pixels)
        "BH",   // [Double] Value for the above
        "BI",   // [String] Area of Analysis (mm^2)
        "BJ"    // [Double] Value for the above
    )


    fun readingTest() {
        val csvParser = CSVParser(
            Files.newBufferedReader(Paths.get(inputCSV)),
            CSVFormat.DEFAULT
                .withHeader()
                .withIgnoreHeaderCase()
                .withTrim()
        );

        log_.info("Parsing started")
        val timer = Timer()
        timer.startTimer()
        val rawHeaders: Map<String, Int> = csvParser.headerMap
        val records = csvParser.records
        csvParser.close()
        timer.stopTimer()
        log_.info("Parsing complete")
        log_.info("Reading took ${timer.milliseconds()} ms.")

        log_.info("Num records: ${records.size}")
        log_.info("1st record: ")
        log_.info(records.get(0))
        log_.info("2nd record: ")
        log_.info(records.get(1).get(8))

    }

    fun parseData() {

        // Add all of the features together, standard and stain
        val allFeatures = this.standardFeatures
        allFeatures.addAll(this.stainFeatures)

        // Read the full records from the above generated file
        val records = getCSVRecords(this.inputCSV)
        log_.info("Total num records: ${records.size}")

        /*
           Now build a map which is HashMap<String, List<CSVRecord>>
           Where
               - The String is the Reference ID for a patient
               - List<CSVRecord> is all records associated with that Reference ID
        */
    records
        CSVExtractor.writeCSVRecord(this.outputCSV, records[0], null)




        // Generate a CSV file with just the standard, non-stain columns
//        val extractor = CSVExtractor(this.inputCSV, this.outputCSV, allFeatures)
//        val path = extractor.writeCSV()
//        log_.info("Output csv at $path")



        /*
        // Put the records in a map based on Reference ID to get a single record per
        // Reference ID which should mean a single record per patient
        val patientMap = HashMap<String, CSVRecord>()
        for(rec in records)
            patientMap[rec.get("Reference ID")] = rec

        log_.info("Num elements in patientMap: ${patientMap.size}")

        var i = 0
        for((key, value) in patientMap){
            log_.info("Key: $key")
            log_.info("Value: $value")
            ++i
            if(i >= 5) break
        }
        */
        /*****
         * MAP OF LIST OF RECORDS WHERE THE LIST IS ALL THE SAME PAITENT AND THE MAP
         * ENTRY IS THE REFERENCE ID WHICH RETURNS THE LIST OF RECORDS FOR THE PATIENT.
         * ..FROM THIS POINT I SHOULD THEN BE ABLE TO PARSE THROUGH THE STAIN SHIT AND
         * DEAL WITH THAT BULLSHIT.
         *
         * FIXME: Staining should be thesholded instead of linear, need to calculate
         * FIXME: H-scores for the stains, probably ignore BCL-2 or now
         */
    }

    private fun getCSVRecords(path: String): List<CSVRecord> {
        val parser = CSVParser(
            Files.newBufferedReader(Paths.get(path)),
            CSVFormat.DEFAULT
                .withHeader()
                .withIgnoreHeaderCase()
                .withTrim()
        )

        return parser.records
    }

    private fun orderCSVHeaders(oooHeaders: Map<String, Int>,
                                headersInOrder: List<String>) {
        val orderedHeadersMap = HashMap<Int?, String>()
        var numInputCols = 0
        for(col in oooHeaders.keys){
            orderedHeadersMap[oooHeaders[col]] = col
            ++numInputCols
        }

        // Put all the headers in order
        val headersInOrder = ArrayList<String?>()
        for(i in 0 until numInputCols)
            headersInOrder[i] = orderedHeadersMap[i]

        // Order them as they appear in the CSV
        var cnt = 0
        for(col in headersInOrder) {
            if()
        }

    }
}
























