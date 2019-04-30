package com.shubhamk.datacollectionwear;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;

import java.util.Calendar;
import java.util.Vector;

public class SensorService extends Service implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor sensor_acc, sensor_gyro, sensor_heartrate, sensor_magnet;
    static Vector<SensorData> sensorData = new Vector<>();

    @Override
    public void onCreate() {
        super.onCreate();
        sensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
        sensor_acc = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensor_gyro = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        sensor_heartrate = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
        sensor_magnet = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        sensorManager.registerListener(this, sensor_acc, SensorManager.SENSOR_DELAY_FASTEST);
        //sensorManager.registerListener(this,sensor_gyro,SensorManager.SENSOR_DELAY_FASTEST);
        //sensorManager.registerListener(this,sensor_heartrate,SensorManager.SENSOR_DELAY_FASTEST);
        //sensorManager.registerListener(this,sensor_magnet,SensorManager.SENSOR_DELAY_FASTEST);

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor mySensor = event.sensor;

        if (mySensor.getType() == Sensor.TYPE_HEART_RATE) {
            float x = event.values[0];
            sensorData.add(new SensorData(x, x, x, mySensor.getName(), Calendar.getInstance().getTime().getTime()));
        } else {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            sensorData.add(new SensorData(x, y, z, mySensor.getName(), Calendar.getInstance().getTime().getTime()));

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}