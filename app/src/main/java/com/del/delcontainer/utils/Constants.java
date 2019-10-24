package com.del.delcontainer.utils;

import java.util.UUID;

public class Constants {

    // String constants
    public static final String BLE_DEVICE  = "BLE_DEVICE";
    public static final String OPERATION   = "OPERATION";
    public static final String CONNECT     = "CONNECT";
    public static final String DISCONNECT  = "DISCONNECT";
    public static final String HR_PROVIDER = "HR_PROVIDER";
    public static final String UART_PROVIDER = "UART_PROVIDER";
    public static final String ZEPHYR_HR   = "Zephyr HXM200010503";

    // Numerical types
    public static final long SCAN_PERIOD        = 20000; // 20 seconds
    public static final int REQUEST_ENABLE_BT   = 1;

    // Bluetooth SIG defined UUIDs
    public static final UUID HEART_RATE_SERVICE             = getUUID(0x180D);
    public static final UUID HEART_RATE_MEASUREMENT_CHAR    = getUUID(0x2A37);
    public static final UUID CLIENT_CHARACTERISTIC_CONFIG   = getUUID(0x2902);  // Need to configure change notifications
    public static final UUID UNKNOWN_SERVICE                = getUUID(0xFEE7);

    // UART Services defined by Nordic
    public static final UUID UART_SERVICE    = getUartUUID(0x0001);
    public static final UUID UART_TX         = getUartUUID(0x0002);    // WRITE, WRITE WITHOUT RESPONSE -> send to device
    public static final UUID UART_RX         = getUartUUID(0x0003);    // NOTIFY -> read from device


    // Broadcast Events
    public static final String APP_REGISTERED   = "com.del.delcontainer.events.APP_REGISTERED";


    /**
     * Hack to get BLE UUIDs from the fixed values
     * Replace X -> 0000XXXX-0000-1000-8000-00805f9b34fb
     * @param id
     * @return
     */
    public static UUID getUUID(int id) {
        final long MSB = 0x0000000000001000L;
        final long LSB = 0x800000805f9b34fbL;
        long value = id & 0xFFFFFFFF;
        return new UUID(MSB | (value << 32), LSB);
    }

    /**
     * Similar to the above, but returns the UART UUIDs
     * Replace X -> 6e40XXXX-b5a3-f393-e0a9-e50e24dcca9e
     * @param id
     * @return
     */
    public static UUID getUartUUID(int id) {
        final long MSB = 0x6e400000b5a3f393L;
        final long LSB = 0xe0a9e50e24dcca9eL;
        long value = id & 0xFFFFFFFF;
        return new UUID(MSB | (value << 32), LSB);
    }
}
