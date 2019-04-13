package com.example.cub_tp;

import android.os.Build;
import android.text.TextUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;

import static com.example.cub_tp.Config.*;

public class FileManager {
    private static String sessionId;
    private static MyGps myGps;
    private static MySensorManager mySensorManager;

    public FileManager(MyGps myGps, MySensorManager mySensorManager) {
        this.myGps = myGps;
        this.mySensorManager = mySensorManager;
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
        header +="y_acc,";
        header +="z_acc,";
        header +="x_gyro,";
        header +="y_gyro,";
        header +="z_gyro,";
        header +="light,";
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
        str += "," + mySensorManager.getLastYAccelometer();
        str += "," + mySensorManager.getLastZAccelometer();

        str += "," + mySensorManager.getmLastXGyroscope();
        str += "," + mySensorManager.getmLastYGyroscope();
        str += "," + mySensorManager.getmLastZGyroscope();

        str += "," + mySensorManager.getmLastLight();

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
            e.printStackTrace();
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
