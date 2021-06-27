package com.gs.im.manager;


import com.gs.base.util.LogUtils;
import com.gs.im.common.data.DataSource;
import com.gs.im.model.db.Message;

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
    private String sessionID;

    private MyMessageHandler() {
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
                    throw new RuntimeException("find null of message!");
                }

                //处理消息
                handleMessage(message);
            }
        }
    }

    public void handleMessage(Message message){
        //处理 message
        if((message.getReceiver() !=null && sessionID.equals(message.getReceiver().getId()))||
                ( message.getGroup() !=null && sessionID.equals(message.getGroup().getId()))){
            if(messageCallback !=null)
                messageCallback.onDataLoaded(message);
        }
    }

    public void setMessageCallback(String session, DataSource.SucceedCallback<Message> messageCallback) {
        this.sessionID = session;
        this.messageCallback = messageCallback;
    }

}
