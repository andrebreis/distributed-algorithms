package tests;

import logic.CausalOrdering;
import logic.Message;
import org.junit.Test;

import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

public class TestSuite {

    private ArrayList<CausalOrdering> processes;

    public void setup() {
        processes = new ArrayList<>();

        //Initialize RMI Registry
        try {
            java.rmi.registry.LocateRegistry.createRegistry(1099);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        //Initialize Processes
        for(int i = 0; i < CausalOrdering.NUM_PROCESSES; i++) {
            try {
                CausalOrdering process = new CausalOrdering(i);
                Naming.bind(String.format("rmi://localhost:1099/causal-ordering-%d", i), process);
                processes.add(process);
            } catch (RemoteException | MalformedURLException | AlreadyBoundException e) {
                e.printStackTrace();
            }
        }

        //Create process awareness of the others
        for (CausalOrdering process : processes) process.addProcesses();
    }

    int[] getCurrentVectorClock(int index) {
        int[] clock = new int[processes.size()];

        for(int i = 0; i <= index; i++) {
            clock[i] = 1;
        }
        return clock;
    }

    //Send one message from each process, ordered, and receive them in order
    @Test
    public void testRegularOrder() throws RemoteException, InterruptedException {
        setup();

        long[] delay = new long[processes.size()];
        for(int i = 0; i < processes.size(); i++) {
            processes.get(i).broadcast(new Message("Message " + i), delay);
            Thread.sleep(500);
            for(CausalOrdering process : processes) {
                assertTrue(Arrays.equals(process.getVectorClock(), getCurrentVectorClock(i))); //[1,0,0,..], [1,1,0,..],...
                assertEquals(process.getDeliveredMessages().size(), i+1);
                assertEquals(process.getReceivedMessages().size(), 0);
            }
        }
    }

    //Send one message from each process, ordered, and receive them in order
    @Test
    public void testBookExample() throws RemoteException, InterruptedException {
        setup();

        long[] noDelay = new long[processes.size()];
        long[] delayAfterSecondProcess = new long[processes.size()];
        for(int i=2; i < processes.size(); i++)
            delayAfterSecondProcess[i] = 2000;

        processes.get(0).broadcast(new Message("Message 0"), delayAfterSecondProcess);
        Thread.sleep(100);

        //Process 1 and 2 should already have delivered the message
        for(int i = 0; i < 2; i++) {
            assertTrue(Arrays.equals(processes.get(i).getVectorClock(), getCurrentVectorClock(0)));
            assertEquals(processes.get(i).getDeliveredMessages().size(), 1);
            assertEquals(processes.get(i).getReceivedMessages().size(), 0);
        }

        //Others mustn't have delivered the message
        for(int i = 2; i < processes.size(); i++) {
            assertTrue(Arrays.equals(processes.get(i).getVectorClock(), new int[processes.size()]));
            assertEquals(processes.get(i).getDeliveredMessages().size(), 0);
            assertEquals(processes.get(i).getReceivedMessages().size(), 0);
        }

        processes.get(1).broadcast(new Message("Message 1"), noDelay);
        Thread.sleep(100);

        //Process 1 and 2 should already have delivered the message
        for(int i = 0; i < 2; i++) {
            assertTrue(Arrays.equals(processes.get(i).getVectorClock(), getCurrentVectorClock(1)));
            assertEquals(processes.get(i).getDeliveredMessages().size(), 2);
            assertEquals(processes.get(i).getReceivedMessages().size(), 0);
        }

        //Others mustn't have delivered the message but must have received it
        for(int i = 2; i < processes.size(); i++) {
            assertTrue(Arrays.equals(processes.get(i).getVectorClock(), new int[processes.size()]));
            assertEquals(processes.get(i).getDeliveredMessages().size(), 0);
            assertEquals(processes.get(i).getReceivedMessages().size(), 1);
        }

        //wait for message 1 to arrive
        Thread.sleep(2000);

        //All processes should have now delivered all messages
        for (CausalOrdering process : processes) {
            assertTrue(Arrays.equals(process.getVectorClock(), getCurrentVectorClock(1)));
            assertEquals(process.getDeliveredMessages().size(), 2);
            assertEquals(process.getReceivedMessages().size(), 0);
        }

//        for(int i = 0; i < processes.size(); i++) {
//            processes.get(i).broadcast(new Message("Message " + i), delay);
//            Thread.sleep(500);
//            for(CausalOrdering process : processes) {
//                assertTrue(Arrays.equals(process.getVectorClock(), getCurrentVectorClock(i))); //[1,0,0,..], [1,1,0,..],...
//                assertEquals(process.getDeliveredMessages().size(), i+1);
//                assertEquals(process.getReceivedMessages().size(), 0);
//            }
//        }
    }
}
