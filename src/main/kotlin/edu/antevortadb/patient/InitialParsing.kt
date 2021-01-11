package edu.antevortadb.patient

import javalibs.TSL

fun main(args: Array<String>) {
    val log_ = TSL.get()

    val parsing = BCParser()
    parsing.parseData()
   // parsing.quickTest()

    log_.shutDown()
}