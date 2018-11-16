package logic;

import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.ArrayList;

public class CausalOrderingMain {


    public static void main(String[] args) {
        try {
            java.rmi.registry.LocateRegistry.createRegistry(1099);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        ArrayList<CausalOrdering> processes = new ArrayList<>();

        for(int i = 0; i < CausalOrdering.NUM_PROCESSES; i++) {
            try {
                CausalOrdering process = new CausalOrdering(i);
                Naming.bind(String.format("rmi://localhost:1099/causal-ordering-%d", i), process);
                processes.add(process);
            } catch (RemoteException | MalformedURLException | AlreadyBoundException e) {
                e.printStackTrace();
            }
        }

        for (CausalOrdering process : processes) process.addProcesses();


        try {
            processes.get(0).broadcast(new Message("ola"), new long[] {0,0,10000});
            Thread.sleep(500);
            processes.get(1).broadcast(new Message("ola2"),new long[] {2000,2000,2000});
            //fix send vector
        } catch (RemoteException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
