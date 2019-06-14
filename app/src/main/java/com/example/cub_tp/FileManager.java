package com.example.cub_tp;

import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import static com.example.cub_tp.Config.*;

public class FileManager {
    private static String sessionId;
    private static MyGps myGps;
    private static MySensorManager mySensorManager;

    public static String sftHost = null;
    public static String sftUser = null;
    public static String privateKey = null;
    public static String sftWorkingDir = null;
    public static int sftPort = 0;

    public FileManager(MyGps myGps, MySensorManager mySensorManager) {
        this.myGps = myGps;
        this.mySensorManager = mySensorManager;
    }

    public static boolean fileExists(){
        String finalPath = ANDROID_BASE_FILE_PATH + FILENAME + FILE_EXTENSION;
        File file = new File(finalPath);
        return file.exists();
    }

    public static void saveOnTxtFile(){

        try {
            String finalPath = ANDROID_BASE_FILE_PATH + FILENAME + FILE_EXTENSION;
            File file = new File(finalPath);
            FileOutputStream fos;
            file.getParentFile().mkdirs();

            //if the file does not exist will be created a new one... so we need to define its header
            if(!file.exists())
                defineHeaderFile(file);

            fos = new FileOutputStream(file, true);

            String content = getValuesFromAllSensors();

            fos.write(content.toString().getBytes());
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void defineHeaderFile(File file) throws IOException {
        FileOutputStream fos;
        fos = new FileOutputStream(file, true);

        String header = "";

        header +="session_id,";
        header +="lat,";
        header +="lng,";
        header +="alt,";
        header +="timestamp,";
        header +="x_acc,";
        header +="x_acc_mean,";
        header +="x_acc_median,";
        header +="y_acc,";
        header +="y_acc_mean,";
        header +="y_acc_median,";
        header +="z_acc,";
        header +="z_acc_mean,";
        header +="z_acc_median,";
        header +="x_gyro,";
        header +="x_gyro_mean,";
        header +="x_gyro_median,";
        header +="y_gyro,";
        header +="y_gyro_mean,";
        header +="y_gyro_median,";
        header +="z_gyro,";
        header +="z_gyro_mean,";
        header +="z_gyro_median,";
        header +="light,";
        header +="light_mean,";
        header +="light_median,";
        header +="activity";

        header +="\n";
        fos.write(header.toString().getBytes());
        fos.close();
    }

    private static String getValuesFromAllSensors() {
        String str ="";

        str += getSessionId();
        str += "," + myGps.getActualLatitude();
        str += "," + myGps.getActualLongitude();
        str += "," + myGps.getActualAltitude();

        str += "," + new Timestamp(System.currentTimeMillis());

        str += "," + mySensorManager.getLastXAccelometer();
        str += "," + (mySensorManager.getLastXAccelometerValuesMean() != null ? mySensorManager.getLastXAccelometerValuesMean() : " ");
        str += "," + (mySensorManager.getLastXAccelometerValuesMedian() != null ? mySensorManager.getLastXAccelometerValuesMedian() : " ");

        str += "," + mySensorManager.getLastYAccelometer();
        str += "," + (mySensorManager.getLastYAccelometerValuesMean() != null ? mySensorManager.getLastYAccelometerValuesMean() : " ");
        str += "," + (mySensorManager.getLastYAccelometerValuesMedian() != null ? mySensorManager.getLastYAccelometerValuesMedian() : " ");

        str += "," + mySensorManager.getLastZAccelometer();
        str += "," + (mySensorManager.getLastZAccelometerValuesMean() != null ? mySensorManager.getLastZAccelometerValuesMean() : " ");
        str += "," + (mySensorManager.getLastZAccelometerValuesMedian() != null ? mySensorManager.getLastZAccelometerValuesMedian() : " ");

        str += "," + mySensorManager.getmLastXGyroscope();
        str += "," + (mySensorManager.getLastXGyroscopeValuesMean() != null ? mySensorManager.getLastXGyroscopeValuesMean() : " ");
        str += "," + (mySensorManager.getLastXGyroscopeValuesMedian() != null ? mySensorManager.getLastXGyroscopeValuesMedian() : " ");

        str += "," + mySensorManager.getmLastYGyroscope();
        str += "," + (mySensorManager.getLastYGyroscopeValuesMean() != null ? mySensorManager.getLastYGyroscopeValuesMean() : " ");
        str += "," + (mySensorManager.getLastYGyroscopeValuesMedian() != null ? mySensorManager.getLastYGyroscopeValuesMedian() : " ");

        str += "," + mySensorManager.getmLastZGyroscope();
        str += "," + (mySensorManager.getLastZGyroscopeValuesMean() != null ? mySensorManager.getLastZGyroscopeValuesMean() : " ");
        str += "," + (mySensorManager.getLastZGyroscopeValuesMedian() != null ? mySensorManager.getLastZGyroscopeValuesMedian() : " ");

        str += "," + mySensorManager.getmLastLight();
        str += "," + (mySensorManager.getmLastLightMean() != null ? mySensorManager.getmLastLightMean() : " ");
        str += "," + (mySensorManager.getmLastLightMedian() != null ? mySensorManager.getmLastLightMedian() : " ");


        str += "," + MainActivity.actualUserActivity;

        str +="\n";
        return str;
    }


    private static String getSessionId(){
        if(sessionId == null)
        {
            sessionId = generateSessionId();
            saveSessionId(sessionId);
        }
        return sessionId;
    }

    //whenever the user stops collecting it's necessary to restart session id. In other words, we need put the session id as null, since getSessionId generate a new one
    public static void restartSessionId(){
        sessionId = null;
    }

    private static String generateSessionId(){
        List<String> items;
        int sidNumber;
        String lastSessionId = getSessionIdFromFile();

        if(lastSessionId == null)
            sidNumber = 0;
        else
        {
            items = Arrays.asList(lastSessionId.split("_"));
            if(items.size() != 2)
                throw new IllegalStateException("The sid file is corrupted.");

            sidNumber = Integer.parseInt(items.get(1));
            sidNumber++;
        }

        String androidModel = getDeviceName();
        androidModel.replaceAll(" ","");
        return "" + androidModel + "_" + sidNumber;
    }

    private static String getSessionIdFromFile() {

        String finalPath = ANDROID_BASE_FILE_PATH + FILENAME_SSID + FILE_EXTENSION_SSID;
        String sid = null;

        File filePath = new File(finalPath);

        try {
            FileInputStream fis;
            fis = new FileInputStream(filePath);
            ObjectInputStream is = new ObjectInputStream(fis);

            sid = (String) is.readObject();

            is.close();
            fis.close();

        } catch (Exception e) {
            Log.d("FileManager", "File of sid not found. Will be created a new one.");
        }

        return sid;
    }

    public static boolean saveSessionId(String sid) {
        String finalPath = ANDROID_BASE_FILE_PATH + FILENAME_SSID + FILE_EXTENSION_SSID;

        File filePath = new File(finalPath);

        FileOutputStream fos;
        ObjectOutputStream os;

        try {
            fos = new FileOutputStream(filePath);
            os = new ObjectOutputStream(fos);
            os.writeObject(sid);
            os.close();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public static boolean loadFileServerConfigData(){

        try{
            String path = ANDROID_BASE_FILE_PATH + FILENAME_SERVER_CONFIG + FILE_EXTENSION_SERVER_CONFIG;

            List<String> allLines = new ArrayList<>();

            File file = new File(path);

            //if file does not exist create a new one to user define the vars
            if(!file.exists())
            {
                createFileServerConfigData();
                return false;
            }

            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                allLines.add(line);
            }

            if(allLines.size()!=5)
                return false;

            sftHost = allLines.get(0).replace("SFTP_HOST:", "");
            String sftPortStr = allLines.get(1).replace("SFTP_PORT:", "");
            sftPort = Integer.parseInt(sftPortStr);
            sftUser = allLines.get(2).replace("SFTP_USER:", "");
            privateKey = allLines.get(3).replace("PRIVATE_KEY:", "");
            sftWorkingDir = allLines.get(4).replace("SFTP_WORKING_DIR:", "");

            return true;

        } catch (Exception e) {
            Log.d("FileManager", "Error: " + e);
            return false;
        }
    }

    private static boolean createFileServerConfigData(){

        try{

            List<String> content = new ArrayList<>();

            content.add("SFTP_HOST:");
            content.add("SFTP_PORT:");
            content.add("SFTP_USER:");
            content.add("PRIVATE_KEY:");
            content.add("SFTP_WORKING_DIR:");

            StringBuilder strContent = new StringBuilder();
            for (String str : content) {
                strContent.append(str).append("\n");
            }

            String path = ANDROID_BASE_FILE_PATH + FILENAME_SERVER_CONFIG + FILE_EXTENSION_SERVER_CONFIG;
            File file = new File(path);
            FileOutputStream fos;
            file.getParentFile().mkdirs();

            fos = new FileOutputStream(file);
            fos.write(strContent.toString().getBytes());
            fos.close();

            return true;

        } catch (Exception ex) {
            Log.d("FileManager", "Error: " + ex);
            return false;
        }


    }



    //methods to get device name
    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        }
        //return capitalize(manufacturer) + "_" + model;

        return capitalize(manufacturer);
    }
    private static String capitalize(String str) {
        if (TextUtils.isEmpty(str)) {
            return str;
        }
        char[] arr = str.toCharArray();
        boolean capitalizeNext = true;

        StringBuilder phrase = new StringBuilder();
        for (char c : arr) {
            if (capitalizeNext && Character.isLetter(c)) {
                phrase.append(Character.toUpperCase(c));
                capitalizeNext = false;
                continue;
            } else if (Character.isWhitespace(c)) {
                capitalizeNext = true;
            }
            phrase.append(c);
        }

        return phrase.toString();
    }


}
