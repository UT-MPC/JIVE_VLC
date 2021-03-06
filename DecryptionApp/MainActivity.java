package com.felhr.serialportexamplesync;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Base64;

import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.util.Arrays;
import java.util.Set;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.SecretKeySpec;

import static org.bouncycastle.pqc.math.linearalgebra.ByteUtils.fromHexString;


public class MainActivity extends AppCompatActivity {
    public static TextView t;
    String finalKey = "";
    /*
     * Notifications from UsbService will be received here.
     */
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Request permission from Android to use USB
            switch (intent.getAction()) {
                case UsbService.ACTION_USB_PERMISSION_GRANTED: // USB PERMISSION GRANTED
                    Toast.makeText(context, "USB Ready", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_USB_PERMISSION_NOT_GRANTED: // USB PERMISSION NOT GRANTED
                    Toast.makeText(context, "USB Permission not granted", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_NO_USB: // NO USB CONNECTED
                    Toast.makeText(context, "No USB connected", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_USB_DISCONNECTED: // USB DISCONNECTED
                    Toast.makeText(context, "USB disconnected", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_USB_NOT_SUPPORTED: // USB NOT SUPPORTED
                    Toast.makeText(context, "USB device not supported", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };
    public static String fakeKey = "";
    String buffer;
    public int count;
    public String Encryption_Key;
    public TextView message_received;
    private UsbService usbService;
    private TextView display;
    private EditText editText;
    private CheckBox box9600, box38400;
    private MyHandler mHandler;
    private static final Object locker = new Object();
    private final ServiceConnection usbConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName arg0, IBinder arg1) {
            usbService = ((UsbService.UsbBinder) arg1).getService();
            usbService.setHandler(mHandler);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            usbService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        t = findViewById((R.id.encryption_received));           //layout
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mHandler = new MyHandler(this);
        message_received = (TextView) findViewById(R.id.encryption_received);       //wait for key received
        message_received.setText("No Decryption Key Received");
        display = (TextView) findViewById(R.id.textView1);
        editText = (EditText) findViewById(R.id.editText1);
        Button sendButton = (Button) findViewById(R.id.buttonSend);
        Encryption_Key = null;
        this.count = 0;
        sendButton.setOnClickListener(new View.OnClickListener() {          //wait for button to be pressed
            @Override
            public void onClick(View v) {
                if (!editText.getText().toString().equals("")) {
                    String data = editText.getText().toString();
                    if (usbService != null) {                        // if UsbService was correctly binded, Send data
                        usbService.write(data.getBytes());
                    }
                }
            }
        });


        Button baudrateButton = (Button) findViewById(R.id.buttonBaudrate);
        baudrateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                usbService.keepBaudRate();                  //set the Baud rate to the same as the listener

            }
        });
        char arr[] = new char [128];

        String encrypt = "";
        Log.d("Char array", arr.toString());
        //encrypt = new String(arr);
        //encrypt = "111111111";
        Log.d("Final char encrypt seq", encrypt);

        try{
            if(finalKey.length()>128)
                finalKey = finalKey.substring(0,128);                       //ensure that the key is 128bits

            encrypt = encrypt.concat(finalKey);                            //concat to create encrypt
        }
        catch (Exception e){
            message_received.setText("ERROR IN GETTING LENGTH!");
            Log.d("trace ", e.getClass().getName());
        }


        Button encryptButton = (Button) findViewById(R.id.encrypt_button);      //button to encrypt data

        String finalEncrypt = encrypt;
        Log.d("Final Encrypt Key", finalEncrypt);
        encryptButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                Log.d("Here", "In the click");
                Security.addProvider(new BouncyCastleProvider());           //add the encryption provider
                EditText inputString = findViewById(R.id.message_input);
                String input = inputString.getText().toString();            //get the message and store
                Log.d("Input String", input);
                TextView enText = findViewById(R.id.encrypted_message);
                //  enText.setText(input);
                byte[] keyBytes;
                TextView encryptReceive = findViewById(R.id.encryption_received);



                //TODO Change the encryption key to get it from USB through arduino

                String[] bigKey = new String[16];
                int index = 0;
                int base = 0;
                for (int x = 8; x <= 128; x += 8) { //parse this into byte strings
                    bigKey[index] = finalEncrypt.substring(base, x);
                    base += 8;
                    index++;
                }
                byte[] newKeyByte = new byte[16];
                for (int x = 0; x < bigKey.length; x++) { // split the byte into 2 4 bit messages and convert them to hex
                    byte high, low;
                    String stringHigh = bigKey[x].substring(0, 4);
                    String stringLow = bigKey[x].substring(4, 8);
                    String ans = "";
                    high = (byte) (Byte.parseByte(stringHigh, 2));
                    ans += Integer.toHexString(high);
                    low = (byte) (Byte.parseByte(stringLow, 2));
                    ans += Integer.toHexString(low);
                    high = (byte) (high << 4);
                    int unsignedByte = 0x0ff & Integer.parseInt(ans, 16); // add the 2 messages together and in the array
                    newKeyByte[x] = (byte) unsignedByte;
                    Log.d("Byteind", Integer.toString(newKeyByte[x]));
                } // end parsing
                Log.d("Byte", Arrays.toString(newKeyByte));
                keyBytes = newKeyByte;
                SecretKeySpec key = new SecretKeySpec(keyBytes, "AES"); // using AES with the 128 bit key
                Log.d("Secret Key", key.toString());
                Log.d("KeyByte", keyBytes.toString());
                Cipher cipher = null;

                //try-catch statements for exceptions
                try {
                    cipher = Cipher.getInstance("AES", "BC");
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (NoSuchPaddingException e) {
                    e.printStackTrace();
                } catch (NoSuchProviderException e) {
                    e.printStackTrace();
                }
                Log.d("Cipher get Instance", cipher.toString());
//
                try {
                    cipher = Cipher.getInstance("AES/ECB/PKCS7Padding", "BC");
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (NoSuchProviderException e) {
                    e.printStackTrace();
                } catch (NoSuchPaddingException e) {
                    e.printStackTrace();
                }
                try {
                    cipher.init(Cipher.DECRYPT_MODE, key);
                } catch (InvalidKeyException e) {
                    e.printStackTrace();
                    System.exit(-2);
                }
                Log.d("DecryptSt", cipher.toString());
                int ctLength = input.length();
                byte[] plainText = new byte[cipher.getOutputSize(ctLength)];        //decrypted message array
                Log.d("plainTextSize", Integer.toString(plainText.length));
                int ptLength = 0;
                try {
                    try {
                        ptLength = cipher.update(input.getBytes("Cp1252"), 0, ctLength, plainText,0);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                } catch (ShortBufferException e) {
                    e.printStackTrace();
                }
                Log.d("ptLength", Integer.toString(ptLength));
                try {
                    ptLength +=cipher.doFinal(plainText, ptLength);     //set the plain Text Length
                } catch (IllegalBlockSizeException e) {
                    e.printStackTrace();
                } catch (ShortBufferException e) {
                    e.printStackTrace();
                } catch (BadPaddingException e) {
                    e.printStackTrace();
                }
                Log.d("ptLength", Integer.toString(ptLength));
                //int ctLength = c
                // ipher.update(input, 0, input.length, cipherText, 0);
                String finalans = "X";
                try {
                    finalans =new String(plainText, "Cp1252");      //using cp1252 encoding set the plain text
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                Log.d("Plain Text", finalans);
                //String plainT = new String(plainText);
                enText.setText(finalans);                                       //print final answer
                Log.d("Plain Text length", Integer.toString(ptLength));
            }
        });




    }

    @Override
    public void onResume() {
        super.onResume();
        setFilters();                                               // Start listening notifications from UsbService
        startService(UsbService.class, usbConnection, null); // Start UsbService(if it was not started before) and Bind it
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(mUsbReceiver);
        unbindService(usbConnection);
    }

    private void startService(Class<?> service, ServiceConnection serviceConnection, Bundle extras) {
        if (!UsbService.SERVICE_CONNECTED) {
            Intent startService = new Intent(this, service);        //setup start service
            if (extras != null && !extras.isEmpty()) {
                Set<String> keys = extras.keySet();
                for (String key : keys) {
                    String extra = extras.getString(key);
                    startService.putExtra(key, extra);
                }
            }
            startService(startService);
        }
        Intent bindingIntent = new Intent(this, service);
        bindService(bindingIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private void setFilters() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbService.ACTION_USB_PERMISSION_GRANTED);
        filter.addAction(UsbService.ACTION_NO_USB);
        filter.addAction(UsbService.ACTION_USB_DISCONNECTED);
        filter.addAction(UsbService.ACTION_USB_NOT_SUPPORTED);
        filter.addAction(UsbService.ACTION_USB_PERMISSION_NOT_GRANTED);
        registerReceiver(mUsbReceiver, filter);
    }

    /*
     * This handler will be passed to UsbService. Data received from serial port is displayed through this handler
     */
    private class MyHandler extends Handler {
        private final WeakReference<MainActivity> mActivity;

        public MyHandler(MainActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            //handle the message based on whats passed through the USB Serial port
            switch (msg.what) {
                case UsbService.MESSAGE_FROM_SERIAL_PORT:
                    String data = (String) msg.obj;
                    mActivity.get().display.append(data);
                    break;
                case UsbService.CTS_CHANGE:
                    Toast.makeText(mActivity.get(), "CTS_CHANGE", Toast.LENGTH_LONG).show();
                    break;
                case UsbService.DSR_CHANGE:
                    Toast.makeText(mActivity.get(), "DSR_CHANGE", Toast.LENGTH_LONG).show();
                    break;
                case UsbService.SYNC_READ:

                    //synchronized(locker){
                    mActivity.get().buffer = (String) msg.obj;
                    finalKey+=mActivity.get().buffer;
                    mActivity.get().Encryption_Key=(String) msg.obj;
                    Log.d("myTag", mActivity.get().buffer);
                    Log.d("Final Key", finalKey);
                    mActivity.get().display.setText(mActivity.get().buffer);
                    if(mActivity.get().count==0) {
                        mActivity.get().count++;

                    }
                    char arr[] = new char [128];

                    String encrypt = "";
                    Log.d("Char array", arr.toString());

                    Log.d("Final char encrypt seq", encrypt);

                    try{
                        if(finalKey.length()>128)
                            finalKey = finalKey.substring(0,128);       //ensure that the key is 128bit
                        if(finalKey.length()>=120)
                            message_received.setText("Key Received");      //notify that the key is received
                        encrypt = encrypt.concat(finalKey);
                    }
                    catch (Exception e){
                        message_received.setText("ERROR IN GETTING LENGTH!");
                        Log.d("trace ", e.getClass().getName());
                    }


                    Button encryptButton = (Button) findViewById(R.id.encrypt_button);

                    String finalEncrypt = encrypt;
                    Log.d("Final Encrypt Key", finalEncrypt);
                    encryptButton.setOnClickListener(new View.OnClickListener() {

                        public void onClick(View v) {
                            Log.d("Here", "In the click");
                            Security.addProvider(new BouncyCastleProvider());               //using Bouncy Castle
                            EditText inputString = findViewById(R.id.message_input);
                            String input = inputString.getText().toString();                //get input string

                            Log.d("Input String", input);
                            TextView enText = findViewById(R.id.encrypted_message);
                            //  enText.setText(input);
                            byte[] keyBytes;

                            TextView encryptReceive = findViewById(R.id.encryption_received);


                            //TODO Change the encryption key to get it from USB through arduino
                            //encryption schema
                            String[] bigKey = new String[16];
                            int index = 0;
                            int base = 0;
                            for (int x = 8; x <= 128; x += 8) {             //parse this into byte strings
                                bigKey[index] = finalEncrypt.substring(base, x);
                                base += 8;
                                index++;
                            }
                            byte[] newKeyByte = new byte[16];
                            for (int x = 0; x < bigKey.length; x++) { // split the byte into 2 4 bit messages and convert them to hex
                                byte high, low;
                                String stringHigh = bigKey[x].substring(0, 4);
                                String stringLow = bigKey[x].substring(4, 8);
                                String ans = "";
                                high = (byte) (Byte.parseByte(stringHigh, 2));
                                ans += Integer.toHexString(high);
                                low = (byte) (Byte.parseByte(stringLow, 2));
                                ans += Integer.toHexString(low);
                                high = (byte) (high << 4);
                                int unsignedByte = 0x0ff & Integer.parseInt(ans, 16); // add the 2 messages together and in the array
                                //  Log.d("byte", Integer.toString(unsignedByte));
                                newKeyByte[x] = (byte) unsignedByte;
                                Log.d("Byteind", Integer.toString(newKeyByte[x]));
                            } // end parsing
                            Log.d("Byte", Arrays.toString(newKeyByte));
                            keyBytes = newKeyByte;
                            SecretKeySpec key = new SecretKeySpec(keyBytes, "AES"); // using AES with the 128 bit key
                            Log.d("Secret Key", key.toString());
                            Log.d("KeyByte", keyBytes.toString());
                            Cipher cipher = null;

                            //try-catch statements for Exceptions
                            try {
                                cipher = Cipher.getInstance("AES", "BC");
                            } catch (NoSuchAlgorithmException e) {
                                e.printStackTrace();
                            } catch (NoSuchPaddingException e) {
                                e.printStackTrace();
                            } catch (NoSuchProviderException e) {
                                e.printStackTrace();
                            }
                            Log.d("Cipher get Instance", cipher.toString());
//
                            try {
                                cipher = Cipher.getInstance("AES/ECB/PKCS7Padding", "BC");
                            } catch (NoSuchAlgorithmException e) {
                                e.printStackTrace();
                            } catch (NoSuchProviderException e) {
                                e.printStackTrace();
                            } catch (NoSuchPaddingException e) {
                                e.printStackTrace();
                            }
                            try {
                                // cipher.init(Cipher.DECRYPT_MODE, key);
                                cipher.init(Cipher.DECRYPT_MODE, key);
                            } catch (InvalidKeyException e) {
                                e.printStackTrace();
                                System.exit(-2);
                            }
                            Log.d("DecryptSt", cipher.toString());
                            int ctLength = input.length();
                            byte[] plainText = new byte[cipher.getOutputSize(ctLength)];    //setup plain text size
                            Log.d("plainTextSize", Integer.toString(plainText.length));
                            int ptLength = 0;
                            //statements to ensure that there are no exceptions
                            try {
                                try {
                                    ptLength = cipher.update(input.getBytes("Cp1252"), 0, ctLength, plainText,0);
                                } catch (UnsupportedEncodingException e) {
                                    e.printStackTrace();
                                }
                            } catch (ShortBufferException e) {
                                e.printStackTrace();
                            }
                            Log.d("ptLength", Integer.toString(ptLength));
                            try {
                                ptLength +=cipher.doFinal(plainText, ptLength);
                            } catch (IllegalBlockSizeException e) {
                                e.printStackTrace();
                            } catch (ShortBufferException e) {
                                e.printStackTrace();
                            } catch (BadPaddingException e) {
                                e.printStackTrace();
                            }
                            Log.d("ptLength", Integer.toString(ptLength));

                            String finalans = "X";
                            try {
                                finalans =new String(plainText, "Cp1252");
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
                            Log.d("Plain Text", finalans);
                            //String plainT = new String(plainText);
                            //enText.setText(Integer.toString(ptLength));
                            Log.d("Plain Text length", Integer.toString(ptLength));

                            enText.setText(finalans);               //print out final ansert
                        }
                    });



                    break;

            }
        }
        //function to convert string to a byte array
        public byte[] fromHexString(String s) {
            int len = s.length();
            byte[] data = new byte[len / 2];
            for (int i = 0; i < len; i += 2) {
                data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                        + Character.digit(s.charAt(i+1), 16));
            }
            return data;
        }
    }
}