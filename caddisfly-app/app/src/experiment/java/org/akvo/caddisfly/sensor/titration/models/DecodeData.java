package org.akvo.caddisfly.sensor.titration.models;

import android.media.Image;

import org.akvo.caddisfly.helper.FileHelper;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.sensor.qrdetector.FinderPattern;
import org.akvo.caddisfly.sensor.qrdetector.FinderPatternInfo;
import org.akvo.caddisfly.sensor.qrdetector.PerspectiveTransform;
import org.akvo.caddisfly.util.ImageUtil;
import org.apache.commons.math3.linear.RealMatrix;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.akvo.caddisfly.sensor.titration.decode.DecodeProcessor.NO_TILT;

public class DecodeData {

//    private static final String TAG = "Caddisfly-DecodeData";

    private Image decodeImage;
    private byte[] decodeImageByteArray;
    private int decodeWidth;
    private int decodeHeight;
    private int previewWidth;
    private int previewHeight;
    private FinderPatternInfo patternInfo;
    private List<FinderPattern> finderPatternsFound;
    private int tilt = NO_TILT;
    private boolean distanceOk;
    private PerspectiveTransform cardToImageTransform;
    private List<float[]> shadowPoints;
    private float[][] whitePointArray;
    private float percentageShadow;
    private float[] deltaEStats;
    private Map<Integer, Integer> versionNumberMap;
    private Map<String, int[]> measuredPatchRGB;
    private Map<String, int[]> calibrationPatchRGB;
    private float[] illumData;
    private RealMatrix calMatrix;
    private Map<Integer, int[]> stripImageMap;
    private int stripPixelWidth;
    private TestInfo testInfo;

    public DecodeData() {
        this.versionNumberMap = new HashMap<>();
        this.stripImageMap = new HashMap<>();
    }

    public void addStripImage(int[] image, int delay) {
        this.stripImageMap.put(delay, image);
    }

    public Map<Integer, int[]> getStripImageMap() {
        return this.stripImageMap;
    }

    //put version number in array: number, frequency
    public void addVersionNumber(Integer number) {
        Integer existingFrequency = versionNumberMap.get(number);
        if (existingFrequency != null) {
            versionNumberMap.put(number, versionNumberMap.get(number) + 1);
        } else {
            versionNumberMap.put(number, 1);
        }
    }

    public int getMostFrequentVersionNumber() {
        int mostFrequent = 0;
        int largestValue = -1;

        //look for the most frequent value
        for (Integer key : versionNumberMap.keySet()) {
            int freq = versionNumberMap.get(key);
            if (freq > mostFrequent) {
                mostFrequent = freq;
                largestValue = key;
            }
        }
        return largestValue;
    }

    public int getDecodeWidth() {
        return decodeWidth;
    }

    public void setDecodeWidth(int decodeWidth) {
        this.decodeWidth = decodeWidth;
    }

    public int getDecodeHeight() {
        return decodeHeight;
    }

    public void setDecodeHeight(int decodeHeight) {
        this.decodeHeight = decodeHeight;
    }

    public List<FinderPattern> getFinderPatternsFound() {
        return finderPatternsFound;
    }

    public void setFinderPatternsFound(List<FinderPattern> finderPatternsFound) {
        this.finderPatternsFound = finderPatternsFound;
    }

    public Image getDecodeImage() {
        return decodeImage;
    }

    public void setDecodeImage(Image decodeImage) {
        this.decodeImage = decodeImage;
    }

    public FinderPatternInfo getPatternInfo() {
        return patternInfo;
    }

    public void setPatternInfo(FinderPatternInfo patternInfo) {
        this.patternInfo = patternInfo;
    }

    public int getTilt() {
        return tilt;
    }

    public void setTilt(int tilt) {
        this.tilt = tilt;
    }

    public PerspectiveTransform getCardToImageTransform() {
        return cardToImageTransform;
    }

    public void setCardToImageTransform(PerspectiveTransform cardToImageTransform) {
        this.cardToImageTransform = cardToImageTransform;
    }

    public List<float[]> getShadowPoints() {
        return shadowPoints;
    }

    public void setShadowPoints(List<float[]> shadowPoints) {
        this.shadowPoints = shadowPoints;
    }

    public float[][] getWhitePointArray() {
        return whitePointArray;
    }

    public void setWhitePointArray(float[][] whitePointArray) {
        this.whitePointArray = whitePointArray;
    }

    public float getPercentageShadow() {
        return percentageShadow;
    }

    public void setPercentageShadow(float percentageShadow) {
        this.percentageShadow = percentageShadow;
    }

    public float[] getDeltaEStats() {
        return deltaEStats;
    }

    public void setDeltaEStats(float[] deltaE2000Stats) {
        this.deltaEStats = deltaE2000Stats;
    }

    public Map<String, int[]> getMeasuredPatchRGB() {
        return measuredPatchRGB;
    }

    public void setMeasuredPatchRGB(Map<String, int[]> measuredPatchRGB) {
        this.measuredPatchRGB = measuredPatchRGB;
    }

    public Map<String, int[]> getCalibrationPatchRGB() {
        return calibrationPatchRGB;
    }

    public void setCalibrationPatchRGB(Map<String, int[]> calibrationPatchRGB) {
        this.calibrationPatchRGB = calibrationPatchRGB;
    }

    public byte[] getDecodeImageByteArray() {
        return decodeImageByteArray;
    }

    public void setDecodeImageByteArray(byte[] decodeImageByteArray) {
        this.decodeImageByteArray = decodeImageByteArray;
    }

    public int getPreviewWidth() {
        return previewWidth;
    }

    public void setPreviewWidth(int previewWidth) {
        this.previewWidth = previewWidth;
    }

    public int getPreviewHeight() {
        return previewHeight;
    }

    public void setPreviewHeight(int previewHeight) {
        this.previewHeight = previewHeight;
    }

    public boolean getDistanceOk() {
        return distanceOk;
    }

    public void setDistanceOk(boolean distanceOk) {
        this.distanceOk = distanceOk;
    }

    public float[] getIllumData() {
        return illumData;
    }

    public void setIllumData(float[] illumData) {
        this.illumData = illumData;
    }


    public RealMatrix getCalMatrix() {
        return calMatrix;
    }

    public void setCalMatrix(RealMatrix calMatrix) {
        this.calMatrix = calMatrix;
    }

    public TestInfo getTestInfo() {
        return testInfo;
    }

    public void setTestInfo(TestInfo testInfo) {
        this.testInfo = testInfo;
    }

    public int getStripPixelWidth() {
        return stripPixelWidth;
    }

    public void setStripPixelWidth(int stripPixelWidth) {
        this.stripPixelWidth = stripPixelWidth;
    }

    public void clearData() {
        if (this.decodeImage != null) {
            this.decodeImage.close();
        }
        this.decodeImage = null;
        this.patternInfo = null;
        this.decodeImageByteArray = null;
        this.shadowPoints = null;
        this.measuredPatchRGB = null;
        this.calibrationPatchRGB = null;
        this.tilt = NO_TILT;
        this.distanceOk = true;
        calMatrix = null;
        illumData = null;
    }

    //todo remove debug code
    public void saveImage() {
        ImageUtil.saveImage(decodeImageByteArray, FileHelper.FileType.TEST_IMAGE,
                String.valueOf(Calendar.getInstance().getTimeInMillis()));
    }
}
