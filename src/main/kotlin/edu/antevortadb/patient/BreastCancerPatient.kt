package edu.antevortadb.patient

class BreastCancerPatient: CancerPatient {

    // Present or not, or unknown
    protected var angioLymphInvasion = StructuralFeature.UNKNOWN
    // 1 (> 75%), 2 (10-75%), 3 (0-10%)
    protected var tubuleFormation: Int = -1

    // Physiological features
    protected var lobularExtension = StructuralFeature.UNKNOWN
    protected var pagetoidSpread = StructuralFeature.UNKNOWN
    protected var perineurealInvasion = StructuralFeature.UNKNOWN
    protected var calcifications = StructuralFeature.UNKNOWN

    // Various stains, which may not be specific to breast cancer but are, non-the-less,
    // being stored in the breast cancer class to reduce confusion with other stains


    override fun parsePatientData(cols: List<String>): Boolean {
        TODO("Not yet implemented")
    }
}