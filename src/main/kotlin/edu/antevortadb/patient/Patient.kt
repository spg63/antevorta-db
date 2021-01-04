package edu.antevortadb.patient

import javalibs.Logic
import javalibs.TSL

enum class Gender {
    MALE,
    FEMAILE,
    OTHER,
    UNKNOWN
}

abstract class Patient {
    protected val log_ = TSL.get()
    protected val logic_ = Logic.get()

    protected var ID: String = ""
    protected var age: Int = 0
    protected var bioGen: Gender = Gender.UNKNOWN

    abstract fun parse(cols: List<String>): Boolean
}