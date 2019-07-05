package com.example.cub_tp;

import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

public class WekaManagement {

    public String predict(ArrayList<Float> lastAccelometerDataProcessed, ArrayList<Float> lastGyroscopeDataProcessed, String lightMedianScale)  {

        try{
            Log.d("WekaManagement", "Predicting...");

            String filePathModel1 = Config.ANDROID_BASE_FILE_PATH + Config.FILENAME_TRAINED_MODEL1 + Config.FILE_EXTENSION_MODEL;
            String filePathModel2 = Config.ANDROID_BASE_FILE_PATH + Config.FILENAME_TRAINED_MODEL2 + Config.FILE_EXTENSION_MODEL;

            // read model and header
            Classifier cl = (Classifier) weka.core.SerializationHelper.read(filePathModel1);

            List<Attribute> attributeArrayList = getAttributeList(true);
            Instances dataUnlabeled = getInstances(attributeArrayList,lastAccelometerDataProcessed,lastGyroscopeDataProcessed,lightMedianScale);

            double classif = cl.classifyInstance(dataUnlabeled.firstInstance());

            //get the prediction percentage or distribution
            double[] percentages=cl.distributionForInstance(dataUnlabeled.firstInstance());
            double accuracy = percentages[(int) classif];


            if(accuracy < Config.MIN_ACCURACY_TO_PREDICT)
                return Config.WEAK_PREDICT_MESSAGE;

            String activity = getActivitiesListForModel1().get((int) classif);

            //if it isn't unified return the actual activity predicted...
            if(!activity.equals(UserActivity.UNIFIED.toString()))
            {
                Log.d("WekaManagement", "WekaManagement - Classif:" +  classif);
                Log.d("WekaManagement", "Distribution (the accuracy):" +  accuracy);
                return activity;
            }

            Classifier c2 = (Classifier) weka.core.SerializationHelper.read(filePathModel2);
            attributeArrayList = getAttributeList(false);
            dataUnlabeled = getInstances(attributeArrayList,lastAccelometerDataProcessed,lastGyroscopeDataProcessed,lightMedianScale);

            classif = c2.classifyInstance(dataUnlabeled.firstInstance());

            //get the prediction percentage or distribution
            percentages=c2.distributionForInstance(dataUnlabeled.firstInstance());
            accuracy = percentages[(int) classif];

            Log.d("WekaManagement", "WekaManagement - Classif:" +  classif);
            Log.d("WekaManagement", "Distribution (the accuracy):" +  accuracy);

            return getActivitiesListForModel2().get((int) classif);

        }catch (IOException e){
            Log.d("WekaManagement","Error: " + e);

            return "Error: The file of the model was not found.";
        }catch (Exception e){
            Log.d("WekaManagement","Error: " + e);

            return "Error: " + e;
        }

    }

    private Instances getInstances(List<Attribute> attributeArrayList, ArrayList<Float> lastAccelometerDataProcessed, ArrayList<Float> lastGyroscopeDataProcessed, String lightMedianScale) {
        Instances dataUnlabeled = new Instances("TestInstances", (ArrayList<Attribute>) attributeArrayList, 0);

        // Create empty instance with three attribute values
        Instance inst = new DenseInstance(dataUnlabeled.numAttributes());

        for(int i = 0; i < Config.MIN_VALUES_TO_FFT; i++)
            inst.setValue(dataUnlabeled.attribute("accelometer" + (i+1)), lastAccelometerDataProcessed.get(i));

        for(int i = 0; i < Config.MIN_VALUES_TO_FFT; i++)
            inst.setValue(dataUnlabeled.attribute("gyroscope" + (i+1)), lastGyroscopeDataProcessed.get(i));

        inst.setValue(dataUnlabeled.attribute("light"), lightMedianScale);
        inst.setValue(dataUnlabeled.attribute("activity"), 0);

        inst.setDataset(dataUnlabeled);

        dataUnlabeled.add(inst);
        dataUnlabeled.setClassIndex(dataUnlabeled.numAttributes() - 1);

        return dataUnlabeled;
    }

    private List<Attribute> getAttributeList(boolean isForModel1) {
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
        List<String> activities= (isForModel1 ? getActivitiesListForModel1() : getActivitiesListForModel2());
        attributeArrayList.add(new Attribute("activity",activities));

        return attributeArrayList;
    }

    private List<String> getActivitiesListForModel1() {
        List<String> activities = new ArrayList<>();
        activities.add(UserActivity.WALKING.name());
        activities.add(UserActivity.LAYING.name());
        activities.add(UserActivity.UNIFIED.name());
        return activities;
    }
    private List<String> getActivitiesListForModel2() {
        List<String> activities = new ArrayList<>();
        activities.add(UserActivity.WALKING_DOWNSTAIRS.name());
        activities.add(UserActivity.WALKING_UPSTAIRS.name());
        return activities;
    }

}
