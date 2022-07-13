package com.onrpiv.uploadmedia.Experiment;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.onrpiv.uploadmedia.Experiment.Popups.LoadExperimentPopup;
import com.onrpiv.uploadmedia.R;

/**
 * author: sarbajit mukherjee
 * Created by sarbajit mukherjee on 09/07/2020.
 */
public class MainActivity extends HomeActivity implements View.OnClickListener {
    private Button image;
    private Button video;

    public static String userName = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        image = (Button) findViewById(R.id.image);
        video = (Button) findViewById(R.id.video);
        Button feedbackBtn = (Button) findViewById(R.id.feedback);
        Button loadExpBtn = (Button) findViewById(R.id.main_load_exp_btn);
        Button cameraCalibBtn = (Button) findViewById(R.id.main_create_calibration);

        if (null == userName || userName.isEmpty()) {
            userNameDialog();
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                image.setEnabled(false);
                video.setEnabled(false);
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                        },
                        0);
            } else {
                image.setEnabled(true);
                video.setEnabled(true);
            }
            image.setOnClickListener(this);
            video.setOnClickListener(this);
            feedbackBtn.setOnClickListener(this);
            loadExpBtn.setOnClickListener(this);
            cameraCalibBtn.setOnClickListener(this);
        }

        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(navListener);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 0) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                image.setEnabled(true);
                video.setEnabled(true);
            }
        }
    }

    private void userNameDialog(){
        // Create a AlertDialog Builder.
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
        // Set title, icon, can not cancel properties (the box still remains on the screen if clicked outside).
        alertDialogBuilder.setTitle("Who is using the App");
        alertDialogBuilder.setIcon(R.drawable.ic_launcher_background);
        alertDialogBuilder.setCancelable(false);

        // username popup view
        LayoutInflater layoutInflater = LayoutInflater.from(MainActivity.this);
        View popupInputDialogView = layoutInflater.inflate(R.layout.popup_name_dialog, null);

        // username input
        EditText userNameEditText = (EditText)popupInputDialogView.findViewById(R.id.userName);

        // Set the inflated layout view object to the AlertDialog builder.
        alertDialogBuilder.setView(popupInputDialogView);

        // set the save button listener
        alertDialogBuilder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Get user data from popup dialog editeext.
                MainActivity.userName = userNameEditText.getText().toString();
                dialog.dismiss();
                Toast.makeText(MainActivity.this, userName +" is using the APP", Toast.LENGTH_SHORT).show();
            }
        });

        // restart Main Activity with new username
        alertDialogBuilder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                recreate();
            }
        });

        // Create AlertDialog and show.
        alertDialogBuilder.create().show();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.image:
                if (userName != null && !userName.isEmpty()){
                    Intent intent1 = new Intent(this, ImageActivity.class);
                    intent1.putExtra("UserName", userName);
                    startActivity(intent1);
                } else {
                    Toast.makeText(this, "Please input User Name", Toast.LENGTH_SHORT).show();
                    userNameDialog();
                }
                break;
            case R.id.video:
                if (userName != null && !userName.isEmpty()){
                    Intent intent2 = new Intent(this, VideoActivity.class);
                    intent2.putExtra("UserName", userName);
                    startActivity(intent2);
                } else {
                    Toast.makeText(this, "Please input User Name", Toast.LENGTH_SHORT).show();
                    userNameDialog();
                }
                break;
            case R.id.main_load_exp_btn:
                if (userName != null && !userName.isEmpty()) {
                    LoadExperimentPopup loadExpPopup = new LoadExperimentPopup(this, userName);
                    loadExpPopup.show();
                } else {
                    Toast.makeText(this, "Please input User Name", Toast.LENGTH_SHORT).show();
                    userNameDialog();
                }
                break;
            case R.id.main_create_calibration:
                if (userName != null && !userName.isEmpty()) {
                    Intent calibrationIntent = new Intent(this, CalibrationActivity.class);
                    calibrationIntent.putExtra("UserName", userName);
                    startActivity(calibrationIntent);
                } else {
                    Toast.makeText(this, "Please input User Name", Toast.LENGTH_SHORT).show();
                    userNameDialog();
                }
                break;
            case R.id.feedback:
                Intent intent = new Intent("android.intent.action.VIEW", Uri.parse("https://usu.co1.qualtrics.com/jfe/form/SV_3WtfQHquWuN0ujj"));
                startActivity(intent);
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("username", userName);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        userName = savedInstanceState.getString("username");
    }

    // ensuring the screen is locked to vertical position
    @Override
    protected void onResume() {
        super.onResume();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Override
    protected void onPause() {
        super.onPause();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }
}