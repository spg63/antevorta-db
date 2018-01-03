package edu.gbcg.dbInteraction.dbSelector;

import java.util.ArrayList;
import java.util.List;

/**
 * The main credit for this class goes to John Krasnay. I've modified it for my own uses.
 * https://github.com/jkrasnay/sqlbuilder
 */
public class DBSelector {

    private List<String> tableName = new ArrayList<>();
    private List<String> columns = new ArrayList<>();
    private List<String> joins = new ArrayList<>();
    private List<String> leftJoins = new ArrayList<>();
    private List<String> wheres = new ArrayList<>();
    private List<String> groupBys = new ArrayList<>();
    private List<String> havings = new ArrayList<>();
    private List<DBSelector> unions = new ArrayList<>();
    private List<String> orderBys = new ArrayList<>();
    private int limit = 0;
    private int offset = 0;
    private boolean forUpdate;
    private boolean noWait;
    private boolean distinct;

    public DBSelector(){}
    public DBSelector(String tableName){
        this.tableName.add(tableName);
    }

    public DBSelector and(String expr){
        return where(expr);
    }

    public DBSelector column(String name){
        columns.add(name);
        return this;
    }

    public DBSelector column(String name, boolean groupBy){
        columns.add(name);
        if(groupBy)
            groupBys.add(name);
        return this;
    }

    public DBSelector limit(int limit, int offset){
        this.limit = limit;
        this.offset = offset;
        return this;
    }

    public DBSelector limit(int limit){
        return limit(limit, 0);
    }

    public DBSelector distinct(){
        this.distinct = true;
        return this;
    }

    public DBSelector forUpdate(){
        this.forUpdate = true;
        return this;
    }

    public DBSelector from(String table){
        this.tableName.add(table);
        return this;
    }

    public List<DBSelector> getUnions(){
        return this.unions;
    }

    public DBSelector groupBy(String expr){
        groupBys.add(expr);
        return this;
    }

    public DBSelector having(String expr){
        havings.add(expr);
        return this;
    }

    public DBSelector join(String join){
        joins.add(join);
        return this;
    }

    public DBSelector leftJoin(String join){
        leftJoins.add(join);
        return this;
    }

    public DBSelector noWait(){
        if(!forUpdate)
            throw new RuntimeException("noWait without forUpdate cannot be called");
        noWait = true;
        return this;
    }

    public DBSelector orderBy(String name){
        orderBys.add(name);
        return this;
    }

    public DBSelector orderBy(String name, boolean ascending){
        if(ascending)
            orderBys.add(name + " asc");
        else
            orderBys.add(name + " desc");
        return this;
    }

    public DBSelector union(DBSelector dbSelector){
        unions.add(dbSelector);
        return this;
    }

    public DBSelector where(String expr){
        wheres.add(expr);
        return this;
    }

    public String sql(){
        return this.toString();
    }

    @Override
    public String toString(){
        StringBuilder sql = new StringBuilder("select ");
        if(distinct)
            sql.append("distinct ");
        if(columns.size() == 0)
            sql.append("*");
        else
            appendList(sql, columns, "", ", ");

        appendList(sql, tableName, " from ", ", ");
        appendList(sql, joins, " join ", " join ");
        appendList(sql, leftJoins, " left join ", " left join ");
        appendList(sql, wheres, " where ", " and ");
        appendList(sql, groupBys, " group by ", ", ");
        appendList(sql, havings, " having ", " and ");
        appendList(sql, unions, " union ", " union ");
        appendList(sql, orderBys, " order by", ", ");

        if(forUpdate){
            sql.append(" for update");
            if(noWait)
                sql.append(" nowait");
        }

        if(limit > 0)
            sql.append(" limit " + limit);
        if(offset > 0)
            sql.append(", " + offset);

        return sql.toString();
    }

    private void appendList(StringBuilder sql, List<?> list, String init, String sep){
        boolean first = true;
        for(Object s : list){
            if(first)
                sql.append(init);
            else
                sql.append(sep);
            sql.append(s);
            first = false;
        }

    }

}
