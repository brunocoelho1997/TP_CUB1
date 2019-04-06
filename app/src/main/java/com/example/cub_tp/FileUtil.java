package com.example.cub_tp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

import static com.example.cub_tp.Config.*;

public class FileUtil {
    private String sessionId;


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


        return str;
    }


    private String getSessionId(){
        if(sessionId == null)
            sessionId = generateSessionId();
        return sessionId;
    }

    private String generateSessionId() throws IllegalArgumentException{
        String lastSessionId = getSessionIdFromFile();

        List<String> items = Arrays.asList(lastSessionId.split("_"));

        if(items.size() > 2 || items.size() <= 0)
            throw new IllegalArgumentException("ads");


        return null;
    }

    private String getSessionIdFromFile() {
        try {
            String finalPath = ANDROID_BASE_FILE_PATH + FILENAME_SSID + FILE_EXTENSION_SSID;
            File file = new File(finalPath);
            FileInputStream fis;
            file.getParentFile().mkdirs();
            fis = new FileInputStream(file);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis));

            String content = reader.readLine();

            fis.close();

            return content;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

}
