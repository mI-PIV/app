package com.onrpiv.uploadmedia.Experiment;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.onrpiv.uploadmedia.R;
import com.onrpiv.uploadmedia.Utilities.PathUtil;
import com.onrpiv.uploadmedia.Utilities.PersistedData;

public class Account extends MainActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // When the MainActivity class is opened, it prompts user to login. Account must extend
        // MainActivity to keep login information. We might want to consider not prompting the user
        // to login when opening this page.

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Button changeUserButton = (Button) findViewById(R.id.changeUserBtn);
        changeUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userNameDialog();
            }
        });

        Button deleteUserDataButton = (Button) findViewById(R.id.deleteDataBtn);
        deleteUserDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // warning dialog
                new androidx.appcompat.app.AlertDialog.Builder(Account.this)
                        .setTitle("Delete User Settings")
                        .setMessage("All extracted frames, calibrations, and saved experiments will be deleted." +
                                " Are you sure you wish to continue?")

                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                final ProgressDialog pDialog = new ProgressDialog(Account.this);
                                pDialog.setCancelable(false);
                                if (!pDialog.isShowing()) pDialog.show();

                                Thread thread = new Thread() {
                                    @Override
                                    public void run(){
                                        // delete all frames
                                        pDialog.setMessage("Deleting Frames...");
                                        PathUtil.deleteRecursive(PathUtil.getFramesDirectory(Account.this, userName));

                                        // delete all experiments
                                        pDialog.setMessage("Deleting Experiments...");
                                        PathUtil.deleteRecursive(PathUtil.getExperimentsDirectory(Account.this, userName));

                                        // delete all persisted data
                                        pDialog.setMessage("Deleting persisted data...");
                                        PersistedData.clearUserPersistedData(Account.this, userName);

                                        // delete any camera calibrations
                                        pDialog.setMessage("Deleting camera calibration...");
                                        PathUtil.deleteRecursive(PathUtil.getUserDirectory(Account.this, userName));

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
    }

    private void userNameDialog(){
        // Create a AlertDialog Builder.
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(Account.this);
        // Set title, icon, can not cancel properties (the box still remains on the screen if clicked outside).
        alertDialogBuilder.setTitle("Who is using the App");
        alertDialogBuilder.setIcon(R.drawable.ic_launcher_background);
        alertDialogBuilder.setCancelable(false);

        // username popup view
        LayoutInflater layoutInflater = LayoutInflater.from(Account.this);
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
                Toast.makeText(Account.this, userName +" is using the APP", Toast.LENGTH_SHORT).show();
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