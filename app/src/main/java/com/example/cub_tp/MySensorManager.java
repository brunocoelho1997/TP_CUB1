package com.example.cub_tp;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;

import java.util.List;


public class MySensorManager extends AppCompatActivity implements SensorEventListener {

    //----sensors
    private SensorManager sensorManager;
    private List<Sensor> deviceSensors;
    private Sensor gyroscope;
    private Sensor accelometer;

    private static float mLastXGyroscope, mLastYGyroscope, mLastZGyroscope; //used by gyroscope
    private final float NOISE = (float) 2.0; //used by gyroscope

    private static float lastXAccelometer, lastYAccelometer, lastZAccelometer;

    public MySensorManager(SensorManager sensorManager) {

        //for sensors
        this.sensorManager = sensorManager;
        this.deviceSensors = sensorManager.getSensorList(Sensor.TYPE_ALL);
        for(int i=0; i<deviceSensors.size();i++) {
            MainActivity.tvSensorList.setText( MainActivity.tvSensorList.getText() + deviceSensors.get(i).getName() + "\n");
        }

        //define Gyroscope
        if (sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null){
            this.gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        }

        //define Accelometer
        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null){
            this.accelometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }
    }

    public void startSensors(){
        sensorManager.registerListener(MySensorManager.this, gyroscope, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(MySensorManager.this, accelometer, SensorManager.SENSOR_DELAY_NORMAL);

    }

    public void stopSensors(){
        sensorManager.unregisterListener(MySensorManager.this);
    }

    //for gyroscope and accelometer
    @Override
    public void onSensorChanged(SensorEvent event) {

        //applyLowPassFilter(linear_acceleration, event);

        if(event.sensor.getType() == Sensor.TYPE_GYROSCOPE)
        {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            float deltaX = Math.abs(mLastXGyroscope - x);
            float deltaY = Math.abs(mLastYGyroscope - y);
            float deltaZ = Math.abs(mLastZGyroscope - z);
            if (deltaX < NOISE) deltaX = (float)0.0;
            if (deltaY < NOISE) deltaY = (float)0.0;
            if (deltaZ < NOISE) deltaZ = (float)0.0;
            mLastXGyroscope = x;
            mLastYGyroscope = y;
            mLastZGyroscope = z;

            MainActivity.tvInfoGyroscope.setText("Gyroscope: x= " + mLastXGyroscope + " y= " + mLastYGyroscope + " z= " + mLastZGyroscope);

        }
        else if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
        {
            lastXAccelometer = event.values[0];
            lastYAccelometer = event.values[1];
            lastZAccelometer = event.values[2];
        }

        FileManager.saveOnTxtFile();
    }



    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something here if sensor accuracy changes.
    }

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
}
