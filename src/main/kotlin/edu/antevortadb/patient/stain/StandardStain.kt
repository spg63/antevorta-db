package edu.antevortadb.patient.stain

class StandardStain: BaseStain {
    protected var percentPosNuclei: Double = 0.0
    protected var intensityScore: Int = 0
    protected var percent3PlusNuclei: Double = 0.0
    protected var percent2PlusNuclei: Double = 0.0
    protected var percent1PlusNuclei: Double = 0.0
    protected var percent0PlusNuclei: Double = 0.0
    protected var avgPosIntensity: Double = 0.0
    protected var avgNegIntensity: Double = 0.0
    protected var plus3CellCount: Int = 0
    protected var plus2CellCount: Int = 0
    protected var plus1CellCount: Int = 0
    protected var plus0CellCount: Int = 0
    protected var totalCells: Int = 0
    protected var totalNuclei: Int = 0
    protected var avgNuclearRGBIntensity: Double = 0.0
    protected var avgNuclearSizePixels: Double = 0.0
    protected var avgNuclearSizeMicometers: Double = 0.0
    protected var areaOfAnalysisPixels: Double = 0.0
    // Area...so squared micrometers
    protected var areaOfAnalysisMicrometers: Double = 0.0


    constructor(shortName: String, fullName: String): super(shortName, fullName) { }
}