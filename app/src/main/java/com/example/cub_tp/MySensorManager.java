package com.example.cub_tp;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.example.cub_tp.Config.MIN_VALUES_TO_FFT;
import static com.example.cub_tp.Config.MIN_VALUES_TO_MEAN_MEDIAN;
import static com.example.cub_tp.Config.NOISE;


public class MySensorManager extends AppCompatActivity implements SensorEventListener {

    //----sensors
    private SensorManager sensorManager;
    private List<Sensor> deviceSensors;
    private Sensor gyroscope;
    private Sensor accelometer;
    private Sensor light;

    boolean mInitializedGyroscope = false, mInitializedAccelometer = false;

    private static float mLastXGyroscope, mLastYGyroscope, mLastZGyroscope; //used by gyroscope
    private static float mLastLight; //used by light


    private static float lastXAccelometer, lastYAccelometer, lastZAccelometer;

    //list to save the 10 last elements of gyroscope to apply mean and median
    private List<Float> lastXGyroscopeValuesToMedian;
    private List<Float> lastYGyroscopeValuesToMedian;
    private List<Float> lastZGyroscopeValuesToMedian;
    //list to save the 10 last elements of accelometer to apply mean and median
    private List<Float> lastXAccelometerValuesToMedian;
    private List<Float> lastYAccelometerValuesToMedian;
    private List<Float> lastZAccelometerValuesToMedian;

    //list to save the 10 last elements of light to apply mean and median
    private List<Float> lastLightValues;


    //list to save the 10 last elements of gyroscope to apply fft
    private List<Double> listAngularVelocityGyroscope;
    //list to save the 10 last elements of accelometer to apply fft
    private List<Double> listAngularVelocityAccelometer;

    //all data are filtered and applied the fft from sensors
    private ArrayList<Float> lastAccelometerDataProcessed = new ArrayList<>();
    private ArrayList<Float> lastGyroscopeDataProcessed = new ArrayList<>();

    private WekaManagement wekaManagement;



    public MySensorManager(SensorManager sensorManager) {

        //for sensors
        this.sensorManager = sensorManager;
        this.deviceSensors = sensorManager.getSensorList(Sensor.TYPE_ALL);
        for(int i=0; i<deviceSensors.size();i++) {
            MainActivity.tvSensorList.setText( MainActivity.tvSensorList.getText() + deviceSensors.get(i).getName() + "\n");
        }

        //define arraylists for mean and median
        this.lastXGyroscopeValuesToMedian = new ArrayList<>();
        this.lastYGyroscopeValuesToMedian = new ArrayList<>();
        this.lastZGyroscopeValuesToMedian = new ArrayList<>();
        this.lastXAccelometerValuesToMedian = new ArrayList<>();
        this.lastYAccelometerValuesToMedian = new ArrayList<>();
        this.lastZAccelometerValuesToMedian = new ArrayList<>();
        this.lastLightValues = new ArrayList<>();
        this.listAngularVelocityGyroscope = new ArrayList<>();
        this.listAngularVelocityAccelometer = new ArrayList<>();

        //define Gyroscope
        if (sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null){
            this.gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        }

        //define Accelometer
        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null){
            this.accelometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }

        //define Light Sensor
        if (sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT) != null){
            this.light = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        }

        this.wekaManagement = new WekaManagement();
    }

    public void startSensors(){
        sensorManager.registerListener(MySensorManager.this, gyroscope, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(MySensorManager.this, accelometer, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(MySensorManager.this, light, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void stopSensors(){
        sensorManager.unregisterListener(MySensorManager.this);
    }

    //for gyroscope and accelometer
    @Override
    public void onSensorChanged(SensorEvent event) {

        //applyLowPassFilter(linear_acceleration, event);

        boolean accelometerSensorChanged = false;
        boolean gyroscopeSensorChanged = false;

        //flag when has values to write in file
        boolean newValuesFftToWrite = false;

        if(event.sensor.getType() == Sensor.TYPE_GYROSCOPE)
        {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            if (!mInitializedGyroscope) {
                mLastXGyroscope = x;
                mLastYGyroscope = y;
                mLastZGyroscope = z;
                //tvX.setText("0.0");
                //tvY.setText("0.0");
                //tvZ.setText("0.0");
                mInitializedGyroscope = true;
            } else {


                float deltaX = Math.abs(mLastXGyroscope - x);
                float deltaY = Math.abs(mLastYGyroscope - y);
                float deltaZ = Math.abs(mLastZGyroscope - z);

                if (deltaX < NOISE)
                    deltaX = (float)0.0;
                else
                {
                    mLastXGyroscope = x;
                    gyroscopeSensorChanged = true;
                }

                if (deltaY < NOISE)
                    deltaY = (float)0.0;
                else
                {
                    mLastYGyroscope = y;
                    gyroscopeSensorChanged = true;
                }

                if (deltaZ < NOISE)
                    deltaZ = (float)0.0;
                else
                {
                    mLastZGyroscope = z;
                    gyroscopeSensorChanged = true;
                }
            }

            if(gyroscopeSensorChanged)
            {
                //Log.d("MySensorManager", "gyroscopeSensorChanged: Gyroscope: x= " + mLastXGyroscope + " y= " + mLastYGyroscope + " z= " + mLastZGyroscope);

                lastXGyroscopeValuesToMedian.add(mLastXGyroscope);
                lastYGyroscopeValuesToMedian.add(mLastYGyroscope);
                lastZGyroscopeValuesToMedian.add(mLastZGyroscope);
                MainActivity.tvInfoGyroscope.setText("Gyroscope: x= " + mLastXGyroscope + " y= " + mLastYGyroscope + " z= " + mLastZGyroscope);
            }
            else
                MainActivity.tvInfoGyroscope.setText("Gyroscope:");

            if(lastXGyroscopeValuesToMedian.size()>MIN_VALUES_TO_MEAN_MEDIAN)
                lastXGyroscopeValuesToMedian.remove(0);

            if(lastYGyroscopeValuesToMedian.size()>MIN_VALUES_TO_MEAN_MEDIAN)
                lastYGyroscopeValuesToMedian.remove(0);

            if(lastZGyroscopeValuesToMedian.size()>MIN_VALUES_TO_MEAN_MEDIAN)
                lastZGyroscopeValuesToMedian.remove(0);

            if(lastXGyroscopeValuesToMedian.size() >= MIN_VALUES_TO_MEAN_MEDIAN && lastYGyroscopeValuesToMedian.size() >= MIN_VALUES_TO_MEAN_MEDIAN && lastZGyroscopeValuesToMedian.size()>= MIN_VALUES_TO_MEAN_MEDIAN)
            {
                float angularVelocity = getAngularVelocity(getMedian(lastXGyroscopeValuesToMedian),getMedian(lastYGyroscopeValuesToMedian),getMedian(lastZGyroscopeValuesToMedian));
                listAngularVelocityGyroscope.add((double) angularVelocity);
            }

        }
        else if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
        {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            if (!mInitializedAccelometer) {
                lastXAccelometer = x;
                lastYAccelometer = y;
                lastZAccelometer = z;
                //tvX.setText("0.0");
                //tvY.setText("0.0");
                //tvZ.setText("0.0");
                mInitializedAccelometer = true;
            } else {


                float deltaX = Math.abs(lastXAccelometer - x);
                float deltaY = Math.abs(lastYAccelometer - y);
                float deltaZ = Math.abs(lastZAccelometer - z);
                if (deltaX < NOISE)
                    deltaX = (float)0.0;
                else
                {
                    lastXAccelometer = x;
                    accelometerSensorChanged = true;

                }

                if (deltaY < NOISE)
                    deltaY = (float)0.0;
                else
                {
                    accelometerSensorChanged = true;
                    lastYAccelometer = y;
                }


                if (deltaZ < NOISE)
                    deltaZ = (float)0.0;
                else
                {
                    accelometerSensorChanged = true;
                    lastZAccelometer = z;
                }
            }

            if(accelometerSensorChanged)
            {
                //Log.d("MySensorManager", "accelometerSensorChanged: Accelometer: x= " + lastXAccelometer + " y= " + lastYAccelometer + " z= " + lastZAccelometer);

                lastXAccelometerValuesToMedian.add(lastXAccelometer);
                lastYAccelometerValuesToMedian.add(lastYAccelometer);
                lastZAccelometerValuesToMedian.add(lastZAccelometer);

                MainActivity.tvInfoAccelometer.setText("Accelometer: x= " + lastXAccelometer + " y= " + lastYAccelometer + " z= " + lastZAccelometer);
            }
            else
                MainActivity.tvInfoAccelometer.setText("Accelometer: ");



            if(lastXAccelometerValuesToMedian.size()>MIN_VALUES_TO_MEAN_MEDIAN)
                lastXAccelometerValuesToMedian.remove(0);
            if(lastYAccelometerValuesToMedian.size()>MIN_VALUES_TO_MEAN_MEDIAN)
                lastYAccelometerValuesToMedian.remove(0);
            if(lastZAccelometerValuesToMedian.size()>MIN_VALUES_TO_MEAN_MEDIAN)
                lastZAccelometerValuesToMedian.remove(0);

            if(lastXAccelometerValuesToMedian.size() >= MIN_VALUES_TO_MEAN_MEDIAN && lastYAccelometerValuesToMedian.size() >= MIN_VALUES_TO_MEAN_MEDIAN && lastZAccelometerValuesToMedian.size()>= MIN_VALUES_TO_MEAN_MEDIAN )
            {
                float angularVelocity = getAngularVelocity(getMedian(lastXAccelometerValuesToMedian),getMedian(lastYAccelometerValuesToMedian),getMedian(lastZAccelometerValuesToMedian));
                listAngularVelocityAccelometer.add((double) angularVelocity);
            }
        }
        else if(event.sensor.getType() == Sensor.TYPE_LIGHT)
        {
            mLastLight = event.values[0];

            //Log.d("MySensorManager", "MySensorManager max value of light sensor: " + sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT).getMaximumRange());
            //Log.d("MySensorManager", "Actual value: " + mLastLight);


            if(mLastLight > Config.LIGHT_MAX_VALUE)
                mLastLight = Config.LIGHT_MAX_VALUE;

            lastLightValues.add(mLastLight);

            if(lastLightValues.size()>MIN_VALUES_TO_MEAN_MEDIAN)
                lastLightValues.remove(0);

            MainActivity.tvInfoLight.setText("Light: " + mLastLight);

        }

        //if(sensorChanged)
            //FileManager.saveOnCsvFile();

        if(listAngularVelocityAccelometer.size() >= MIN_VALUES_TO_FFT)
        {
            Log.d("MySensorManager", "lastAccelometerDataProcessed: " + lastAccelometerDataProcessed);

            Fft fft = new Fft(MIN_VALUES_TO_FFT);

            double[] re = new double[MIN_VALUES_TO_FFT];
            double[] im = new double[MIN_VALUES_TO_FFT];
            for (int i = 0; i < re.length; i++)
                re[i] = listAngularVelocityAccelometer.get(i);

            Log.d("MySensorManager", "log date before fft acc");
            fft.fft(re, im);
            Log.d("MySensorManager", "log date: after fft acc");

            listAngularVelocityAccelometer.clear();
            lastAccelometerDataProcessed.clear();
            for(int i = 0; i < re.length; i++)
                lastAccelometerDataProcessed.add(getAngularVelocity(re[i], im[i]));

            newValuesFftToWrite = true;

        }
        if(listAngularVelocityGyroscope.size() >= MIN_VALUES_TO_FFT)
        {

            Log.d("MySensorManager", "lastGyroscopeDataProcessed: " + lastGyroscopeDataProcessed);

            Fft fft = new Fft(MIN_VALUES_TO_FFT);

            double[] re = new double[MIN_VALUES_TO_FFT];
            double[] im = new double[MIN_VALUES_TO_FFT];
            for (int i = 0; i < re.length; i++)
                re[i] = listAngularVelocityGyroscope.get(i);

            Log.d("MySensorManager", "log date before fft gyro");
            fft.fft(re, im);
            Log.d("MySensorManager", "log date: after fft gyro");

            listAngularVelocityGyroscope.clear();
            lastGyroscopeDataProcessed.clear();
            for(int i = 0; i < re.length; i++)
                lastGyroscopeDataProcessed.add(getAngularVelocity(re[i], im[i]));

            newValuesFftToWrite = true;
        }

        //TODO: verify this
        //if(accelometerSensorChanged == false || gyroscopeSensorChanged == false)
            //return;

        if(newValuesFftToWrite && lastGyroscopeDataProcessed.size() >= MIN_VALUES_TO_FFT && lastAccelometerDataProcessed.size() >= MIN_VALUES_TO_FFT)
        {
            //if the AutoMode isn't checked only save the data on the file
            if(MainActivity.ckAutoMode.isChecked()){
                String predictedActivity;

                //need to send to weka managemnet the getAngularVelocity(reGyroscope[i], imGyroscope[i])
                predictedActivity = wekaManagement.predict(lastAccelometerDataProcessed,lastGyroscopeDataProcessed, getLightScale());
                MainActivity.tvActualActivityPredicted.setText("" + predictedActivity);
            }

            Log.d("MySensorManager", "log date: before file");
            FileManager.saveOnArffFile(lastAccelometerDataProcessed,lastGyroscopeDataProcessed, getLightScale(), MainActivity.actualUserActivity.toString());
            Log.d("MySensorManager", "log date: after file");


            //TODO COLOCAR A ESCRITA EM ASINCRONA
            //TODO: clear do 64 DO FFT remover os 1ºs 32 valores...
            //TODO: ver o novo noise...
            //TODO: os 0s... corrigi-los!!!
            //todo: Uma leitura a cada 2 segundos
        }

    }

    public void clearAngularVelocities(){
        if(this.listAngularVelocityGyroscope != null)
            this.listAngularVelocityGyroscope.clear();

        if(this.listAngularVelocityAccelometer != null)
            this.listAngularVelocityAccelometer.clear();

        if(this.lastAccelometerDataProcessed != null)
            this.lastAccelometerDataProcessed.clear();

        if(this.lastGyroscopeDataProcessed!= null)
            this.lastGyroscopeDataProcessed.clear();

    }

    public String getLightScale(){

        float tmp;

        //if the lastLight Values does not have 10 values will use the mean
        if(lastLightValues.size() >= MIN_VALUES_TO_MEAN_MEDIAN)
            tmp = getMedian(lastLightValues);
        else
            tmp = getMean(lastLightValues);

        if(tmp <= 50)
            return Config.LIGHT_LOW;
        else if(tmp <= 150)
            return Config.LIGHT_NORMAL;
        else
            return Config.LIGHT_HIGH;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something here if sensor accuracy changes.
    }

    private float getMedian(List<Float> valuesArrayList){

        Float[] itemsArray = new Float[valuesArrayList.size()];
        itemsArray = valuesArrayList.toArray(itemsArray);

        Arrays.sort(itemsArray);
        double median;
        if (itemsArray.length % 2 == 0)
            median = ((double)itemsArray[itemsArray.length/2] + (double)itemsArray[itemsArray.length/2 - 1])/2;
        else
            median = (double) itemsArray[itemsArray.length/2];

        float tmp = (float)median;

        //Log.d("MySensorManager", "getMedian: \n" + "values: " + valuesArrayList +"\nMedian: " + tmp);

        return Float.valueOf(tmp);
    }

    private float getMean(List<Float> valuesArrayList){

        float sum = 0;

        for (Float mark : valuesArrayList) {
            sum += mark;
        }
        float tmp = sum / valuesArrayList.size();

        //Log.d("MySensorManager", "getMean: \n" + "values: " + valuesArrayList +"\nMean: " + tmp);

        return Float.valueOf(tmp);
    }


    public float getAngularVelocity(float x, float y, float z){
        return (float) Math.sqrt(Math.pow(x,2) + Math.pow(y,2) + Math.pow(z,2));
    }

    public float getAngularVelocity(Double x, Double y){
        return (float) Math.sqrt(Math.pow(x,2) + Math.pow(y,2));
    }

    //getters and setters
    public static float getmLastXGyroscope() {
        return mLastXGyroscope;
    }

    public static float getmLastYGyroscope() {
        return mLastYGyroscope;
    }

    public static float getmLastZGyroscope() {
        return mLastZGyroscope;
    }

    public static float getLastXAccelometer() {
        return lastXAccelometer;
    }

    public static float getLastYAccelometer() {
        return lastYAccelometer;
    }

    public static float getLastZAccelometer() {
        return lastZAccelometer;
    }

    public static float getmLastLight() {
        return mLastLight;
    }

}
