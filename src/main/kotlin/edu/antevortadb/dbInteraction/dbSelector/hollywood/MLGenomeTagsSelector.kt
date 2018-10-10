/*
 * Copyright (c) 2018 Sean Grimes. All Rights Reserved.
 * License: MIT
 */

package edu.antevortadb.dbInteraction.dbSelector.hollywood

import edu.antevortadb.configs.DBLocator
import edu.antevortadb.configs.Finals
import edu.antevortadb.dbInteraction.columnsAndKeys.MovielensGenomeTags
import edu.antevortadb.dbInteraction.dbSelector.DBSelector
import edu.antevortadb.dbInteraction.dbSelector.RSMapper
import edu.antevortadb.dbInteraction.dbSelector.SelectionWorker
import edu.antevortadb.dbInteraction.dbSelector.Selector
import java.util.concurrent.ConcurrentHashMap

val tagMemoMap = ConcurrentHashMap<String, Int>()

class MLGenomeTagsSelector: Selector() {
    private val tagcol = "tag"
    private val tagidcol = "tagid"

    init {
        this.tableName = Finals.ML_GENOME_TAGS_TABLE
        this.listOfColumns = MovielensGenomeTags.columnNames()
    }

    override fun generalSelection(SQLStatement: String): List<RSMapper> {
        val dbs = DBLocator.hollywoodAbsolutePaths()
        verifyDBsExist(dbs)

        val workers = ArrayList<SelectionWorker>()
        for(i in 0 until dbs.size)
            workers.add(SelectionWorker(dbs[i], SQLStatement, MLGenomeTagsSetMapper()))
        return genericSelect(workers, SQLStatement)
    }

    /**
     * @return the tagID if it's found from the tagText, else -1
     */
    fun getTagIDFromTagText(tagText: String): Int {
        val memoResult = tagMemoMap[tagText]
        if(memoResult != null)
            return memoResult

        // Need to clean up the string
        val tag = tagText.toLowerCase().replace("'", "").replace("\"", "")

        val dbsql = DBSelector()
                .column(tagidcol)
                .from(this.tableName)
                .where("$tagcol = '$tag'")

        val res = this.generalSelection(dbsql.sql())

        // Oh, how disappointing.
        if(res.isEmpty()){
            return -1
        }

        // Instead of returning -1 here, just return the first result. Let the code continue
        if(res.size > 1)
            logger.err("${res.size} results for '$tagText'. Returning the last added result.")

        // Get the tagid value from the RSMapper object
        val tagid = res[0].getInt(tagidcol)

        // Add the result to the map for future look-ups
        if(!tagMemoMap.contains(tagText)) tagMemoMap[tagText] = tagid

        return tagid
    }
}
