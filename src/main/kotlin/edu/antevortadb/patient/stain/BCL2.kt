package edu.antevortadb.patient.stain

class BCL2: BaseStain {
    protected var HScore: Double = 0.0
    protected var avgPositiveIntensity: Double = 0.0
    protected var percentPositiveCells: Double = 0.0
    protected var nuclearAreaMilliMeters: Double = 0.0

    constructor(shortName: String, fullName: String): super(shortName, fullName) { }
}