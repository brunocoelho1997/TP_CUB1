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
    private final float NOISE = (float) 2.0; //used by gyroscope

    private static float lastXAccelometer, lastYAccelometer, lastZAccelometer;

    //list to save the 10 last elements of gyroscope to apply mean and median
    private List<Float> lastXGyroscopeValues;
    private List<Float> lastYGyroscopeValues;
    private List<Float> lastZGyroscopeValues;
    //list to save the 10 last elements of accelometer to apply mean and median
    private List<Float> lastXAccelometerValues;
    private List<Float> lastYAccelometerValues;
    private List<Float> lastZAccelometerValues;

    //list to save the 10 last elements of light to apply mean and median
    private List<Float> lastLightValues;


    //list to save the 10 last elements of gyroscope to apply fft
    private List<Double> listAngularVelocityGyroscope;
    //list to save the 10 last elements of accelometer to apply fft
    private List<Double> listAngularVelocityAccelometer;


    private WekaManagement wekaManagement;


    public MySensorManager(SensorManager sensorManager) {

        //for sensors
        this.sensorManager = sensorManager;
        this.deviceSensors = sensorManager.getSensorList(Sensor.TYPE_ALL);
        for(int i=0; i<deviceSensors.size();i++) {
            MainActivity.tvSensorList.setText( MainActivity.tvSensorList.getText() + deviceSensors.get(i).getName() + "\n");
        }

        //define arraylists for mean and median
        this.lastXGyroscopeValues = new ArrayList<>();
        this.lastYGyroscopeValues = new ArrayList<>();
        this.lastZGyroscopeValues = new ArrayList<>();
        this.lastXAccelometerValues = new ArrayList<>();
        this.lastYAccelometerValues = new ArrayList<>();
        this.lastZAccelometerValues = new ArrayList<>();
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

        boolean sensorChanged = false;


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
                    sensorChanged = true;
                }

                if (deltaY < NOISE)
                    deltaY = (float)0.0;
                else
                {
                    mLastYGyroscope = y;
                    sensorChanged = true;
                }

                if (deltaZ < NOISE)
                    deltaZ = (float)0.0;
                else
                {
                    mLastZGyroscope = z;
                    sensorChanged = true;
                }
            }

            lastXGyroscopeValues.add(mLastXGyroscope);
            lastYGyroscopeValues.add(mLastYGyroscope);
            lastZGyroscopeValues.add(mLastZGyroscope);

            if(lastXGyroscopeValues.size()>MIN_VALUES_TO_MEAN_MEDIAN)
                lastXGyroscopeValues.remove(0);

            if(lastYGyroscopeValues.size()>MIN_VALUES_TO_MEAN_MEDIAN)
                lastYGyroscopeValues.remove(0);

            if(lastZGyroscopeValues.size()>MIN_VALUES_TO_MEAN_MEDIAN)
                lastZGyroscopeValues.remove(0);

            if(lastXGyroscopeValues.size() >= MIN_VALUES_TO_MEAN_MEDIAN && lastYGyroscopeValues.size() >= MIN_VALUES_TO_MEAN_MEDIAN && lastZGyroscopeValues.size()>= MIN_VALUES_TO_MEAN_MEDIAN )
            {
                float angularVelocity = getAngularVelocity(getMedian(lastXGyroscopeValues),getMedian(lastYGyroscopeValues),getMedian(lastZGyroscopeValues));
                listAngularVelocityGyroscope.add((double) angularVelocity);
                if(listAngularVelocityGyroscope.size()>MIN_VALUES_TO_FFT)
                    listAngularVelocityGyroscope.remove(0);
            }

            MainActivity.tvInfoGyroscope.setText("Gyroscope: x= " + mLastXGyroscope + " y= " + mLastYGyroscope + " z= " + mLastZGyroscope);
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
                    sensorChanged = true;

                }

                if (deltaY < NOISE)
                    deltaY = (float)0.0;
                else
                {
                    sensorChanged = true;
                    lastYAccelometer = y;
                }


                if (deltaZ < NOISE)
                    deltaZ = (float)0.0;
                else
                {
                    sensorChanged = true;
                    lastZAccelometer = z;
                }
            }

            lastXAccelometerValues.add(lastXAccelometer);
            lastYAccelometerValues.add(lastYAccelometer);
            lastZAccelometerValues.add(lastZAccelometer);

            if(lastXAccelometerValues.size()>MIN_VALUES_TO_MEAN_MEDIAN)
                lastXAccelometerValues.remove(0);
            if(lastYAccelometerValues.size()>MIN_VALUES_TO_MEAN_MEDIAN)
                lastYAccelometerValues.remove(0);
            if(lastZAccelometerValues.size()>MIN_VALUES_TO_MEAN_MEDIAN)
                lastZAccelometerValues.remove(0);

            if(lastXAccelometerValues.size() >= MIN_VALUES_TO_MEAN_MEDIAN && lastYAccelometerValues.size() >= MIN_VALUES_TO_MEAN_MEDIAN && lastZAccelometerValues.size()>= MIN_VALUES_TO_MEAN_MEDIAN )
            {
                float angularVelocity = getAngularVelocity(getMedian(lastXAccelometerValues),getMedian(lastYAccelometerValues),getMedian(lastZAccelometerValues));
                listAngularVelocityAccelometer.add((double) angularVelocity);
                if(listAngularVelocityAccelometer.size()>MIN_VALUES_TO_FFT)
                    listAngularVelocityAccelometer.remove(0);
            }
            MainActivity.tvInfoAccelometer.setText("Accelometer: x= " + lastXAccelometer + " y= " + lastYAccelometer + " z= " + lastZAccelometer);
        }
        else if(event.sensor.getType() == Sensor.TYPE_LIGHT)
        {
            mLastLight = event.values[0];
            lastLightValues.add(mLastLight);

            Log.d("MySensorManager", "MySensorManager max value of light sensor: " + sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT).getMaximumRange());
            Log.d("MySensorManager", "Actual value: " + mLastLight);

            if(lastLightValues.size()>MIN_VALUES_TO_MEAN_MEDIAN)
                lastLightValues.remove(0);
        }

        //if(sensorChanged)
            //FileManager.saveOnCsvFile();

        if(listAngularVelocityGyroscope.size() >= MIN_VALUES_TO_FFT && listAngularVelocityAccelometer.size() >= MIN_VALUES_TO_FFT)
        {
            Fft fft = new Fft(MIN_VALUES_TO_FFT);

            double[] window = fft.getWindow();

            double[] reGyroscope = new double[MIN_VALUES_TO_FFT];
            double[] imGyroscope = new double[MIN_VALUES_TO_FFT];

            for (int i = 0; i < reGyroscope.length; i++)
                reGyroscope[i] = listAngularVelocityGyroscope.get(i);

            double[] reAccelometer = new double[MIN_VALUES_TO_FFT];
            double[] imAccelometer = new double[MIN_VALUES_TO_FFT];
            for (int i = 0; i < reAccelometer.length; i++)
                reAccelometer[i] = listAngularVelocityAccelometer.get(i);

            fft.fft(reGyroscope, imGyroscope);
            fft.fft(reAccelometer, imAccelometer);

            //all data are filtered and applied the fft
            ArrayList<Float> accelometerDataProcessed = new ArrayList<>();
            ArrayList<Float> gyroscopeDataProcessed = new ArrayList<>();

            for(int i = 0; i < reAccelometer.length; i++)
                accelometerDataProcessed.add(getAngularVelocity(reAccelometer[i], imAccelometer[i]));

            for(int i = 0; i < reGyroscope.length; i++)
                gyroscopeDataProcessed.add(getAngularVelocity(reGyroscope[i], imGyroscope[i]));

            //if the AutoMode isn't checked only save the data on the file
            if(MainActivity.ckAutoMode.isChecked()){
                String predictedActivity;

                //need to send to weka managemnet the getAngularVelocity(reGyroscope[i], imGyroscope[i])
                predictedActivity = wekaManagement.predict(accelometerDataProcessed,gyroscopeDataProcessed,getLightMedian());

                MainActivity.tvActualActivityPredicted.setText("" + predictedActivity);

            }
            else
                FileManager.saveOnArffFile(accelometerDataProcessed,gyroscopeDataProcessed,getLightMedian(), MainActivity.actualUserActivity.toString());


            clearAngularVelocities();
        }
    }

    public void clearAngularVelocities(){
        if(this.listAngularVelocityGyroscope != null)
            this.listAngularVelocityGyroscope.clear();

        if(this.listAngularVelocityAccelometer != null)
            this.listAngularVelocityAccelometer.clear();
    }

    public String getLightMedian(){
        return Config.LIGHT_NORMAL;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something here if sensor accuracy changes.
    }

    private Float getMedian(List<Float> valuesArrayList){
        if(valuesArrayList == null)
            return null;

        if(valuesArrayList.size() < MIN_VALUES_TO_MEAN_MEDIAN)
            return null;

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

    private Float getMean(List<Float> valuesArrayList, int sizeOfArray){

        if(sizeOfArray < MIN_VALUES_TO_MEAN_MEDIAN)
            return null;

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
