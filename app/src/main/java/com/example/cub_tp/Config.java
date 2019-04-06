package com.example.cub_tp;

public class Config {
    public static final int MY_PERMISSIONS_REQUEST_GET_ACCESS_LOCATION = 1;
    public static final int MY_PERMISSIONS_REQUEST_GET_WRITE_EXTERNAL_STORAGE = 1;


    public static String ANDROID_BASE_FILE_PATH = "/storage/emulated/0/";
    public static String FILENAME = "tmp";
    public static String FILE_EXTENSION = ".csv";

    public static String FILENAME_SSID = "ssid";
    public static String FILE_EXTENSION_SSID = ".bin";




    /*Below we have declared and defined the SFTP HOST, PORT, USER
                   and Local private key from where you will make connection */
    public static String SFTPHOST = "urbysense.dei.uc.pt";
    public static int    SFTPPORT = 22;
    public static String SFTPUSER = "cubistudent";
    public static String privateKey = "mis_cubi_2019";
    public static String SFTP_WORKING_DIR = "data/a21250078/";
}