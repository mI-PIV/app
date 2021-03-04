package com.onrpiv.uploadmedia.Experiment;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.onrpiv.uploadmedia.R;

/**
 * author: sarbajit mukherjee
 * Created by sarbajit mukherjee on 09/07/2020.
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Button image, video, changeUser;
    // Below edittext and button are all exist in the popup dialog view.
    private View popupInputDialogView = null;
    // Get Image1.
    private EditText userNameEditText = null;
    // Click this button in popup dialog to save user input data
    private Button saveUserDataButton = null;
    // Click this button to cancel edit user data.
    private Button cancelUserDataButton = null;
    public String userName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        image = (Button) findViewById(R.id.image);
        video = (Button) findViewById(R.id.video);
        changeUser = (Button) findViewById(R.id.changeUser);

        alertDialogFunction();

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
        changeUser.setOnClickListener(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 0) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                image.setEnabled(true);
                video.setEnabled(true);
            }
        }
    }

    private void alertDialogFunction(){
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
                    alertDialogFunction();
                }
                break;
            case R.id.video:
                if (userName != null && !userName.isEmpty()){
                    Intent intent2 = new Intent(this, VideoActivity.class);
                    intent2.putExtra("UserName", userName);
                    startActivity(intent2);
                } else {
                    Toast.makeText(this, "Please input User Name", Toast.LENGTH_SHORT).show();
                    alertDialogFunction();
                }
                break;
            case R.id.changeUser:
                alertDialogFunction();
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
        saveUserDataButton = popupInputDialogView.findViewById(R.id.button_save_user_data);
        cancelUserDataButton = popupInputDialogView.findViewById(R.id.button_cancel_user_data);
    }
}
