package com.myBackup.client;

import java.net.NetworkInterface;
import java.util.Enumeration;
import org.springframework.stereotype.Service;

@Service
public class Utility {

    public static String getMACAddress() {
        try {
            // Iterate over all network interfaces
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();

                // Ignore loopback and virtual interfaces
                if (networkInterface.isLoopback() || networkInterface.isVirtual() || !networkInterface.isUp()) {
                    continue;
                }

                byte[] macAddressBytes = networkInterface.getHardwareAddress();
                if (macAddressBytes != null) {
                    // Convert byte array to MAC address format
                    StringBuilder macAddress = new StringBuilder();
                    for (int i = 0; i < macAddressBytes.length; i++) {
                        macAddress.append(String.format("%02X", macAddressBytes[i]));
                        if (i < macAddressBytes.length - 1) {
                            macAddress.append(":");
                        }
                    }
                    return macAddress.toString();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "MAC Address not found";
    }
}
