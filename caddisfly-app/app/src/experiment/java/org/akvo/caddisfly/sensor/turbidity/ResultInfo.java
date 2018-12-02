package org.akvo.caddisfly.sensor.turbidity;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ResultInfo implements Serializable {
    public final String id;
    public int testNumber;
    final SimpleDateFormat dateFormat = new SimpleDateFormat("dd, MMM yyyy, HH:mm");
    public String date;
    public String media;
    public String testType;
    public String volume;
    public String startImage;
    public String turbidImage;
    public String endImage;
    public String turbidTime;
    public String totalTime;
    public String result;
    public Date testDate;
    public int version;
    public boolean resultBoolean;
    public String folder;
    SimpleDateFormat sourceDateFormat = new SimpleDateFormat("yyyyMMddHHmm", Locale.US);

    public ResultInfo(String id) {
        this.id = id;
    }

    public void setDate(String value) {

        Calendar calendar = Calendar.getInstance();

        try {
            testDate = sourceDateFormat.parse(value);
            calendar.setTime(testDate);
            date = dateFormat.format(calendar.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public Date getTestDate() {
        return testDate;
    }
}
