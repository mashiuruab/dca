package com.sannsyn.dca.service;

/**
 * A query object for querying aggregates.
 * <p>
 * Created by jobaer on 4/12/16.
 */
public class AggregateQuery {
    private String name;
    private String id;
    private SortBy sortBy;
    private String size;

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public SortBy getSortBy() {
        return sortBy;
    }

    public String getSize() {
        return size;
    }

    public AggregateQuery(String name, String id, SortBy sortBy, String size) {
        this.name = name;
        this.id = id;
        this.sortBy = sortBy;
        this.size = size;
    }

    public static AggregateQuery copyOf(AggregateQuery query) {
        return new AggregateQuery(query.name, query.id, query.sortBy, query.size);
    }

    @Override
    public String toString() {
        return "AggregateQuery{" +
            "name='" + name + '\'' +
            ", id='" + id + '\'' +
            ", sortBy=" + sortBy +
            ", size='" + size + '\'' +
            '}';
    }
}
