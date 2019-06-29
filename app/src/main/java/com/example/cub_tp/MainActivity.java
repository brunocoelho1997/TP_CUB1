package com.example.cub_tp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.SensorManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

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
    public static Button btnDownloadFromServer;
    public static TextView tvSensorList;
    public static TextView tvInfoGyroscope;
    public static TextView tvInfoAccelometer;
    public static TextView tvInfoLight;
    public static TextView tvActualActivityPredicted;
    public static TextView tvInfoGps;
    public static Button btnStartCollectData;
    public static Button btnStopCollectingData;
    public static CheckBox ckAutoMode;
    public static Button btnMaps;


    private boolean userHasPermissions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.userHasPermissions = hasPermissions(this, PERMISSIONS);

        if(userHasPermissions)
        {
            defineLayout();
            defineSensors();
        }
        else
            requestPermissionsToUser();
    }

    private void defineLayout() {

        this.rgGroupRadio = findViewById(R.id.rg_radio_buttons);
        this.btnStartCollectData = findViewById(R.id.btn_start_collect);
        this.btnStopCollectingData = findViewById(R.id.btn_stop_collect);
        this.btnSaveToServer = findViewById(R.id.btn_send_data);
        this.btnDownloadFromServer = findViewById(R.id.btn_download_model);
        this.tvInfoGyroscope = findViewById(R.id.tv_info_gyroscope);
        this.tvInfoAccelometer= findViewById(R.id.tv_info_accelometer);
        this.tvInfoLight= findViewById(R.id.tv_info_light);
        this.tvActualActivityPredicted = findViewById(R.id.tv_info_predicted_activity);
        this.tvInfoGps = findViewById(R.id.tv_info_gps);
        this.tvSensorList = findViewById(R.id.tv_info_sensors);
        this.ckAutoMode = findViewById(R.id.ck_auto_mode);
        this.btnMaps = findViewById(R.id.btn_maps);

        this.tvSensorList.setText("");

        //define onclick event to btn save to server
        btnSaveToServer.setOnClickListener(new SaveToServerListener(this, btnSaveToServer));
        if(!FileManager.dataFileExists())
            btnSaveToServer.setEnabled(false);

        //define onclick event to btn download model from server
        btnDownloadFromServer.setOnClickListener(new LoadModelFromServerListener(this, btnDownloadFromServer));


        //start collection data
        btnStartCollectData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mySensorManager.startSensors();
                myGps.startGpsListening(v.getContext());
                btnSaveToServer.setEnabled(false);
                btnMaps.setEnabled(false);
                btnStartCollectData.setEnabled(false);
                btnStopCollectingData.setEnabled(true);
            }
        });

        //stop collection data
        btnStopCollectingData.setEnabled(false);
        btnStopCollectingData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mySensorManager.stopSensors();
                mySensorManager.clearAngularVelocities();
                myGps.stopGpsListening(v.getContext());
                FileManager.restartSessionId();
                tvInfoGps.setText("");
                tvInfoGyroscope.setText("");
                tvInfoAccelometer.setText("");

                btnSaveToServer.setEnabled(true);
                btnMaps.setEnabled(true);
                btnSaveToServer.setEnabled(true);
                btnStopCollectingData.setEnabled(false);
                btnStartCollectData.setEnabled(true);
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

            if(mySensorManager != null)
                mySensorManager.clearAngularVelocities();
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

        ckAutoMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                {
                    btnSaveToServer.setEnabled(false);
                    btnStartCollectData.setEnabled(false);
                    btnStopCollectingData.setEnabled(false);

                    for (int i = 0; i < rgGroupRadio.getChildCount(); i++) {
                        rgGroupRadio.getChildAt(i).setEnabled(false);
                    }
                    mySensorManager.startSensors();
                }
                else
                {
                    mySensorManager.stopSensors();

                    for (int i = 0; i < rgGroupRadio.getChildCount(); i++) {
                        rgGroupRadio.getChildAt(i).setEnabled(true);
                    }
                    if(FileManager.dataFileExists())
                        btnSaveToServer.setEnabled(true);
                    btnStartCollectData.setEnabled(true);
                    //btnStopCollectingData.setEnabled(false);
                }
            }
        });
    }

    private void defineSensors() {
        //define sensor manager
        this.mySensorManager = new MySensorManager((SensorManager) getSystemService(Context.SENSOR_SERVICE));
        //define GPS
        this.myGps = new MyGps();

        //define where will be written the data
        this.fileManager = new FileManager(myGps,mySensorManager);
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
        if(mySensorManager != null)
            mySensorManager.stopSensors();
    }

    /*

     ---------------------permissions code -------------------------------

      */

    private void requestPermissionsToUser() {
        if(!hasPermissions(this, PERMISSIONS)){
            ActivityCompat.requestPermissions(this, PERMISSIONS, Config.MY_PERMISSIONS_REQUEST_CODE);
        }
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
            defineLayout();
            defineSensors();
        }
        else
        {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.permissions_denied)
                    .setMessage(R.string.permissions_denied_desc)

                    // Specifying a listener allows you to take an action before dismissing the dialog.
                    // The dialog is automatically dismissed when a dialog button is clicked.
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
    }
}
