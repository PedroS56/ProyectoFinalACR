package proyectofinalacr;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface searchInterface extends Remote {
    int buscaArchRemote(int startIndex,int indexNode, String fileName) throws RemoteException;
}
