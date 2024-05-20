package constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum XmlAttribute {

    REPO_DATE("REPO_DATE"),
    AUCTION_ID("AUCTION_ID"),
    CURRENCY("CURRENCY"),
    START_TIME("START_TIME"),
    RATE_TYPE_AUCT("RATE_TYPE_AUCT"),
    END_TIME("END_TIME"),
    FIRST_LEG_DATE("FIRST_LEG_DATE"),
    SECOND_LEG_DATE("SECOND_LEG_DATE"),
    MAXIMUM_REPO_PERIOD("MAXIMUM_REPO_PERIOD"),
    LIMITDATE("LIMITDATE"),
    INFOTYPE("INFOTYPE"),
    BID_START_TIME("BID_START_TIME"),
    BID_END_TIME("BID_END_TIME"),
    FIX_ID("FIX_ID"),
    LIMIT("LIMIT"),
    DOC_DATE("doc_date"),
    DOC_TIME("doc_time"),
    DOC_NAME("doc_name"),
    SENDER_DOC_ID("sender_doc_id"),
    SELECTION_DATE("selection_date"),
    SELECTION_ID("selection_id"),
    FINSTR("finstr"),
    TOTAL_SUM("total_sum"),
    PERIOD("period"),
    RATE_TYPE("rate_type"),
    BENCHMARK("benchmark"),
    PAYING_DATE("paying_date"),
    REPAYM_DATE("repaym_date"),
    MIN_ORD_RATE("min_ord_rate"),
    FIX_DISCOUNT("fix_discount"),
    BOOKING_TIME_START("booking_time_start"),
    BOOKING_TIME_FINISH("booking_time_finish"),;

    private final String value;
}
