package edu.antevortadb.patient.stain

// Allow inheritance
open class BaseStain {
    protected val abbrev: String
    protected val fullName: String

    constructor(shortName: String, fullName: String){
        this.abbrev = shortName
        this.fullName = fullName
    }
}