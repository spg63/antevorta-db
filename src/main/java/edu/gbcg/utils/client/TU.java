/*
 * Copyright (c) 2018 Sean Grimes. All Rights Reserved.
 * License: MIT
 */

package edu.gbcg.utils.client;

/**
 * @author Andrew W.E. McDonald
 *
 * Key:
 *  -> us => microseconds
 *  -> ms => milliseconds
 *  -> sec => seconds
 *  -> min => minutes
 *  -> qh => quarter hour (15 minutes)
 *  -> hh => half hour
 *  -> hr => hour
 *  -> day => month. no, just kidding. it's day.
 *  -> week => week
 *
 */

public enum TU {

    US (1), MS (2) , SEC (3), MIN (4), QH(5), HH(6), HR(7), DAY(8), WEEK(9);

    int id;

    TU (int id){
        this.id = id;
    }
}
