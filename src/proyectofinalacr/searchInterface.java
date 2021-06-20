package proyectofinalacr;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface searchInterface extends Remote {
//    int searchRemoteFile(int indexNode, String fileName) throws RemoteException;
    int searchRemoteFile(int startIndex,int indexNode, String fileName) throws RemoteException;
}
