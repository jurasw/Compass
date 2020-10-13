package com.jerzywisniewski.compass;

import android.Manifest;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MainActivity extends AppCompatActivity implements SensorEventListener {


    private ImageView image, shield;
    private float currentDegree = 0f;
    private float currentDegreeForNorth = 0f;
    private SensorManager mSensorManager;
    private TextView tvHeading, distanceText;
    private Location location = new Location("A");
    private Location target = new Location("B");
    private EditText latitudeInput, longitudeInput;
    private Button setLocationBtn;
    private double lo, la;
    private FusedLocationProviderClient client;
    private Dialog myDialog;
    private  double defaultLatitude = 54.904618;
    private  double defaultLongitude = 23.978782;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        image = (ImageView) findViewById(R.id.direction);
        shield = (ImageView) findViewById(R.id.shield);
        tvHeading = (TextView) findViewById(R.id.tvHeading);
        setLocationBtn = (Button) findViewById(R.id.button);
        distanceText = (TextView) findViewById(R.id.distnace);

        myDialog = new Dialog(this);

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        target.setLatitude(defaultLatitude);
        target.setLongitude(defaultLongitude);
        la = defaultLatitude;
        lo = defaultLongitude;


        setLocationBtn.performClick();
    }

    @Override
    protected void onStart() {
        super.onStart();
        requestPermission();
        client = LocationServices.getFusedLocationProviderClient(this);
        if(ActivityCompat.checkSelfPermission(MainActivity.this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ){
            return;
        }
        client.getLastLocation().addOnSuccessListener(MainActivity.this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                Location startPoint=new Location("locationA");
                startPoint.setLatitude(location.getLongitude());
                startPoint.setLongitude( location.getLongitude());

                Location endPoint=new Location("locationA");
                endPoint.setLatitude(lo);
                endPoint.setLongitude(la);

                double distance=startPoint.distanceTo(endPoint);
                distanceText.setText("distance: " + distance + "m");
            }
        });
    }

    private void requestPermission(){
        ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION}, 1);
    }


    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }



    @Override
    public void onSensorChanged(SensorEvent event) {
        float degree = Math.round(event.values[0]);
        float bearing = location.bearingTo(target);
        degree = (bearing - degree) * -1;
        degree = normalizeDegree(degree);
        tvHeading.setText("Heading: " + Float.toString(degree) + " degrees");
        RotateAnimation ra = new RotateAnimation(currentDegree, -degree, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        ra.setDuration(210);
        ra.setFillAfter(true);

        // Start the animation
        image.startAnimation(ra);
        currentDegree = -degree;
        float degree2 = Math.round(event.values[0]);
        RotateAnimation ra2 = new RotateAnimation(currentDegreeForNorth, -degree2, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        ra2.setDuration(210);
        ra2.setFillAfter(true);

        // Start the animation
        shield.startAnimation(ra2);
        currentDegreeForNorth = -degree2;
    }

    private float normalizeDegree(float value) {
        if (value >= 0.0f && value <= 180.0f) {
            return value;
        } else {
            return 180 + (180 + value);
        }
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // not in use
    }


    public void ShowPopup(View v) {
        myDialog.setContentView(R.layout.popup);
        Button setBtn;
        setBtn =(Button) myDialog.findViewById(R.id.setBtn);
        latitudeInput =(EditText) myDialog.findViewById(R.id.latitudeInput);
        longitudeInput =(EditText) myDialog.findViewById(R.id.longitudeInput);
        setBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(latitudeInput.getText().toString() == null || longitudeInput.getText().toString() == null){
                    Toast.makeText(MainActivity.this, "You did not enter a coordinates", Toast.LENGTH_SHORT).show();
                }
                else {
                    target.setLatitude(Double.parseDouble(latitudeInput.getText().toString()));
                    la = Double.parseDouble(latitudeInput.getText().toString());
                    target.setLongitude(Double.parseDouble(longitudeInput.getText().toString()));
                    lo = Double.parseDouble(longitudeInput.getText().toString());
                    myDialog.dismiss();
                }
            }
        });
        myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        myDialog.show();
    }
}