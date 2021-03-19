package com.onrpiv.uploadmedia.Learn;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.onrpiv.uploadmedia.R;

public class VideoTutorial extends LearnPIV {

    String [] tutorialArray = {"Choosing an Experiment","Setting up an Experiment","Getting the lighting Right","How Big can my Experiment be?",
    "All About Lasers!","Selecting the right Seed Particles","How PIV calculates Flow Velocity","How mI-PIV works"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_tutorial);

        ArrayAdapter adapter = new ArrayAdapter<String>(this,
                R.layout.activity_listview, tutorialArray);

        ListView listView = (ListView) findViewById(R.id.tutorial_list);
        listView.setAdapter(adapter);
    }
}
