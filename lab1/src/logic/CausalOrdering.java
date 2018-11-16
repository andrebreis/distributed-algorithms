package logic;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Arrays;

public class CausalOrdering extends UnicastRemoteObject implements CausalOrderingRMI {

    public static final int NUM_PROCESSES = 3;

    private ArrayList<Message> receivedMessages;
    private ArrayList<Message> deliveredMessages;


    private ArrayList<CausalOrderingRMI> processes;

    private int[] vectorClock;
    private int processId;

    protected CausalOrdering(int processId) throws RemoteException {
        this.processId = processId;
        this.vectorClock = new int[NUM_PROCESSES];

        receivedMessages = new ArrayList<>();
        deliveredMessages = new ArrayList<>();

        processes = new ArrayList<>();
    }

    public void addProcesses() {
        for(int i = 0; i < NUM_PROCESSES; i++) {
            try {
                processes.add((CausalOrderingRMI) Naming.lookup(String.format("rmi://localhost/causal-ordering-%d", i)));
            } catch (NotBoundException | MalformedURLException | RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void broadcast(Message m, long[] delay) throws RemoteException {
        vectorClock[processId]++;
        m.prepareMessage(processId, vectorClock);
        new Thread(() -> {
            for (int i = 0; i < processes.size(); i++) {
                try {
                    Thread.sleep(delay[i]);
                    processes.get(i).receive(m);
                } catch (RemoteException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public boolean isVectorBigger(int[] a, int[] b) {
        System.out.println(Arrays.toString(a));
        System.out.println(Arrays.toString(b));
        for(int i = 0; i < a.length; i++) {

            if(a[i] < b[i]) return false;
        }
        return true;
    }

    public int[] incrementClock(int[] clock, int processId) {
        int[] copy = Arrays.copyOf(clock, clock.length);
        copy[processId]++;
        return copy;
    }

    public void deliver(Message m) {
        deliveredMessages.add(m);
        if(processId != m.getSenderId())    //Process is sending the message to itself
            vectorClock[m.getSenderId()]++; //But it shouldn't increment twice
        receivedMessages.remove(m);
        System.out.println(String.format("Process %d delivered message %s", processId, m));
    }

    @Override
    public void receive(Message m) throws RemoteException {
        if(isVectorBigger(incrementClock(vectorClock, m.getSenderId()), m.getVector())) { // V+ej >= Vm
            deliver(m);
            for(int i = 0; i < receivedMessages.size(); i++) {
                if(isVectorBigger(incrementClock(vectorClock,receivedMessages.get(i).getSenderId()), receivedMessages.get(i).getVector())) {
                    deliver(receivedMessages.get(i));
                    i = 0;
                }
            }
        }
        else {
            System.out.println(String.format("Process %d is not ready for message %s", processId, m));
            receivedMessages.add(m);
        }
    }
}
