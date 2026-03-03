package com.example;

import java.util.List;
import java.sql.SQLException;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface EMPService extends Remote {
    public EMP findEmployeeById(String eno) throws RemoteException, SQLException;

    public int addNewEmployee(String eno, String ename, String title) throws RemoteException, SQLException;

    public int updateEmployee(String eno, String ename, String title) throws RemoteException, SQLException;

    public int deleteEmployee(String eno) throws RemoteException, SQLException;

    public List<EMP> getAllEmployees() throws RemoteException, SQLException;
}