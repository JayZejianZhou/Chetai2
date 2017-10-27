package com.example.zejian.chetai2;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import android.os.AsyncTask;
import android.widget.TextView;

import static android.R.attr.port;

/**
 * Created by zejian on 10/26/2017.
 * This process contains socket connection and command send and read.
 */

public class Command_process {
    String dst_address;
    int dst_port;
    String response="";
    TextView textResponse;
    Socket socket;

    Command_process(String addr, int port){
        dst_address=addr;
        dst_port=port;
        //textResponse=text;


    }

    public void send_command(){
        OutputStream out_put = null;
       try {
           out_put = socket.getOutputStream();
       }catch (IOException e){
           e.printStackTrace();
       }

        PrintWriter output = new PrintWriter(out_put);

        output.println("Test fucking Socket");
    }

    public void connect(){
        /*try to connect the socket*
        */
        try{
            socket=new Socket(dst_address,dst_port);
        }catch (IOException e){
            e.printStackTrace();
            response="IOException: "+e.toString();
        }
    }

    public void disconnect(){
        try{
            socket.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

}
