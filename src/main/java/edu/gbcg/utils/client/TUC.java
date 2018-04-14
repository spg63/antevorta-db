/*
 * Copyright (c) 2018 Sean Grimes. All Rights Reserved.
 * License: MIT
 */

package edu.gbcg.utils.client;


/**
 * @author Andrew W.E. McDonald
 *
 * Time Unit Converter. But no one wants to type that out, especially with descriptive method names.
 *
 * This takes a value, passed to 'convert' and converts it from 'from' units, to 'to' units,
 * as defined in the TU enum.
 *
 * It works using two different functions -- one to go from a larger time unit to a smaller time unit,
 * and one to go from a smaller one to a larger one.
 *
 * Each function has a switch statement with a case that gets triggered on the 'from' time unit,
 * and either returns the value (if 'to' == 'from'), or starts the conversion process (via a flag).
 * From that point on, all following cases are triggered, and the conversion is done, step-by-step,
 * until the case with TU.id == 'to' (the 'to' time unit) has been triggered.
 * At that point, one last conversion is done, and the converted value is returned.
 *
 * It basically works like 'conversion factors' from high school chem.
 *
 */
public class TUC {


    public static final int usPerMs = 1000;
    public static final int msPerSec = 1000;
    public static final int secPerMin = 60;
    public static final int minPerQh = 15;
    public static final int qhPerHh = 2;
    public static final int hhPerHr = 2;
    public static final int hrPerDay = 24;
    public static final int dayPerWeek = 7;

    public static double convert(long value, TU from, TU to){
        if (from.id < to.id){
            return doSmallToLargeConversion(value, from, to);
        }
        else {
            return doLargeToSmallConversion(value, from, to);
        }
    }

    public static double doLargeToSmallConversion(double value, TU from, TU to){
        double converted = value;
        boolean conversionStarted = false;
        boolean conversionComplete = false;
        // then we want to do the week to day conversion
        // (if not, we either return the value untouched if conversionComplete = true, or set conversionStarted = true)
        switch(from){
            case WEEK:
                if (to.id == TU.WEEK.id) return converted;
                conversionStarted = true;
            case DAY:
                if (to.id == TU.DAY.id) conversionComplete = true;
                if (conversionStarted) converted *= dayPerWeek;
                if (conversionComplete) return converted;
                conversionStarted = true;
             case HR:
                if (to.id == TU.HR.id) conversionComplete = true;
                if (conversionStarted) converted *= hrPerDay;
                if (conversionComplete) return converted;
                conversionStarted = true;
            case HH:
                if (to.id == TU.HH.id) conversionComplete = true;
                if (conversionStarted) converted *= hhPerHr;
                if (conversionComplete) return converted;
                conversionStarted = true;
            case QH:
                if (to.id == TU.QH.id) conversionComplete = true;
                if (conversionStarted) converted *= qhPerHh;
                if (conversionComplete) return converted;
                conversionStarted = true;
            case MIN:
                if (to.id == TU.MIN.id) conversionComplete = true;
                if (conversionStarted) converted *= minPerQh;
                if (conversionComplete) return converted;
                conversionStarted = true;
            case SEC:
                if (to.id == TU.SEC.id) conversionComplete = true;
                if (conversionStarted) converted *= secPerMin;
                if (conversionComplete) return converted;
                conversionStarted = true;
            case MS:
                if (to.id == TU.MS.id) conversionComplete = true;
                if (conversionStarted) converted *= msPerSec;
                if (conversionComplete) return converted;
                conversionStarted = true;
            case US:
                if (to.id == TU.US.id) conversionComplete = true;
                if (conversionStarted) converted *= usPerMs;
                if (conversionComplete) return converted;
            default:
                return -1;

        }
    }

    public static double doSmallToLargeConversion(double value, TU from, TU to){

        double converted = value;
        boolean conversionStarted = false;
        boolean conversionComplete = false;
        switch(from){
            case US:
                if (to.id == TU.US.id) return converted;
                conversionStarted = true;
            case MS:
                if (to.id == TU.MS.id) conversionComplete = true;
                if (conversionStarted) converted /= usPerMs;
                if (conversionComplete) return converted;
                conversionStarted = true;
            case SEC:
                if (to.id == TU.SEC.id) conversionComplete = true;
                if (conversionStarted) converted /= msPerSec;
                if (conversionComplete) return converted;
                conversionStarted = true;
            case MIN:
                if (to.id == TU.MIN.id) conversionComplete = true;
                if (conversionStarted) converted /= secPerMin;
                if (conversionComplete) return converted;
                conversionStarted = true;
            case QH:
                if (to.id == TU.QH.id) conversionComplete = true;
                if (conversionStarted) converted /= minPerQh;
                if (conversionComplete) return converted;
                conversionStarted = true;
            case HH:
                if (to.id == TU.HH.id) conversionComplete = true;
                if (conversionStarted) converted /= qhPerHh;
                if (conversionComplete) return converted;
                conversionStarted = true;
            case HR:
                if (to.id == TU.HR.id) conversionComplete = true;
                if (conversionStarted) converted /= hhPerHr;
                if (conversionComplete) return converted;
                conversionStarted = true;
            case DAY:
                if (to.id == TU.DAY.id) conversionComplete = true;
                if (conversionStarted) converted /= hrPerDay;
                if (conversionComplete) return converted;
                conversionStarted = true;
            case WEEK:
                if (to.id == TU.WEEK.id) conversionComplete = true;
                if (conversionStarted) converted /= dayPerWeek;
                if (conversionComplete) return converted;
            default:
                return -1;

        }
    }

    public static void main(String[] args){

        double res = TUC.convert(1, TU.HR, TU.DAY);
        System.out.printf("TUC: 1 hr == %f days\n",res);

        res = TUC.convert(1, TU.DAY, TU.HR);
        System.out.printf("TUC: 1 day == %.2f hours\n",res);

        res = TUC.convert(1, TU.DAY, TU.MS);
        System.out.printf("TUC: 1 day == %.2f ms\n",res);

        res = TUC.convert(1, TU.DAY, TU.SEC);
        System.out.printf("TUC: 1 day == %.2f sec\n",res);

        res = TUC.convert(5, TU.MIN, TU.MS);
        System.out.printf("TUC: 5 min == %.2f ms\n",res);

        res = TUC.convert(10, TU.MIN, TU.SEC);
        System.out.printf("TUC: 10 min == %.2f sec\n",res);



    }

}


