
package com.example;

import java.sql.*;
import java.util.*;
import java.io.Serializable;

/**
 * This class is a DAO (Data Access Object) - Use JDBC API
 * It encapsulates all the database operations for the EMP table
 */
public class EMPDAO implements Serializable {
    private static final long serialVersionUID = 1L;

    public EMP findEmployeeById(String eno) throws SQLException {
        Connection conn = DBConnection.getConnection();
        String querySQL = "SELECT * FROM EMP WHERE ENO=?;";
        PreparedStatement pstmt = conn.prepareStatement(querySQL);
        pstmt.setString(1, eno);
        ResultSet rs = pstmt.executeQuery();

        EMP emp = null;

        if (rs.next()) {
            String ename = rs.getString("ENAME");
            String title = rs.getString("TITLE");
            // put the data from the resultset into an EMP Object
            emp = new EMP(eno, ename, title);
        }

        conn.commit(); // Task 3: Manual commit (for read consistency)
        // close result set, statement and the connection to the database
        pstmt.close();
        rs.close();
        conn.close();

        return emp;
    }

    public int addNewEmployee(String eno, String ename, String title) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBConnection.getConnection();
            String insertSQL = "INSERT INTO EMP VALUES(?, ?, ?);";
            pstmt = conn.prepareStatement(insertSQL);
            pstmt.setString(1, eno);
            pstmt.setString(2, ename);
            pstmt.setString(3, title);
            int insertStatus = pstmt.executeUpdate();

            conn.commit(); // Task 3: Manual commit
            return insertStatus;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback(); // Rollback on error
                } catch (SQLException ex) {
                    System.err.println("Error during rollback: " + ex.getMessage());
                }
            }
            throw e; // Re-throw the exception
        } finally {
            if (pstmt != null)
                try {
                    pstmt.close();
                } catch (SQLException e) {
                    System.err.println("Error closing PreparedStatement: " + e.getMessage());
                }
            if (conn != null)
                try {
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("Error closing Connection: " + e.getMessage());
                }
        }
    }

    public int updateEmployee(String eno, String ename, String title) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBConnection.getConnection();
            String updateSQL = "UPDATE EMP SET ENAME=?, TITLE=? WHERE ENO=?;";
            pstmt = conn.prepareStatement(updateSQL);
            pstmt.setString(1, ename);
            pstmt.setString(2, title);
            pstmt.setString(3, eno);
            int updateStatus = pstmt.executeUpdate();

            conn.commit(); // Task 3: Manual commit
            return updateStatus;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback(); // Rollback on error
                } catch (SQLException ex) {
                    System.err.println("Error during rollback: " + ex.getMessage());
                }
            }
            throw e; // Re-throw the exception
        } finally {
            if (pstmt != null)
                try {
                    pstmt.close();
                } catch (SQLException e) {
                    System.err.println("Error closing PreparedStatement: " + e.getMessage());
                }
            if (conn != null)
                try {
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("Error closing Connection: " + e.getMessage());
                }
        }
    }

    public int deleteEmployee(String eno) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBConnection.getConnection();
            String deleteSQL = "DELETE FROM EMP WHERE ENO=?;";
            pstmt = conn.prepareStatement(deleteSQL);
            pstmt.setString(1, eno);
            int deleteStatus = pstmt.executeUpdate();

            conn.commit(); // Task 3: Manual commit
            return deleteStatus;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback(); // Rollback on error
                } catch (SQLException ex) {
                    System.err.println("Error during rollback: " + ex.getMessage());
                }
            }
            throw e; // Re-throw the exception
        } finally {
            if (pstmt != null)
                try {
                    pstmt.close();
                } catch (SQLException e) {
                    System.err.println("Error closing PreparedStatement: " + e.getMessage());
                }
            if (conn != null)
                try {
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("Error closing Connection: " + e.getMessage());
                }
        }
    }

    public List<EMP> getAllEmployees() throws SQLException {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            String sql = "SELECT * FROM EMP";
            conn = DBConnection.getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);

            List<EMP> empList = new ArrayList<>();
            while (rs.next()) {
                String eno = rs.getString("ENO");
                String ename = rs.getString("ENAME");
                String title = rs.getString("TITLE");
                EMP emp = new EMP(eno, ename, title);
                empList.add(emp);
            }

            conn.commit(); // Task 3: Manual commit (for read consistency)
            return empList;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.err.println("Error during rollback: " + ex.getMessage());
                }
            }
            throw e;
        } finally {
            if (rs != null)
                try {
                    rs.close();
                } catch (SQLException e) {
                    System.err.println("Error closing ResultSet: " + e.getMessage());
                }
            if (stmt != null)
                try {
                    stmt.close();
                } catch (SQLException e) {
                    System.err.println("Error closing Statement: " + e.getMessage());
                }
            if (conn != null)
                try {
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("Error closing Connection: " + e.getMessage());
                }
        }
    }
}
