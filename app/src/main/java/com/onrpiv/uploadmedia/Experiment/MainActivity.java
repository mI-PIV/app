package com.onrpiv.uploadmedia.Experiment;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.onrpiv.uploadmedia.Experiment.Popups.LoadExperimentPopup;
import com.onrpiv.uploadmedia.R;
import com.onrpiv.uploadmedia.Utilities.Camera.Calibration.CalibrationPopup;
import com.onrpiv.uploadmedia.Utilities.PathUtil;
import com.onrpiv.uploadmedia.Utilities.PersistedData;

/**
 * author: sarbajit mukherjee
 * Created by sarbajit mukherjee on 09/07/2020.
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Button image;
    private Button video;
    private ActivityResultLauncher<Uri> takePhotoLauncher;

    public static String userName = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        image = (Button) findViewById(R.id.image);
        video = (Button) findViewById(R.id.video);
        Button userSettings = (Button) findViewById(R.id.userSettings);
        Button loadExpBtn = (Button) findViewById(R.id.main_load_exp_btn);
        Button cameraCalibBtn = (Button) findViewById(R.id.main_create_calibration);

        if (null == userName || userName.isEmpty()) {
            userNameDialog();
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                image.setEnabled(false);
                video.setEnabled(false);
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
            } else {
                image.setEnabled(true);
                video.setEnabled(true);
            }
            image.setOnClickListener(this);
            video.setOnClickListener(this);
            userSettings.setOnClickListener(this);
            loadExpBtn.setOnClickListener(this);
            cameraCalibBtn.setOnClickListener(this);

            // camera calibration popup
            ActivityResultCallback<Boolean> takePhotoResultCallback = CalibrationPopup.getResultCallback(MainActivity.this, userName);
            takePhotoLauncher = registerForActivityResult(new ActivityResultContracts.TakePicture(), takePhotoResultCallback);
        }
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
                    CalibrationPopup.show(MainActivity.this, takePhotoLauncher);
                } else {
                    Toast.makeText(this, "Please input User Name", Toast.LENGTH_SHORT).show();
                    userNameDialog();
                }
                break;
            case R.id.userSettings:
                userSettingsPopup(MainActivity.this, getWindow());
                break;
        }
    }

    private void userSettingsPopup(final Context context, final Window activityWindow)
    {
        // inflate view
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View userSettingsView = inflater.inflate(R.layout.user_settings, null);

        // buttons
        ImageButton exitBtn = userSettingsView.findViewById(R.id.userSettingsCloseBtn);
        Button changeUserBtn = userSettingsView.findViewById(R.id.changeUserBtn);
        Button deleteDataBtn = userSettingsView.findViewById(R.id.deleteDataBtn);

        // listeners
        changeUserBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userNameDialog();
            }
        });

        deleteDataBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // warning dialog
                new androidx.appcompat.app.AlertDialog.Builder(context)
                        .setTitle("Delete User Settings")
                        .setMessage("All extracted frames, calibrations, and saved experiments will be deleted." +
                                " Are you sure you wish to continue?")

                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                final ProgressDialog pDialog = new ProgressDialog(context);
                                pDialog.setCancelable(false);
                                if (!pDialog.isShowing()) pDialog.show();

                                Thread thread = new Thread() {
                                    @Override
                                    public void run(){
                                        // delete all frames
                                        pDialog.setMessage("Deleting Frames...");
                                        PathUtil.deleteRecursive(PathUtil.getFramesDirectory(context, userName));

                                        // delete all experiments
                                        pDialog.setMessage("Deleting Experiments...");
                                        PathUtil.deleteRecursive(PathUtil.getExperimentsDirectory(context, userName));

                                        // delete all persisted data
                                        pDialog.setMessage("Deleting persisted data...");
                                        PersistedData.clearUserPersistedData(context, userName);

                                        // delete any camera calibrations
                                        pDialog.setMessage("Deleting camera calibration...");
                                        PathUtil.deleteRecursive(PathUtil.getUserDirectory(context, userName));

                                        if (pDialog.isShowing()) pDialog.dismiss();
                                    }
                                };
                                thread.start();
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();
            }
        });

        // popup
        activityWindow.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

        final PopupWindow userSettingsPopup = new PopupWindow(userSettingsView,
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        if (Build.VERSION.SDK_INT >= 21) {
            userSettingsPopup.setElevation(5f);
        }
        exitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userSettingsPopup.dismiss();
                activityWindow.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            }
        });

        userSettingsPopup.showAtLocation(activityWindow.getDecorView(), Gravity.CENTER, 0, 0);
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
