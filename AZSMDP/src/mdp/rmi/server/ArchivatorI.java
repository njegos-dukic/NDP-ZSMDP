package mdp.rmi.server;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface ArchivatorI extends Remote {

	boolean upload(Report report) throws RemoteException;

	Report download(String fileName) throws RemoteException;

	ArrayList<ReportMetadata> list() throws RemoteException;
}