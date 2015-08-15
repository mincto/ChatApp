/*
 사용자가 입력버튼을 누르지 않더라도, 서버에서 보내오는 메세지는 언제나
 청취가능해야 하므로, 무한루프로 메세지를 청취할 쓰레드가 필요하다!!
 */
package com.study.chatapp;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class ClientThread extends Thread{
    Socket client;/*단지 스트림을 얻기위한 레퍼런스*/
    BufferedReader buffr;
    BufferedWriter buffw;
    TextView txt_area;
    Handler handler;

    public ClientThread(Socket client, TextView area) {
        this.client=client;
        this.txt_area=area;

        try {
            buffr = new BufferedReader(new InputStreamReader(client.getInputStream(),"UTF-8"));
            buffw = new BufferedWriter(new OutputStreamWriter(client.getOutputStream(),"UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        handler = new Handler(){
            public void handleMessage(Message message) {
                Bundle bundle=message.getData();
                String msg=bundle.getString("msg");
                txt_area.append(msg+"\n");/*UI제어는 쓰레드가 못한다!*/
            }
        };

    }

    public void run() {
        listen();
    }

    /*서버가 보내온 메세지를 청취(입력)*/
    public void listen(){

        try {
            while(true){
                String msg=null;
                msg=buffr.readLine();

                Message message = new Message();
                Bundle bundle = new Bundle();
                bundle.putString("msg", msg);
                message.setData(bundle);

                handler.sendMessage(message);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /*서버에게 메세지를 전송 (출력)*/
    public void send(String msg){
        try {
            buffw.write(msg+"\n");
            buffw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}










