package logic;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface CausalOrderingRMI extends Remote {

    void broadcast(Message m, long[] delay) throws RemoteException;
    void receive(Message m) throws RemoteException;

}
