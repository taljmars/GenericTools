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
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.print(" ]: Done\n");

        return res;
    }

    public void run(Object port) throws Exception {
        serialConnection.setPortName(port.toString());
        serialConnection.setBaud(115200);
        serialConnection.connect();

        Thread w = new Thread(new Writer());
        w.start();

        Thread r = new Thread(new Reader());
        r.start();
    }

    class Writer implements Runnable {

        @Override
        public void run() {
            do {
                String val = writeQueue.poll();
                if (val != null && !val.isEmpty()) {
                    serialConnection.write(val);
                }
            }
            while (!writeQueue.isEmpty());
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
                System.err.println();
            }
        }
    }

    public static void main(String[] str ) throws Exception {

        Tester t = new Tester();
        Object[] ports = t.load();
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
        }

        t.run(ports[portId]);

        while (true) {
            Scanner reader = new Scanner(System.in);  // Reading from System.in
            System.out.print("Write Message: ");
            String msg = reader.next();
            t.writeQueue.offer(msg);
        }
    }
}
