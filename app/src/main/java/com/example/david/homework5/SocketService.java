package com.example.david.homework5;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;


/**
 * Created by David on 12/9/2015.
 */
public class SocketService extends IntentService {

    // Defines a custom Intent action
    public static final String BROADCAST_ACTION =
            "com.example.android.threadsample.BROADCAST";

    // Defines the key for the status "extra" in an Intent
    public static final String EXTENDED_DATA_STATUS =
            "com.example.android.threadsample.STATUS";

    public static final String EXTENDED_DATA_TYPE =
            "com.example.android.threadsample.TYPE";

    private final static String SOCKET_CONNECTION_FAILURE = "Socket couldn't connect to host";

    private static SocketService intHandler;
    private static int port;
    private static String host;
    private static LinkedBlockingQueue<String> commands = new LinkedBlockingQueue<>();
    private BufferedInputStream in;
    private BufferedOutputStream out;
    private Socket socket;


    public SocketService() {
        super("SocketService");
    }

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public SocketService(int port, String host) {
        super("SocketService");
        this.port = port;
        this.host = host;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        intHandler=this;
        Log.d("SocketService", "this = " + this.toString());
        if (!connect()) {
            sendToUI("error", "Couldn't connect to [" + host + ":" + port + "]");
        }

        sendToUI("connected", "true");
        byte[] message = new byte[1024];
        try {
            while (true) {
                try {
                    synchronized (commands) {
                        while (commands.isEmpty()) {
                            System.out.println("Client: Waiting for command");
                            commands.wait();
                        }
                        System.out.println("Client: Writing command");
                        out.write(commands.take().getBytes());
                        out.flush();

                        String response = getServerResponse(message);
                        System.out.println("Client: Server response = " + response);
                        sendToUI("message", response);
                    }
                } catch (IOException ex) {
                    sendToUI("error", "Connection failed catastrophically. Couldn't connect.");
                }
            }
        } catch (Exception e) {

        }
    }

    void sendToUI(String type, String status) {
        Intent localIntent =
                new Intent(BROADCAST_ACTION);
        // Puts the status into the Intent
        localIntent.putExtra(EXTENDED_DATA_STATUS, status);
        localIntent.putExtra(EXTENDED_DATA_TYPE, type);
        // Broadcasts the Intent to receivers in this app.
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }

    public void addCommand(String command) {
        System.out.println("Client: Added command = " + command);
        Log.d("SocketService", "this (addCommand) = " + this.toString() + ", |" + command + "|");
        commands.add(command);
        final SocketService cu = intHandler;
        (new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (commands) {
                    commands.notify();
                }
            }
        })).start();
    }

    private String getServerResponse(byte[] message) throws IOException {
        int bytesRead = 0, n;
        while ((n = in.read(message, bytesRead, 256)) != -1) {
            bytesRead += n;
            if (bytesRead == 1024)
                break;

            if (in.available() == 0)
                break;
        }
        String response = new String(message, 0, bytesRead);
        return response;
    }

    public boolean connect() {
        try {
            socket = new Socket(host, port);
            in = new BufferedInputStream(socket.getInputStream());
            out = new BufferedOutputStream(socket.getOutputStream());
            System.out.println("Client: Created socket with host = " + host + ", port = " + port);
            return true;
        } catch (IOException ex) {
            sendToUI("status", SOCKET_CONNECTION_FAILURE);
            return false;
        }
    }
}
