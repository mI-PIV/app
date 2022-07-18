package com.onrpiv.uploadmedia.Experiment.Popups;

import android.content.Context;
import android.content.Intent;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.onrpiv.uploadmedia.Experiment.ViewMultipleResultsActivity;
import com.onrpiv.uploadmedia.Experiment.ViewResultsActivity;
import com.onrpiv.uploadmedia.R;
import com.onrpiv.uploadmedia.Utilities.FileIO;
import com.onrpiv.uploadmedia.Utilities.PathUtil;
import com.onrpiv.uploadmedia.Utilities.Popup;
import com.onrpiv.uploadmedia.pivFunctions.PivParameters;

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
        TableLayout tableLayout = popupView.findViewById(R.id.load_popup_table);
        List<Integer> savedExperimentsNumList = FileIO.getSavedExperimentsDict(context, userName);

        if (savedExperimentsNumList.isEmpty()) {
            // No experiments found
            tableLayout.addView(createTextView("No previous experiments found.\nGo back to start a new experiment."), 0);
        } else {
            // experiments found
            loadBtn.setEnabled(true);
            for (Integer i : savedExperimentsNumList) {
                // read the piv parameter object file
                PivParameters params = (PivParameters) FileIO.read(context, userName, i, PivParameters.IO_FILENAME);
                if (null == params) continue;

                // create a string that contains the piv parameter data
                String paramString = params.prettyPrintData_small();
                // create a table row with the param data string and add it to our table
                tableLayout.addView(createDataRow(i, paramString), 0);
            }

            // get the most recent experiment radio button and set it as the currently selected
            currentlySelected = (RadioButton) ((TableRow) tableLayout.getChildAt(0)).getChildAt(0);
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
        } else {
            Class<?> singleIntentClass = ViewResultsActivity.loadFromFiles(context, userName, experimentNum);
            displayIntent = new Intent(context, singleIntentClass);
        }

        // start the results activity
        context.startActivity(displayIntent);
        closePopup();
    }

    private TableRow createDataRow(int expNum, String dataString) {
        TableRow dataRow = createTableRow();

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

    private TableRow createTableRow() {
        TableRow newRow = new TableRow(context);
        newRow.setGravity(Gravity.CENTER_HORIZONTAL);

        TableRow.LayoutParams layoutParams = new TableRow.LayoutParams();
        layoutParams.width = TableRow.LayoutParams.MATCH_PARENT;
        layoutParams.height = TableRow.LayoutParams.WRAP_CONTENT;
        layoutParams.gravity = Gravity.CENTER;
        newRow.setLayoutParams(layoutParams);

        return newRow;
    }
}
