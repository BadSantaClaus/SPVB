package db.auction;

import db.SqlQuery;

import java.util.ArrayList;
import java.util.List;

public class AuctionFinder {
    private final StringBuilder query;
    private final List<Object> params;
    private static final String START_QUERY = "SELECT * FROM initiators.auction a WHERE true";

    public AuctionFinder() {
        this.query = new StringBuilder(START_QUERY);
        this.params = new ArrayList<>();
    }

    public AuctionFinder(String startQuery) {
        this.query = new StringBuilder(startQuery);
        this.params = new ArrayList<>();
    }

    public AuctionFinder withStatus(String status) {
        query.append(" AND a.status = ?");
        params.add(status);
        return this;
    }

    public SqlQuery build() {
        return new SqlQuery(query.toString(), params.toArray());
    }
}
