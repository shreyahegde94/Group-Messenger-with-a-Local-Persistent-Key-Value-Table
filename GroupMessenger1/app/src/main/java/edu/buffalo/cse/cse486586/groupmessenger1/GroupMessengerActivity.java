package edu.buffalo.cse.cse486586.groupmessenger1;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * GroupMessengerActivity is the main Activity for the assignment.
 *
 * @author stevko
 */
public class GroupMessengerActivity extends Activity {

    static final String TAG = GroupMessengerActivity.class.getSimpleName();

    static final String[] REMOTE_PORT_ARRAY = new String[]{"11108", "11112", "11116", "11120", "11124"};
    static final int SERVER_PORT = 10000;

    // String Constants Used in the file
    public static String CONTENT = "content";
    public static String URI = "edu.buffalo.cse.cse486586.groupmessenger1.provider";
    public static String CANT_CREATE_SOCKET = "Can't create a ServerSocket";
    public static String PRESSING_SEND = "On Pressing Send - ";
    public static String RCVD_MSG_IN_SERVER = "Received msg in doInBackground Server- ";
    public static String ACKNOWLEDGE_RECEPTION_MSG = "ACKNOWLEDGE_RECEPTION_MSG";
    public static String ACKNOWLEDGE_RECEPTION = "Received Message -";
    public static String EXCEPTION_IN_SERVER = "Exception in Server -";
    public static String SENT_MSG_DOINBACKGROUND_CLIENT = "Sent msg doInBackground in Client -";
    public static String WAITING_FOR_ACK_FROM_SERVER = "In Client - Waiting for ACK from Server";
    public static String RECEIVED_ACL_FROM_SERVER = "In Client - Received ACK from Server -";
    public static String CLOSING_SOCKET = "Going to Close Socket";
    public static String CLIENT_UNKNOWNHOST_EXCEPTION = "ClientTask UnknownHostException";
    public static String CLIENT_SOCKET_IOEXCEPTION = "ClientTask socket IOException";
    public static String KEY = "key";
    public static String VALUE = "value";
    public static String SHARED_PREFERENCES_NAME = "gGroupMessenger";
    public static String INSERET_METHOD = "insert method -";
    //
    private final Uri mUri=buildUri("content", "edu.buffalo.cse.cse486586.groupmessenger1.provider");

    /**
     * References -
     * https://developer.android.com/training/basics/data-storage/shared-preferences.html
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_messenger);

        /*
         * Calculate the port number that this AVD listens on.
         * It is just a hack that I came up with to get around the networking limitations of AVDs.
         * The explanation is provided in the PA1 spec.
         */
        TelephonyManager tel = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
        final String myPort = String.valueOf((Integer.parseInt(portStr) * 2));

        TextView tv = (TextView) findViewById(R.id.textView1);
        tv.setMovementMethod(new ScrollingMovementMethod());

        try {
            /*
             * Create a server socket as well as a thread (AsyncTask) that listens on the server
             * port.
             *
             * AsyncTask is a simplified thread construct that Android provides. Please make sure
             * you know how it works by reading
             * http://developer.android.com/reference/android/os/AsyncTask.html
             */
            ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
            new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverSocket);

        } catch (IOException e) {
            Log.e(TAG,CANT_CREATE_SOCKET);
            return;
        }

        /*
         * Registers OnPTestClickListener for "button1" in the layout, which is the "PTest" button.
         * OnPTestClickListener demonstrates how to access a ContentProvider.
         */
        findViewById(R.id.button1).setOnClickListener(
                new OnPTestClickListener(tv, getContentResolver()));


        findViewById(R.id.button4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText editText = (EditText) findViewById(R.id.editText1);
                TextView tview = (TextView) findViewById(R.id.textView1);

                String msg = editText.getText().toString() + "\n";
                editText.setText("");
                tview.append("\t" + msg);

                Log.i(TAG,PRESSING_SEND + msg);
                new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, msg, myPort);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_group_messenger, menu);
        return true;
    }

    /***
     * ServerTask is an AsyncTask that should handle incoming messages. It is created by
     * ServerTask.executeOnExecutor() call in SimpleMessengerActivity.
     * <p>
     * Please make sure you understand how AsyncTask works by reading
     * http://developer.android.com/reference/android/os/AsyncTask.html
     *
     * @author stevko
     */
    private class ServerTask extends AsyncTask<ServerSocket, String, Void> {

        private String dataReceived;

        @Override
        protected Void doInBackground(ServerSocket... sockets) {
            ServerSocket serverSocket = sockets[0];
            try {
                while (true) {
                    Socket socket = null;
                    socket = serverSocket.accept();
                    BufferedReader messageReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    dataReceived = messageReader.readLine();
                    Log.i(TAG, RCVD_MSG_IN_SERVER + dataReceived);
                    // messageReader.close();

                    OutputStream os = socket.getOutputStream();
                    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os));
                    String ackMessage = ACKNOWLEDGE_RECEPTION_MSG + "\n";
                    bw.write(ackMessage);
                    bw.flush();
                    Log.i(TAG, ACKNOWLEDGE_RECEPTION + ackMessage);

                    ContentValues keyValueToInsert = new ContentValues();
                    keyValueToInsert.put(KEY, IDGeneratorUtil.StaticIDGeneratorUtil.getNewKeyId().toString());
                    keyValueToInsert.put(VALUE, dataReceived);
                    Uri newUri = getContentResolver().insert(mUri, keyValueToInsert);
                    socket.close();
                    publishProgress(dataReceived);
                }
            } catch (IOException e) {
                Log.e(TAG, EXCEPTION_IN_SERVER + dataReceived);
                e.printStackTrace();

            }
            return null;


        }

        protected void onProgressUpdate(String... strings) {
            /*
             * The following code displays what is received in doInBackground().
             */
            String strReceived = strings[0].trim();
            TextView tview = (TextView) findViewById(R.id.textView1);
            tview.append(strReceived + "\t\n");
            return;
        }
    }

    /***
     * ClientTask is an AsyncTask that should send a string over the network.
     * It is created by ClientTask.executeOnExecutor() call whenever OnKeyListener.onKey() detects
     * an enter key press event.
     *
     * @author stevko
     */
    private class ClientTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... msgs) {
            try {
                String msgToSend = msgs[0];
                for (int i = 0; i < REMOTE_PORT_ARRAY.length; i++) {
                    Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                            Integer.parseInt(REMOTE_PORT_ARRAY[i]));

                    OutputStream os = socket.getOutputStream();
                    OutputStreamWriter osw = new OutputStreamWriter(os);
                    BufferedWriter bw = new BufferedWriter(osw);
                    bw.write(msgToSend);
                    Log.i(TAG, SENT_MSG_DOINBACKGROUND_CLIENT + msgToSend);
                    bw.flush();
                    // bw.close();

                    Log.i(TAG, WAITING_FOR_ACK_FROM_SERVER);
                    InputStream input_stream = socket.getInputStream();
                    BufferedReader buffered_reader = new BufferedReader(new InputStreamReader(input_stream));
                    String msgReceived = buffered_reader.readLine();
                    if (msgReceived.equals(ACKNOWLEDGE_RECEPTION_MSG)) {
                        Log.i(TAG,RECEIVED_ACL_FROM_SERVER + msgReceived);
                        Log.i(TAG,CLOSING_SOCKET);
                        socket.close();
                    }
                }

            } catch (UnknownHostException e) {
                Log.e(TAG,CLIENT_UNKNOWNHOST_EXCEPTION);
            } catch (IOException e) {
                Log.e(TAG,CLIENT_SOCKET_IOEXCEPTION);
            }
            return null;
        }
    }

    /**
     * buildUri() demonstrates how to build a URI for a ContentProvider.
     *
     * @param scheme
     * @param authority
     * @return the URI
     */
    private Uri buildUri(String scheme, String authority) {
        Uri.Builder uriBuilder = new Uri.Builder();
        uriBuilder.authority(authority);
        uriBuilder.scheme(scheme);
        return uriBuilder.build();
    }
}