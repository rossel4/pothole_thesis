package com.example.demo2;

import static java.lang.Math.abs;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.PermissionChecker;

import android.Manifest;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.lang.Math;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;


public class MainActivity extends AppCompatActivity implements SensorEventListener, View.OnClickListener {
    private static final String TAG = "MainActivity";
    SensorManager sensorManager;
    Sensor accelerometer;
    Context context;
    Sensor giro;
    String sensorName;

    double[] arrGps = new double[2];

    int flag = 0;

    int numSamples = 119;
    String[][] accBuff = new String[3][238];
    String[] accBuffX = new String[238];
    String[] accBuffY = new String[238];
    String[] accBuffZ = new String[238];
    int take = 0;
    int samplesRead = numSamples;
    float accelerationThreshold = 10;
    long counter = 0;

    // GIROCOPIO
    int girnumSamples = 119;
    float[][] girBuff = new float[3][238];
    float[] girBuffX = new float[238];
    float[] girBuffY = new float[238];
    float[] girBuffZ = new float[238];
    int girtake = 0;
    int girsamplesRead = girnumSamples;
    float giroscopeThreshold = 19;
    long gircounter = 0;

    //INTERFAZ
    String textViewContent = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        context = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView letrero1 = (TextView) findViewById((R.id.letrero1));
        letrero1.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                ClipboardManager clip = (ClipboardManager)getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
                clip.setText(letrero1.getText().toString());
                Toast.makeText(getApplicationContext(),"Ya:p",Toast.LENGTH_SHORT).show();
            }
        });
        letrero1.setMovementMethod(new ScrollingMovementMethod());
        letrero1.append("AX,AY,AZ,GX,GY,GZ\n");

        TextView letrero2 = (TextView) findViewById((R.id.letrero2));
        letrero2.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                ClipboardManager clip = (ClipboardManager)getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
                clip.setText(letrero2.getText().toString());
                Toast.makeText(getApplicationContext(),"Ya:p",Toast.LENGTH_SHORT).show();
            }
        });
        letrero2.setMovementMethod(new ScrollingMovementMethod());
        letrero2.append("AX,AY,AZ,GX,GY,GZ\n");

        TextView letrero3 = (TextView) findViewById((R.id.letrero3));
        letrero3.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                ClipboardManager clip = (ClipboardManager)getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
                clip.setText(letrero3.getText().toString());
                Toast.makeText(getApplicationContext(),"Ya:p",Toast.LENGTH_SHORT).show();
            }
        });
        letrero3.setMovementMethod(new ScrollingMovementMethod());
        letrero3.append("AX,AY,AZ,GX,GY,GZ\n");

        Button botonBache = (Button) findViewById((R.id.botonBache));
        Button botonTope = (Button) findViewById((R.id.botonTope));
        Button botonNormal = (Button) findViewById((R.id.botonNormal));

        LocationManager locationManager = (LocationManager) MainActivity.this.getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListener = new LocationListener() {
            int permissionCheck = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION);

            @Override
            public void onLocationChanged(Location location) {
                String cords = "Coordenadas => Longitud: " + location.getLongitude() + "Latitud: " + location.getLatitude();
                arrGps[0] = location.getLongitude();
                arrGps[1] = location.getLatitude();
                Log.d(TAG, cords);
            }

            public void onProviderEnabled(String provider) {
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }
        };

        botonBache.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(textViewContent!="") {
                    letrero1.append(textViewContent);
                    letrero1.append("\n");
                    textViewContent = "";
                }
                if(letrero1.getVisibility() == View.INVISIBLE) {
                    letrero2.setVisibility((View.INVISIBLE));
                    letrero3.setVisibility((View.INVISIBLE));
                    letrero1.setVisibility((View.VISIBLE));
                }
            }
        });

        botonTope.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(textViewContent!="") {
                    letrero2.append(textViewContent);
                    letrero2.append("\n");
                    textViewContent = "";
                }
                textViewContent="";
                if(letrero2.getVisibility() == View.INVISIBLE) {
                    letrero1.setVisibility((View.INVISIBLE));
                    letrero3.setVisibility((View.INVISIBLE));
                    letrero2.setVisibility((View.VISIBLE));
                }
            }
        });

        botonNormal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(textViewContent!="") {
                    letrero3.append(textViewContent);
                    letrero3.append("\n");
                    textViewContent = "";
                }
                if(letrero3.getVisibility() == View.INVISIBLE) {
                    letrero1.setVisibility((View.INVISIBLE));
                    letrero2.setVisibility((View.INVISIBLE));
                    letrero3.setVisibility((View.VISIBLE));
                }
            }
        });

        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);


        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        Log.d(TAG, "Iniciando acelerometro");
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
        Log.d(TAG, "Acelerometro iniciado");

        Log.d(TAG, "Iniciando giroscopio");
        giro = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        sensorManager.registerListener(this, giro, SensorManager.SENSOR_DELAY_FASTEST);
        Log.d(TAG, "Giroscopio iniciado");

        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionCheck == PackageManager.PERMISSION_DENIED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {

            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
    }

    boolean isAccelData = false;
    boolean isGyroData = false;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            isGyroData = true;
            sensorName = sensorEvent.sensor.getName();
            //Log.d(TAG,sensorName + " Accel X: "+ sensorEvent.values[0] + " Accel Y: "+ sensorEvent.values[1] + " Accel Z: "+ sensorEvent.values[2]);
            //buffer
            while (girsamplesRead == girnumSamples) {
                float gSum = abs(sensorEvent.values[0]) + abs(sensorEvent.values[1]) + abs(sensorEvent.values[2]);
                if (gircounter < 118)
                    gircounter++;
                for (int i = 0; i < 119; i++) {
                    for (int j = 0; j < 3; j++) {
                        girBuff[j][i] = girBuff[j][i+1];
                        if(j==0){
                            girBuffX[i] = girBuffX[i+1];
                        }else if(j==1){
                            girBuffY[i] = girBuffY[i+1];
                        }else if(j==2){
                            girBuffZ[i] = girBuffZ[i+1];
                        }
                    }
                }

                girBuff[0][118] = sensorEvent.values[0];
                girBuff[1][118] = sensorEvent.values[1];
                girBuff[2][118] = sensorEvent.values[2];
                girBuffX[118] = sensorEvent.values[0];
                girBuffY[118] = sensorEvent.values[1];
                girBuffZ[118] = sensorEvent.values[2];

                //Log.d(TAG, "Suma " + Float.toString(aSum));

                if (flag == 1 && gircounter >= 118) {
                    girsamplesRead = 0;
                    break;
                } else {
                    break;
                }
            }

            while (girsamplesRead < girnumSamples) {
                girsamplesRead++;
                girBuff[0][girsamplesRead + 118] = sensorEvent.values[0];
                girBuff[1][girsamplesRead + 118] = sensorEvent.values[1];
                girBuff[2][girsamplesRead + 118] = sensorEvent.values[2];
                girBuffX[118+girsamplesRead] = sensorEvent.values[0];
                girBuffY[118+girsamplesRead] = sensorEvent.values[1];
                girBuffZ[118+girsamplesRead] = sensorEvent.values[2];

                if (girsamplesRead == girnumSamples) {
                    for (int i = 0; i < 238; i++) {
                        //Log.d(TAG, sensorName + i + " X: " + Float.toString(girBuff[0][i]) + " Y: " + Float.toString(girBuff[1][i]) + " Z: " + Float.toString(girBuff[2][i]));

                        // \n
                    }
                    // \n
                    //samplesRead = 118;
                    flag = 0;
                    break;

                }
                break;
            }

        }
        if (sensorEvent.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            isAccelData = true;
            sensorName = sensorEvent.sensor.getName();
            //Log.d(TAG,sensorName + " Accel X: "+ sensorEvent.values[0] + " Accel Y: "+ sensorEvent.values[1] + " Accel Z: "+ sensorEvent.values[2]);

            //buffer0
            while (samplesRead == numSamples) {
                float aSum = abs(sensorEvent.values[0]) + abs(sensorEvent.values[1]) + abs(sensorEvent.values[2]);
                if (counter < 118)
                    counter++;
                for (int i = 0; i < 119; i++) {
                    for (int j = 0; j < 3; j++) {
                        accBuff[j][i] = accBuff[j][i+1];
                        if(j==0){
                            accBuffX[i] = accBuffX[i+1];
                        }else if(j==1){
                            accBuffY[i] = accBuffY[i+1];
                        }else if(j==2){
                            accBuffZ[i] = accBuffZ[i+1];
                        }
                    }
                }

                accBuff[0][118] =  Float.toString(sensorEvent.values[0]);
                accBuff[1][118] =  Float.toString(sensorEvent.values[1]);
                accBuff[2][118] =  Float.toString(sensorEvent.values[2]);
                accBuffX[118] =  Float.toString(sensorEvent.values[0]);
                accBuffY[118] =  Float.toString(sensorEvent.values[1]);
                accBuffZ[118] =  Float.toString(sensorEvent.values[2]);

                //Log.d(TAG, "Suma " + Float.toString(aSum));

                if (aSum >= accelerationThreshold && counter >= 118) {
                    flag = 1;
                    samplesRead = 0;
                    break;
                } else {
                    break;
                }
            }

            while (samplesRead < numSamples) {
                samplesRead++;
                accBuff[0][samplesRead + 118] = Float.toString(sensorEvent.values[0]);
                accBuff[1][samplesRead + 118] =  Float.toString(sensorEvent.values[1]);
                accBuff[2][samplesRead + 118] =  Float.toString(sensorEvent.values[2]);
                accBuffX[118+samplesRead] =  Float.toString(sensorEvent.values[0]);
                accBuffY[118+samplesRead] =  Float.toString(sensorEvent.values[1]);
                accBuffZ[118+samplesRead] =  Float.toString(sensorEvent.values[2]);
                if (samplesRead == numSamples) {
                    for (int i = 0; i < 238; i++) {
                        textViewContent=textViewContent+(accBuff[0][i] + "," + accBuff[1][i] + "," + accBuff[2][i] + "," + girBuff[0][i] + "," + girBuff[1][i] + "," + girBuff[2][i] + "\n");
                        // \n
                    }
                    Log.d(TAG, textViewContent);
                }
                break;
            }
        }

        if (isAccelData & isGyroData) {
            isAccelData = false;
            isGyroData = false;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public void onClick(View view) {

    }
}