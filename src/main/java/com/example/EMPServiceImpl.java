package com.example;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.SQLException;
import java.util.List;

public class EMPServiceImpl extends UnicastRemoteObject implements EMPService {
    private static final long serialVersionUID = 1L;
    private EMPDAO empDAO;

    public EMPServiceImpl() throws Exception {
        super();
        empDAO = new EMPDAO();
    }

    @Override
    public EMP findEmployeeById(String eno) throws SQLException, RemoteException {
        return empDAO.findEmployeeById(eno);
    }

    @Override
    public int addNewEmployee(String eno, String ename, String title) throws SQLException, RemoteException {
        return empDAO.addNewEmployee(eno, ename, title);

    }

    @Override
    public int updateEmployee(String eno, String ename, String title) throws SQLException, RemoteException {
        return empDAO.updateEmployee(eno, ename, title);
    }

    @Override
    public int deleteEmployee(String eno) throws SQLException, RemoteException {
        return empDAO.deleteEmployee(eno);
    }

    @Override
    public List<EMP> getAllEmployees() throws SQLException, RemoteException {
        return empDAO.getAllEmployees();
    }
}
