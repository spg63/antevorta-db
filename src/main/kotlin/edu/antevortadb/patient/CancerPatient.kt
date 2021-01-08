package edu.antevortadb.patient

enum class StructuralFeature {
    PRESENT,
    PRESENT_DCIS,
    ABSENT,
    NOT_IDENTIFIED,
    INDETERMINATE,
    UNKNOWN
}

/*
 * NOTE: In most cases, negative value indicates no data or a problem with the data
 */
abstract class CancerPatient: Patient {

    // NOTE: Negative dimensions indicate no data available
    protected var tumorMaxLen: Double = -1.0
    protected var tumorLenX: Double = -1.0
    protected var tumorLenY: Double = -1.0
    protected var tumorLenZ: Double = -1.0
    protected var tumorVolume: Double = -1.0
    protected var tumorVolAvailable: Boolean = false

    // Short, 1-5 word description of tumor location
    protected var tumorBodySite: String = ""

    // Location of biopsy, perhaps additional description
    protected var specimenType: String = ""

    // Initial diagnosis of edu.antevortadb.patient
    protected var diagnosis: String = ""

    // Staged for tumor, lymph, and metastisis
    // Raw is string data, numeric is trying to parse a state number from data
    protected var pTStageRaw: String = ""
    protected var pTNumeric: Int = -1
    protected var pNStageRaw: String = ""
    protected var pNNumeric: Int = -1
    protected var pMStageRaw: String = ""
    protected var PMNumeric: Int = -1

    // -1, 1, 2, 3
    protected var histologicGrade: Int = -1
    protected var nuclearGrade: Int = -1
    // FIXME: How was this parsed from Mark?
    // Mitotic count, 1, 2, 3 or some number that translates to a score/grade
    protected var mitoticCount: Int = -1

    // Checking for spread from the primary tumor
    // FIXME: This cannot be used for predictions, right?
    protected var sentinelNodesTested: Int = -1
    protected var sentinelNodesPositive: Int = -1
    protected var axillaryNodesTested: Int = -1
    protected var axillaryNodesPositive: Int = -1

    constructor(): super() { }
}

























