package com.onrpiv.uploadmedia.Experiment.Popups;

import android.content.Context;
import android.content.Intent;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import com.onrpiv.uploadmedia.Experiment.ViewMultipleResultsActivity;
import com.onrpiv.uploadmedia.Experiment.ViewResultsActivity;
import com.onrpiv.uploadmedia.R;
import com.onrpiv.uploadmedia.Utilities.FileIO;
import com.onrpiv.uploadmedia.Utilities.PathUtil;
import com.onrpiv.uploadmedia.Utilities.Popup;
import com.onrpiv.uploadmedia.pivFunctions.PivParameters;
import com.onrpiv.uploadmedia.pivFunctions.PivResultData;

import java.util.HashMap;
import java.util.List;

public class LoadExperimentPopup extends Popup {
    public final String userName;
    private final Context context;
    private Button cancelBtn, loadBtn;
    private RadioButton currentlySelected = null;
    private final HashMap<View, Integer> selectionToExpNumDict;
    private final View.OnClickListener radioListener;


    public LoadExperimentPopup(Context context, String userName) {
        super(context, R.layout.popup_load_experiment, "Load Experiment", true);
        this.context = context;
        this.userName = userName;
        selectionToExpNumDict = new HashMap<>();

        radioListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentlySelected.setChecked(false);
                currentlySelected = (RadioButton) v;
                currentlySelected.setChecked(true);
            }
        };

        // load our table with data
        LinearLayout layout = popupView.findViewById(R.id.load_popup_table);
        List<Integer> savedExperimentsNumList = FileIO.getSavedExperimentsDict(context, userName);

        if (savedExperimentsNumList.isEmpty()) {
            // No experiments found
            layout.addView(createTextView("No previous experiments found.\nGo back to start a new experiment."), 0);
        } else {
            // experiments found
            loadBtn.setEnabled(true);
            for (Integer i : savedExperimentsNumList) {
                // read the piv parameter object file
                PivParameters params = (PivParameters) FileIO.read(context, userName, i, PivParameters.IO_FILENAME);
                if (null == params) continue;

                // create a string that contains the piv parameter data
                String paramString = params.prettyPrintData_small();
                // create a row with the param data string and add it to our layout
                layout.addView(createDataRow(i, paramString), 0);
            }

            // get the most recent experiment radio button and set it as the currently selected
            currentlySelected = (RadioButton) ((LinearLayout) layout.getChildAt(0)).getChildAt(0);
            currentlySelected.toggle();
        }
    }

    @Override
    protected View[] initOnClickViews() {
        cancelBtn = popupView.findViewById(R.id.load_popup_cancel_button);
        loadBtn = popupView.findViewById(R.id.load_popup_load_btn);
        loadBtn.setEnabled(false);

        return new View[]{cancelBtn, loadBtn};
    }

    @Override
    public void onClick(View v) {
        if (v == loadBtn) {
            load();
        } else if (v == cancelBtn) {
            closePopup();
        }
    }

    private void load() {
        int experimentNum = selectionToExpNumDict.get(currentlySelected);
        Intent displayIntent;
        if (PathUtil.isMultipleResult(PathUtil.getExperimentNumberedDirectory(context, userName, experimentNum))) {
            Class<?> multipleIntentClass = ViewMultipleResultsActivity.loadFromFiles(context, userName, experimentNum);
            displayIntent = new Intent(context, multipleIntentClass);
            displayIntent.putExtra(PivResultData.REPLACED_BOOL, ViewMultipleResultsActivity.pivParameters.isReplace());
        } else {
            Class<?> singleIntentClass = ViewResultsActivity.loadFromFiles(context, userName, experimentNum);
            displayIntent = new Intent(context, singleIntentClass);
            displayIntent.putExtra(PivResultData.REPLACED_BOOL, ViewResultsActivity.pivParameters.isReplace());
        }

        displayIntent.putExtra(PivResultData.EXP_NUM, experimentNum);
        displayIntent.putExtra(PivResultData.USERNAME, userName);

        // start the results activity
        context.startActivity(displayIntent);
        closePopup();
    }

    private LinearLayout createDataRow(int expNum, String dataString) {
        LinearLayout dataRow = createRow();

        // radio select
        RadioButton radioSelect = createRadioButton(expNum);
        dataRow.addView(radioSelect);

        // data text
        TextView textView = createTextView(dataString);
        dataRow.addView(textView);

        return dataRow;
    }

    private TextView createTextView(String text) {
        TextView textView = new TextView(context);
        textView.setText(text);
        textView.setGravity(Gravity.CENTER_HORIZONTAL);
        textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

        return textView;
    }

    private RadioButton createRadioButton(int expNum) {
        RadioButton radioButton = new RadioButton(context);
        radioButton.setText("Experiment " + expNum);
        radioButton.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

        // layout
        radioButton.setGravity(Gravity.CENTER);
        radioButton.setPaddingRelative(
                radioButton.getPaddingStart(),
                radioButton.getPaddingTop(),
                20,
                20);

        // logic
        selectionToExpNumDict.put(radioButton, expNum);
        radioButton.setOnClickListener(radioListener);

        return radioButton;
    }

    private LinearLayout createRow() {
        LinearLayout newRow = new LinearLayout(context);
        newRow.setGravity(Gravity.START);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.gravity = Gravity.CENTER;
        newRow.setLayoutParams(layoutParams);

        return newRow;
    }
}
