package com.example.cub_tp;

import android.content.res.AssetManager;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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


    public String predict(ArrayList<Float> lastAccelometerDataProcessed, ArrayList<Float> lastGyroscopeDataProcessed, String lightMedianScale)  {

        try{
            Log.d("WekaManagement", "Predicting...");

            String filePath = Config.ANDROID_BASE_FILE_PATH + Config.FILENAME_TRAINED_MODEL + Config.FILE_EXTENSION_MODEL;

            // read model and header
            Classifier cl = (Classifier) weka.core.SerializationHelper.read(filePath);

            List<Attribute> attributeArrayList = getAttributeList();
            Instances dataUnlabeled = new Instances("TestInstances", (ArrayList<Attribute>) attributeArrayList, 0);

            // Create empty instance with three attribute values
            Instance inst = new DenseInstance(dataUnlabeled.numAttributes());

            for(int i = 0; i < Config.MIN_VALUES_TO_FFT; i++){
                inst.setValue(dataUnlabeled.attribute("accelometer" + (i+1)), lastAccelometerDataProcessed.get(i));
            }

            for(int i = 0; i < Config.MIN_VALUES_TO_FFT; i++){
                inst.setValue(dataUnlabeled.attribute("gyroscope" + (i+1)), lastGyroscopeDataProcessed.get(i));
            }

            inst.setValue(dataUnlabeled.attribute("light"), lightMedianScale);
            inst.setValue(dataUnlabeled.attribute("activity"), 0);

            inst.setDataset(dataUnlabeled);

            Log.d("WekaManagement", "Instance: " + inst);

            dataUnlabeled.add(inst);
            dataUnlabeled.setClassIndex(dataUnlabeled.numAttributes() - 1);
            double classif = cl.classifyInstance(dataUnlabeled.firstInstance());

            Log.d("WekaManagement", "WekaManagement: " +  inst.classValue() + " -> " + classif);

            return getActivitiesList().get((int) classif);

        }catch (IOException e){
            Log.d("WekaManagement","Error: " + e);

            return "Error: The file of the model was not found.";
        }catch (Exception e){
            Log.d("WekaManagement","Error: " + e);

            return "Error: " + e;
        }

    }

    private List<Attribute> getAttributeList() {
        List<Attribute> attributeArrayList = new ArrayList<>();

        for(int i = 0; i < Config.MIN_VALUES_TO_FFT; i++)
            attributeArrayList.add(new Attribute("accelometer" + (i+1)));
        for(int i = 0; i < Config.MIN_VALUES_TO_FFT; i++)
            attributeArrayList.add(new Attribute("gyroscope" + (i+1)));

        List<String> typeOfLights= new ArrayList<>();
        typeOfLights.add(Config.LIGHT_NORMAL);
        typeOfLights.add(Config.LIGHT_LOW);
        typeOfLights.add(Config.LIGHT_HIGH);
        attributeArrayList.add(new Attribute("light",typeOfLights));
        List<String> activities= getActivitiesList();
        attributeArrayList.add(new Attribute("activity",activities));

        return attributeArrayList;
    }

    private List<String> getActivitiesList() {
        List<String> activities = new ArrayList<>();
        activities.add(UserActivity.LAYING.name());
        activities.add(UserActivity.SITTING.name());
        activities.add(UserActivity.WALKING.name());
        activities.add(UserActivity.WALKING_DOWNSTAIRS.name());
        activities.add(UserActivity.WALKING_UPSTAIRS.name());
        return activities;
    }


}
