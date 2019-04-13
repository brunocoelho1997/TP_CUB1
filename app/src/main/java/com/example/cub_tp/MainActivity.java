package com.example.cub_tp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.util.List;
import java.util.Locale;

import static com.example.cub_tp.Config.*;

public class MainActivity extends AppCompatActivity {

    //----sensors
    private MySensorManager mySensorManager;
    private MyGps myGps;
    public static UserActivity actualUserActivity;

    private FileManager fileManager;

    //layout vars
    public static RadioGroup rgGroupRadio;
    public static Button btnSaveToServer;
    public static TextView tvSensorList;
    public static TextView tvInfoGyroscope;
    public static TextView tvInfoGps;
    public static Button btnStartCollectData;
    public static Button btnStopCollectingData;
    public static CheckBox ckAutoMode;
    public static Button btnMaps;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        defineLayout();
        defineSensors();

        this.fileManager = new FileManager(myGps,mySensorManager);
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
        this.btnMaps = findViewById(R.id.btn_maps);

        this.tvSensorList.setText("");

        //define onclick event to btn save to server
        btnSaveToServer.setOnClickListener(new SaveToServerListener(getApplicationContext()));

        //start collection data
        btnStartCollectData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mySensorManager.startSensors();
                myGps.startGpsListening(v.getContext());
            }
        });

        //stop collection data
        btnStopCollectingData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mySensorManager.stopSensors();
                myGps.stopGpsListening(v.getContext());
                FileManager.restartSessionId();
                tvInfoGps.setText("");
                tvInfoGyroscope.setText("");
            }
        });


        //define radio buttons group
        rgGroupRadio.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                RadioButton button = (RadioButton) group.findViewById(checkedId);

                switch(button.getId()) {
                    case R.id.rb_walking:
                        actualUserActivity = UserActivity.WALKING;
                        break;
                    case R.id.rb_sitting:
                        actualUserActivity = UserActivity.SITTING;
                        break;
                    case R.id.rb_walking_upstairs:
                        actualUserActivity = UserActivity.WALKING_UPSTAIRS;
                        break;
                    case R.id.rb_walking_downstairs:
                        actualUserActivity = UserActivity.WALKING_DOWNSTAIRS;
                        break;
                    case R.id.rb_laying:
                        actualUserActivity = UserActivity.LAYING;
                        break;
                }
            }
        });

        ((RadioButton)rgGroupRadio.getChildAt(0)).setChecked(true);


        btnMaps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String uri = String.format(Locale.ENGLISH, "geo:%f,%f", myGps.getActualLatitude(), myGps.getActualLongitude());
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                v.getContext().startActivity(intent);
            }
        });
    }

    private void defineSensors() {

        //if user doen't have permissions need to ask him
        checkPermissions(); //TODO: need to return boolean value;

        //define sensor manager
        this.mySensorManager = new MySensorManager((SensorManager) getSystemService(Context.SENSOR_SERVICE));

        //define GPS
        this.myGps = new MyGps();
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



    @Override
    protected void onResume() {
        super.onResume();
        //just register listener when we click start button
        //sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_NORMAL);
        //sensorManager.registerListener(this, accelometer, SensorManager.SENSOR_DELAY_NORMAL);

    }

    @Override
    protected void onPause() {
        super.onPause();
        mySensorManager.stopSensors();
    }
}
