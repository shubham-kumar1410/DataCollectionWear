package com.shubhamk.datacollectionwear;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ammarptn.gdriverest.DriveServiceHelper;
import com.ammarptn.gdriverest.GoogleDriveFileHolder;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

import static com.ammarptn.gdriverest.DriveServiceHelper.getGoogleDriveService;

public class MainActivity extends AppCompatActivity {
    private DriveServiceHelper mDriveServiceHelper;
    ListView listView;
    ArrayList<String> file_name = new ArrayList<>();
    private static final String TAG = "TAG";
    String folderId = null;
    File[] files;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    private static final int REQUEST_ENABLE_BT = 1;
    protected Handler myHandler;
    String fileName;
    File file = null;
    GoogleSignInClient mGoogleSignInClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sharedPreferences = getSharedPreferences("MyPref", 0);
        editor = sharedPreferences.edit();
        listView = findViewById(R.id.listView);
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Snackbar snackbar = Snackbar.make(listView, "Device doesn't support Bluetooth", Snackbar.LENGTH_LONG);
            snackbar.show();
        }
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        String path = Environment.getExternalStorageDirectory().toString()+"/Activity Recognition";
        Log.d("Files", "Path: " + path);
        File directory = new File(path);
        files = directory.listFiles();
        Log.d("Files", "Size: "+ files.length);
        for (int i = 0; i < files.length; i++)
        {
            file_name.add(files[i].getName());
            Log.d("Files", "FileName:" + files[i].getName());
        }

        listView.setAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,
                file_name) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView view = (TextView) super.getView(position, convertView, parent);
                view.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icons8_document_24, 0, 0, 0);
                view.setCompoundDrawablePadding(10);
                return view;
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final int q = position;
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(MainActivity.this);
                alertBuilder.setTitle("Continue")
                        .setMessage("Are you sure you want to continue uploading this file to Google Drive ?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                final ProgressDialog progress = new ProgressDialog(getApplicationContext());
                                progress.setMessage("Uploading file to Google Drive ");
                                progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                                progress.setIndeterminate(true);
                                if(mDriveServiceHelper == null){
                                    Log.d(TAG,"mDriveServiceHelper is null");
                                }
                                folderId = sharedPreferences.getString("folderId",null);
                                if (folderId == null){
                                    mDriveServiceHelper.createFolder("Data Collection", null)
                                            .addOnSuccessListener(new OnSuccessListener<GoogleDriveFileHolder>() {
                                                @Override
                                                public void onSuccess(GoogleDriveFileHolder googleDriveFileHolder) {
                                                    folderId = googleDriveFileHolder.getId();
                                                    Gson gson = new Gson();
                                                    Log.d(TAG, "onSuccess: " + gson.toJson(googleDriveFileHolder));
                                                    editor.putString("folderId",folderId);
                                                    editor.commit();
                                                    mDriveServiceHelper.uploadFile(files[q], "text/plain", folderId)
                                                            .addOnSuccessListener(new OnSuccessListener<GoogleDriveFileHolder>() {
                                                                @Override
                                                                public void onSuccess(GoogleDriveFileHolder googleDriveFileHolder) {
                                                                    Gson gson = new Gson();
                                                                    Log.d(TAG, "onSuccess: " + gson.toJson(googleDriveFileHolder));
                                                                    //Toast.makeText(getApplicationContext(),"File uploaded",Toast.LENGTH_SHORT).show();
                                                                    Snackbar snackbar = Snackbar
                                                                            .make(listView, "File uploaded to Google Drive successfully", Snackbar.LENGTH_LONG);
                                                                    snackbar.show();
                                                                    progress.dismiss();
                                                                }
                                                            })
                                                            .addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {
                                                                    Log.d(TAG, "onFailure: " + e.getMessage());
                                                                }
                                                            });
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.d(TAG, "onFailure: " + e.getMessage());

                                                }
                                            });

                                }
                                else {
                                    mDriveServiceHelper.uploadFile(files[q], "text/plain", folderId)
                                            .addOnSuccessListener(new OnSuccessListener<GoogleDriveFileHolder>() {
                                                @Override
                                                public void onSuccess(GoogleDriveFileHolder googleDriveFileHolder) {
                                                    Gson gson = new Gson();
                                                    Log.d(TAG, "onSuccess: " + gson.toJson(googleDriveFileHolder));
                                                    //Toast.makeText(getApplicationContext(),"File uploaded",Toast.LENGTH_SHORT).show();
                                                    Snackbar snackbar = Snackbar
                                                            .make(listView, "File uploaded to Google Drive successfully", Snackbar.LENGTH_LONG);
                                                    snackbar.show();
                                                    progress.dismiss();
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.d(TAG, "onFailure: " + e.getMessage());
                                                }
                                            });
                                }


                            }
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .show();
            }
        });
        myHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                Bundle stuff = msg.getData();
                messageText(stuff.getString("messageText"));
                return true;
            }
        });
        IntentFilter messageFilter = new IntentFilter(Intent.ACTION_SEND);
        Receiver messageReceiver = new Receiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver, messageFilter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(Drive.SCOPE_FILE)
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getApplicationContext());
        if(account == null){
            Intent intent = new Intent(this,LoginActivity.class);
            startActivity(intent);
        }
        mDriveServiceHelper = new DriveServiceHelper(getGoogleDriveService(getApplicationContext(), account, "Data Collection Wear"));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.logout){

            mGoogleSignInClient.signOut()
                    .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
                            startActivity(intent);
                        }
                    });
        }
        return true;
    }

    public void messageText(String newinfo) {
        if (newinfo.compareTo("") != 0) {
            //textView.append("\n" + newinfo);
        }
    }

    public class Receiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("message");
            Toast.makeText(getApplicationContext(),"Received message from watch at "+
                    String.valueOf(Calendar.getInstance().getTime().getTime()) ,Toast.LENGTH_SHORT).show();
            fileName = String.valueOf(Calendar.getInstance().getTime().getTime()) + ".csv";
            try {
                final File directory =
                        new File(Environment.getExternalStorageDirectory()
                                + "/Activity Recognition");
                if (!directory.exists()) {
                    if (!directory.mkdirs()) {
                        throw new FileNotFoundException("Could not create requested directory");
                    }
                }
                file = new File(directory, fileName);
                FileWriter fileWriter = new FileWriter(file.getAbsoluteFile());
                try {
                    fileWriter.append(message);
                } catch (Exception e) {
                    Log.v("Error", e.toString());
                } finally {
                    try {
                        fileWriter.flush();
                        fileWriter.close();
                        file_name.add(fileName);
                        listView.getAdapter().notify();
                    } catch (IOException e) {
                        System.out.println("Error while flushing/closing fileWriter !!!");
                        Log.v("Error", e.toString());
                    }
                }

            } catch (Exception e) {

            }

        }
    }

}
