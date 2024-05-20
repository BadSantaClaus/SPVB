package db;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SqlQuery {
    private String sqlString;
    private Object[] params;
}
