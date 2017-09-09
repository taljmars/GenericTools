package com.javaws;

import org.apache.cxf.tools.java2ws.JavaToWS;
import org.apache.cxf.tools.wsdlto.WSDLToJava;

import java.util.List;

public class JavaToWSCodeGenerator {

    public static boolean JavaClass2WSDL(String targetDirectory, List<String> classListPaths) {
        System.out.println("Start Conversion");
        for (String classPath: classListPaths) {
            JavaToWS.main(new String[] {"-d", targetDirectory, "-createxsdimports", "-wsdl",
                    classPath });
        }
        System.out.println("Finish Conversion");
        return true;
    }

    public static boolean WSDL2JavaClass(String wsdlSourceDirectory, String classTargetDirectory,
                                         List<String> wsdlListNames) {
        System.out.println("Start Conversion");
        for (String wsdlName: wsdlListNames) {
            WSDLToJava.main(new String[] {"-d" , classTargetDirectory, wsdlSourceDirectory + "/" + wsdlName});
        }
        System.out.println("Finish Conversion");
        return true;
    }

    public static void main(String[] args) {
//        try {
//            JavaToWS.main(new String[] {"-d", "wsdl", "-createxsdimports", "-wsdl",
//                    "com.dronedb.persistence.ws.internal.DroneDbCrudSvcRemote" });
//            JavaToWS.main(new String[] {"-d", "wsdl", "-createxsdimports", "-wsdl",
//                    "com.dronedb.persistence.ws.internal.MissionCrudSvcRemote" });
//            JavaToWS.main(new String[] {"-d", "wsdl", "-createxsdimports", "-wsdl",
//                    "com.dronedb.persistence.scheme.Land1" });
//            System.out.println("finished %%%%%%%%%%");
//
//            WSDLToJava.main(new String[] {"-d" , "wsdl/", "wsdl/MissionCrudSvcRemoteService.wsdl"});
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

    }

}
