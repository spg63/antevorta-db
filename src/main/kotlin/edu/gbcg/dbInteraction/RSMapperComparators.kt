/*
 * Copyright (c) 2018 Sean Grimes. All Rights Reserved.
 * License: MIT
 */

package edu.gbcg.dbInteraction

import edu.gbcg.dbInteraction.dbSelector.OrderBySelection
import edu.gbcg.dbInteraction.dbSelector.RSMapper
import edu.gbcg.utils.TSL

class RSMapperComparators {
    companion object: Comparator<RSMapper> {

        var columnsWithOrder: OrderBySelection? = null

        override fun compare(rs1: RSMapper, rs2: RSMapper): Int {
            // columns with order *must* be set before this function can work, it's the only way we know how to sort
            // the mappers, by date, name, or other. If this is null, it wasn't set, we'll leave things unsorted by
            // telling the sortWith function using this comparator that all values are equal and we'll log an error
            // message. The user should reset this variable to null when they're done the sorting.
            if(columnsWithOrder == null) {
                TSL.get().err("RSMapperComparators.columnsWithOrder was null, sorting was not completed!")
                return 0
            }




            return 0
        }
    }

    fun compareAscending(rs1: RSMapper, rs2: RSMapper, columnName: String){

    }
}


/*
class CompareObjects {

    companion object : Comparator<MyDate> {

        override fun compare(a: MyDate, b: MyDate): Int = when {
            a.year != b.year -> a.year - b.year
            a.month != b.month -> a.month - b.month
            else -> a.day - b.day
        }
    }
}
*/