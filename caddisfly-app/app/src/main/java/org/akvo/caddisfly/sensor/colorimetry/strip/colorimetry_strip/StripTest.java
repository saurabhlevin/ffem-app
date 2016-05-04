package org.akvo.caddisfly.sensor.colorimetry.strip.colorimetry_strip;

import android.content.Context;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.CaddisflyApp;
import org.akvo.caddisfly.sensor.colorimetry.strip.util.App;
import org.akvo.caddisfly.sensor.colorimetry.strip.util.AssetsManager;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by linda on 8/19/15.
 */
public class StripTest{

    public StripTest()
    {

    }

    public List<String> getBrandsAsList()
    {
        List<String> brandnames = new ArrayList<>();

        String json = fromJson();
        try {
            JSONObject object = new JSONObject(json);

            if (!object.isNull("strips")) {
                JSONArray stripsJson = object.getJSONArray("strips");
                JSONObject strip;
                if (stripsJson != null) {
                    for (int i = 0; i < stripsJson.length(); i++) {

                        strip = stripsJson.getJSONObject(i);
                        brandnames.add(strip.getString("brand"));

                    }
                }
            }
        }
        catch (Exception e)
        {
            return null;
        }
        return brandnames;
    }

    /*
    * Matches a test UUID to a brand name
    * if not found, returns null
     */
    public String matchUuidToBrand(String uuid){
        String json = fromJson();
        String result = null;
        try {
            JSONObject object = new JSONObject(json);

            if (!object.isNull("strips")) {
                JSONArray stripsJson = object.getJSONArray("strips");
                JSONObject strip;
                if (stripsJson != null) {
                    for (int i = 0; i < stripsJson.length(); i++) {
                        strip = stripsJson.getJSONObject(i);
                        if (strip.getString("uuid").equals(uuid)){
                            result = strip.getString("brand");
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            return result;
        }
        return result;
    }

    public Brand getBrand(String brand)
    {
        return new Brand(brand);
    }

    public enum groupType {GROUP, INDIVIDUAL};

    public class Brand
    {
        private String name;
        private String uuid;
        private double stripLenght;
        private double stripHeight;
        private groupType groupingType;
        private List<Patch> patches = new ArrayList<>();
        private JSONArray instructions;

        public Brand(String brand) {

            System.out.println("***Striptest brand: " + brand);
            try {
                // read the json file with strip information from assets
                String json = fromJson();
                JSONObject object = new JSONObject(json);

                if (!object.isNull("strips")) {
                    JSONArray stripsJson = object.getJSONArray("strips");
                    JSONObject strip;

                    if (stripsJson != null) {
                        for (int i = 0; i < stripsJson.length(); i++) {
                            strip = stripsJson.getJSONObject(i);

                            if (!strip.getString("brand").equalsIgnoreCase(brand)) {
                                continue;
                            } else {
                                try {
                                    this.uuid = strip.getString("uuid");
                                    this.stripLenght = strip.getDouble("length");
                                    this.stripHeight = strip.getDouble("height");
                                    this.groupingType = strip.getString("groupingType").equals(groupType.GROUP.toString()) ? groupType.GROUP : groupType.INDIVIDUAL;
                                    this.name = strip.getString("name");

                                    JSONArray patchesArr = strip.getJSONArray("patches");
                                    for (int ii = 0; ii < patchesArr.length(); ii++) {

                                        JSONObject patchObj = patchesArr.getJSONObject(ii);

                                        String patchDesc = patchObj.getString("patchDesc");
                                        int patchPos = patchObj.getInt("patchPos");
                                        int id = patchObj.getInt("id");
                                        int patchWidth = patchObj.getInt("patchWidth");
                                        double timeLapse = patchObj.getDouble("timeLapse");
                                        String unit = patchObj.getString("unit");
                                        JSONArray colours = patchObj.getJSONArray("colours");

                                        patches.add(new Patch(id, patchDesc, patchWidth, 0, patchPos, timeLapse, unit, colours));
                                    }

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }

                //add instructions
                String instructions = instructionsFromJson();
                JSONObject instructionObj = new JSONObject(instructions);
                JSONArray stripsJson = instructionObj.getJSONArray("strips");
                JSONObject strip;

                if (stripsJson != null) {

                    for (int i = 0; i < stripsJson.length(); i++) {

                        strip = stripsJson.getJSONObject(i);

                        //System.out.println("***Striptest brand = " + i + " = " + strip.getString("brand"));

                        if (!strip.getString("brand").equalsIgnoreCase(brand)) {
                            continue;
                        } else {
                            this.instructions = strip.getJSONArray("instructions");

                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public List<Patch> getPatches() {
            return patches;
        }

//        public double getStripHeight() {
//            return stripHeight;
//        }

        public groupType getGroupingType() {
            return groupingType;
        }

        public double getStripLenght() {
            return stripLenght;
        }

        public String getName() {
            return name;
        }

        public String getUuid() {
            return uuid;
        }

        public JSONArray getInstructions() {
            return instructions;
        }


        public List<Patch> getPatchesOrderedByTimelapse()
        {
            Collections.sort(patches, new PatchComparator());
            return patches;
        }

        public class Patch {
            int id;
            String desc;
            double width; //mm
            double height;//mm
            double position;//x in mm
            double timeLapse; //seconds between this and previous patch
            String unit;

            JSONArray colours;

            public Patch(int id, String desc, double width, double height, double position,
                         double timeLapse, String unit, JSONArray colours) {
                this.id = id;
                this.desc = desc;
                this.width = width;
                this.height = height;
                this.position = position;
                this.timeLapse = timeLapse;
                this.unit = unit;
                this.colours = colours;
            }

            public int getId() {
                return id;
            }

            public String getDesc() {
                return desc;
            }

            public double getPosition() {
                return position;
            }

            public JSONArray getColours() {
                return colours;
            }

            public double getTimeLapse() {
                return timeLapse;
            }

            public String getUnit() {
                return unit;
            }

        }
    }

    private String fromJson()
    {
        String filename = CaddisflyApp.getApp().getApplicationContext().getString(R.string.strips_json);

        return AssetsManager.getInstance().loadJSONFromAsset(filename);

    }

    private String instructionsFromJson()
    {
        String filename = CaddisflyApp.getApp().getApplicationContext().getString(R.string.strips_instruction_json);
        return AssetsManager.getInstance().loadJSONFromAsset(filename);

    }
    private class PatchComparator implements Comparator<Brand.Patch>
    {

        @Override
        public int compare(Brand.Patch lhs, Brand.Patch rhs) {
            if (lhs.timeLapse < rhs.timeLapse)
                return -1;
            if(lhs.timeLapse == rhs.timeLapse)
                return 0;

            return 1;
        }
    }


}
