package edu.antevortadb.patient

import edu.antevortadb.configs.RawDataLocator
import javalibs.CSVExtractor
import javalibs.Logic
import javalibs.TSL
import javalibs.Timer
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVPrinter
import org.apache.commons.csv.CSVRecord
import java.nio.file.Files
import java.nio.file.Paths

class BCParser {
    val inputCSV = RawDataLocator.bcOriginalCSVAbsolutePath()
    val outputCSV = RawDataLocator.bcCSVAbsolutePath()
    val log_ = TSL.get()
    val logic_ = Logic.get()
    val refID = "Reference ID"

    // All of the headers from the CSV file in the order they appear in the file
    lateinit var headersInOrder: List<String>
    // Intermediate step, holds a map of all records where the patient ID points to all
    // records for that patient. This step only lasts until the stain data can be
    // turned into the proper column format I need
    lateinit var fullRecordMap: Map<String, List<CSVRecord>>

    val standardFeatures = mutableListOf<String>(
        "Age",
        "Gender",
        refID,
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

    fun parseData() {

        // Add all of the features together, standard and stain
        val allFeatures = this.standardFeatures
        allFeatures.addAll(this.stainFeatures)

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

        // Order the headers from the CSV file, as they appear in the file
        this.headersInOrder = orderCSVHeaders(parser.headerMap)
        logic_.require(this.headersInOrder.isNotEmpty(), "Failed to order headers")

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
        log_.info("patientRecordMap size: ${patientRecordMap.size}")
        val pat1 = patientRecordMap["IG14-23"]
        val row1 = pat1!![1]
        for(col in 0 until row1.size())
            log_.info(row1[col])






        /*****
         *
         * FIXME: Staining should be thesholded instead of linear, need to calculate
         * FIXME: H-scores for the stains, probably ignore BCL-2 or now
         */
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
























