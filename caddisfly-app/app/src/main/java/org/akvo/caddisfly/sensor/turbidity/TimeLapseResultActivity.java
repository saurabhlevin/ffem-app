package org.akvo.caddisfly.sensor.turbidity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.common.ConstantKey;
import org.akvo.caddisfly.helper.FileHelper;
import org.akvo.caddisfly.model.Result;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.preference.AppPreferences;
import org.akvo.caddisfly.ui.BaseActivity;
import org.akvo.caddisfly.util.ApiUtil;
import org.akvo.caddisfly.util.AssetsManager;
import org.akvo.caddisfly.util.GMailSender;
import org.akvo.caddisfly.util.PreferencesUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import timber.log.Timber;

import static org.akvo.caddisfly.util.FileUtil.saveToFile;

/**
 * Activity that displays the results.
 */
public class TimeLapseResultActivity extends BaseActivity {
    static StringBuilder html2 = new StringBuilder();
    static String style = "<style>body{font-family:sans-serif}h1{font-size:18px;margin-bottom:5px}.nopadding{padding:0} @media print {body {-webkit-print-color-adjust: exact;}}td{font-size: 14px;text-align:center;padding:0 10px;}tfoot td{background-color:rgb(75,75,75);color:white;}tfoot td{padding:10 10px;}th{font-size: 14px;background-color:rgb(75,75,75);color:white;padding:10 10px;margin-bottom: 10px;}</style></head><body>";
    static FilenameFilter imageFilter = (dir, name) -> {
        String lowercaseName = name.toLowerCase();
        return lowercaseName.endsWith(".jpg");
    };
    static Point centerPoint;
    File folder;
    boolean isTurbid;
    private Button buttonSave;
    private LinearLayout layout;
    private TestInfo testInfo;
    String durationString;

    private static boolean isGreen(int color, int compareColor, String media) {
        float[] hsb = new float[3];
        Color.RGBToHSV(Color.red(color), Color.green(color), Color.blue(color), hsb);

        //Color.RGBtoHSB(97, 58, 1, hsb);

        if (hsb[1] < 0.1 && hsb[2] > 0.9) return false;
        else if (hsb[2] < 0.1) return false;
        else {
            float deg = hsb[0] * 360;
            //if (deg >=  25 && deg < 70) return true;
            if (media.equalsIgnoreCase("himedia")) {
                return (deg >= 25 && deg < 70) && getColorDistanceRgb(color, compareColor) > 70;
            } else {
                return (deg >= 10 && deg < 40) && getColorDistanceRgb(color, compareColor) > 50;
            }
        }
    }

    /**
     * Computes the Euclidean distance between the two colors
     *
     * @param color1 the first color
     * @param color2 the color to compare with
     * @return the distance between the two colors
     */
    private static double getColorDistanceRgb(int color1, int color2) {
        double r, g, b;

        r = Math.pow(Color.red(color2) - Color.red(color1), 2.0);
        g = Math.pow(Color.green(color2) - Color.green(color1), 2.0);
        b = Math.pow(Color.blue(color2) - Color.blue(color1), 2.0);

        return Math.sqrt(b + g + r);
    }

    private static String getColorHexString(int color) {
        return ("00" + Integer.toHexString(Color.red(color))).substring(Integer.toHexString(Color.red(color)).length()) +
                ("00" + Integer.toHexString(Color.green(color))).substring(Integer.toHexString(Color.green(color)).length()) +
                ("00" + Integer.toHexString(Color.blue(color))).substring(Integer.toHexString(Color.blue(color)).length());
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_strip_result);
        setTitle(R.string.result);

        folder = FileHelper.getFilesDir(FileHelper.FileType.TEMP_IMAGE,
                getIntent().getStringExtra(ConstantKey.SAVE_FOLDER));

        testInfo = getIntent().getParcelableExtra(ConstantKey.TEST_INFO);

        buttonSave = findViewById(R.id.button_save);
        buttonSave.setOnClickListener(v -> {

            Intent resultIntent = new Intent();

            resultIntent.putExtra(testInfo.getResults().get(0).getCode(),
                    testInfo.getResults().get(0).getResult());
            resultIntent.putExtra(testInfo.getResults().get(1).getCode(),
                    testInfo.getResults().get(1).getResult());
            resultIntent.putExtra(testInfo.getResults().get(2).getCode(),
                    testInfo.getResults().get(2).getResult());

            setResult(Activity.RESULT_OK, resultIntent);

            finish();
        });
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        layout = findViewById(R.id.layout_results);
        layout.removeAllViews();

        startAnalysis();

        buttonSave.setVisibility(View.VISIBLE);
    }

    @SuppressLint("InflateParams")
    private void inflateView(String title, String valueString, Bitmap resultImage) {

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout itemResult;
        if (inflater != null) {
            itemResult = (LinearLayout) inflater.inflate(R.layout.item_result,
                    null, false);
            TextView textTitle = itemResult.findViewById(R.id.text_title);
            textTitle.setText(title);

            if (resultImage != null) {
//                resultImage = ImageUtil.rotateImage(resultImage, 90);
                ImageView imageResult = itemResult.findViewById(R.id.image_result);
                imageResult.setImageBitmap(resultImage);
            }

            TextView textResult = itemResult.findViewById(R.id.text_result);
            if (valueString.isEmpty()) {
                textResult.setVisibility(View.GONE);
            } else {
                textResult.setText(valueString);
            }

            layout.addView(itemResult);
        }
    }

    private void sendEmail(String body, File firstImage, File turbidImage,
                           File lastImage, String from, String to, String password) {
        new Thread(() -> {
            try {
                GMailSender sender = new GMailSender(from, password);
                sender.sendMail("Coliform test: " + Calendar.getInstance().getTimeInMillis(),
                        body, firstImage, turbidImage, lastImage, from, to);
            } catch (Exception e) {
                Timber.e(e);
            }
        }).start();
    }

    private void startAnalysis() {
        if (folder.isDirectory()) {
//            ResultInfo resultInfo = readResult(new File(folder, "result"));
//
//            if (resultInfo == null) {
//                new AnalyzeTask(folder).execute();
//                return;
//            }

//            Toast.makeText(this, "No files to analyze", Toast.LENGTH_LONG).show();
//            finish();

            Random random = new Random(Calendar.getInstance().getTimeInMillis());

            Boolean resultValue = random.nextBoolean();

            File firstImage = new File(PreferencesUtil.getString(this, "firstImage", ""));
            File lastImage = new File(PreferencesUtil.getString(this, "lastImage", ""));
            File turbidImage = null;
            if (resultValue) {
                turbidImage = new File(PreferencesUtil.getString(this, "turbidImage", ""));
            }

            String emailTemplate;
            if (resultValue) {
                emailTemplate = AssetsManager.getInstance().loadJsonFromAsset("templates/email_template_unsafe.html");
            } else {
                emailTemplate = AssetsManager.getInstance().loadJsonFromAsset("templates/email_template_safe.html");
            }

            if (emailTemplate != null) {
                long startTime = PreferencesUtil.getLong(this, ConstantKey.TEST_START_TIME);
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm, dd MMM yyyy", Locale.US);
                String date = simpleDateFormat.format(new Date(startTime));
                emailTemplate = emailTemplate.replace("{startTime}", date);

                long duration = Calendar.getInstance().getTimeInMillis() - startTime;

                durationString = String.format(Locale.US, "%02d:%02d Hours", TimeUnit.MILLISECONDS.toHours(duration),
                        TimeUnit.MILLISECONDS.toMinutes(duration) % TimeUnit.HOURS.toMinutes(1));

                emailTemplate = emailTemplate.replace("{detectionDuration}", durationString);
                emailTemplate = emailTemplate.replace("{detectionTime}",
                        simpleDateFormat.format(Calendar.getInstance().getTime()));
            }

            for (Result result : testInfo.getResults()) {
                switch (result.getName()) {
                    case "Broth":
                        result.setResult("Hi Media");
                        break;
                    case "Time to detect":
                        result.setResult(durationString);
                        break;
                    case "First Image":
                        inflateView(result.getName(), "", BitmapFactory.decodeFile(firstImage.getAbsolutePath()));
                        break;
                    case "Turbid Image":
                        if (resultValue) {
                            inflateView(result.getName(), "", BitmapFactory.decodeFile(turbidImage.getAbsolutePath()));
                        }
                        break;
                    case "Last Image":
                        inflateView(result.getName(), "", BitmapFactory.decodeFile(lastImage.getAbsolutePath()));
                        break;
                    default:
                        if (resultValue) {
                            result.setResult("High Risk - Contaminated");
                        } else {
                            result.setResult("Low Risk - Possibly Safe");
                        }
                        inflateView(result.getName(), result.getResult(), null);
                        break;
                }
            }

            String notificationEmails = AppPreferences.getNotificationEmails();

            String email = PreferencesUtil.getString(this, "username", "");
            String password = PreferencesUtil.getString(this, "password", "");
            if (!email.isEmpty() && !password.isEmpty() && !notificationEmails.isEmpty()) {
                sendEmail(emailTemplate, firstImage, turbidImage, lastImage, email, notificationEmails, password);
            }
        }
    }

    private ResultInfo analyzeFiles(File folder) {

        ResultInfo resultInfo = new ResultInfo();

        String firstImageName = "";
        int firstImageValue = 0;
        String incubationImageName = "";
        boolean colorChangeFound = false;
        int fileCount = 0;
        //int incubationTime = 0;
        //int confirmed = 0;

        centerPoint = null;

        int timeAtBlurriness;
        //double maxDifference = 0;

        try {
            if (folder.exists()) {
                String details[] = folder.getName().split("_");

                if (details.length < 3) {
                    return null;
                }

                String reportDate = details[1];

                String type = "";

                String beforeQty = "-";

                String mainTitle = beforeQty;

                Calendar reportCalendar = Calendar.getInstance();
                SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMdd", Locale.US);

                try {
                    reportCalendar.setTime(sdf1.parse(reportDate));
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.US);

                String title = dateFormat.format(reportCalendar.getTime());

                List<ImageInfo> imageInfos = new ArrayList<>();

                //int previousResultValue = 0;
                //Mat destInitialMat = null;


                File mainFolder = new File(folder.getAbsolutePath() + File.separator + "input");
                if (!mainFolder.exists()) {
                    //noinspection ResultOfMethodCallIgnored
                    mainFolder.mkdirs();
                }


                mainFolder = new File(folder.getAbsolutePath() + File.separator + "output");
                if (!mainFolder.exists()) {
                    //noinspection ResultOfMethodCallIgnored
                    mainFolder.mkdirs();
                }


                File[] files = folder.listFiles(imageFilter);
                if (files != null) {
                    fileCount = files.length;
                    for (File file : files) {

//                        Mat initialMat = Imgcodecs.imread(file.getAbsolutePath(), Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
//
//                        if (centerPoint == null) {
//                            centerPoint = new Point(initialMat.width() / 2, initialMat.height() / 2);
//                        }
//
//                        int x = (int) centerPoint.x;
//                        int y = (int) centerPoint.y;
//
//                        Mat colorMat = Imgcodecs.imread(file.getAbsolutePath(), Imgcodecs.CV_LOAD_IMAGE_COLOR);
//
//                        Rect roi = new Rect(x - 150, y - 150, 300, 300);
//                        Mat matImageGrey = new Mat(initialMat, roi);
//                        Mat matImageColor = new Mat(colorMat, roi);
//
                        ImageInfo imageInfo = new ImageInfo();
//
//                        try {
//
//                            //Mat source = Imgcodecs.imread("digital_image_processing.jpg",  Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
//                            //Mat destination = new Mat(source.rows(),source.cols(),source.type());
//
//                            Imgproc.threshold(matImageGrey, matImageGrey, 90, 255, Imgproc.THRESH_TOZERO);
//                            int count = Core.countNonZero(matImageGrey);
                        imageInfo.setCount(10000);
                        imageInfo.setImageName(file.getName());
//
//                            imageInfo.setMat(matImageGrey);
//
//                            Imgcodecs.imwrite(folder.getAbsolutePath() + File.separator + "output" + File.separator + file.getName(), matImageGrey);
//                            Imgcodecs.imwrite(folder.getAbsolutePath() + File.separator + "input" + File.separator + file.getName(), matImageColor);
//                        } catch (Exception e) {
//                            System.out.println("error: " + e.getMessage());
//                        }
//
                        imageInfos.add(imageInfo);
                    }
                }

                if (imageInfos.size() < 1) {
                    return null;
                }

                String scriptStart = "<html><head><script type=\"text/javascript\" src=\"https://www.gstatic.com/charts/loader.js\"></script><script type=\"text/javascript\">google.charts.load('current', {'packages':['line']});google.charts.setOnLoadCallback(drawChart);function drawChart() {var data = new google.visualization.DataTable();data.addColumn('number', 'Minutes');data.addColumn('number', 'Black');data.addColumn('number', 'White');data.addColumn('number', 'Difference');data.addRows([";
                String scriptEnd = "]);var options = {chart: {title: '" + title + "',subtitle: 'Brightness level of two points on black and white sides of the backdrop',},vAxis: {title: 'Brightness'},width: 700,height: 500,legend: { position: 'bottom', alignment: 'start' },colors:['black','orange','green']};var chart = new google.charts.Line(document.getElementById('linechart_material'));chart.draw(data, google.charts.Line.convertOptions(options));}</script>";
                String reportTitle = "<h1>Coliform Test - " + mainTitle + "</h1>";
                //"<div id=\"linechart_material\" style=\"width: 900px; height: 500px;margin-bottom:20px\"></div>";
                StringBuilder html = new StringBuilder();
                StringBuilder data = new StringBuilder();

                html.append("<table><thead><tr><th>Time</th><th>Minutes</th><th>Color</th><th>Original</th><th>Image</th><th>Threshold</th><th>Brightness</th><th>Difference</th></thead></tr>");

                Calendar start = Calendar.getInstance();
                Calendar end = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmm", Locale.US);

                int totalTime = 0;
                try {
                    ImageInfo imageInfo = imageInfos.get(0);
                    String date = imageInfo.getImageName().substring(0, 13);
                    start.setTime(sdf.parse(date));
                    imageInfo = imageInfos.get(imageInfos.size() - 1);
                    date = imageInfo.getImageName().substring(0, 13);
                    end.setTime(sdf.parse(date));

                    totalTime = (int) ((end.getTimeInMillis() - start.getTimeInMillis()) / 1000 / 60);

                } catch (ParseException e) {
                    e.printStackTrace();
                }

                Calendar calendar = Calendar.getInstance();

                long previousDate = 0;
                int totalDiff = 0;
                boolean found = false;
                String incubationHours = "";
                Integer startColor = 0;
                Integer endColor = 0;

                for (int i = 0; i < imageInfos.size(); i++) {

                    ImageInfo imageInfo = imageInfos.get(i);

                    String time = imageInfo.getImageName().substring(0, 13);

                    if (i < 2) {
                        firstImageName = imageInfo.getImageName();
                    }

                    if (i == 2) {
                        firstImageName = imageInfo.getImageName();
                        firstImageValue = imageInfo.getCount();
                    } else if (i < 15 && firstImageValue < imageInfo.getCount()) {
                        firstImageValue = imageInfo.getCount();
                    }

                    try {
                        calendar.setTime(sdf.parse(time));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    long diff = 0;
                    if (previousDate != 0) {
                        diff = (calendar.getTimeInMillis() - previousDate) / 1000 / 60;
                    }

                    previousDate = calendar.getTimeInMillis();

                    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.US);

                    totalDiff += diff;


                    if (!isTurbid) {
                        isTurbid = i > 15 && (firstImageValue < 50000 && (imageInfo.getCount() - firstImageValue) > 7700)
                                || (firstImageValue > 5000 && imageInfo.getCount() < 2000)
                                || (firstImageValue > 50000 && (imageInfo.getCount() - firstImageValue) > 6700)
                                || (firstImageValue > 10000 && firstImageValue < 50000 && imageInfo.getCount() < 4000)
                                || (firstImageValue < 5000 && firstImageValue > 3000 && imageInfo.getCount() < 3000);
                    }


                    if (!found && (isTurbid || i == imageInfos.size() - 1)) {

                        //if (confirmed > 2 || i == results.size() - 1) {

                        incubationImageName = imageInfo.getImageName();
                        //incubationTime = totalDiff;
                        timeAtBlurriness = totalDiff;
                        found = true;


//                        try {
//                            startColor = getColor(path + folder.getName() + File.separator + imageInfo.getImageName(), centerPoint);
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }

                        int hours = timeAtBlurriness / 60;
                        int minutes = timeAtBlurriness % 60;
                        incubationHours = String.format(Locale.US, "%d:%02d", hours, minutes);
                    }

//                    double distance = 0;
//                    try {
//                        endColor = getColor(path + folder.getName() + File.separator + imageInfo.getImageName(), centerPoint);
//                        int compareColor = Color.rgb(97, 58, 1);
//                        // distance = getColorDistanceRgb(startColor, endColor);
//                        distance = getColorDistanceRgb(compareColor, endColor);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
                    if (!colorChangeFound && ((found && isGreen(endColor, startColor, "himedia") && isTurbid) || i == imageInfos.size() - 1)) {

                        colorChangeFound = true;

                        int hours = totalDiff / 60;
                        int minutes = totalDiff % 60;
                        String colorChangeTime = String.format(Locale.US, "%d:%02d", hours, minutes);

                        html2.append("<tr>");
//                        html2.append("<td>").append(testNumber).append("</td>");
                        html2.append("<td>").append(dateFormat.format(reportCalendar.getTime())).append("</td>");
//                        html2.append("<td>").append(phone).append("</td>");
//                        html2.append("<td>").append(chamber).append("</td>");
//                        html2.append("<td>").append(media).append("</td>");
                        html2.append("<td>").append(type).append("</td>");
//                        html2.append("<td>").append(volume).append("</td>");
                        html2.append("<td>").append(beforeQty).append("</td>");
                        html2.append("<td class=\"nopadding\"><img style=\"width:100px;\" src=\"")
                                .append(folder.getName())
                                .append(File.separator).append(firstImageName).append("\"></td>");
                        if (isTurbid) {
                            html2.append("<td class=\"nopadding\"><img style=\"width:100px;\" src=\"")
                                    .append(folder.getName())
                                    .append(File.separator).append(incubationImageName).append("\"></td>");
                        } else {
                            html2.append("<td class=\"nopadding\" style=\"width:100px;\"></td>");
                        }

                        html2.append("<td class=\"nopadding\"><img style=\"width:100px;\" src=\"")
                                .append(folder.getName())
                                .append(File.separator).append(imageInfo.getImageName()).append("\"></td>");

                        if (i == imageInfos.size() - 1) {
                            if (isTurbid) {
                                html2.append("<td class=\"nopadding\"><div style=\"border-radius:50px;width:50px;height:50px;margin:auto;background:#" + getColorHexString(startColor) + "\">").append("").append("</div></td>");
                            } else {
                                html2.append("<td></td>");
                            }
                            //html2.append("<td class=\"nopadding\"><div style=\"border-radius:50px;width:50px;height:50px;margin:auto;background:#" + getColorHexString(endColor) + "\">").append("").append("</div></td>");
                            html2.append("<td></td>");
                            if (isTurbid) {
                                html2.append("<td>").append(incubationHours).append("</td>");
                            } else {
                                html2.append("<td>").append("-").append("</td>");
                            }
                        } else {
                            html2.append("<td class=\"nopadding\"><div style=\"border-radius:50px;width:50px;height:50px;margin:auto;background:#" + getColorHexString(startColor) + "\">").append("").append("</div></td>");
                            if (getColorDistanceRgb(startColor, endColor) > 10) {
                                html2.append("<td class=\"nopadding\"><div style=\"border-radius:50px;width:50px;height:50px;margin:auto;background:#" + getColorHexString(endColor) + "\">").append("").append("</div></td>");
                            } else {
                                html2.append("<td></td>");
                            }
                            html2.append("<td>").append(incubationHours).append("</td>");
                        }

                        html2.append("<td>").append(colorChangeTime).append("</td>");

                        if (isTurbid) {
                            resultInfo.result = "Present";
                            html2.append("<td style=\"color:#FF0000\">").append("Present").append("</td>");
                        } else {
                            if (firstImageValue < 100) {
                                resultInfo.result = "Unknown";
                                html2.append("<td style=\"color:#6d6d6d\">").append("Unknown").append("</td>");
                            } else if (fileCount < 15) {
                                resultInfo.result = "Incomplete";
                                html2.append("<td style=\"color:#555\">").append("Incomplete").append("</td>");
                            } else {
                                resultInfo.result = "Absent";
                                html2.append("<td style=\"color:#006633\">").append("Absent").append("</td>");
                            }
                        }
                        html2.append("</tr>");

                        resultInfo.version = ApiUtil.getAppVersionCode(this);
//                        resultInfo.testNumber = testNumber;
                        resultInfo.date = reportDate;
//                        resultInfo.media = media;
                        resultInfo.testType = type;
//                        resultInfo.volume = volume;
                        resultInfo.startImage = new File(folder, firstImageName).getPath();
                        resultInfo.turbidImage = new File(folder, incubationImageName).getPath();
                        resultInfo.endImage = new File(folder, imageInfo.getImageName()).getPath();
                        resultInfo.turbidTime = incubationHours;
                        resultInfo.totalTime = colorChangeTime;
                    }

                    Integer color = 0;
//                    try {
//                        color = getColor(path + folder.getName() + File.separator + imageInfo.getImageName(), centerPoint);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }

                    int hours = totalDiff / 60;
                    int minutes = totalDiff % 60;
                    String totalDiffHours = String.format(Locale.US, "%d:%02d", hours, minutes);


                    html.append("<tr>");
                    html.append("<td>").append(timeFormat.format(calendar.getTime())).append("</td>");
                    html.append("<td>").append(totalDiffHours).append("</td>");
                    //html.append("<td>").append(diff).append("</td>");
                    html.append("<td><div style=\"border-radius:50px;width:50px;height:50px;margin:auto;background:#" + getColorHexString(color) + "\">").append("").append("</div></td>");
                    html.append("<td><img style=\"width:100px;\" src=\"").append(imageInfo.getImageName()).append("\"></td>");
                    html.append("<td><img style=\"width:74px;\" src=\"input/").append(imageInfo.getImageName()).append("\"></td>");
                    html.append("<td><img style=\"width:74px;\" src=\"output/").append(imageInfo.getImageName()).append("\"></td>");
                    html.append("<td>").append(imageInfo.getCount()).append("</td>");
                    html.append("<td>").append(firstImageValue - imageInfo.getCount()).append("</td>");

                    if (found) {
                        html.append("<td style=\"background-color:#006633\">");
                    } else {
                        html.append("<td>");
                    }
                    html.append("");
                    html.append("</td>");
                    html.append("</tr>");

                    data.append("[").append(totalDiff).append(",").append(0).append(",").append(0).append(",").append(0).append("],");

                }

                int hours = totalTime / 60;
                int minutes = totalTime % 60;

                html.append("<tfoot><tr><td>").append(String.format(Locale.US, "%d:%02d", hours, minutes)).append("</td><td></td><td></td><td></td><td></td><td></td><td></td><td></td></tr></tfoot>");
                html.append("</table></body></html>");

                saveToFile(folder, "report.html", scriptStart + data.toString() + scriptEnd + style + reportTitle + html.toString());
            }


            writeResult(resultInfo, new File(folder, "result"));
        } catch (Exception ignored) {
            return null;
        }
        return resultInfo;
    }

    public ResultInfo readResult(File file) {
        ObjectInputStream input;

        try {
            input = new ObjectInputStream(new FileInputStream(file));
            ResultInfo resultInfo = (ResultInfo) input.readObject();
            input.close();

            return resultInfo;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void writeResult(Serializable serializable, File file) {
        ObjectOutput out;

        try {
            out = new ObjectOutputStream(new FileOutputStream(file));
            out.writeObject(serializable);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ImageInfo {

        private String imageName;
        private int count;

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        String getImageName() {
            return imageName;
        }

        void setImageName(String imageName) {
            this.imageName = imageName;
        }

    }

    private class AnalyzeTask extends AsyncTask<String, Void, ResultInfo> {

        private final File folder;

        AnalyzeTask(File folder) {
            this.folder = folder;
        }

        @Override
        protected ResultInfo doInBackground(String... strings) {
            return analyzeFiles(folder);
        }

        protected void onPostExecute(ResultInfo resultInfo) {
            if (resultInfo != null) {
                startAnalysis();
            }
        }
    }

}