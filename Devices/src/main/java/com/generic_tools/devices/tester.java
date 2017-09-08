package com.generic_tools.devices;

import com.generic_tools.devices.internal.TwoWaySerialComm;
import com.generic_tools.environment.Environment;
import com.generic_tools.logger.Logger;

public class tester {

    static boolean flag = false;

    static class A implements Runnable {

        @Override
        public void run() {
            (new TwoWaySerialComm(new Logger(new Environment()))).listPorts();
            flag = true;
        }
    }

    public static void main(String[] str ) {
        A aaa = new A();
        Thread a = new Thread(aaa);
        a.start();


        while (!flag) {
            System.out.print("*");
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
//        SerialConnection s = new TwoWaySerialComm(new Logger(new Environment()));
//        System.out.println("lk");
//
//        Object[] ports = s.listPorts();
//        for (Object stro : ports) {
//            System.out.println(stro);
//        }
    }
}
