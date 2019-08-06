/*
package com.unipi.lykourgoss.earthquakeobserver.filestoremove;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.PointsGraphSeries;
import com.unipi.lykourgoss.earthquakeobserver.R;
import com.unipi.lykourgoss.earthquakeobserver.filestoremove.XYValue;

import java.util.ArrayList;

public class GraphViewActivity extends AppCompatActivity {

    private static final String TAG = "GraphViewActivity";

    private PointsGraphSeries<DataPoint> xySeries;

    private GraphView lineChart;

    private ArrayList<XYValue> xyValueArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        lineChart = findViewById(R.id.line_chart);

        xyValueArrayList = new ArrayList<>();

        init();
    }

    private void  init() {
        xySeries = new PointsGraphSeries<>();
    }
}
*/
