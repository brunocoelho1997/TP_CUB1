package com.example.cub_tp;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.util.List;

import static com.example.cub_tp.Config.*;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    //----sensors
    private SensorManager sensorManager;
    private List<Sensor> deviceSensors;
    private Sensor gyroscope;
    private Sensor accelometer;


    //layout vars
    public static RadioGroup rgGroupRadio;
    public static Button btnSaveToServer;
    public static TextView tvSensorList;
    public static TextView tvInfoGyroscope;
    public static TextView tvInfoGps;
    public static Button btnStartCollectData;
    public static Button btnStopCollectingData;
    public static CheckBox ckAutoMode;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        defineLayout();
        defineSensors();
    }

    private void defineLayout() {

        this.rgGroupRadio = findViewById(R.id.rg_radio_buttons);
        this.btnStartCollectData = findViewById(R.id.btn_start_collect);
        this.btnStopCollectingData = findViewById(R.id.btn_stop_collect);
        this.btnSaveToServer = findViewById(R.id.btn_send_data);
        this.tvInfoGyroscope = findViewById(R.id.tv_info_gyroscope);
        this.tvInfoGps = findViewById(R.id.tv_info_gps);
        this.tvSensorList = findViewById(R.id.tv_info_sensors);
        this.ckAutoMode = findViewById(R.id.ck_auto_mode);

        this.tvSensorList.setText("");

        //define onclick event to btn save to server
        //btnSaveToServer.setOnClickListener(new SaveToServerListener());
    }


    private void defineSensors() {

        //if user doen't have permissions need to ask him
        checkPermissions(); //TODO: need to return boolean value;

        //for sensors
        this.sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        this.deviceSensors = sensorManager.getSensorList(Sensor.TYPE_ALL);
        for(int i=0; i<deviceSensors.size();i++) {
            this.tvSensorList.setText( this.tvSensorList.getText() + deviceSensors.get(i).getName() + "\n");
        }

        //define Gyroscope
        if (sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null){
            this.gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        }

        //define Accelometer
        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null){
            this.accelometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }

        GpsUtil.defineGPSSensor(getApplicationContext());
    }

    private void checkPermissions() {

        //for GPS Location
        // Register the listener with the Location Manager to receive location updates
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_GET_ACCESS_LOCATION);
            }
        }

        //TODO: for external save - this isn't working...
        /*
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_GET_WRITE_EXTERNAL_STORAGE);
            }
        }
        */
    }

    //for gyroscope and accelometer
    @Override
    public void onSensorChanged(SensorEvent event) {

        //applyLowPassFilter(linear_acceleration, event);


        final float alpha = (float)0.8;
        float[] gravity = new float[3];
        float[] linear_acceleration = new float[3];
        // Isolate the force of gravity with the low-pass filter.
        gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
        gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
        gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

        // Remove the gravity contribution with the high-pass filter.
        linear_acceleration[0] = event.values[0] - gravity[0];
        linear_acceleration[1] = event.values[1] - gravity[1];
        linear_acceleration[2] = event.values[2] - gravity[2];


        if(event.sensor.getType() == Sensor.TYPE_GYROSCOPE)
            tvInfoGyroscope.setText("Gyroscope: x= " + linear_acceleration[0] + " y= " + linear_acceleration[1] + " z= " + linear_acceleration[2]);
        //else if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            //tvAccelometer.setText("Acccelometer: x= " + linear_acceleration[0] + " y= " + linear_acceleration[1] + " z= " + linear_acceleration[1]);

        FileUtil.saveOnTxtFile();
    }



    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something here if sensor accuracy changes.
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, accelometer, SensorManager.SENSOR_DELAY_NORMAL);

    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }
}
