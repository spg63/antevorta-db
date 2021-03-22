package edu.antevortadb.patient.parsing

import edu.antevortadb.configs.RawDataLocator
import javalibs.FileUtils
import javalibs.Logic
import javalibs.TSL
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVPrinter
import org.apache.commons.csv.CSVRecord
import java.nio.file.Files
import java.nio.file.Paths

fun main(args: Array<String>) {
    val log_ = TSL.get()

    val parsing = BCParser()
    parsing.parseData()

    log_.shutDown()
}

class BCParser {
    companion object {
        val standardFeatures = listOf<String>(
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

        val stainFeatures = listOf<String>(
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

        // Columns to be create for:
        //  ER, PR, KI67, P53
        val colsForStandardStain = listOf<String>(
            "percentPosNuclei",
            "intensityScore",
            "3+percentNuclei",
            "2+percentNuclei",
            "1+percentNuclei",
            "0+percentNuclei",
            "avgPosIntensity",
            "avgNegIntensity",
            "3+nuclei",
            "2+nuclei",
            "1+nuclei",
            "0+nuclei",
            "totalNuclei",
            "avgNuclearRGBIntensity",
            "avgNuclearSizePixels",
            "avgNuclearSizeMicroM",
            "areaOfAnalysisPixels",
            "areaOfAnalysisMilliM"
        )

        // NOTE: Access must be in parallel with the above colsForStandardStain
        val standardStainCols = listOf<String>(
            "AB",
            "AD",
            "AF",
            "AH",
            "AJ",
            "AL",
            "AN",
            "AP",
            "AR",
            "AT",
            "AV",
            "AX",
            "AZ",
            "BB",
            "BD",
            "BF",
            "BH",
            "BJ"
        )

        val colsForHER2Stain = listOf<String>(
            "her2Score",
            "3+percentCells",
            "2+percentCells",
            "1+percentCells",
            "0+percentCells",
            "percentComplete",
            "membraneIntensityAvg",
            "3+cells",
            "2+cells",
            "1+cells",
            "0+cells",
            "totalCells",
            "completeCells"
        )

        // NOTE: Access must be in parallel with the above colsForHER2Stain
        val her2StainCols = listOf<String>(
            "AB",
            "AD",
            "AF",
            "AH",
            "AJ",
            "AL",
            "AN",
            "AP",
            "AR",
            "AT",
            "AV",
            "AX",
            "AZ"
        )

        val colsForBCL2Stain = listOf<String>(
            "hScore_1",
            "avgPosIntensity_1",
            "percentPosCells_1",
            "hScore_2",
            "avgPosIntensity_2",
            "percentPosCells_2",
            "percent0+",
            "percent1+_1",
            "percent2+_1",
            "percent3+_1",
            "percent0+_1",
            "percent1+_2",
            "percent2+_2",
            "percent3+_2",
            "numCells",
            "percentColocalized",
            "areaOfAnalysis_mm",
            "cytoplasmArea_mm",
            "nuclearArea_mm"
        )

        val bcl2StainCols = listOf<String>(
            "AB",
            "AD",
            "AF",
            "AH",
            "AJ",
            "AL",
            "AN",
            "AP",
            "AR",
            "AT",
            "AV",
            "AX",
            "AZ",
            "BB",
            "BD",
            "BF",
            "BH",
            "BJ",
            "BL"
        )

        val standardStainName = listOf<String>(
            "ER",
            "PR",
            "P53",
            "KI67"
        )

        val her2StainName = "HER2"
        val bcl2StainName = "BCL2"

        val usedStains = listOf<String>(
            "ER",
            "PR",
            "P53",
            "KI67",
            "HER2",         // FIXME: TEMP SKIPPING
            "BCL2"         // FIXME: TEMP SKIPPING
        )

        val ignoredStains = listOf<String>(
            "AR",           // Not sure what this is...
            "NEGCTRL",      // Don't care about the control
            "",             // Empty stains
            "CD10",         // No apparent values
            "CD117",        // No apparent values
            "CK5/6",        // No apparent values
            "D240",         // No apparent values
            "P63/MYOSIN",   // No apparent values
            "CK14",         // No apparent values
            "P53GLANDS",    // No apparent values
            "P53SURFACE",   // No apparent values
            "H&E"           // 7 total samples, not enough to include
        )
    }   // end companion

    val inputCSV: String
    val outputCSV: String
    val log_: TSL
    val logic_: Logic
    val refID: String
    var currentMaxHeaderValue = 0
    lateinit var csvHeaders: MutableMap<String, Int>
    val allHeaders: MutableList<String>
    val standardStainNamesToAccessHeadersMap: MutableMap<String, String>


    // All of the headers from the CSV file in the order they appear in the file
    lateinit var headersInOrder: List<String>
    // Intermediate step, holds a map of all records where the patient ID points to all
    // records for that patient. This step only lasts until the stain data can be
    // turned into the proper column format I need
    lateinit var fullRecordMap: Map<String, List<CSVRecord>>

    init {
        this.inputCSV = RawDataLocator.bcOriginalCSVAbsolutePath()
        this.outputCSV = RawDataLocator.bcCSVAbsolutePath()
        this.log_ = TSL.get()
        this.logic_ = Logic.get()
        this.refID = "Reference ID"
        this.allHeaders = buildHeaderMapOutputCSV()
        this.standardStainNamesToAccessHeadersMap = buildHeaderMapFromOrigCSV()
    }

    fun parseData() {

        // Add all of the features together, standard and stain
        val allFeatures = BCParser.standardFeatures.toMutableList()
        allFeatures.addAll(BCParser.stainFeatures)

        // Create the parser, read the file into memory
        val parser = CSVParser(
            Files.newBufferedReader(Paths.get(this.inputCSV)),
            CSVFormat.DEFAULT
                .withHeader()
                .withIgnoreHeaderCase()
                .withTrim()
        )

        // Get a reference to all of the CSV records
        val allRecords = parser.records
        logic_.require(allRecords != null && allRecords.size > 0, "Failed to get records")
        log_.info("Total number of records: ${allRecords.size}")

        // Reference to the headers, which will up updated while processing the stains
        this.csvHeaders = parser.headerMap
        // Determine the current max value in the headerMap (i.e. the number of cols in
        // the csv as it sits currently)
        this.currentMaxHeaderValue = this.csvHeaders.values.max()!!

        // Order the headers from the CSV file, as they appear in the file
        this.headersInOrder = orderCSVHeaders(this.csvHeaders)
        logic_.require(this.headersInOrder.isNotEmpty(), "Failed to order headers")

        /////
        //this.csvHeaders["test"] = ++this.currentMaxHeaderValue
        //this.headersInOrder = orderCSVHeaders(this.csvHeaders)
        //for(header in headersInOrder)
        //    log_.info("New headers in order: $header")
        //log_.die()

        /*
           Now build a map which is HashMap<String, List<CSVRecord>>
           Where
               - The String is the Reference ID for a patient
               - List<CSVRecord> is all records associated with that Reference ID
        */
        val patientRecordMap = HashMap<String, MutableList<CSVRecord>>()
        for(record in allRecords){
            val refID = record.get(refID)
            // If the key does not already exist, create the list for this reference ID
            // and add the record to the list
            if(!patientRecordMap.containsKey(refID)){
                patientRecordMap[refID] = ArrayList<CSVRecord>()
                patientRecordMap[refID]!!.add(record)
            }
            // The reference ID exists in the map, add the record to the list
            // associated with that reference ID
            else
                patientRecordMap[refID]!!.add(record)
        }

        /*
            All data for each patient is now associated with the patient. Time to go
            through and organize the stain information into columns.

            NOTE: Not all patients have rows for all stains, so each stain needs to be
            checked for without assuming that information (or even the name of the
            stain) exist for the patient. Futher, there are some patients with two
            entries for a stain, at least for HER2, we're just going to ignore this as
            a possible issue for the time being, looking for each stain exactly once
            and on a first come, first serve basis.
        */

        // List of Reference ID that are stored (i.e. the list of patients) [unordered]
        val listOfPatients = patientRecordMap.keys
        logic_.require(listOfPatients.size == patientRecordMap.size)


        //FIXME val outputWriter = Files.newBufferedWriter(Paths.get(this.outputCSV))
        //FIXME val setOfCreatedColumnHeaders = HashSet<String>()

        // This will be the new list of headers, in the correct order, and starting
        // with the list of standard features (i.e. non-stain features)
        // FIXME var masterHeaderRecord: MutableList<String> = standardFeatures.toMutableList()
        val allCompletedRecords = ArrayList<MutableList<String>>()
        for(patient in listOfPatients) {
            // The list of rows for this specific patient
            val rowsForPatient: MutableList<CSVRecord> = patientRecordMap[patient]!!

            // The complete record, standard and stain features
            // Start with the header list for proper length
            //val completeRecord = allHeaders.toMutableList()
            val completeRecord: MutableList<String> = ArrayList()
            for(i in 0 until allHeaders.size) completeRecord.add("-1")
            // Gather the standard features first, so only go to standardFeature.size
            for(i in 0 until BCParser.standardFeatures.size) {
                // NOTE: Just using the first row, standard features are repeated
                completeRecord[i] = rowsForPatient[0].get(i)
            }

            for(row in rowsForPatient){
                var stain = row.get("Stain")
                // Remove all content inside parenthesis
                stain = stain.replace("\\s*\\([^\\)]*\\)\\s*".toRegex(), "")
                // Remove all dashes
                stain = stain.replace("-", "")
                // Remove all extra white space from the ends
                stain = stain.trim()
                // Remove all extra interior white space (making a note in logs)
                log_.trace("Removing all interior whitespace from stain column")
                stain = stain.replace(" ", "")
                // Convert to lowercase for all remaining characters
                stain = stain.toUpperCase()
                // If the stain is from the ignored list just continue and ignore it
                if(ignoredStains.contains(stain)) continue
                // Forced check to make sure the stain exists in the usedStains list
                logic_.require(usedStains.contains(stain), "Unknown stain: $stain")


                // Deal with the BCL2 stain information
                when {
                    stain.equals("BCL2") -> {
                        log_.info("BCL2")
                        for(col in BCParser.colsForBCL2Stain){
                            val stainHeader = "${stain}_$col"
                            // Find the element number based on the master headers
                            val eleNum = this.allHeaders.indexOf(stainHeader)
                            // Get the existing value for that stain
                            val recordCol = standardStainNamesToAccessHeadersMap[col]
                            val recordVal = row.get(recordCol)
                            // Now that we have a value, add it to the complete record
                            completeRecord[eleNum] = recordVal
                        }
                    }
                    // Now the HER2 information
                    stain.equals("HER2") -> {
                        log_.info("HER2")
                        for(col in BCParser.colsForHER2Stain){
                            val stainHeader = "${stain}_$col"
                            val eleNum = this.allHeaders.indexOf(stainHeader)
                            val recordCol = standardStainNamesToAccessHeadersMap[col]
                            val recordVal = row.get(recordCol)
                            completeRecord[eleNum] = recordVal
                        }
                    }
                    // Else the stain is a regular stain, use standard stain headers
                    else -> {
                        // Loop through the possible stain columns
                        for(col in BCParser.colsForStandardStain){
                            val stainHeader = "${stain}_$col"
                            // Find the element number based on the master headers
                            val eleNum = this.allHeaders.indexOf(stainHeader)
                            // Get the existing value for that stain
                            val recordCol = standardStainNamesToAccessHeadersMap[col]
                            val recordVal = row.get(recordCol)
                            // Now that we have a vaule, add it to the complete record
                            completeRecord[eleNum] = recordVal
                        }
                    }
                }
            }
            allCompletedRecords.add(completeRecord)

        }

        // SYRACUSe, oregon, nc state, lousianna tech, cleveland stat, WSU,

        val path = RawDataLocator.bcCSVAbsolutePath()
        val bw = Files.newBufferedWriter(Paths.get(path))
        val printer = CSVPrinter(bw, CSVFormat.DEFAULT)
        printer.printRecord(this.allHeaders)
        for(patient in allCompletedRecords)
            printer.printRecord(patient)
        printer.flush()
        printer.close()
        log_.die()

        /*****
         *
         * FIXME: Staining should be thesholded instead of linear, need to calculate
         * FIXME: H-scores for the stains, probably ignore BCL-2 or now
         * FIXME: Should go back and remove the broken stain shit after the parsing is
         * FIXME: done
         */
    }

    private fun buildHeaderMapOutputCSV(): MutableList<String> {
        // Starting with the non-stain features
        val stainColNames = BCParser.standardFeatures.toMutableList()

        // Run through the standard stains, and columns for the stains, and add them
        for(stain in BCParser.standardStainName) {
            for(feature in BCParser.colsForStandardStain) {
                val newColName = "${stain}_$feature"
                stainColNames.add(newColName)
            }
        }

        // Run through the features for HER2
        for(feature in BCParser.colsForHER2Stain) {
            val newColName = "${BCParser.her2StainName}_$feature"
            stainColNames.add(newColName)
        }

        // Run through BCL2
        for(feature in BCParser.colsForBCL2Stain) {
            val newColName = "${BCParser.bcl2StainName}_$feature"
            stainColNames.add(newColName)
        }

        val b = StringBuilder()
        for(jawn in stainColNames)
            b.append(jawn).append(",")
        val s = b.toString()
        FileUtils.get().writeNewFile(RawDataLocator.bcDirPath() + "allHeaders.csv", s)

        return stainColNames
    }

    private fun buildHeaderMapFromOrigCSV():MutableMap<String, String> {
        val map = HashMap<String, String>()
        for(i in 0 until BCParser.colsForStandardStain.size)
            map[BCParser.colsForStandardStain[i]] = BCParser.standardStainCols[i]
        for(i in 0 until BCParser.colsForBCL2Stain.size)
            map[BCParser.colsForBCL2Stain[i]] = BCParser.bcl2StainCols[i]
        for(i in 0 until BCParser.colsForHER2Stain.size)
            map[BCParser.colsForHER2Stain[i]] = BCParser.her2StainCols[i]
        return map
    }


    private fun orderCSVHeaders(oooHeaders: Map<String, Int>): List<String> {
        val orderedHeadersMap = HashMap<Int?, String>()
        var numInputCols = 0
        for(col in oooHeaders.keys){
            orderedHeadersMap[oooHeaders[col]] = col
            ++numInputCols
        }

        // Put all the headers in order
        val headersInOrder: MutableList<String> = ArrayList<String>()
        for(i in 0 until numInputCols)
            headersInOrder.add(orderedHeadersMap[i]!!)

        return headersInOrder
    }
}
