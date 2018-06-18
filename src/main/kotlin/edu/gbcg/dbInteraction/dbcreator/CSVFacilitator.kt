/*
 * Copyright (c) 2018 Sean Grimes. All Rights Reserved.
 * License: MIT
 */

package edu.gbcg.dbInteraction.dbcreator

import org.apache.commons.csv.CSVFormat

abstract class CSVFacilitator: Facilitator {
    protected val parseFormat = CSVFormat.DEFAULT!!
    protected val parser = null

    constructor(): super()

    override fun pushDataIntoDBs() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun letWorkersFly(lines: List<List<String>>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}