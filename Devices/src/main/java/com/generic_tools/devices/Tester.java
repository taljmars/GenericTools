package com.generic_tools.devices;

import com.generic_tools.devices.internal.TwoWaySerialComm;
import com.generic_tools.environment.Environment;
import com.generic_tools.logger.Logger;

import java.util.Scanner;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Tester {

    private boolean flag = false;
    private SerialConnection serialConnection;
    private ConcurrentLinkedQueue<String> writeQueue;

    public Tester() {
        serialConnection = new TwoWaySerialComm(new Logger(new Environment()));
        writeQueue = new ConcurrentLinkedQueue<>();
    }

    Object[] res = {};
    public Object[] load() {

        int interval = 500;
        double cycles = 0.0;
        Thread loader = new Thread(() -> {
            res = serialConnection.listPorts();
            if (res == null || res.length == 0)
                System.out.println("No port found !");
            flag = true;
        });
        loader.start();

        System.out.print("Loading driver [ ");
        while (!flag) {
            System.out.print("*");
            try {
                cycles++;
                Thread.sleep(interval);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.print(" ]: Done (Port mapping time: " + (cycles * interval / 1000.0) + " sec)\n");

        return res;
    }

    public boolean run(Object port) throws Exception {
        serialConnection.setPortName(port.toString());
        serialConnection.setBaud(115200);
        if (!serialConnection.connect())
            return false;

        Thread w = new Thread(new Writer());
        System.out.println("Start Sending thread");
        w.start();

        Thread r = new Thread(new Reader());
        System.out.println("Start Receiver thread");
        r.start();

        return true;
    }

    class Writer implements Runnable {

        @Override
        public void run() {
            do {
                if (writeQueue.isEmpty())
                    continue;
                String val = writeQueue.poll();
                if (val != null && !val.isEmpty()) {
                    System.out.println("Sending: " + val);
                    serialConnection.write(val);
                }
            }
            while (true);
        }
    }

    class Reader implements Runnable {

        @Override
        public void run() {
            while (true) {
                final byte[] readBuffer = new byte[4096 * 8];
                int len = serialConnection.read(readBuffer, readBuffer.length);
                for (int i = 0 ; i < len ; i++)
                    System.err.print(readBuffer[i] + ",");
                if (len > 0)
                    System.err.println();
            }
        }
    }

    public static void main(String[] str ) throws Exception {

        Tester tester = new Tester();
        Object[] ports = tester.load();
        int portId = 0;
        for (Object port : ports) {
            System.out.println((portId++) + ": " + port);
        }

        portId = -1;
        while (portId < 0 || portId > ports.length - 1) {
            Scanner reader = new Scanner(System.in);  // Reading from System.in
            System.out.print("Choose port number: ");
            portId = reader.nextInt();
            System.out.println();
            if (!tester.run(ports[portId]))
                portId = -1;
        }


        while (true) {
            Scanner reader = new Scanner(System.in);  // Reading from System.in
            System.out.print("Write Message: ");
            String msg = reader.next();
            tester.writeQueue.offer(msg);
        }
    }
}
