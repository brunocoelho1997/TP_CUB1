package com.example.cub_tp;

import android.Manifest;

public class Config {
    public static final int MY_PERMISSIONS_REQUEST_CODE = 1;
    public static final String[] PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE};


    public static String ANDROID_BASE_FILE_PATH = "/storage/emulated/0/";
    public static String FILENAME = "tmp";
    public static String FILE_EXTENSION = ".csv";
    public static String FILE_EXTENSION_ARFF = ".arff";


    public static String FILENAME_SSID = "ssid";
    public static String FILE_EXTENSION_SSID = ".bin";


    public static String FILENAME_SERVER_CONFIG = "server_config";
    public static String FILE_EXTENSION_SERVER_CONFIG = ".txt";

    public static int MIN_VALUES_TO_MEAN_MEDIAN = 5;
    public static int MIN_VALUES_TO_FFT = 64;

}
