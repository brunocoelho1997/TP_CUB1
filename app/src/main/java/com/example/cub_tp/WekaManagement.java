package com.example.cub_tp;

import android.content.res.AssetManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.Vector;

import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.experiment.InstanceQuery;

public class WekaManagement {


    //receive all parameters and returns a intance to send to predict method
    //public Instance getInstance();


    public String predict(ArrayList<Float> lastAccelometerDataProcessed, ArrayList<Float> lastGyroscopeDataProcessed, String lightMedian)  {

        try{
            Log.d("WekaManagement", "Predicting...");

            String filePath = Config.ANDROID_BASE_FILE_PATH + Config.FILENAME_TRAINED_MODEL + Config.FILE_EXTENSION_MODEL;

            // read model and header
            Classifier cl = (Classifier) (Classifier) weka.core.SerializationHelper.read(filePath);

            // Create empty instance with three attribute values
            Instance inst = new DenseInstance(lastAccelometerDataProcessed.size() + lastGyroscopeDataProcessed.size() + 1 + 1);

            // Set instance's values for the attributes (all 64 values of accelometer and gyroscope and the light
            for(int i = 0; i < 64; i++)
                inst.setValue(new Attribute("accelometer" + (i+1)), lastAccelometerDataProcessed.get(i));

            for(int i = 0; i < 64; i++)
                inst.setValue(new Attribute("gyroscope" + (i+1)), lastGyroscopeDataProcessed.get(i));

            inst.setValue(new Attribute("light"), lightMedian);

            inst.setValue(new Attribute("activity"), "?");

            Log.d("WekaManagement", "Instance: " + inst);

            if (inst.classIndex() == -1)
                inst.setMissing(inst.numAttributes() - 1);

            // predict class
            double pred = cl.classifyInstance(inst);
            Log.d("WekaManagement", "WekaManagement: " +  inst.classValue() + " -> " + pred);

            Log.d("WekaManagement","Predicting finished!");

            // this does the trick
            inst.setClassValue(pred);
            Log.d("WekaManagement","Result: " + inst.stringValue(inst.numAttributes()));

            return "";
        }catch (Exception e){
            Log.d("WekaManagement","Error: " + e);

            return "Error: " + e;
        }

    }
}
