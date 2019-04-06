package com.example.cub_tp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import static com.example.cub_tp.Config.*;

public class FileUtil {

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
}
