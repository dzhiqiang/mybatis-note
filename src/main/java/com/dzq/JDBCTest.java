package com.dzq;

import java.sql.*;

/**
 * 
 * https://blog.csdn.net/lihao21/article/details/80694503
 */
public class JDBCTest {
    public static void main(String[] args) {
        System.out.println("MySQL JDBC Example.");
        Connection conn = null;
        String url = "jdbc:mysql://10.9.224.45:3306/activiti?useUnicode=true&characterEncoding=utf8&serverTimezone=UTC&useSSL=false";
        String driver = "com.mysql.jdbc.Driver";
        String userName = "root";
        String password = "root";
        Statement stmt = null;
        ResultSet rs = null;
        try {
            Class.forName(driver);
            conn = DriverManager.getConnection(url, userName, password);
            stmt = conn.createStatement();
            String sql = "select * from transaction";
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                String flowKey = rs.getString("flow_key");
                String nodeKey = rs.getString("node_key");
                System.out.println("flow_key = " + flowKey + ", node_key = " + nodeKey);
            }
            rs.close();
            stmt.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException sqlEx) { } // ignore
            }

            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException sqlEx) { } // ignore
            }
        }

    }
}
