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
import android.view.View;
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
    private float mLastX, mLastY, mLastZ; //used by gyroscope
    private final float NOISE = (float) 2.0; //used by gyroscope
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

        //start collection data
        btnStartCollectData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sensorManager.registerListener(MainActivity.this, gyroscope, SensorManager.SENSOR_DELAY_NORMAL);
                GpsUtil.startGpsListening(v.getContext());
            }
        });

        //stop collection data
        btnStopCollectingData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sensorManager.unregisterListener(MainActivity.this);
                GpsUtil.stopGpsListening(v.getContext());

                tvInfoGps.setText("");
                tvInfoGyroscope.setText("");
            }
        });
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
            tvInfoGyroscope.setText("Gyroscope: x= " + mLastX + " y= " + mLastY + " z= " + mLastZ);
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
        //sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_NORMAL);
        //sensorManager.registerListener(this, accelometer, SensorManager.SENSOR_DELAY_NORMAL);

    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }
}
