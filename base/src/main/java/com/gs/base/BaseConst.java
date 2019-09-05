package com.gs.base;

import com.gs.base.util.FileUtils;

public class BaseConst {

    //语音存放位置
    public static final String AUDIO_SAVE_DIR = FileUtils.getDir("audio");
    public static final int DEFAULT_MAX_AUDIO_RECORD_TIME_SECOND = 120;
    //视频存放位置
    public static final String VIDEO_SAVE_DIR = FileUtils.getDir("video");
    //照片存放位置
    public static final String PHOTO_SAVE_DIR = FileUtils.getDir("photo");
    //头像保存位置
    public static final String HEADER_SAVE_DIR = FileUtils.getDir("header");
    //文件存放位置
    public static final String FILE_SAVE_DIR = FileUtils.getDir("files");
}
