package org.akvo.caddisfly.sensor.chamber;

import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.common.ConstantKey;
import org.akvo.caddisfly.entity.Calibration;
import org.akvo.caddisfly.model.TestInfo;

import java.util.List;

public class CalibrationGraphActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calibration_graph);

        TestInfo testInfo = getIntent().getParcelableExtra(ConstantKey.TEST_INFO);

        List<Calibration> calibrations = testInfo.getCalibrations();

        GraphView graphRed = findViewById(R.id.graphRed);
        GraphView graphGreen = findViewById(R.id.graphGreen);
        GraphView graphBlue = findViewById(R.id.graphBlue);

        LineGraphSeries<DataPoint> seriesRed = new LineGraphSeries<>(getDataPoints(calibrations, Color.RED));
        seriesRed.setColor(Color.RED);
        seriesRed.setThickness(4);
        seriesRed.setDrawDataPoints(true);
        seriesRed.setDataPointsRadius(8);
        graphRed.getViewport().setYAxisBoundsManual(true);
        graphRed.getViewport().setMinY(245);
        graphRed.getViewport().setMaxY(255);
        graphRed.addSeries(seriesRed);

        LineGraphSeries<DataPoint> seriesRed2 = new LineGraphSeries<>(getDataPoints2(calibrations, Color.RED));
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);
        paint.setPathEffect(new DashPathEffect(new float[]{10, 10}, 0));
        seriesRed2.setCustomPaint(paint);
        seriesRed2.setDrawDataPoints(true);
        seriesRed2.setDataPointsRadius(4);
        graphRed.addSeries(seriesRed2);

        LineGraphSeries<DataPoint> seriesGreen2 = new LineGraphSeries<>(getDataPoints2(calibrations, Color.GREEN));
        seriesGreen2.setColor(Color.BLACK);
        seriesGreen2.setThickness(2);
        seriesGreen2.setDrawDataPoints(true);
        seriesGreen2.setDataPointsRadius(4);
        graphGreen.addSeries(seriesGreen2);

        LineGraphSeries<DataPoint> seriesGreen = new LineGraphSeries<>(getDataPoints(calibrations, Color.GREEN));
        seriesGreen.setColor(Color.GREEN);
        seriesGreen.setThickness(4);
        seriesGreen.setDrawDataPoints(true);
        seriesGreen.setDataPointsRadius(8);
        graphGreen.getViewport().setYAxisBoundsManual(true);
        graphGreen.getViewport().setMinY(30);
        graphGreen.getViewport().setMaxY(140);

        graphGreen.addSeries(seriesGreen);

        LineGraphSeries<DataPoint> seriesBlue2 = new LineGraphSeries<>(getDataPoints2(calibrations, Color.BLUE));
        seriesBlue2.setColor(Color.BLACK);
        seriesBlue2.setThickness(2);
        seriesBlue2.setDrawDataPoints(true);
        seriesBlue2.setDataPointsRadius(4);
        graphBlue.addSeries(seriesBlue2);

        LineGraphSeries<DataPoint> seriesBlue = new LineGraphSeries<>(getDataPoints(calibrations, Color.BLUE));
        seriesBlue.setColor(Color.BLUE);
        seriesBlue.setThickness(4);
        seriesBlue.setDrawDataPoints(true);
        seriesBlue.setDataPointsRadius(8);
        graphBlue.addSeries(seriesBlue);
    }

    @NonNull
    private DataPoint[] getDataPoints(List<Calibration> calibrations, int color) {
        DataPoint[] dataPoints = new DataPoint[calibrations.size()];
        int value = 0;
        for (int i = 0; i < calibrations.size(); i++) {
            switch (color) {
                case Color.RED:
                    value = Color.red(calibrations.get(i).color);
                    break;
                case Color.GREEN:
                    value = Color.green(calibrations.get(i).color);
                    break;
                case Color.BLUE:
                    value = Color.blue(calibrations.get(i).color);
                    break;
            }
            dataPoints[i] = new DataPoint(calibrations.get(i).value, value);
        }
        return dataPoints;
    }

    @NonNull
    private DataPoint[] getDataPoints2(List<Calibration> calibrations, int color) {
        DataPoint[] dataPoints = new DataPoint[calibrations.size()];
        int value = 0;
        for (int i = 0; i < calibrations.size(); i++) {
            switch (color) {
                case Color.RED:
                    value = Color.red(calibrations.get(i).color) + 20;
                    break;
                case Color.GREEN:
                    value = Color.green(calibrations.get(i).color) + 10;
                    break;
                case Color.BLUE:
                    value = Color.blue(calibrations.get(i).color) + 15;
                    break;
            }
            dataPoints[i] = new DataPoint(calibrations.get(i).value, value);
        }
        return dataPoints;
    }

}
