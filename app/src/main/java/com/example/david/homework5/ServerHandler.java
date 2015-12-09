package com.example.david.homework5;
import android.app.Activity;
import android.os.AsyncTask;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;
/**
 * Created by David on 12/8/2015.
 */
public class ServerHandler extends AsyncTask<Boolean,String, Integer> {
    private final static String SOCKET_CONNECTION_FAILURE="Socket couldn't connect to host";

    private final int port;
    private final String host;
    private final IActivity gui;
    private final LinkedBlockingQueue<String> commands = new LinkedBlockingQueue<>();
    private BufferedInputStream in;
    private BufferedOutputStream out;
    private Socket socket;

    public ServerHandler(IActivity gui, int port , String host){
        this.port=port;
        this.host=host;
        this.gui=gui;
    }

    @Override
    protected Integer doInBackground(Boolean... params) {
        if(!connect()){
            publishProgress("error","Couldn't connect to ["+host+":"+port+"]");
            return null;
        }

        publishProgress("connected","true");
        byte[] message=new byte[1024];
        try{
            while(true){
                try {
                    synchronized(this){
                        while(commands.isEmpty()){
                            System.out.println("Client: Waiting for command");
                            this.wait();
                        }
                        System.out.println("Client: Writing command");
                        out.write(commands.take().getBytes());
                        out.flush();

                        String response=getServerResponse(message);
                        System.out.println("Client: Server response = " + response);
                        publishProgress("message", response);
                    }
                } catch (IOException ex) {
                    publishProgress("error", "Connection failed catastrophically. Couldn't connect.");
                }
            }
        }catch(Exception e){

        }
        return null;
    }


    @Override
    protected void onProgressUpdate(String... values){
        if(values.length!=2)
            return;

        if(values[0].equals("message")){
            gui.messageReceived(values[1]);
        } else if(values[0].equals("status")){
            gui.statusChanged(values[1]);
        } else if(values[0].equals("connected")){
            gui.connected();
        } else if(values[0].equals("error")){
            gui.error(values[1]);
        }
    }

    public void addCommand(String command){
        System.out.println("Client: Added command = "+command);
        commands.add(command);
        final ServerHandler cu=this;
        (new Thread(new Runnable(){
            @Override
            public void run() {
                synchronized(cu){
                    cu.notify();
                }
            }
        })).start();
    }

    private String getServerResponse(byte[] message) throws IOException {
        int bytesRead=0,n;
        while((n=in.read(message, bytesRead, 256))!=-1){
            bytesRead+=n;
            if(bytesRead==1024)
                break;

            if(in.available()==0)
                break;
        }
        String response=new String(message,0,bytesRead);
        return response;
    }

    public boolean connect(){
        try {
            socket=new Socket(host,port);
            in=new BufferedInputStream(socket.getInputStream());
            out=new BufferedOutputStream(socket.getOutputStream());
            System.out.println("Client: Created socket with host = "+host+", port = "+port);
            return true;
        } catch (IOException ex) {
            publishProgress("status", SOCKET_CONNECTION_FAILURE);
            return false;
        }
    }

}

