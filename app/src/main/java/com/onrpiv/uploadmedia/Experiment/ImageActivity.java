package com.onrpiv.uploadmedia.Experiment;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.appcompat.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.onrpiv.uploadmedia.BuildConfig;
import com.onrpiv.uploadmedia.R;
import com.onrpiv.uploadmedia.Utilities.ArrowDrawOptions;
import com.onrpiv.uploadmedia.Utilities.BoolIntStructure;
import com.onrpiv.uploadmedia.pivFunctions.PivFunctions;

import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

/**
 * author: sarbajit mukherjee
 * Created by sarbajit mukherjee on 09/07/2020.
 */

public class ImageActivity extends AppCompatActivity implements View.OnClickListener {
    ImageView imageView;
    Button parameters, compute, display, save, pickImageMultiple, review;
    private static final int MY_PERMISSIONS_REQUEST_READ_PHONE_STATE = 0;
    private static final int REQUEST_TAKE_PHOTO = 0;
    private static final int REQUEST_PICK_PHOTO = 2;
    private static final int PICK_IMAGE_MULTIPLE = 1;
    private Uri mMediaUri;
    private static final int CAMERA_PIC_REQUEST = 1111;

    private static final String TAG = ImageActivity.class.getSimpleName();

    private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;

    public static final int MEDIA_TYPE_IMAGE = 1;

    private Uri fileUri;
    private String imagePath;
    private ArrayList<String> imagePathList = new ArrayList<>();
    private ArrayList<String> postPathMultiple = new ArrayList<>();
    private String mediaPath;

    private String serverMessage;

    private Button btnCapturePicture;

    private String mImageFileLocation = "";
    public static final String IMAGE_DIRECTORY_NAME = "Android File Upload";
    ProgressDialog pDialog;
    private String postPath;
    private String IMEINumber;
    String SCAN_PATH;
    File[] allFiles ;
    // Below edittext and button are all exist in the popup dialog view.
    private View popupInputDialogView = null;
    // Get Sets.
    private TextView setEditText = null;
    // Get Set Number.
    private EditText setNumberEditText = null;
    // Get Image1.
    private EditText img1EditText = null;
    // Get Image2.
    private EditText img2EditText = null;
    // Click this button in popup dialog to save user input data
    private Button saveUserDataButton = null;
    // Click this button to cancel edit user data.
    private Button cancelUserDataButton = null;
    private String userName;
    private File storageDirectory;

    private Map<String, double[][]> pivCorrelation = null;
    private Map<String, double[]> interrCenters = null;
    private PivFunctions piv = null;
    private double[][] vorticityValues = null;
    private Map<String, double[][]> pivCorrelationProcessed = null;
    private Map<String, double[][]> pivReplaceMissing = null;
    private Map<String, double[][]> pivCorrelationMulti = null;
    private Map<String, double[][]> pivReplaceMissing2 = null;
    private int rows;
    private int cols;
    private Mat grayFrame1;
    private Mat grayFrame2;

    private TableRow tableDt = null;
    private View popupPIVDialogView = null;
    private TextView setEditTextPIV = null;
    private TextView windowSizeText = null;
    private TextView overlapText = null;
    private TextView dtText = null;
    private TextView dt_text = null;
    private TextView nMaxUpperText = null;
    private TextView nMaxUpper_text = null;
    private TextView nMaxLowerText = null;
    private TextView nMaxLower_text = null;
    private TextView qMinText = null;
    private TextView qMin_text = null;
    private TextView EText = null;
    private TextView E_text = null;
    private TextView groupradio_text = null;
    private Button savePIVDataButton = null;
    private RadioGroup radioGroup;
    // Click this button to cancel edit user data.
    private Button cancelPIVDataButton = null;
    private int windowSize = 64;
    private int overlap = 32;
    private int selectedId;
    private double nMaxUpper, nMaxLower, maxDisplacement = 0.0;
    private Double qMin, dt, E = 0.0;
    private ScaleGestureDetector mScaleGestureDetector;
    private float mScaleFactor = 1.0f;
    volatile boolean running = true;
//    ZoomageView imageZoom;
    private CheckBox checkBox2;
    private boolean checked=false;
    private RadioButton radioButton;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_layout);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        context = this;

//        imageView = (ImageView) findViewById(R.id.preview);
//        imageZoom = (ZoomageView)findViewById(R.id.myZoomageView);
        pickImageMultiple = (Button) findViewById(R.id.pickImageMultiple);
        parameters = (Button) findViewById(R.id.parameters);
        compute = (Button) findViewById(R.id.compute);
        display = (Button) findViewById(R.id.display);
        review = (Button) findViewById(R.id.Review);
        mScaleGestureDetector = new ScaleGestureDetector(this, new ScaleListener());
        // Get the transferred data from source activity.
        Intent userNameIntent = getIntent();
        userName = userNameIntent.getStringExtra("UserName");

        storageDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + "/PIV_Frames_" + userName);
        // Then we create the storage directory if does not exists
        if (!storageDirectory.exists()){
            Toast.makeText(this, "You have not generated any frames yet", Toast.LENGTH_SHORT).show();
        } else {
            OpenCVLoader.initDebug();
            pickImageMultiple.setOnClickListener(this);
            parameters.setOnClickListener(this);
            compute.setOnClickListener(this);
            loadIMEI();
            initDialog();
        }
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector scaleGestureDetector){
            mScaleFactor *= scaleGestureDetector.getScaleFactor();
            mScaleFactor = Math.max(0.1f,Math.min(mScaleFactor, 10.0f));
            imageView.setScaleX(mScaleFactor);
            imageView.setScaleY(mScaleFactor);
            return true;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        mScaleGestureDetector.onTouchEvent(motionEvent);
        return true;
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



    /**
     * Called when the 'loadIMEI' function is triggered.
     */
    public void loadIMEI() {
        // Check if the READ_PHONE_STATE permission is already available.
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            // READ_PHONE_STATE permission has not been granted.
            requestReadPhoneStatePermission();
        } else {
            // READ_PHONE_STATE permission is already been granted.
            doPermissionGrantedStuffs();
        }
    }


    @Override
    public void onClick(final View v) {
        display.setEnabled(false);
        switch (v.getId()) {
            case R.id.pickImageMultiple:
                review.setEnabled(false);
                parameters.setEnabled(false);
                compute.setEnabled(false);
                display.setEnabled(false);
                // Create a AlertDialog Builder.
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ImageActivity.this);
                // Set title, icon, can not cancel properties.
                alertDialogBuilder.setTitle("User Data Collection Dialog.");
//                alertDialogBuilder.setIcon(R.drawable.ic_launcher_background);
                alertDialogBuilder.setCancelable(false);
                // Init popup dialog view and it's ui controls.
                initPopupViewControls();

                // Set the inflated layout view object to the AlertDialog builder.
                alertDialogBuilder.setView(popupInputDialogView);

                // Create AlertDialog and show.
                final AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();

                File folder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + "/PIV_Frames_" + userName);
                allFiles = folder.listFiles();
                Arrays.sort(allFiles);
                int lenAllFiles = allFiles.length;
                int set = lenAllFiles/20;
                setEditText.setText("User '"+userName + "' has "+set+" image sets. The highest number set corresponds to the lastest generated frames");
                setEditText.setTextSize(15);
                if (set == 1) {
                    setNumberEditText.setHint("Number 1");
                } else {
                    setNumberEditText.setHint("Number 1 - "+set);
                }

                // When user click the save user data button in the popup dialog.
                saveUserDataButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        imagePathList.clear();
                        postPathMultiple.clear();
                        // Get user data from popup dialog editeext.
                        String setNumber = setNumberEditText.getText().toString();
                        String img1 = img1EditText.getText().toString();
                        String img2 = img2EditText.getText().toString();

                        if (!setNumber.isEmpty() && !img1.isEmpty() && !img2.isEmpty()){
                            // Create data for the listview.
                            String[] titleArr = { "SetNumber", "Image1", "Image2"};
                            String[] dataArr = {setNumber, img1, img2};

                            ArrayList<Map<String,Object>> itemDataList = new ArrayList<Map<String,Object>>();;

                            int titleLen = titleArr.length;
                            for(int i =0; i < titleLen; i++) {
                                Map<String,Object> listItemMap = new HashMap<String,Object>();
                                listItemMap.put("title", titleArr[i]);
                                listItemMap.put("data", dataArr[i]);
                                itemDataList.add(listItemMap);
                            }
                            int image1 = Integer.parseInt(String.valueOf(itemDataList.get(1).get("data")));
                            int image2 = Integer.parseInt(String.valueOf(itemDataList.get(2).get("data")));


                            int allFileImage1 = image1 + (Integer.parseInt(setNumber) - 1)*20 - 1;
                            int allFileImage2 = image2 + (Integer.parseInt(setNumber) - 1)*20 - 1;

                            imagePathList.add(String.valueOf(allFiles[allFileImage1]));
                            imagePathList.add(String.valueOf(allFiles[allFileImage2]));
                            postPathMultiple = imagePathList;
                            postPath = "multiple image";
                            pickImageMultiple.setBackgroundColor(Color.parseColor("#00CC00"));
                            alertDialog.cancel();
                            Toast.makeText(ImageActivity.this, postPathMultiple.toString(), Toast.LENGTH_SHORT).show();
                            review.setEnabled(true);
                            parameters.setEnabled(true);
                        } else {
                            Toast.makeText(ImageActivity.this, "Please Provide Values In All The Fields", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                cancelUserDataButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        alertDialog.cancel();
                    }
                });
                break;
            case R.id.parameters:
                // Create a AlertDialog Builder.
                AlertDialog.Builder alertDialogParametersBuilder = new AlertDialog.Builder(ImageActivity.this);
                // Set title, icon, can not cancel properties.
                alertDialogParametersBuilder.setTitle("PIV Parameters Dialog.");
                alertDialogParametersBuilder.setIcon(R.drawable.ic_launcher_background);
                alertDialogParametersBuilder.setCancelable(false);
                // Init popup dialog view and it's ui controls.
                initPivParametersControls();

                // Set the inflated layout view object to the AlertDialog builder.
                alertDialogParametersBuilder.setView(popupPIVDialogView);

                // Create AlertDialog and show.
                final AlertDialog alertDialogParameters = alertDialogParametersBuilder.create();
                alertDialogParameters.show();

                setEditTextPIV.setText("Please Input the parameters to be used in your PIV experiment");
                setEditTextPIV.setTextSize(20);
                // Uncheck or reset the radio buttons initially
//                radioGroup.clearCheck();
                // When user click the save user data button in the popup dialog.
                savePIVDataButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (!checked){
                            dtText.setText("1");
                            nMaxUpperText.setText("25");
                            nMaxLowerText.setText("5");
                            qMinText.setText("1");
                            EText.setText("2");
                            radioGroup.check(R.id.yesRadio);
                        }
                        // Get user data from popup dialog editeext.
                        String window_size = windowSizeText.getText().toString();
                        String Overlap = overlapText.getText().toString();
                        String Dt = dtText.getText().toString();
                        String NMaxUpper = nMaxUpperText.getText().toString();
                        String NMaxLower = nMaxLowerText.getText().toString();
                        String QMin = qMinText.getText().toString();
                        String EMedian = EText.getText().toString();

                        selectedId = radioGroup.getCheckedRadioButtonId();
                        if (selectedId==R.id.yesRadio){
                            selectedId=0;
                        } else if (selectedId==R.id.noRadio){
                            selectedId=1;
                        }
                        if (!window_size.isEmpty() && !Overlap.isEmpty() && !Dt.isEmpty() && !NMaxUpper.isEmpty() && !NMaxLower.isEmpty() && !QMin.isEmpty() && !EMedian.isEmpty() && selectedId != -1){
                            windowSize =  Integer.parseInt(window_size);
                            overlap =  Integer.parseInt(Overlap);
                            dt = Double.parseDouble(Dt);
                            nMaxUpper = Integer.parseInt(NMaxUpper);
                            nMaxLower = Integer.parseInt(NMaxLower);
                            qMin = Double.parseDouble(QMin);
                            E = Double.parseDouble(EMedian);
                            parameters.setBackgroundColor(Color.parseColor("#00CC00"));
                            alertDialogParameters.cancel();
                            Toast.makeText(ImageActivity.this, "Parameters Selected", Toast.LENGTH_SHORT).show();
                            compute.setEnabled(true);
                            running = true;
                        } else {
                            Toast.makeText(ImageActivity.this, "Please Provide Values In All The Fields", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                cancelPIVDataButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        alertDialogParameters.cancel();
                    }
                });
                break;
            case R.id.compute:
                processFile();
                break;
        }
    }

    /* Initialize popup dialog view and ui controls in the popup dialog. */
    private void initPopupViewControls()
    {
        // Get layout inflater object.
        LayoutInflater layoutInflater = LayoutInflater.from(ImageActivity.this);

        // Inflate the popup dialog from a layout xml file.
        popupInputDialogView = layoutInflater.inflate(R.layout.popup_input_dialog, null);

        // Get user input edittext and button ui controls in the popup dialog.
        setEditText = (TextView) popupInputDialogView.findViewById(R.id.textView);
        setNumberEditText = (EditText) popupInputDialogView.findViewById(R.id.imgSet);
        img1EditText = (EditText) popupInputDialogView.findViewById(R.id.img1);
        img2EditText = (EditText) popupInputDialogView.findViewById(R.id.img2);
        saveUserDataButton = popupInputDialogView.findViewById(R.id.button_save_user_data);
        cancelUserDataButton = popupInputDialogView.findViewById(R.id.button_cancel_user_data);
    }


    //handling clicks from inside dialog box
    public void onCheckboxClicked(View view){
        // Is the view now checked?
        checked = ((CheckBox) view).isChecked();
        // Check which checkbox was clicked
        switch(view.getId()) {
            case R.id.checkbox_2:
                if (checked){
                    dtText.setVisibility(View.VISIBLE);
                    dt_text.setVisibility(View.VISIBLE);
                    dtText.setText("1");

                    nMaxUpperText.setVisibility(View.VISIBLE);
                    nMaxUpper_text.setVisibility(View.VISIBLE);
                    nMaxUpperText.setText("25");

                    nMaxLowerText.setVisibility(View.VISIBLE);
                    nMaxLower_text.setVisibility(View.VISIBLE);
                    nMaxLowerText.setText("5");

                    qMinText.setVisibility(View.VISIBLE);
                    qMin_text.setVisibility(View.VISIBLE);
                    qMinText.setText("1");

                    EText.setVisibility(View.VISIBLE);
                    E_text.setVisibility(View.VISIBLE);
                    EText.setText("2");

                    radioGroup.setVisibility(View.VISIBLE);
                    groupradio_text.setVisibility(View.VISIBLE);
                    radioGroup.check(R.id.yesRadio);
                }else{
                    dtText.setVisibility(View.GONE);
                    dt_text.setVisibility(View.GONE);

                    nMaxUpperText.setVisibility(View.GONE);
                    nMaxUpper_text.setVisibility(View.GONE);

                    nMaxLowerText.setVisibility(View.GONE);
                    nMaxLower_text.setVisibility(View.GONE);

                    qMinText.setVisibility(View.GONE);
                    qMin_text.setVisibility(View.GONE);

                    EText.setVisibility(View.GONE);
                    E_text.setVisibility(View.GONE);

                    radioGroup.setVisibility(View.GONE);
                    groupradio_text.setVisibility(View.GONE);
                }break;
        }
    }

    /* Initialize popup dialog view and ui controls in the popup dialog. */
    private void initPivParametersControls()
    {
        // Get layout inflater object.
        LayoutInflater layoutInflater = LayoutInflater.from(ImageActivity.this);

        // Inflate the popup dialog from a layout xml file.
        popupPIVDialogView = layoutInflater.inflate(R.layout.popup_piv_dialog, null);
        // Get user input edittext and button ui controls in the popup dialog.
        setEditTextPIV = (TextView) popupPIVDialogView.findViewById(R.id.textView);
        windowSizeText = (EditText) popupPIVDialogView.findViewById(R.id.windowSize);
        windowSizeText.setText("64");
        overlapText = (EditText) popupPIVDialogView.findViewById(R.id.overlap);
        overlapText.setText("32");

        // Set the default overlap to 50% of windowSize if windowSize is changed from default
        windowSizeText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                BoolIntStructure userInputCheckResult = checkUserInputInt(s.toString());
                if (s.length() != 0 && userInputCheckResult.getBool()) {
                    int half = (int) Math.round((double) userInputCheckResult.getInt() / 2);
                    overlapText.setText(String.valueOf(half));
                }
            }
        });


        checkBox2=(CheckBox)popupPIVDialogView.findViewById(R.id.checkbox_2);

        dtText = (EditText) popupPIVDialogView.findViewById(R.id.dt);
        dt_text = (TextView) popupPIVDialogView.findViewById(R.id.dt_text);

        nMaxUpperText = popupPIVDialogView.findViewById(R.id.nMaxUpper);
        nMaxUpper_text = (TextView) popupPIVDialogView.findViewById(R.id.nMaxUpper_text);

        nMaxLowerText = popupPIVDialogView.findViewById(R.id.nMaxLower);
        nMaxLower_text = (TextView) popupPIVDialogView.findViewById(R.id.nMaxLower_text);

        qMinText = popupPIVDialogView.findViewById(R.id.qMin);
        qMin_text = (TextView) popupPIVDialogView.findViewById(R.id.qMin_text);

        EText = popupPIVDialogView.findViewById(R.id.E);
        E_text = (TextView) popupPIVDialogView.findViewById(R.id.E_text);

        groupradio_text = (TextView) popupPIVDialogView.findViewById(R.id.groupradio_text);

        radioGroup = (RadioGroup) popupPIVDialogView.findViewById(R.id.groupradio);
        // Add the Listener to the RadioGroup
        radioGroup.setOnCheckedChangeListener(
                new RadioGroup
                        .OnCheckedChangeListener() {
                    @Override

                    // The flow will come here when
                    // any of the radio buttons in the radioGroup
                    // has been clicked

                    // Check which radio button has been clicked
                    public void onCheckedChanged(RadioGroup group,
                                                 int checkedId)
                    {

                        // Get the selected Radio Button
                        radioButton = (RadioButton) findViewById(checkedId);
                    }
                });
        savePIVDataButton = popupPIVDialogView.findViewById(R.id.button_save_piv_data);
        cancelPIVDataButton = popupPIVDialogView.findViewById(R.id.button_cancel_piv_data);
    }

    private BoolIntStructure checkUserInputInt(String userInput)
    {
        boolean success = false;
        int integer;
        try {
            integer = Integer.parseInt(userInput);
            success = true;
        }
        catch (NumberFormatException e) {
            integer = 0;
        }
        return new BoolIntStructure(success, integer);
    }

    private boolean isExternalStorageAvailable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_TAKE_PHOTO || requestCode == REQUEST_PICK_PHOTO) {
                if (data != null) {
                    // Get the Image from data
                    Uri selectedImage = data.getData();
                    String[] filePathColumn = {MediaStore.Images.Media.DATA};

                    Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                    assert cursor != null;
                    cursor.moveToFirst();

                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    mediaPath = cursor.getString(columnIndex);
                    // Set the Image in ImageView for Previewing the Media
                    imageView.setImageBitmap(BitmapFactory.decodeFile(mediaPath));
                    cursor.close();


                    postPath = mediaPath;
                }
            } else if (requestCode == CAMERA_PIC_REQUEST) {
                imageView.setImageBitmap(BitmapFactory.decodeFile(mImageFileLocation));
                postPath = mImageFileLocation;
            }
        } else if (resultCode != RESULT_CANCELED) {
            Toast.makeText(this, "Sorry, there was an error!", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Checking device has camera hardware or not
     * */
    private boolean isDeviceSupportCamera() {
        if (getApplicationContext().getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_CAMERA_ANY)) {
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    protected void initDialog() {

        pDialog = new ProgressDialog(this);
        pDialog.setMessage(getString(R.string.msg_loading));
        pDialog.setCancelable(false);
    }


    protected void showpDialog() {

        if (!pDialog.isShowing()) pDialog.show();
    }

    protected void hidepDialog() {

        if (pDialog.isShowing()) pDialog.dismiss();
    }


    /**
     * Launching camera app to capture image
     */
    private void captureImage() {
        int j = 0;
        Intent callCameraApplicationIntent = new Intent();
        callCameraApplicationIntent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);

        // We give some instruction to the intent to save the image
        File photoFile = null;
        try {
            // If the createImageFile will be successful, the photo file will have the address of the file
            photoFile = createImageFile();
            j =0;
            // Here we call the function that will try to catch the exception made by the throw function
        } catch (IOException e) {
            Logger.getAnonymousLogger().info("Exception error in generating the file");
            e.printStackTrace();
        }
        // Here we add an extra file to the intent to put the address on to. For this purpose we use the FileProvider, declared in the AndroidManifest.
        Uri outputUri = FileProvider.getUriForFile(
                this,
                BuildConfig.APPLICATION_ID + ".provider",
                photoFile);
        callCameraApplicationIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputUri);

        // The following is a new line with a trying attempt
        callCameraApplicationIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);

        Logger.getAnonymousLogger().info("Calling the camera App by intent");

        // The following strings calls the camera app and wait for his file in return.
        while(j < 2){
            startActivityForResult(callCameraApplicationIntent, CAMERA_PIC_REQUEST);
            j = j+1;
        }
    }

    File createImageFile() throws IOException {
        Logger.getAnonymousLogger().info("Generating the image - method started");

        // Here we create a "non-collision file name", alternatively said, "an unique filename" using the "timeStamp" functionality
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmSS").format(new Date());
        String imageFileName = "IMAGE_" + timeStamp;
        // Here we specify the environment location and the exact path where we want to save the so-created file
        File storageDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + "/photo_saving_app");
        Logger.getAnonymousLogger().info("Storage directory set");

        // Then we create the storage directory if does not exists
        if (!storageDirectory.exists()) storageDirectory.mkdir();

        // Here we create the file using a prefix, a suffix and a directory
        File image = new File(storageDirectory, imageFileName + ".jpg");
        // File image = File.createTempFile(imageFileName, ".jpg", storageDirectory);

        // Here the location is saved into the string mImageFileLocation
        Logger.getAnonymousLogger().info("File name and path set");

        mImageFileLocation = image.getAbsolutePath();
        // fileUri = Uri.parse(mImageFileLocation);
        // The file is returned to the previous intent across the camera application
        return image;
    }


    /**
     * Here we store the file url as it will be null after returning from camera
     * app
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // save file url in bundle as it will be null on screen orientation
        // changes
        outState.putParcelable("file_uri", fileUri);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // get the file url
        fileUri = savedInstanceState.getParcelable("file_uri");
    }


    /**
     * Receiving activity result method will be called after closing the camera
     * */

    /**
     * ------------ Helper Methods ----------------------
     * */

    /**
     * Creating file uri to store image/video
     */
    public Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /**
     * returning image / video
     */
    private static File getOutputMediaFile(int type) {

        // External sdcard location
        File mediaStorageDir = new File(
                Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                IMAGE_DIRECTORY_NAME);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(TAG, "Oops! Failed create "
                        + IMAGE_DIRECTORY_NAME + " directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                    + "IMG_" + ".jpg");
        } else {
            return null;
        }

        return mediaFile;
    }

    public void reviewFile(View view) {
        reviewImageFromUrl(postPathMultiple);
        review.setBackgroundColor(Color.parseColor("#00CC00"));
    }


    private void reviewImageFromUrl(ArrayList<String> images) {
        String[] urls = new String[2];
        for (int i = 0; i < images.size(); i++) {
            urls[i] = images.get(i);
        }
        Intent intent = new Intent(this, ViewPagerActivity.class).putExtra("string-array-urls", urls);
        startActivity(intent);
    }

    public void displayFile(View view) {
        Intent displayIntent = new Intent(this, ViewResultsActivity.class);
        displayIntent.putExtra("max-displacement", maxDisplacement);
        displayIntent.putExtra("n-max-lower", nMaxLower);
        displayIntent.putExtra("image-paths", postPathMultiple);
        displayIntent.putExtra("username", userName);
        displayIntent.putExtra("selection-Id", selectedId);
        displayIntent.putExtra("pivCorrelation", (Serializable) pivCorrelation);
        displayIntent.putExtra("interrCenters", (Serializable) interrCenters);
        displayIntent.putExtra("vorticityValues", vorticityValues);
        displayIntent.putExtra("pivCorrelationMulti", (Serializable) pivCorrelationMulti);
        displayIntent.putExtra("pivReplaceMissing2", (Serializable) pivReplaceMissing2);
        displayIntent.putExtra("rows", rows);
        displayIntent.putExtra("cols", cols);

        startActivity(displayIntent);
        pickImageMultiple.setBackgroundColor(Color.parseColor("#243EDF"));
        compute.setBackgroundColor(Color.parseColor("#243EDF"));
        review.setBackgroundColor(Color.parseColor("#243EDF"));
        parameters.setBackgroundColor(Color.parseColor("#243EDF"));
    }

    // Process Images
    private void processFile() {
        piv = new PivFunctions(postPathMultiple.get(0),
                postPathMultiple.get(1),
                windowSize,
                overlap,
                dt,
                "peak2peak");

        rows = piv.getRows();
        cols = piv.getCols();
        grayFrame1 = piv.getFirstFrameGray();
        grayFrame2 = piv.getSecondFrameGray();

        final String imgFileSaveName = postPathMultiple.get(0).split("/")[6].split(".png")[0]
                + "-"
                +postPathMultiple.get(1).split("/")[6].split("_")[3].split(".png")[0]+".png";

        if (postPathMultiple.size() != 0){

            //---------------------------------Using Threads--------------------------------------//
            showpDialog();
            Thread thread = new Thread() {
                @Override
                public void run() {
                    if(!running) {
                        return;
                    }
                    else {
                        pivCorrelation = PivFunctions.extendedSearchAreaPiv_update(
                                grayFrame1,
                                grayFrame2,
                                rows,
                                cols,
                                windowSize,
                                overlap
                        );

                        interrCenters = PivFunctions.getCoordinates(rows, cols, windowSize, overlap);

                        ArrowDrawOptions arrowDrawOptions = new ArrowDrawOptions();
                        arrowDrawOptions.scale = 5d;

                        // Save first frame for output base image
                        piv.saveBaseImage(userName, "Base", imgFileSaveName);

//                        String calibrationStep = "Calibration";
//                        CameraCalibration calibration = new CameraCalibration(context);
//                        double pixelToCmRatio = calibration.calibrate(postPathMultiple.get(0), postPathMultiple.get(1));
//                        if (calibration.isCalibrated()) {
//                            piv.saveImage(calibration.undistortImage(), userName, calibrationStep, imgFileSaveName);
//                            piv.saveVectorCentimeters(pivCorrelation, interrCenters, pixelToCmRatio, userName, "CENTIMETERS", imgFileSaveName);
//                        }

                        String vortStep = "Vorticity";
                        vorticityValues = PivFunctions.calculateVorticityMap(pivCorrelation, (int)(interrCenters.get("x")[1] - interrCenters.get("x")[0]));
                        PivFunctions.saveVortMapFile(vorticityValues, userName, vortStep, imgFileSaveName);
                        PivFunctions.saveColorMapImage(vorticityValues, userName, vortStep, imgFileSaveName);

                        String step = "SinglePass";
                        PivFunctions.saveVectors(pivCorrelation, interrCenters, userName, step, imgFileSaveName, dt);
                        PivFunctions.createVectorField(pivCorrelation, interrCenters, userName, step, imgFileSaveName, arrowDrawOptions, rows, cols);
                        pivCorrelationProcessed = PivFunctions.vectorPostProcessing(pivCorrelation, cols, rows, windowSize, overlap, dt, nMaxUpper, qMin, E);

                        String stepPro = "VectorPostProcess";
                        PivFunctions.saveVectors(pivCorrelationProcessed, interrCenters, userName, stepPro,imgFileSaveName, dt);
                        PivFunctions.createVectorField(pivCorrelationProcessed, interrCenters, userName, stepPro, imgFileSaveName, arrowDrawOptions, rows, cols);

                        if (selectedId == 0){
                            pivReplaceMissing = PivFunctions.replaceMissingVectors(pivCorrelationProcessed, rows, cols, windowSize, overlap);
                            pivCorrelationMulti = PivFunctions.calculateMultipass(pivReplaceMissing, interrCenters, grayFrame1, grayFrame2, rows, cols, windowSize, overlap);

                            String stepMulti = "Multipass";
                            PivFunctions.saveVectors(pivCorrelationMulti, interrCenters, userName, stepMulti, imgFileSaveName, dt);
                            PivFunctions.createVectorField(pivCorrelationMulti, interrCenters, userName, stepMulti, imgFileSaveName, arrowDrawOptions, rows, cols);
                            pivReplaceMissing2 = PivFunctions.replaceMissingVectors(pivCorrelationMulti, rows, cols, windowSize, overlap);

                            String stepReplace2 = "Replaced2";
                            PivFunctions.saveVectors(pivReplaceMissing2, interrCenters, userName, stepReplace2, imgFileSaveName, dt);
                            PivFunctions.createVectorField(pivReplaceMissing2, interrCenters, userName, stepReplace2, imgFileSaveName, arrowDrawOptions, rows, cols);

                            maxDisplacement = PivFunctions.checkMaxDisplacement(pivReplaceMissing2);

                        } else if (selectedId == 1) {
                            pivCorrelationMulti = PivFunctions.calculateMultipass(pivCorrelationProcessed, interrCenters, grayFrame1, grayFrame2, rows, cols, windowSize, overlap);

                            String stepMulti = "Multipass";
                            PivFunctions.saveVectors(pivCorrelationMulti, interrCenters, userName, stepMulti, imgFileSaveName, dt);
                            PivFunctions.createVectorField(pivCorrelationMulti, interrCenters, userName, stepMulti, imgFileSaveName, arrowDrawOptions, rows, cols);

                            maxDisplacement = PivFunctions.checkMaxDisplacement(pivCorrelationMulti);
                        }
                        hidepDialog();
                    }
                }
            };

            thread.start();
            display.setEnabled(true);
            running = false;
            //-------------------------------Thread End-------------------------------------------//
            compute.setBackgroundColor(Color.parseColor("#00CC00"));
        }
    }

    /**
     * Requests the READ_PHONE_STATE permission.
     * If the permission has been denied previously, a dialog will prompt the user to grant the
     * permission, otherwise it is requested directly.
     */
    private void requestReadPhoneStatePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.READ_PHONE_STATE)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // For example if the user has previously denied the permission.
            new AlertDialog.Builder(ImageActivity.this)
                    .setTitle("Permission Request")
                    .setMessage(getString(R.string.permission_read_phone_state_rationale))
                    .setCancelable(false)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            //re-request
                            ActivityCompat.requestPermissions(ImageActivity.this,
                                    new String[]{Manifest.permission.READ_PHONE_STATE},
                                    MY_PERMISSIONS_REQUEST_READ_PHONE_STATE);
                        }
                    })
                    .show();
        } else {
            // READ_PHONE_STATE permission has not been granted yet. Request it directly.
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE},
                    MY_PERMISSIONS_REQUEST_READ_PHONE_STATE);
        }
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        if (requestCode == MY_PERMISSIONS_REQUEST_READ_PHONE_STATE) {
            // Received permission result for READ_PHONE_STATE permission.est.");
            // Check if the only required permission has been granted
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // READ_PHONE_STATE permission has been granted, proceed with displaying IMEI Number
                //alertAlert(getString(R.string.permision_available_read_phone_state));
                doPermissionGrantedStuffs();
            } else {
                alertAlert(getString(R.string.permissions_not_granted_read_phone_state));
            }
        }
    }

    private void alertAlert(String msg) {
        new AlertDialog.Builder(ImageActivity.this)
                .setTitle("Permission Request")
                .setMessage(msg)
                .setCancelable(false)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do somthing here
                    }
                })
                .show();
    }

    public void doPermissionGrantedStuffs() {
        //Have an  object of TelephonyManager
        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        //Get IMEI Number of Phone  //////////////// for this task i only need the IMEI
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            IMEINumber = tm != null ? tm.getImei() : null;
        }

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1 && android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            IMEINumber = tm != null ? tm.getDeviceId() : null;
        }

        /************************************************
         * **********************************************
         * This is just an icing on the cake
         * the following are other children of TELEPHONY_SERVICE
         *
         //Get SIM Serial Number
         String SIMSerialNumber=tm.getSimSerialNumber();

         //Get Network Country ISO Code
         String networkCountryISO=tm.getNetworkCountryIso();

         //Get SIM Country ISO Code
         String SIMCountryISO=tm.getSimCountryIso();

         //Get the device software version
         String softwareVersion=tm.getDeviceSoftwareVersion()

         //Get the Voice mail number
         String voiceMailNumber=tm.getVoiceMailNumber();


         //Get the Phone Type CDMA/GSM/NONE
         int phoneType=tm.getPhoneType();

         switch (phoneType)
         {
         case (TelephonyManager.PHONE_TYPE_CDMA):
         // your code
         break;
         case (TelephonyManager.PHONE_TYPE_GSM)
         // your code
         break;
         case (TelephonyManager.PHONE_TYPE_NONE):
         // your code
         break;
         }

         //Find whether the Phone is in Roaming, returns true if in roaming
         boolean isRoaming=tm.isNetworkRoaming();
         if(isRoaming)
         phoneDetails+="\nIs In Roaming : "+"YES";
         else
         phoneDetails+="\nIs In Roaming : "+"NO";


         //Get the SIM state
         int SIMState=tm.getSimState();
         switch(SIMState)
         {
         case TelephonyManager.SIM_STATE_ABSENT :
         // your code
         break;
         case TelephonyManager.SIM_STATE_NETWORK_LOCKED :
         // your code
         break;
         case TelephonyManager.SIM_STATE_PIN_REQUIRED :
         // your code
         break;
         case TelephonyManager.SIM_STATE_PUK_REQUIRED :
         // your code
         break;
         case TelephonyManager.SIM_STATE_READY :
         // your code
         break;
         case TelephonyManager.SIM_STATE_UNKNOWN :
         // your code
         break;

         }
         */
    }
}
