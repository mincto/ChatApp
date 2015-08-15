package com.study.chatapp;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.net.Socket;


public class MainActivity extends Activity{
    TextView txt_area;
    EditText txt_input;

    Socket client;
    String ip="192.168.0.137";
    int port=7777;
    Thread thread; /*접속 시도용 쓰레드*/
    Handler handler;
    /*개발자가 정의한 쓰레드는 UI를 제어할 수 없다.
        이유? 메인쓰레드와 충돌날 가능성이 있으므로,
        따라서 UI 에 대한 제어는 오직 메인쓰레드만이 할 수 있고, 메인쓰레드에게
        UI제어를 부탁하려면, 중간에서 Handler에게 접수해야 한다!
    */

    /*대화용 쓰레드 작동하기 위함*/
    ClientThread clientThread;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txt_area = (TextView)findViewById(R.id.txt_area);
        txt_input = (EditText)findViewById(R.id.txt_input);

        handler = new Handler(){
            /* 동생쓰레드가 sendMessage 메서드를 호출하면, 아래의
            * 메서드가 동작하며, 그 주된 내용은 UI제어이다!!*/
            public void handleMessage(Message message) {
                Bundle bundle=message.getData();
                String msg=bundle.getString("msg");
                String result=bundle.getString("result");

                printInfo(msg);
                if(result.equals("OK")){
                    /*대화란, 접속이후의 시점에 가능하므로, 위의 쓰레드보다 시점을
                    더 늦춘다!!*/
                    clientThread = new ClientThread(client, txt_area);
                    clientThread.start(); /*서버의 메세지 청취하기 시작!!*/
                }
            }
        };

        thread = new Thread(){
            /*메인 쓰레드와는 독립되서 수행시키고 싶은 기능은 run에 기재*/
            public void run() {
                Message message = new Message();
                Bundle bundle = new Bundle();

                if(connect()){
                    bundle.putString("msg", "접속 성공");
                    bundle.putString("result", "OK");
                }else{
                    bundle.putString("msg","접속 실패");
                    bundle.putString("result", "FAIL");
                }
                message.setData(bundle);
                handler.sendMessage(message);
            }
        };

        thread.start(); /*Runnable 상태로 진입시킴!!*/

    }

    /*채팅 서버에 접속하는 메서드*/
    public boolean connect(){
        boolean result=false;
        try {
            client = new Socket(ip,port); /*접속발생*/
            result=true;
        } catch (IOException e) {
            e.printStackTrace();
            result=false;
        }
        return result;
    }

    public void printInfo(String msg){
        Toast.makeText(this, msg , Toast.LENGTH_SHORT).show();
    }

    public void btnClick(View view){
        /*대화용 쓰레드인 CilentThread가 보유한 send() 메서드에
         보내고 싶은 메세지를 인수로 전달한 후 호출!! = 서버에 말걸기!
        */
        String msg = txt_input.getText().toString();
        clientThread.send(msg);

        /*입력한 글씨 다시 지우기 */
        txt_input.setText("");
    }
}








