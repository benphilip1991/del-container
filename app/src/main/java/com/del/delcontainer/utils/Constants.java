package com.del.delcontainer.utils;

import java.util.UUID;

public class Constants {

    // String constants
    public static final String HOST_VIEW        = "HOST_VIEW";
    public static final String BLE_DEVICE       = "BLE_DEVICE";
    public static final String OPERATION        = "OPERATION";
    public static final String CONNECT          = "CONNECT";
    public static final String DISCONNECT       = "DISCONNECT";
    public static final String HR_PROVIDER      = "HR_PROVIDER";
    public static final String UART_PROVIDER    = "UART_PROVIDER";
    public static final String ISSC_PROVIDER    = "ISSC_PROVIDER";
    public static final String ZEPHYR_HR        = "Zephyr HXM200010503";

    // Scheduled Job constants
    public static final int JOB_ID              = 12345;

    // App specific identifiers
    public static final String APP_IDENT        = "APP_IDENT";
    public static final String APP_DESCRIPTION  = "APP_DESCRIPTION";
    public static final String APP_IMAGE        = "APP_IMAGE";
    public static final String APP_ID           = "appId";
    public static final String APP_NAME         = "appName";
    public static final String REQUEST          = "request";
    public static final String CALLBACK         = "callback";
    public static final String INTERVAL         = "interval";
    public static final String DEL_UTILS        = "DelUtils";

    // Data notifications
    public static final String HR_DATA          = "HR_DATA";
    public static final String DATA_TYPE        = "DATA_TYPE";
    public static final String DATA_VALUE       = "DATA_VALUE";

    // HTTP ERROR CODES
    public static final int HTTP_SUCCESS         = 200;
    public static final int HTTP_BAD_REQUEST     = 400;
    public static final int HTTP_UNAUTHORIZED    = 401;
    public static final int HTTP_NOT_FOUND       = 404;
    public static final int HTTP_CONFLICT        = 409;

    // Numerical types
    public static final int PERMISSION_REQUEST_CODE = 101;
    public static final long SCAN_PERIOD            = 20000; // 20 seconds
    public static final int REQUEST_ENABLE_BT       = 1;

    // Bluetooth SIG defined UUIDs
    public static final UUID HEART_RATE_SERVICE             = getUUID(0x180D);
    public static final UUID HEART_RATE_MEASUREMENT_CHAR    = getUUID(0x2A37);
    public static final UUID CLIENT_CHARACTERISTIC_CONFIG   = getUUID(0x2902);  // Need to configure change notifications
    public static final UUID UNKNOWN_SERVICE                = getUUID(0xFEE7);

    // UART Services defined by Nordic
    public static final UUID UART_SERVICE    = getUartUUID(0x0001);
    public static final UUID UART_TX         = getUartUUID(0x0002);    // WRITE, WRITE WITHOUT RESPONSE -> send to device
    public static final UUID UART_RX         = getUartUUID(0x0003);    // NOTIFY -> read from device

    // ISSC Proprietary Service and Characteristics
    public static final UUID ISSC_PROP_SERVICE          = UUID.fromString("49535343-FE7D-4AE5-8FA9-9FAFD205E455");
    public static final UUID ISSC_PROP_CONNECTION_PARAM = UUID.fromString("49535343-6DAA-4D02-ABF6-19569ACA69FE");
    public static final UUID ISSC_PROP_AIR_PATCH        = UUID.fromString("49535343-ACA3-481C-91EC-D85E28A60318");

    // Broadcast Events
    public static final String EVENT_APP_REGISTERED   = "com.del.delcontainer.events.APP_REGISTERED";
    public static final String EVENT_DEVICE_DATA      = "com.del.delcontainer.events.DEVICE_DATA";

    //BLE status change events
    public static  final String BLE_STATUS_RECIEVER = "BLE_STATUS_RECIEVER";
    public static  final int BLE_STATUS_CHANGED = 1;
    public static  final String BLE_STATUS = "BLE_STATUS";
    public static  final String BLE_STATUS_CONNECTED = "BLE_STATUS_CONNECTED";
    public static  final String BLE_STATUS_DISCONNECTED = "BLE_STATUS_DISCONNECTED";

    // User application operations
    public static final String APP_ADD          = "add";
    public static final String APP_DELETE       = "delete";

    //Chatbot application actions
    public static final int APP_OPEN         = 1;

    //Chatbot message types
    public static final String USER_MESSAGE          = "message_received";
    public static final String INITIAL_MESSAGE       = "hello";

    // IP addresses and hostnames
    public static final String HTTP_PREFIX          = "http://";
    public static final String HTTPS_PREFIX         = "https://";
    public static final String DEL_SERVICE_IP       = "10.24.0.8";
    public static final String DEL_PORT             = "3050";
    public static final String API_BASE_PATH        = "/api/v1/";
    public static final String APP_RESOURCE_PATH     = "application/package/";
    // Dialog information types
    public static final String DIALOG_ERROR         = "error";
    public static final String DIALOG_WARNING       = "warning";
    public static final String DIALOG_INFO          = "information";


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
