package br.com.actia.usbsample2;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.hardware.usb.UsbRequest;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import br.com.actia.usbsample2.R;

import static android.R.attr.data;

/**
 * Created by Gabriel  on 22/05/2018.
 */

public class UsbTestActivity extends Activity implements View.OnClickListener {

    private UsbManager mUsbManager;
    private UsbDevice mDevice;
    private UsbDeviceConnection mConnection = null;
    private UsbEndpoint mEndpointIntr;
    private static PendingIntent mPermissionIntent;
    private static final String TAG = "UsbTestActivity";
    byte buffer[] = new byte[16];
    private Button btSend;

    static public final byte CAN_CMD_POWER_ON = 0x01;
    //static public final byte[] DEFAULT_NO_CMD = "00ff00ffffffffff".getBytes();

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usb_test);
        mUsbManager = (UsbManager)getSystemService(Context.USB_SERVICE);
        Log.d(TAG, "onCreate");

        btSend = findViewById(R.id.button);
        btSend.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button
                try2();
            }
        });
    }

    @Override
    public void onPause(){
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "Running...");
        //try2();
    }

    public void try1(){
        Log.d(TAG, " try1 Running...");

        //UsbManager usbManager = getSystemService(UsbManager.class);
        //UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        Map<String, UsbDevice> connectedDevices = mUsbManager.getDeviceList();
        for (UsbDevice device : connectedDevices.values()) {
            //if (device.getVendorId() == 0x2341 && device.getProductId() == 0x0001) {
            Log.i(TAG, "Device found: " + device.getDeviceName());
            //startSerialConnection(usbManager, device);
            //break;
            //}
        }
    }

    public void try2(){
        Log.d(TAG, " try2 Running...");

        // This snippet will open the first usb device connected, excluding usb root hubs
        UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        UsbDevice device = null;
        UsbDeviceConnection connection = null;
        HashMap<String, UsbDevice> usbDevices = usbManager.getDeviceList();
        if(!usbDevices.isEmpty())
        {
            boolean keep = true;
            for(Map.Entry<String, UsbDevice> entry : usbDevices.entrySet())
            {
                device = entry.getValue();
                int deviceVID = device.getVendorId();
                int devicePID = device.getProductId();
                if(deviceVID != 0x1d6b || (devicePID != 0x0001 || devicePID != 0x0002 || devicePID != 0x0003))
                {
                    // We are supposing here there is only one device connected and it is our serial device
                    //usbManager.requestPermission(device, mPermissionIntent);
                    connection = usbManager.openDevice(device);

                    keep = false;
                }else
                {
                    connection = null;
                    device = null;
                    Log.d(TAG, " unknow device");
                }

                if(!keep)
                    break;
            }
        }else{
            Log.d(TAG, " usbDevices is empty!...");
        }
        // A callback for received data must be defined
        UsbSerialInterface.UsbReadCallback mCallback = new UsbSerialInterface.UsbReadCallback()
        {
            @Override
            public void onReceivedData(byte[] arg0)
            {
                Log.d(TAG, "Data received!");
                // Code here
            }
        };

        UsbSerialDevice serialPort = UsbSerialDevice.createUsbSerialDevice(device, connection);
        if(serialPort != null)
        {
            if(serialPort.open())
            {
                // Devices are opened with default values, Usually 9600,8,1,None,OFF
                // CDC driver default values 115200,8,1,None,OFF
                serialPort.setBaudRate(250000);
                serialPort.setDataBits(UsbSerialInterface.DATA_BITS_8);
                serialPort.setStopBits(UsbSerialInterface.STOP_BITS_1);
                serialPort.setParity(UsbSerialInterface.PARITY_NONE);
                serialPort.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF);
                serialPort.read(mCallback);
            }else
            {
                // Serial port could not be opened, maybe an I/O error or it CDC driver was chosen it does not really fit
                Log.d(TAG, "Serial closed = null");
            }
        }else
        {
            Log.d(TAG, "Serial port = null");
            // No driver for given device, even generic CDC driver could not be loaded
        }
        /*
        UsbSerialInterface.UsbFrameCallback mFrameCallBack = new UsbSerialInterface.UsbFrameCallback(){

            @Override
            public void onFramingError() {
                Log.d(TAG, "UsbFrameCallback error!!!");
            }
        };*/

        //serialPort.getFrame(mFrameCallBack);



        CanMSG canMSG = new CanMSG();
        canMSG.setId(CanMSG.MSGID_EQUIPAMENT_CTRL);
        canMSG.setLength((byte) 8);
        canMSG.setType(CanMSG.MSGTYPE_EXTENDED);
        canMSG.buildCmdData(CAN_CMD_POWER_ON);

        int msgId = canMSG.getId();
        byte msgData[] = canMSG.getData();
        int checkSum = 0;

        //write(getNoCommandBuffer()); //write no command
        //Log.d(TAG, "### SEND NO COMMAND");

        buffer[0] = 0x55;
        buffer[1] = 0x40; //Extended frame
        buffer[2] = (byte)((msgId & 0xFF000000) >> 24);
        buffer[3] = (byte)((msgId & 0x00FF0000) >> 16);
        buffer[4] = (byte)((msgId & 0x0000FF00) >> 8);
        buffer[5] = (byte)((msgId & 0x000000FF));
        buffer[6] = 0x08;
        buffer[7] = msgData[0];
        buffer[8] = msgData[1];
        buffer[9] = msgData[2];
        buffer[10] = msgData[3];
        buffer[11] = msgData[4];
        buffer[12] = msgData[5];
        buffer[13] = msgData[6];
        buffer[14] = msgData[7];

        for(int i = 0; i < buffer.length - 1; i++) {
            checkSum += buffer[i];
            System.out.printf("DATA[%d] = %X\n", i, buffer[i]);
        }
        buffer[15] = (byte) (checkSum & 0xFF);

        //serialPort.write("Hola!".getBytes());
        //serialPort.write(buffer);

        for(int i=0 ; i <= 1 ; i++){
            serialPort.write(buffer);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Log.d(TAG,"Writed: "+i);
        }


        serialPort.getFrame(new UsbSerialInterface.UsbFrameCallback() {
            @Override
            public void onFramingError() {

            }
        });

    }

    @Override
    public void onClick(View v) {
        Log.d(TAG,"Button pressed");
    }
    /*
    public synchronized void sendCommand(CanMSG canMSG) {
        byte buffer[] = new byte[16];
        int msgId = canMSG.getId();
        byte msgData[] = canMSG.getData();
        int checkSum = 0;

        //write(getNoCommandBuffer()); //write no command
        //Log.d(TAG, "### SEND NO COMMAND");

        buffer[0] = 0x55;
        buffer[1] = 0x40; //Extended frame
        buffer[2] = (byte)((msgId & 0xFF000000) >> 24);
        buffer[3] = (byte)((msgId & 0x00FF0000) >> 16);
        buffer[4] = (byte)((msgId & 0x0000FF00) >> 8);
        buffer[5] = (byte)((msgId & 0x000000FF));
        buffer[6] = 0x08;
        buffer[7] = msgData[0];
        buffer[8] = msgData[1];
        buffer[9] = msgData[2];
        buffer[10] = msgData[3];
        buffer[11] = msgData[4];
        buffer[12] = msgData[5];
        buffer[13] = msgData[6];
        buffer[14] = msgData[7];

        for(int i = 0; i < buffer.length - 1; i++) {
            checkSum += buffer[i];
            //System.out.printf("DATA[%d] = %X\n", i, buffer[i]);
        }
        buffer[15] = (byte) (checkSum & 0xFF);
        //System.out.printf("DATA[%d] = %X\n", 15, buffer[15]);
        write(buffer);*/
    }
