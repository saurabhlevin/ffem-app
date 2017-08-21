/*
 * Copyright (C) Stichting Akvo (Akvo Foundation)
 *
 * This file is part of Akvo Caddisfly.
 *
 * Akvo Caddisfly is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Akvo Caddisfly is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Akvo Caddisfly. If not, see <http://www.gnu.org/licenses/>.
 */

package org.akvo.caddisfly.sensor.colorimetry.liquid;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.CaddisflyApp;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.ui.BaseActivity;
import org.akvo.caddisfly.util.PreferencesUtil;

import java.util.Locale;

public class SelectDilutionActivity extends BaseActivity implements EditCustomDilution.OnFragmentInteractionListener {
    private static final int REQUEST_TEST = 1;
    EditCustomDilution editCustomDilution;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_dilution);

        setTitle(R.string.dilution);

        Button noDilutionButton = findViewById(R.id.buttonNoDilution);
        Button percentButton1 = findViewById(R.id.buttonDilution1);
        Button percentButton2 = findViewById(R.id.buttonDilution2);
        Button buttonCustomDilution = findViewById(R.id.buttonCustomDilution);

        //todo: remove hardcoding of dilution times
        percentButton1.setText(String.format(Locale.getDefault(), getString(R.string.timesDilution), 2));
        percentButton2.setText(String.format(Locale.getDefault(), getString(R.string.timesDilution), 5));

        noDilutionButton.setOnClickListener(view -> startTest(1));

        percentButton1.setOnClickListener(view -> startTest(2));

        percentButton2.setOnClickListener(view -> startTest(5));

        buttonCustomDilution.setOnClickListener(view -> showCustomDilutionDialog());

        TestInfo testInfo = CaddisflyApp.getApp().getCurrentTestInfo();
        ((TextView) findViewById(R.id.textTitle)).setText(testInfo.getName());
    }

    private void showCustomDilutionDialog() {
        final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        editCustomDilution = EditCustomDilution.newInstance();
        editCustomDilution.show(ft, "editCustomDilution");
    }


    private void startTest(int dilution) {
        final Intent intent = new Intent(getIntent());
        intent.setClass(getBaseContext(), ColorimetryLiquidActivity.class);
        intent.putExtra("dilution", dilution);
        startActivityForResult(intent, REQUEST_TEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_TEST:
                if (resultCode == Activity.RESULT_OK) {
                    Intent intent = new Intent(data);
                    this.setResult(Activity.RESULT_OK, intent);
                    finish();
                }
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFragmentInteraction(Integer value) {
        PreferencesUtil.setInt(this, R.string.customDilutionKey, value);
        startTest(value);
    }
}
