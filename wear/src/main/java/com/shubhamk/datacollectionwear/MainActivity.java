package com.shubhamk.datacollectionwear;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MainActivity extends WearableActivity {
    private SensorManager sensorManager;
    private Sensor sensor1,sensor2;
    private int checker = -1;
    private Button startButton , stopButton;
    String activity = "Walking";
    private static final String FILE_HEADER = "Timestamp,ax,ay,az,Sensor,Activity";
    private static final String COMMA_DELIMITER = ",";
    private static final String NEW_LINE_SEPARATOR = "\n";
    String fileName;
    RadioGroup radioGroup;
    String[] act = {"Walking","Jogging","Sitting","Standing"};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startButton = findViewById(R.id.startRecord);
        stopButton = findViewById(R.id.stopRecord);
        sensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
        sensor1 = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensor2 = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        radioGroup = findViewById(R.id.rg);
        radioGroup.removeAllViews();
        for(int i = 0;i < 4;i++){
            RadioButton radioButton = new RadioButton(this);
            radioButton.setText(act[i]);
            radioGroup.addView(radioButton);
        }

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checker == 0){
                    Toast toast = Toast.makeText(getApplicationContext(), "Already recording data", Toast.LENGTH_LONG);
                    toast.show();
                }
                else{
                    checker = 0;
                    Intent intent = new Intent(getApplicationContext(), SensorService.class);
                    startService(intent);
                    startButton.setText("Recording data ....");
                }
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checker ==1){
                    Toast toast = Toast.makeText(getApplicationContext(),"Start a new recording",Toast.LENGTH_LONG);
                    toast.show();
                }
                else{
                    checker = 1;
                    startButton.setText("Start Recording");
                    Intent intent = new Intent(getApplicationContext(), SensorService.class);
                    stopService(intent);
                    int selectedId = radioGroup.getCheckedRadioButtonId();
                    if(selectedId!=-1){
                        RadioButton radioButton = findViewById(selectedId);
                        activity = radioButton.getText().toString();
                    }
                    fileName = activity + String.valueOf(Calendar.getInstance().getTime().getTime()) + ".csv";
                    try {
                        File file = new File(getApplication().getFilesDir(),fileName);
                        if (!file.exists()){
                            file.createNewFile();
                        }
                        FileWriter fileWriter = new FileWriter(file.getAbsoluteFile());
                        try {
                            fileWriter.append(FILE_HEADER);
                            fileWriter.append(NEW_LINE_SEPARATOR);
                            for (int i = 0; i < SensorService.sensorData.size(); i++) {
                                fileWriter.append(String.valueOf(SensorService.sensorData.get(i).getTimestamp()));
                                fileWriter.append(COMMA_DELIMITER);
                                fileWriter.append(String.valueOf(SensorService.sensorData.get(i).getAx()));
                                fileWriter.append(COMMA_DELIMITER);
                                fileWriter.append(String.valueOf(SensorService.sensorData.get(i).getAy()));
                                fileWriter.append(COMMA_DELIMITER);
                                fileWriter.append(String.valueOf(SensorService.sensorData.get(i).getAz()));
                                fileWriter.append(COMMA_DELIMITER);
                                fileWriter.append(String.valueOf(SensorService.sensorData.get(i).getSensor()));
                                fileWriter.append(COMMA_DELIMITER);
                                fileWriter.append(activity);
                                fileWriter.append(NEW_LINE_SEPARATOR);
                            }
                        }
                        catch (Exception e){
                            Log.v("Error",e.toString());
                        }
                        finally {
                            try {
                                fileWriter.flush();
                                fileWriter.close();
                                SensorService.sensorData.clear();
                                String datapath = "/my_path";
                                new SendMessage(datapath, file).start();
                            } catch (IOException e) {
                                System.out.println("Error while flushing/closing fileWriter !!!");
                                Log.v("Error",e.toString());
                            }
                        }

                    }
                    catch (IOException e){
                        Log.v("Error",e.toString());
                    }

                }
            }
        });
        IntentFilter newFilter = new IntentFilter(Intent.ACTION_SEND);
        Receiver messageReceiver = new Receiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver, newFilter);

    }

    public class Receiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String onMessageReceived = "I just received a message from the handheld ";
            Log.v("Receive", onMessageReceived);
        }
    }

    class SendMessage extends Thread {
        String path;
        File file;
        SendMessage(String p, File m) {
            path = p;
            file = m;
        }
        public void run() {
            Task<List<Node>> nodeListTask =
                    Wearable.getNodeClient(getApplicationContext()).getConnectedNodes();
            try {
                List<Node> nodes = Tasks.await(nodeListTask);
                for (Node node : nodes) {
                    byte[] bytes = new byte[(int) file.length()];
                    try {
                        FileInputStream fis = new FileInputStream(file);
                        fis.read(bytes);
                        fis.close();
                    }
                    catch (Exception e){

                    }
                    Task<Integer> sendMessageTask =
                            Wearable.getMessageClient(MainActivity.this).sendMessage(node.getId(), path, bytes);
                    try {
                        Integer result = Tasks.await(sendMessageTask);
                    }
                    catch (ExecutionException exception) {
                        Log.e("Error",exception.toString());
                    }
                    catch (InterruptedException exception) {
                        Log.e("InterruptedError",exception.toString());
                    }
                    file.delete();
                }
            }
            catch (ExecutionException exception) {
                Log.e("Error",exception.toString());
            }
            catch (InterruptedException exception) {
                Log.e("InterruptedError",exception.toString());
            }
        }
    }
}