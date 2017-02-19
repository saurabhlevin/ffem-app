package org.akvo.caddisfly.sensor.turbidity;

import java.io.Serializable;

/**
 * Created by n on 27/11/16.
 */
public class ResultInfo implements Serializable {
    public int testNumber;
    public String date;
    public String phone;
    public String chamber;
    public String media;
    public String testType;
    public String volume;
    public String startImage;
    public String turbidImage;
    public String endImage;
    public String turbidTime;
    public String totalTime;
    public String result;
    public int version;

}
