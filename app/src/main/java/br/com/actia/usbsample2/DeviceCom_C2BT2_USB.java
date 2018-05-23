package br.com.actia.usbsample2;

/**
 * Created by Gabriel on 22/05/2018.
 */

public class DeviceCom_C2BT2_USB {

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

        write(buffer);
    }
    */
/*
    public void write(byte[] out) {
        // Create temporary object
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (mState != STATE_CONNECTED) return;
            r = mConnectedThread;
        }
        // Perform the write unsynchronized
        r.write(out);
    }*/


}
