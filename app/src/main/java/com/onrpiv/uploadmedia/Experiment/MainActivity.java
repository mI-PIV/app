package com.onrpiv.uploadmedia.Experiment;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
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

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.onrpiv.uploadmedia.Experiment.Popups.LoadExperimentPopup;
import com.onrpiv.uploadmedia.R;
import com.onrpiv.uploadmedia.Utilities.PathUtil;
import com.onrpiv.uploadmedia.Utilities.PersistedData;

/**
 * author: sarbajit mukherjee
 * Created by sarbajit mukherjee on 09/07/2020.
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Button image;
    private Button video;
    // Below edittext and button are all exist in the popup dialog view.
    private View popupInputDialogView = null;
    // Get Image1.
    private EditText userNameEditText = null;
    // Click this button in popup dialog to save user input data
    private Button saveUserDataButton = null;
    // Click this button to cancel edit user data.
    private Button cancelUserDataButton = null;
    public static String userName = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        image = (Button) findViewById(R.id.image);
        video = (Button) findViewById(R.id.video);
        Button userSettings = (Button) findViewById(R.id.userSettings);
        Button loadExpBtn = (Button) findViewById(R.id.main_load_exp_btn);

        if (null == userName || userName.isEmpty())
            userNameDialog();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            image.setEnabled(false);
            video.setEnabled(false);
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE }, 0);
        } else {
            image.setEnabled(true);
            video.setEnabled(true);
        }
        image.setOnClickListener(this);
        video.setOnClickListener(this);
        userSettings.setOnClickListener(this);
        loadExpBtn.setOnClickListener(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
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
        // Init popup dialog view and it's ui controls.
        initPopupViewControls();

        // Set the inflated layout view object to the AlertDialog builder.
        alertDialogBuilder.setView(popupInputDialogView);

        // Create AlertDialog and show.
        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();

        // When user click the save user data button in the popup dialog.
        saveUserDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Get user data from popup dialog editeext.
                userName = userNameEditText.getText().toString();
                alertDialog.cancel();
                Toast.makeText(MainActivity.this, userName +" is using the APP", Toast.LENGTH_SHORT).show();
            }
        });

        cancelUserDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.cancel();
            }
        });
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
            case R.id.userSettings:
                userSettingsPopup(MainActivity.this, getWindow());
                break;
        }
    }

    /* Initialize popup dialog view and ui controls in the popup dialog. */
    private void initPopupViewControls()
    {
        // Get layout inflater object.
        LayoutInflater layoutInflater = LayoutInflater.from(MainActivity.this);

        // Inflate the popup dialog from a layout xml file.
        popupInputDialogView = layoutInflater.inflate(R.layout.popup_name_dialog, null);

        // Get user input edittext and button ui controls in the popup dialog.
        userNameEditText = (EditText) popupInputDialogView.findViewById(R.id.userName);
        saveUserDataButton = popupInputDialogView.findViewById(R.id.button_save_user);
        cancelUserDataButton = popupInputDialogView.findViewById(R.id.button_cancel_user);
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
                        .setMessage("All extracted frames and experiments will be deleted." +
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
}
