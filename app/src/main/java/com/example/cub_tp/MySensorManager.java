package com.example.cub_tp;

import android.content.Context;
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
    private float mLastX, mLastY, mLastZ; //used by gyroscope
    private final float NOISE = (float) 2.0; //used by gyroscope
    private Sensor accelometer;

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

            float deltaX = Math.abs(mLastX - x);
            float deltaY = Math.abs(mLastY - y);
            float deltaZ = Math.abs(mLastZ - z);
            if (deltaX < NOISE) deltaX = (float)0.0;
            if (deltaY < NOISE) deltaY = (float)0.0;
            if (deltaZ < NOISE) deltaZ = (float)0.0;
            mLastX = x;
            mLastY = y;
            mLastZ = z;


        }
        MainActivity.tvInfoGyroscope.setText("Gyroscope: x= " + mLastX + " y= " + mLastY + " z= " + mLastZ);
        //else if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
        //tvAccelometer.setText("Acccelometer: x= " + linear_acceleration[0] + " y= " + linear_acceleration[1] + " z= " + linear_acceleration[1]);

        FileManager.saveOnTxtFile();
    }



    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something here if sensor accuracy changes.
    }
}
