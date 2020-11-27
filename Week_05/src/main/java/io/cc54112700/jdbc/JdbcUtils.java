package io.cc54112700.jdbc;

import java.sql.*;

public class JdbcUtils {

    public static final String URL = "jdbc:mysql://localhost:3306/test_db";
    public static final String USER = "root";
    public static final String PASSWORD = "root";


    public static void main(String[] args) {

        try {
            Class.forName("com.mysql.jdbc.Driver");

            Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            Statement stm = conn.createStatement();

            conn.setAutoCommit(false);

            insert(conn, stm);
            update(conn, stm);
            delete(conn, stm);

            insertWithStatement(conn);
            updateWithStatement(conn);
            deleteWithStatement(conn);

            // 提交事务
            conn.commit();

        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    private static void deleteWithStatement(Connection conn) throws SQLException {

        String sql = "delete from sys_user where id = ? ";
        PreparedStatement statement = conn.prepareStatement(sql);
        statement.setInt(1, 123);

        statement.execute();
    }

    private static void updateWithStatement(Connection conn) throws SQLException {

        String sql = "update sys_user set name = ? where id = ? ";
        PreparedStatement statement = conn.prepareStatement(sql);
        statement.setString(1, "jjc");
        statement.setInt(2, 123);

        statement.execute();
    }

    private static void insertWithStatement(Connection conn) throws SQLException {

        String sql = "insert into sys_user(id, name) values (?, ?)";
        PreparedStatement statement = conn.prepareStatement(sql);
        statement.setInt(1, 123);
        statement.setString(2, "Wick");

        statement.execute();
    }

    private static void delete(Connection conn, Statement stm) throws SQLException {

        stm.executeUpdate("delete from sys_user where id = 2");
    }

    private static void update(Connection conn, Statement stm) throws SQLException {

        stm.executeUpdate("update sys_user set name = 'JJ' where id = 1");
    }

    private static void insert(Connection conn, Statement stm) throws SQLException {

        stm.execute("insert into sys_user(id, name) values (1, 'may')");
        stm.execute("insert into sys_user(id, name) values (2, 'han')");
    }
}
