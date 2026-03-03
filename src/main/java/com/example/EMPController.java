package com.example;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EMPController {

    /**
     * Get all employees from database
     * Task 3: Manual transaction with commit/rollback
     */
    public List<EMP> getAllEmployees() {
        List<EMP> employees = new ArrayList<>();
        Connection conn = null;

        try {
            conn = DBConnection.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT ENO, ENAME, TITLE FROM EMP");

            while (rs.next()) {
                EMP emp = new EMP(
                        rs.getString("ENO"),
                        rs.getString("ENAME"),
                        rs.getString("TITLE"));
                employees.add(emp);
            }

            // Task 3: Manual commit
            conn.commit();
            System.out.println("✓ Transaction committed: Retrieved " + employees.size() + " employees");

        } catch (SQLException e) {
            // Task 3: Rollback on error
            try {
                if (conn != null) {
                    conn.rollback();
                    System.out.println("✗ Transaction rolled back due to error in getAllEmployees");
                }
            } catch (SQLException ex) {
                System.err.println("Error during rollback: " + ex.getMessage());
                ex.printStackTrace();
            }
            e.printStackTrace();
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return employees;
    }

    /**
     * Find employee by ID
     * Task 3: Manual transaction with commit/rollback
     */
    public EMP findEmployeeById(String eno) {
        EMP emp = null;
        Connection conn = null;

        try {
            conn = DBConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(
                    "SELECT ENO, ENAME, TITLE FROM EMP WHERE ENO = ?");
            pstmt.setString(1, eno);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                emp = new EMP(
                        rs.getString("ENO"),
                        rs.getString("ENAME"),
                        rs.getString("TITLE"));
            }

            // Task 3: Manual commit
            conn.commit();
            System.out.println("✓ Transaction committed: Found employee " + eno);

        } catch (SQLException e) {
            // Task 3: Rollback on error
            try {
                if (conn != null) {
                    conn.rollback();
                    System.out.println("✗ Transaction rolled back due to error in findEmployeeById");
                }
            } catch (SQLException ex) {
                System.err.println("Error during rollback: " + ex.getMessage());
                ex.printStackTrace();
            }
            e.printStackTrace();
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return emp;
    }

    /**
     * Add new employee
     * Task 3: Manual transaction with commit/rollback
     */
    public int addNewEmployee(String eno, String ename, String title) {
        Connection conn = null;
        int result = 0;

        try {
            conn = DBConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(
                    "INSERT INTO EMP (ENO, ENAME, TITLE) VALUES (?, ?, ?)");
            pstmt.setString(1, eno);
            pstmt.setString(2, ename);
            pstmt.setString(3, title);

            result = pstmt.executeUpdate();

            // Task 3: Manual commit
            conn.commit();
            System.out.println("✓ Transaction committed: Added employee " + eno);

        } catch (SQLException e) {
            // Task 3: Rollback on error
            try {
                if (conn != null) {
                    conn.rollback();
                    System.out.println("✗ Transaction rolled back: Failed to add employee " + eno);
                }
            } catch (SQLException ex) {
                System.err.println("Error during rollback: " + ex.getMessage());
                ex.printStackTrace();
            }
            e.printStackTrace();
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    /**
     * Update employee
     * Task 3: Manual transaction with commit/rollback
     */
    public int updateEmployee(String eno, String ename, String title) {
        Connection conn = null;
        int result = 0;

        try {
            conn = DBConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(
                    "UPDATE EMP SET ENAME = ?, TITLE = ? WHERE ENO = ?");
            pstmt.setString(1, ename);
            pstmt.setString(2, title);
            pstmt.setString(3, eno);

            result = pstmt.executeUpdate();

            // Task 3: Manual commit
            conn.commit();
            System.out.println("✓ Transaction committed: Updated employee " + eno);

        } catch (SQLException e) {
            // Task 3: Rollback on error
            try {
                if (conn != null) {
                    conn.rollback();
                    System.out.println("✗ Transaction rolled back: Failed to update employee " + eno);
                }
            } catch (SQLException ex) {
                System.err.println("Error during rollback: " + ex.getMessage());
                ex.printStackTrace();
            }
            e.printStackTrace();
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    /**
     * Delete employee
     * Task 3: Manual transaction with commit/rollback
     */
    public int deleteEmployee(String eno) {
        Connection conn = null;
        int result = 0;

        try {
            conn = DBConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(
                    "DELETE FROM EMP WHERE ENO = ?");
            pstmt.setString(1, eno);

            result = pstmt.executeUpdate();

            // Task 3: Manual commit
            conn.commit();
            System.out.println("✓ Transaction committed: Deleted employee " + eno);

        } catch (SQLException e) {
            // Task 3: Rollback on error
            try {
                if (conn != null) {
                    conn.rollback();
                    System.out.println("✗ Transaction rolled back: Failed to delete employee " + eno);
                }
            } catch (SQLException ex) {
                System.err.println("Error during rollback: " + ex.getMessage());
                ex.printStackTrace();
            }
            e.printStackTrace();
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return result;
    }
}