package com.example.cub_tp;

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

    private static String getValuesFromAllSensors() {
        String str ="";

        str += getSessionId();
        str += "," + myGps.getActualLatitude();
        str += "," + myGps.getActualAltitude();
        str += "," + myGps.getActualLongitude();

        str += "," + new Timestamp(System.currentTimeMillis());

        str += "," + mySensorManager.getLastXAccelometer();
        str += "," + mySensorManager.getLastYAccelometer();
        str += "," + mySensorManager.getLastZAccelometer();

        str += "," + mySensorManager.getmLastXGyroscope();
        str += "," + mySensorManager.getmLastYGyroscope();
        str += "," + mySensorManager.getmLastZGyroscope();

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

        return "" + android.os.Build.MODEL + "_" + sidNumber;
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

}
