package db;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.dbutils.BasicRowProcessor;
import org.apache.commons.dbutils.GenerousBeanProcessor;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;

@Slf4j
public class DbHelper {

    private final Connection connection;
    private final QueryRunner queryRunner;

    private CallableStatement callableStatement;

    @SneakyThrows
    public DbHelper(String url, String login, String password) {
        connection = DriverManager.getConnection(url, login, password);
        this.queryRunner = new QueryRunner();
        Runtime.getRuntime().addShutdownHook(new Thread(this::close));
    }

    @SneakyThrows
    public void close() {
        connection.close();
        if (callableStatement != null)
            callableStatement.close();
        log.info("Connection closed");
    }

    @SneakyThrows
    public <T> List<T> queryList(String sqlQuery, Class<T> clazz, Object... params) {
        ResultSetHandler<List<T>> resultSetHandler =
                new BeanListHandler<>(clazz, new BasicRowProcessor(new GenerousBeanProcessor()));
        return queryRunner.query(connection, sqlQuery, resultSetHandler, params);
    }

    @SneakyThrows
    public <T> List<T> queryList(String sqlQuery, Class<T> clazz) {
        ResultSetHandler<List<T>> resultSetHandler =
                new BeanListHandler<>(clazz, new BasicRowProcessor(new GenerousBeanProcessor()));
        return queryRunner.query(connection, sqlQuery, resultSetHandler);
    }

    public <T> List<T> queryList(Class<T> clazz, SqlQuery query) {
        return queryList(query.getSqlString(), clazz, query.getParams());
    }

    @SneakyThrows
    public void exec(String sqlQuery) {
        callableStatement = connection.prepareCall(sqlQuery);
        callableStatement.executeUpdate();
    }
}
