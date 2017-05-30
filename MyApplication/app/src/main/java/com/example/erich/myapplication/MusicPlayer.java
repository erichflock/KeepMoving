package com.example.erich.myapplication;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;

public class MusicPlayer extends AppCompatActivity implements SensorEventListener, MediaPlayer.OnPreparedListener, android.location.LocationListener {
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 0, MY_PERMISSIONS_REQUEST_ACCESS_LOCATION = 0;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private MediaPlayer mediaPlayerRun, mediaPlayerBike;
    private Button chooseFileRun, chooseFileBike;
    private TextView textViewSpeed;
    private boolean buttonRun;
    private LocationManager mLocationClient;
    private int speed = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_player);

        /*
        Request permission to read the data in the mobile
         */

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
        }

        /*
        create the objects responsible for receiving the location
        http://stackoverflow.com/questions/16898675/how-does-it-work-requestlocationupdates-locationrequest-listener
         */

        mLocationClient = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            /*
            Request permission to access Location
            */
            requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_LOCATION);
            return;
        }
        mLocationClient.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, this);

        /*
        Initialize textViewActivity: displays the activity
         */
        textViewSpeed = (TextView) findViewById(R.id.textViewSpeed);

        /*
        Initialize Media Players objects
        */
        mediaPlayerRun = new MediaPlayer();
        mediaPlayerBike = new MediaPlayer();

        /*
        https://developer.android.com/guide/topics/sensors/sensors_overview.html
         */
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        /*
        http://www.techrepublic.com/blog/software-engineer/a-quick-tutorial-on-coding-androids-accelerometer/
         */
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        /*
        http://stackoverflow.com/questions/17042308/select-a-music-file-to-play-with-mediaplayer
        Allows the user to choose a file
         */

        /*
        Choose a music to run
         */
        chooseFileRun = (Button) findViewById(R.id.chooseFileRun);
        chooseFileRun.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonRun = true;
                /*
                Release and initialize Media Player every time the button is pressed in order to allow the user to change the song.
                */
                mediaPlayerRun.release();
                mediaPlayerRun = new MediaPlayer();
                Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, 10);
            }
        });

        /*
        Choose a music to bike
         */
        chooseFileBike = (Button) findViewById(R.id.chooseFileBike);
        chooseFileBike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonRun = false;
                /*
                Release and initialize Media Player every time the button is pressed in order to allow the user to change the song.
                */
                mediaPlayerBike.release();
                mediaPlayerBike = new MediaPlayer();
                Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, 10);
            }
        });
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            /*
            3 options:
                1. Walking/Running: Speed > 0 and <= 10 km/h
                2. Biking: Speed > 10 and <= 20 km/h
                3. Car or Bus: Speed > 20 km/h
             */
            if ((getAccelerometer(event) > 0.9 && getAccelerometer(event) < 1.1)) {
                /*
                User is not moving the body. The song should stop
                 */
                mediaPlayerBike.pause();
                mediaPlayerRun.pause();
            } else {
                /*
                User is moving. We use the speed to discover which activity the user is doing
                 */
                if (speed > 2 && speed <= 10) {
                    /*
                    Walking/Running
                     */
                    mediaPlayerBike.pause();
                    mediaPlayerRun.start();
                }
                if (speed > 10 && speed <= 20) {
                    /*
                    Biking
                     */
                    mediaPlayerRun.pause();
                    mediaPlayerBike.start();
                }
                if (speed > 20) {
                    /*
                    Car/Bus
                     */
                    mediaPlayerBike.pause();
                    mediaPlayerRun.pause();
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public double getAccelerometer(SensorEvent event) {
        float[] values = event.values;
        // Movement
        float x = values[0];
        float y = values[1];
        float z = values[2];

        //Log.d("Values", "x: " + x + " y: " + y + " z: " + z);

        float accelerationSquareRoot = (float) ((x * x + y * y + z * z)
                / (9.80665 * 9.80665));

        return Math.sqrt(accelerationSquareRoot);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK && requestCode == 10) {
            Uri uriSound = data.getData();
            if (buttonRun)
                playRun(this, uriSound);
            else
                playBike(this, uriSound);
        }
    }

    private void playRun(Context context, Uri uri) {

        try {
            mediaPlayerRun.setDataSource(context, uri);
            mediaPlayerRun.setOnPreparedListener(this);
            mediaPlayerRun.prepareAsync();
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void playBike(Context context, Uri uri) {

        try {
            mediaPlayerBike.setDataSource(context, uri);
            mediaPlayerBike.setOnPreparedListener(this);
            mediaPlayerBike.prepareAsync();
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        mediaPlayer.start();
        mediaPlayer.pause();
    }

    /*
    Location
    http://stackoverflow.com/questions/15570542/determining-the-speed-of-a-vehicle-using-gps-in-android
     */

    @Override
    public void onLocationChanged(Location location) {
         /*
        The speed is converted from m/s to km/h
         */
        if (location != null) {
            speed = (int) ((location.getSpeed() * 3600) / 1000);
            Log.d("Speed: ", String.valueOf(speed));
            textViewSpeed.setText(String.valueOf(speed) + " km/h");
        } else
            textViewSpeed.setText("0 km/h");
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

}