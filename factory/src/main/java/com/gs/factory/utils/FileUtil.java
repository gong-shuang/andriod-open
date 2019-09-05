package com.gs.factory.utils;

import android.util.Log;

import com.gs.base.BaseConst;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class FileUtil {

    /**
     * 删除某个文件夹下的所有文件夹和文件
     * @param file
     * @return
     */
    public static boolean deletefile(File file){
        if(!file.isDirectory()){
            if(!file.delete()){
                return false;
            }
        }else{
            File[] files = file.listFiles();
            for(File fileItem : files){
                if(fileItem.isDirectory()){
                    if(!deletefile(fileItem)){
                        return false;
                    }
                }else {
                    if(!fileItem.delete()){
                        return false;
                    }
                }
            }
            if(!file.delete()){
                return false;
            }
        }
        return true;
    }

    public static boolean doDownload(String url, final String path){
        // 1.获取OkHttpClient的对象
        OkHttpClient okHttpClient = new OkHttpClient();

        // 2.构造发送包
        Request.Builder builder = new Request.Builder();
        Request request = builder
                .get()
                .url(url)   //这个文件名和上传的文件名一样
                .build();

        // 3.获取Call对象
        Call call = okHttpClient.newCall(request);

        // 4.执行call
        // 同步阻塞执行
        Response response = null;
        try {
            response = call.execute();
            if(response.body().contentLength() == 0){
                return false;
            }

            InputStream inputStream = response.body().byteStream();

            int len = 0;
            File file = new File(path);
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            byte buf[] = new byte[10240];

            while ((len= inputStream.read(buf)) != -1){
                fileOutputStream.write(buf,0, len);
            }

            fileOutputStream.flush();
            fileOutputStream.close();
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
//        // 异步执行
//        call.enqueue(new Callback() {
//            @Override
//            public void onFailure(Call call, IOException e) {
//                Log.d("doDownload()", "onFailure");
//
//            }
//
//            @Override
//            public void onResponse(Call call, Response response) throws IOException {
//
//            }
//        });
        Log.i("doDownload()", "Download success");
        return true;
    }

    public static String getLocalFileByOSS(String netPath) {
        if (!netPath.contains("http"))
            return null;

        String fileName;
        if (netPath.contains("audio")) {
            fileName = BaseConst.AUDIO_SAVE_DIR + File.separator + "audio_" + System.currentTimeMillis() + ".mp3";
        } else if (netPath.contains("video")) {
            fileName = BaseConst.VIDEO_SAVE_DIR + File.separator + "video_" + System.currentTimeMillis() + ".mp4";
        } else if (netPath.contains("image")) {
            fileName = BaseConst.PHOTO_SAVE_DIR + File.separator + "image_" + System.currentTimeMillis() + ".jpg";
        } else if (netPath.contains("portrait")) {
            fileName = BaseConst.HEADER_SAVE_DIR + File.separator + "portrait_" + System.currentTimeMillis() + ".jpg";
        } else {
            fileName = BaseConst.FILE_SAVE_DIR + File.separator + "file_" + System.currentTimeMillis() + ".file";
        }

        if (FileUtil.doDownload(netPath, fileName) == false) {
            return null;
        }

        return fileName;
    }
}
