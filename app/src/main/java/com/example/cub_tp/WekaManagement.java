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


    //receive all parameters and returns a intance to send to predict method
    //public Instance getInstance();



    /*
    public String predict(ArrayList<Float> lastAccelometerDataProcessed, ArrayList<Float> lastGyroscopeDataProcessed, String lightMedianScale)  {

        try{
            Log.d("WekaManagement", "Predicting...");

            String filePathModel1 = Config.ANDROID_BASE_FILE_PATH + Config.FILENAME_TRAINED_MODEL1 + Config.FILE_EXTENSION_MODEL;
            String filePathModel2 = Config.ANDROID_BASE_FILE_PATH + Config.FILENAME_TRAINED_MODEL2 + Config.FILE_EXTENSION_MODEL;

            // read model and header
            Classifier cl1 = (Classifier) weka.core.SerializationHelper.read(filePathModel1);
            Classifier cl2 = (Classifier) weka.core.SerializationHelper.read(filePathModel2);

            List<Attribute> attributeArrayListModel1 = getAttributeListForModel1();
            Instances dataUnlabeledModel1 = defineInstancesForModel("TestInstances1", attributeArrayListModel1, lastAccelometerDataProcessed,lastGyroscopeDataProcessed,lightMedianScale);
            double classif = cl1.classifyInstance(dataUnlabeledModel1.firstInstance());

            //Log.d("WekaManagement", "WekaManagement: " +  inst.classValue() + " -> " + classif);

            String predictedActivity = getActivitiesListForModel1().get((int) classif);


            //using the second classifier to classify if is walking_upstairs or downstairs
            if(predictedActivity.equals(UserActivity.UNIFIED.toString()))
            {
                List<Attribute> attributeArrayListModel2 = getAttributeListForModel2();
                Instances dataUnlabeledModel2 = defineInstancesForModel("TestInstances2", attributeArrayListModel2, lastAccelometerDataProcessed,lastGyroscopeDataProcessed,lightMedianScale);
                double secondClassif = cl2.classifyInstance(dataUnlabeledModel2.firstInstance());
                //Log.d("WekaManagement", "WekaManagement - Second predict: " +  inst.classValue() + " -> " + secondClassif);
                predictedActivity = getActivitiesListForModel1().get((int) secondClassif);
            }

            return predictedActivity;

        }catch (IOException e){
            Log.d("WekaManagement","Error: " + e);

            return "Error: The file of the model was not found.";
        }catch (Exception e){
            Log.d("WekaManagement","Error: " + e);

            return "Error: " + e;
        }

    }

    */

    public String predict(ArrayList<Float> lastAccelometerDataProcessed, ArrayList<Float> lastGyroscopeDataProcessed, String lightMedianScale)  {

        try{
            Log.d("WekaManagement", "Predicting...");

            String filePath = Config.ANDROID_BASE_FILE_PATH + Config.FILENAME_TRAINED_MODEL1 + Config.FILE_EXTENSION_MODEL;

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
        activities.add(UserActivity.WALKING.name());
        activities.add(UserActivity.LAYING.name());
        activities.add(UserActivity.UNIFIED.name());
        return activities;
    }

    /*
    private Instances defineInstancesForModel(String name, List<Attribute> attributeArrayList, ArrayList<Float> lastAccelometerDataProcessed, ArrayList<Float> lastGyroscopeDataProcessed, String lightMedianScale){
        Instances dataUnlabeled = new Instances(name, (ArrayList<Attribute>) attributeArrayList, 0);

        // Create empty instance with three attribute values
        Instance inst = defineInstance(dataUnlabeled, lastAccelometerDataProcessed, lastGyroscopeDataProcessed, lightMedianScale);
        dataUnlabeled.add(inst);
        dataUnlabeled.setClassIndex(dataUnlabeled.numAttributes() - 1);

        return dataUnlabeled;
    }

    private Instance defineInstance(Instances dataUnlabeled, ArrayList<Float> lastAccelometerDataProcessed, ArrayList<Float> lastGyroscopeDataProcessed, String lightMedianScale) {

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
        return inst;
    }


    private List<Attribute> getAttributeListForModel1() {
        List<Attribute> attributeArrayList = getSimiliarAttributeList();

        List<String> activities= getActivitiesListForModel1();
        attributeArrayList.add(new Attribute("activity",activities));

        return attributeArrayList;
    }

    private List<Attribute> getAttributeListForModel2() {
        List<Attribute> attributeArrayList = getSimiliarAttributeList();
        List<String> activities= getActivitiesListForModel2();
        attributeArrayList.add(new Attribute("activity",activities));

        return attributeArrayList;
    }

    private List<Attribute> getSimiliarAttributeList() {
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
        return attributeArrayList;
    }

    private List<String> getActivitiesListForModel1() {
        List<String> activities = new ArrayList<>();
        activities.add(UserActivity.LAYING.name());
        activities.add(UserActivity.WALKING.name());
        activities.add(UserActivity.UNIFIED.name());

        return activities;
    }
    private List<String> getActivitiesListForModel2() {
        List<String> activities = new ArrayList<>();
        activities.add(UserActivity.WALKING_DOWNSTAIRS.name());
        activities.add(UserActivity.WALKING_UPSTAIRS.name());
        return activities;
    }

*/

}
