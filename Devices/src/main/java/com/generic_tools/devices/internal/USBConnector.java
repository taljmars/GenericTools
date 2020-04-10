//package com.generic_tools.devices.internal;
//
//import javax.usb.UsbException;
//import javax.usb.UsbInterface;
//import java.io.UnsupportedEncodingException;
//
//public class USBConnector {
//
//    public final UsbInterface usbInterface;
//
//    public USBConnector(UsbInterface point) {
//        this.usbInterface = point;
//    }
//
//    @Override
//    public String toString() {
//        try {
//            return this.hashCode() + " " + usbInterface.getUsbConfiguration().getUsbDevice().getProductString();
//        }
//        catch (UsbException e) {
//            e.printStackTrace();
//        }
//        catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
//        return this.hashCode() + " Error";
//    }
//
//
//}
