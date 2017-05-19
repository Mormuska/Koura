package net.soudunsaari.koura.sync;


import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import net.soudunsaari.koura.Control;

public class IOThread extends Thread {
    private final String TAG = "IOThread";

    public static final int ARDUINO_MESSAGE = 1;

    private final BluetoothSocket mSocket;
    private final InputStream input;
    private final OutputStream output;
    private final Handler mHandler;

    public IOThread(BluetoothSocket socket, Handler handler) {
        // Public constructor
        mSocket = socket;
        mHandler = handler;
        InputStream inTmp = null;
        OutputStream outTmp = null;
        try {
            outTmp = socket.getOutputStream();
            inTmp = socket.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
            Log.i(TAG, "Could not get input/output streams");
        }

        input = inTmp;
        output = outTmp;
    }

    public void run() {
        /*
        *  For reading serialized input data sent by Arduino
        */
        // Buffer for input stream
        byte[] buffer = new byte[1024];

        int begin = 0;
        int bytes = 0;

        while(true) {
            try {
                bytes += input.read(buffer, bytes, buffer.length - bytes);
                for (int i = begin; i < bytes; i++) {
                    if(buffer[i] == Control.DELIMITER_CHAR.getBytes()[0]) {
                        // Delimiter found, send message between indexes (begin, i) from buffer to Handler
                        // Handler will catch the message and send it to main thread for use
                        mHandler.obtainMessage(ARDUINO_MESSAGE, begin, i, buffer).sendToTarget();
                        // Look for messages past the most recent delimiter
                        begin = i + 1;

                        if(i == bytes - 1) {
                            // Reset values at the end of buffer for next read
                            bytes = 0;
                            begin = 0;
                        }

                    }
                }
            } catch (IOException e) {
                // Stop thread when streams are not working properly, or disconnect is invoked.
                break;
            }
        }

    }

    public void write(byte[] bytes) {
        try {
            // Write into bluetooth output stream
            output.write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
            Log.i(TAG, "Could not write to output stream");
        }
    }

    public void cancel() {
        try {
            // Close connection
            mSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
