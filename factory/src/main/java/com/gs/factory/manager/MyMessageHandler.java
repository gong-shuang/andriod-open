package com.gs.factory.manager;


import com.gs.base.util.LogUtils;
import com.gs.factory.common.data.DataSource;
import com.gs.factory.model.db.Message;
import com.gs.factory.model.db.Session;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 数据结构，对应有3个，一个是网络，一个是DB，一个是UI的。
 * 比如消息的话，网络：Card
 *
 * 现在只做UI的操作
 *
 */
public class MyMessageHandler {
    private static MyMessageHandler instance = new MyMessageHandler();
    private LinkedBlockingQueue<Message> queue = new LinkedBlockingQueue();
    private MyThread myThread;
    private DataSource.SucceedCallback<Message> messageCallback;
    private DataSource.SucceedCallback<Session> sessionCallback;

    public MyMessageHandler() {
        myThread = new MyThread("MyMessage");
        myThread.start();
    }

    public static MyMessageHandler getInstance(){
        return instance;
    }

    //发送消息，http接受消息，推送接受消息，都走这个逻辑
    public void add(Message message){
        queue.add(message);
    }

    class MyThread extends Thread{
        private String title;

        public MyThread(String title) {
            super(title);
        }

        @Override
        public void run() {
            while (true){
                Message message = null;  //如果为空，会阻塞
                try {
                    message = queue.take();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if(message ==null){
                    //错误
                    LogUtils.e("My Message queue is null");
                }

                //处理消息
                handleMessage(message);
            }
        }
    }

    public void handleMessage(Message message){
        //处理 message
        if(messageCallback !=null)
            messageCallback.onDataLoaded(message);

//        Session session = Session.createSessionIdentify(message);

        //处理 session
//        if(sessionCallback != null)
//            sessionCallback.onDataLoaded(session);

    }

    public void setMessageCallback(DataSource.SucceedCallback<Message> messageCallback) {
        this.messageCallback = messageCallback;
    }

    public void setSessionCallback(DataSource.SucceedCallback<Session> sessionCallback) {
        this.sessionCallback = sessionCallback;
    }
}
