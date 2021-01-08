package edu.antevortadb.patient.stain

class HER2: BaseStain {
    protected var HER2Score: Int = 0
    protected var percent3Plus: Double = 0.0
    protected var percent2Plus: Double = 0.0
    protected var percent1Plus: Double = 0.0
    protected var percent0Plus: Double = 0.0
    protected var percentComplete: Double = 0.0
    protected var membraneAvgIntensity: Double = 0.0
    protected var plus3CellCount: Int = 0
    protected var plus2CellCount: Int = 0
    protected var plus1CellCount: Int = 0
    protected var plus0CellCount: Int = 0
    protected var totalCells: Int = 0
    protected var completeCells: Int = 0

    constructor(shortName: String, fullName: String): super(shortName, fullName) { }
}