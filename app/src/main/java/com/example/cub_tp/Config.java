package com.example.cub_tp;

import android.Manifest;

public class Config {
    public static final int MY_PERMISSIONS_REQUEST_CODE = 1;
    public static final String[] PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE};


    public static String ANDROID_BASE_FILE_PATH = "/storage/emulated/0/";
    public static String FILENAME = "tmp";
    public static String FILE_EXTENSION = ".csv";
    public static String FILE_EXTENSION_ARFF = ".arff";
    public static String FILENAME_TRAINED_MODEL1 = "trainedModel1";
    public static String FILENAME_TRAINED_MODEL2 = "trainedModel2";
    public static String FILE_EXTENSION_MODEL = ".model";



    public static String FILENAME_SSID = "ssid";
    public static String FILE_EXTENSION_SSID = ".bin";


    public static String FILENAME_SERVER_CONFIG = "server_config";
    public static String FILE_EXTENSION_SERVER_CONFIG = ".txt";

    public static int MIN_VALUES_TO_MEAN_MEDIAN = 10;
    public static int MIN_VALUES_TO_FFT = 16;
    public static float NOISE = (float) 0.09; //used by gyroscope and accelometer
    public static double MIN_ACCURACY_TO_PREDICT = 0.9;

    //some fft values has a group os 0's... Bug on method onSensorChange
    public static int VALID_NUMBER_OF_ZEROS = 5;
    public static String WEAK_PREDICT_MESSAGE = "The system cannot predicted the activity";



    public static final String LIGHT_LOW = "LOW";
    public static final String LIGHT_NORMAL = "NORMAL";
    public static final String LIGHT_HIGH = "HIGH";
    public static final int LIGHT_MAX_VALUE = 10000;


}
