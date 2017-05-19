package net.soudunsaari.koura;

import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.view.MotionEvent;
import android.view.TouchDelegate;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.AsyncTask;

import com.jmedeisis.bugstick.Joystick;
import com.jmedeisis.bugstick.JoystickListener;

import net.soudunsaari.koura.sync.IOThread;

import java.io.IOException;
import java.util.UUID;


public class Control extends AppCompatActivity {

    public static boolean DEBUG = false;

    ImageButton btnUp, btnDown, btnLeft, btnRight;


    private ProgressDialog progress;
    BluetoothAdapter myBluetooth = null;
    BluetoothSocket btSocket = null;

    Handler mHandler;
    IOThread mIOThread;

    String address = null;
    private boolean isBtConnected = false;


    public static final String DELIMITER_CHAR = "#";


    // SPP UUID (default)
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private class Servo {
        static final int ONE = 1;
        static final int TWO = 2;
    }

    private class Direction {
        static final int UP = 1;
        static final int DOWN = -1;
        static final int RIGHT = 1;
        static final int LEFT = -1;
    }


    private int speed = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        address = intent.getStringExtra(DeviceList.EXTRA_ADDRESS); // Address of target bluetooth device

        setContentView(R.layout.activity_servo_control);

        // Initialize views

        btnUp = (ImageButton) findViewById(R.id.btnUp);
        btnDown = (ImageButton) findViewById(R.id.btnDown);
        btnRight = (ImageButton) findViewById(R.id.btnRight);
        btnLeft = (ImageButton) findViewById(R.id.btnLeft);

        btnUp.setOnTouchListener(new ServoTouchListener(Servo.TWO, Direction.UP));
        btnDown.setOnTouchListener(new ServoTouchListener(Servo.TWO, Direction.DOWN));
        btnLeft.setOnTouchListener(new ServoTouchListener(Servo.ONE, Direction.LEFT));
        btnRight.setOnTouchListener(new ServoTouchListener(Servo.ONE, Direction.RIGHT));

        setupIO();
        Joystick joystick = (Joystick) findViewById(R.id.joystick);
        joystick.setJoystickListener(new JoystickListener() {
            @Override
            public void onDown() {
                // ..
            }

            @Override
            public void onDrag(float degrees, float offset) {
                // ..
                setSpeed(Servo.ONE, (int) Math.round(speed*offset*Math.cos(Math.toRadians(degrees))));
                setSpeed(Servo.TWO, (int) Math.round(speed*offset*Math.sin(Math.toRadians(degrees))));
            }

            @Override
            public void onUp() {
                // ..
                setSpeed(Servo.ONE, 0);
                setSpeed(Servo.TWO, 0);
            }
        });

    }

    private void setupIO() {
        /*
        * Connect Bluetooth to target device
        * Initialize Handler for IOThread
        * Initialize IOThread through ConnectBT after successful connection
        */



        if(!DEBUG)
            new ConnectBT().execute();


        // After IOThread has successfully parsed a received message, Handler communicates the message to main thread
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                byte[] writeBuffer = (byte[]) msg.obj;
                int start = msg.arg1;
                int end = msg.arg2;

                switch(msg.what) { // We can define different message types, see official documentation for Handler Message
                    case IOThread.ARDUINO_MESSAGE:
                        String input = new String(writeBuffer);
                        input = input.substring(start, end);
                        Log.i("Handler", "Received " + input);

                        // Parse received message for use
                        parseInput(input);
                        break;
                }
            }
        };
    }

    void parseInput(String input) {
        // Function is called through Handler when a valid message with delimiter character is received in IOThread

    }


    private void setSpeed(int servoId, int speed) {
        // Conversions
        String servoIdStr = String.valueOf(servoId);
        String speedStr = String.valueOf(speed);
        /*
            Delimiter character is used to distinguish serialized messages from each other,
            since we could be transmitting multiple messages at once.
            Delimiter character should be acknowledged in Arduino code.

            Example: output = "110#", where first 1 implies servo one, speed 10, and # is designated delimiter character

            Original problem: Without delimiter character, we could be transmitting 110020, which implies set servo 1 to speed 10020.
            Obviously originally we meant to send 1100 and 20 separately, to control two servos.

         */

        String output = servoIdStr + speedStr + DELIMITER_CHAR;

        transmit(output);
    }

    private void transmit(String output) {
        /*
        *  Write output string to bluetooth output stream
        */
        if(DEBUG) // Debug purposes where no bluetooth connection is available
            Log.i("Transmitting (debug)", output);

        if (btSocket != null) {
            Log.i("Transmitting", output);
            // Write into bluetooth output stream
            if(mIOThread != null) {
                mIOThread.write(output.getBytes());
            }
        }
    }

    private class ServoTouchListener implements View.OnTouchListener {

        int servo;
        int direction;
        private ServoTouchListener(int servo, int direction) {
            this.servo = servo;
            this.direction = direction;
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    setSpeed(servo, direction*speed);
                    break;

                case MotionEvent.ACTION_UP:
                    setSpeed(servo, 0);
                    break;
            }

            return true;
        }
    }



    private class ConnectBT extends AsyncTask<Void, Void, Void> {

        /*
        *  To avoid blocking UI thread, handle Bluetooth connecting using AsyncTask.
        *  If connecting is done on the main thread, application will be unresponsive during connecting.
        */

        private boolean connectSuccess = false;

        @Override
        protected void onPreExecute() {
            progress = ProgressDialog.show(Control.this, "Connecting...", "Please wait");
        }

        @Override
        protected Void doInBackground(Void... devices) {
            try {
                if(!DEBUG) {
                    if (btSocket == null || !isBtConnected) {
                        myBluetooth = BluetoothAdapter.getDefaultAdapter();

                        // Connects to the device's address and checks if it's available
                        BluetoothDevice targetDevice = myBluetooth.getRemoteDevice(address);

                        // Create a RFCOMM (SPP) connection
                        btSocket = targetDevice.createInsecureRfcommSocketToServiceRecord(myUUID);
                        BluetoothAdapter.getDefaultAdapter().cancelDiscovery();

                        btSocket.connect();
                        connectSuccess = true;
                    }
                }
            }
            catch (IOException e)
            {
                connectSuccess = false;
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result) //after the doInBackground, it checks if everything went fine
        {
            super.onPostExecute(result);

            if (!connectSuccess && !DEBUG)
            {
                msg("Connection failed.");
                finish();
            }
            else
            {
                msg("Connected.");
                isBtConnected = true;

                mIOThread = new IOThread(btSocket, mHandler);
                mIOThread.start();
            }
            progress.dismiss();
        }
    }






    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate top-right menu
        getMenuInflater().inflate(R.menu.menu_servo_control, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_disconnect) {

            if(mIOThread != null)
                mIOThread.cancel();

            finish(); // Return to DeviceList activity

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void msg(String s) {
        // Shorter notation for Toasts (Pop-up text messages)
        Toast.makeText(getApplicationContext(),s,Toast.LENGTH_LONG).show();
    }

}
