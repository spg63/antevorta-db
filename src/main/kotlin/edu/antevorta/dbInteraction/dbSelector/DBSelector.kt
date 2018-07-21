/*
 * Copyright (c) 2018 Sean Grimes. All Rights Reserved.
 * License: MIT
 */

package edu.antevorta.dbInteraction.dbSelector

/**
 * The main credit for this class goes to John Krasnay. I've modified it for my own uses.
 * https://github.com/jkrasnay/sqlbuilder
 */
open class DBSelector {
    private var tableName   = ArrayList<String>()
    private var columns     = ArrayList<String>()
    private var joins       = ArrayList<String>()
    private var leftJoins   = ArrayList<String>()
    private var wheres      = ArrayList<String>()
    private var groupBys    = ArrayList<String>()
    private var havings     = ArrayList<String>()
    private var orderBys    = ArrayList<String>()
    private var unions      = ArrayList<DBSelector>()
    private var limit       = 0
    private var offset      = 0
    private var forUpdate   = false
    private var noWait      = false
    private var distinct    = false

    constructor()
    constructor(tableName: String) { this.tableName.add(tableName) }

    fun and(expr: String): DBSelector {
        return where(expr)
    }

    fun column(name: String): DBSelector {
        this.columns.add(name)
        return this
    }

    fun column(name: String, groupBy: Boolean): DBSelector {
        columns.add(name)
        if(groupBy)
            groupBys.add(name)
        return this
    }

    fun limit(limit: Int, offset: Int): DBSelector {
        this.limit = limit
        this.offset = offset
        return this
    }

    fun limit(limit: Int): DBSelector {
        return limit(limit, 0)
    }

    fun distinct(): DBSelector {
        this.distinct = true
        return this
    }

    fun forUpdate(): DBSelector {
        this.forUpdate = true
        return this
    }

    fun from(table: String): DBSelector {
        this.tableName.add(table)
        return this
    }

    fun getUnions(): List<DBSelector> {
        return this.unions
    }

    fun groupBy(expr: String): DBSelector {
        groupBys.add(expr)
        return this
    }

    fun having(expr: String): DBSelector {
        havings.add(expr)
        return this
    }

    fun join(join: String): DBSelector {
        joins.add(join)
        return this
    }

    fun leftJoin(join: String): DBSelector {
        leftJoins.add(join)
        return this
    }

    fun noWait(): DBSelector {
        if(!forUpdate)
            throw RuntimeException("noWait without forUpdate cannot be called")
        noWait = true
        return this
    }

    fun orderBy(name: String): DBSelector {
        orderBys.add("$name asc")
        return this
    }

    fun orderBy(name: String, ascending: Boolean): DBSelector {
        if(ascending)
            orderBys.add("$name asc")
        else
            orderBys.add("$name desc")
        return this
    }

    fun union(dbSelector: DBSelector): DBSelector {
        unions.add(dbSelector)
        return this
    }

    fun where(expr: String): DBSelector {
        wheres.add(expr)
        return this
    }

    fun sql(): String {
        return this.toString()
    }

    override fun toString(): String {
        val sql = StringBuilder("select ")
        if(distinct)
            sql.append("distinct ")
        if(columns.size == 0)
            sql.append("*")
        else
            appendList(sql, columns, "", ", ")

        appendList(sql, tableName, " from ", ", ")
        appendList(sql, joins, " join ", " join ")
        appendList(sql, leftJoins, " left join ", " left join ")
        appendList(sql, wheres, " where ", " and ")
        appendList(sql, groupBys, " group by ", ", ")
        appendList(sql, havings, " having ", " and ")
        appendList(sql, unions, " union ", " union ")
        appendList(sql, orderBys, " order by ", ", ")

        if(forUpdate){
            sql.append(" for update")
            if(noWait)
                sql.append(" nowait")
        }
        if(limit > 0)
            sql.append(" limit $limit")
        if(offset > 0)
            sql.append(", $offset")

        return sql.toString()
    }

    private fun appendList(sql: StringBuilder, list: List<*>, init: String, sep: String) {
        var first = true
        for (s in list) {
            if (first)
                sql.append(init)
            else
                sql.append(sep)
            sql.append(s)
            first = false
        }
    }

}
