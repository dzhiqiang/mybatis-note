package com.dzq;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.*;

public class JDBCConnectionTest {

    Connection conn = null;
    Statement stmt = null;
    ResultSet rs = null;

    @Before
    public void getConnection() throws ClassNotFoundException, SQLException {
        System.out.println("MySQL JDBC Example.");
        String url = "jdbc:mysql://10.9.224.45:3306/activiti?useUnicode=true&characterEncoding=utf8&serverTimezone=UTC&useSSL=false";
        String driver = "com.mysql.jdbc.Driver";
        String userName = "root";
        String password = "root";
        Class.forName(driver);
        conn = DriverManager.getConnection(url, userName, password);

    }
    @After
    public void close() {
        try {
            if (rs != null) {
                rs.close();
                System.out.println("ResultSet close...");
            }
            if (stmt != null) {
                stmt.close();
                System.out.println("Statement close...");
            }
            if (conn != null) {
                conn.close();
                System.out.println("Connection close...");
            }
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
    }

    @Test
    public void test_01() throws SQLException {
        String sql = conn.nativeSQL("select * from transaction where id = ? ");
        System.out.println(sql);
    }

    @Test
    public void test_02() throws SQLException {
        stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_READ_ONLY,ResultSet.HOLD_CURSORS_OVER_COMMIT);
        stmt.setFetchSize(1);
        String sql = "select * from transaction";
        rs = stmt.executeQuery(sql);
        while (rs.next()) {
            String flowKey = rs.getString("flow_key");
            String nodeKey = rs.getString("node_key");
            System.out.println("flow_key = " + flowKey + ", node_key = " + nodeKey);
        }
    }

    @Test
    public void test_03() throws SQLException {
        stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_READ_ONLY,ResultSet.HOLD_CURSORS_OVER_COMMIT);
        stmt.setFetchSize(1);
        String sql = "select * from transaction";
        boolean execute = stmt.execute(sql);
        System.out.println("execute :" + (execute ? "成功" : "失败"));
        rs = stmt.getResultSet();
        while (rs.next()) {
            String flowKey = rs.getString("flow_key");
            String nodeKey = rs.getString("node_key");
            System.out.println("flow_key = " + flowKey + ", node_key = " + nodeKey);
        }
    }

    @Test
    public void test_04() throws SQLException {
        stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_READ_ONLY,ResultSet.HOLD_CURSORS_OVER_COMMIT);
        String sql1 = "update transaction set flow_key = '42' where id = 21";
        String sql2 = "update transaction set flow_key = '42' where node_key = 'task-transaction_045a1a28'";
        stmt.addBatch(sql1);
        stmt.addBatch(sql2);
        int[] result = stmt.executeBatch();
        for (int i = 0; i < result.length; i++) {
            System.out.println("result [" + i + "] :" + result[i]);
        }
    }
    @Test
    public void test_05() throws SQLException {
        stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_READ_ONLY,ResultSet.HOLD_CURSORS_OVER_COMMIT);
        String sql1 = "update transaction set flow_key = '42' where id = 21";
        String sql2 = "update transaction set flow_key = '42' where node_key = 'task-transaction_045a1a28'";
        stmt.addBatch(sql1);
        stmt.addBatch(sql2);
        int[] result = stmt.executeBatch();
        for (int i = 0; i < result.length; i++) {
            System.out.println("result [" + i + "] :" + result[i]);
        }
    }

}
