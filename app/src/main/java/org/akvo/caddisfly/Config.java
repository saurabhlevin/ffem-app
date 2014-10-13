package org.akvo.caddisfly;

/*
Global Configuration
 */
public class Config {

    // For external app connection
    public static final String FLOW_ACTION_EXTERNAL_SOURCE = "org.akvo.flow.action.externalsource";

    // Used to check if flow app is installed
    public static final String FLOW_SURVEY_PACKAGE_NAME = "com.gallatinsystems.survey.device";

    // Caddisfly update file name
    public static final String UPDATE_FILE_NAME = "akvo_caddisfly_update.apk";

    // Caddisfly update check path
    public static final String UPDATE_CHECK_URL
            = "http://caddisfly.ternup.com/akvoapp/v.txt?check=41";

    // Caddisfly update path
    public static final String UPDATE_URL
            = "http://caddisfly.ternup.com/akvoapp/akvo_caddisfly_update.apk?check=41";

    public static final String DEFAULT_LOCALE = "en";

    // Link to product web site for about information
    public static final String PRODUCT_WEBSITE = "http://caddisfly.ternup.com";

    // Link to company web site for about information
    public static final String ORG_WEBSITE = "http://akvo.org";

    public static final String CADDISFLY_PACKAGE_NAME = "com.ternup.caddisfly";

    public static final String CALIBRATE_FOLDER_NAME = "/com.ternup.caddisfly/calibrate/";

    // Tag for debug log filtering
    public static final String DEBUG_TAG = "Caddisfly";

    // new folder name using date
    public static final String FOLDER_NAME_DATE_FORMAT = "yyyyMMddHHmmss";

    // Index of screens that gets displayed in the app
    public static final int HOME_SCREEN_INDEX = 0;

    public static final int SETTINGS_SCREEN_INDEX = 1;

    public static final int CALIBRATE_SCREEN_INDEX = 2;

    public static final int CHECKLIST_SCREEN_INDEX = 4;

    // Index of test types
    public static final int FLUORIDE_INDEX = 0;

    public static final int FLUORIDE_2_INDEX = 1;

    // width and height of cropped image
    public static final int IMAGE_CROP_LENGTH = 300;

    // folder for calibration photos
    public static final String CALIBRATE_FOLDER = "calibrate";

    public static final int INITIAL_DELAY = 4000;

    public static final int MINIMUM_PHOTO_QUALITY = 50;

    public static final int SAMPLING_COUNT_DEFAULT = 6;

    // width and height of cropped image
    public static final int SAMPLE_CROP_LENGTH_DEFAULT = 50;

    public static final String RESULT_VALUE_KEY = "resultValue";

    public static final String RESULT_COLOR_KEY = "resultColor";

    public static final String QUALITY_KEY = "accuracy";
    public static final int ERROR_NOT_YET_CALIBRATED = 1;
    public static final int ERROR_LOW_QUALITY = 2;
    public static final int ERROR_DUPLICATE_SWATCH = 3;
    public static final int ERROR_SWATCH_OUT_OF_PLACE = 4;
    public static final int ERROR_OUT_OF_RANGE = 5;
    public static final int ERROR_COLOR_IS_GRAY = 6;

    public static final boolean CAMERA_TORCH_MODE_DEFAULT = false;
    public static final boolean REQUIRE_SHAKE_DEFAULT = true;
    public static final boolean CAMERA_SOUND_DEFAULT = true;

}
