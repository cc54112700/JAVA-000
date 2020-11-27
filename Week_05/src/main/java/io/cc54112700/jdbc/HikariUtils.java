package io.cc54112700.jdbc;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class HikariUtils {

    public static final String URL = "jdbc:mysql://localhost:3306/test_db";
    public static final String USER = "root";
    public static final String PASSWORD = "root";

    public static void main(String[] args) {

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(URL);
        hikariConfig.setUsername(USER);
        hikariConfig.setPassword(PASSWORD);
        hikariConfig.setDriverClassName("com.mysql.jdbc.Driver");

        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        HikariDataSource dataSource = new HikariDataSource(hikariConfig);

        Connection conn = null;
        PreparedStatement statement = null;

        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);

            insert(conn);
            // update
            // delete
            // select
            // ....

            // 提交事务
            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();

            try {
                conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        } finally {

            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                } finally {
                    statement = null;
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                } finally {
                    conn = null;
                }
            }
        }
    }

    private static void insert(Connection conn) throws SQLException {

        String sql = "insert into sys_user(id, name) values (?, ?)";
//        conn.createStatement();
        PreparedStatement statement = conn.prepareStatement(sql);
        statement.setInt(1, 145);
        statement.setString(2, "JSW");

        statement.execute();
    }
}
