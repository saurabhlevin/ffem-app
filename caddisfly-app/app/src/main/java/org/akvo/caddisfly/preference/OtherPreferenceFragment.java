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

package org.akvo.caddisfly.preference;


import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.CaddisflyApp;
import org.akvo.caddisfly.entity.Calibration;
import org.akvo.caddisfly.helper.SwatchHelper;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.model.TestType;
import org.akvo.caddisfly.ui.AboutActivity;
import org.akvo.caddisfly.util.ListViewUtil;
import org.akvo.caddisfly.viewmodel.TestListViewModel;

import java.util.List;

public class OtherPreferenceFragment extends PreferenceFragment {

    Uri URI = null;
    private ListView list;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_other);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.card_row, container, false);

        Preference aboutPreference = findPreference("about");
        if (aboutPreference != null) {
            aboutPreference.setSummary(CaddisflyApp.getAppVersion(AppPreferences.isDiagnosticMode()));
            aboutPreference.setOnPreferenceClickListener(preference -> {
                final Intent intent = new Intent(getActivity(), AboutActivity.class);
                getActivity().startActivity(intent);
                return true;
            });
        }

        StringBuilder message = new StringBuilder();

        Preference emailSupportPreference = findPreference("emailSupport");
        if (emailSupportPreference != null) {
            emailSupportPreference.setSummary("Send details to support for troubleshooting");
            emailSupportPreference.setOnPreferenceClickListener(preference -> {

                final TestListViewModel viewModel =
                        ViewModelProviders.of((FragmentActivity) getActivity()).get(TestListViewModel.class);

                List<TestInfo> testList = viewModel.getTests(TestType.CHAMBER_TEST);

                for (TestInfo testInfo : testList) {

                    if (testInfo.getIsGroup()) {
                        continue;
                    }

                    testInfo = viewModel.getTestInfo(testInfo.getUuid());

                    boolean calibrated = false;
                    for (Calibration calibration :
                            testInfo.getCalibrations()) {
                        if (calibration.color != Color.TRANSPARENT &&
                                calibration.color != Color.BLACK) {
                            calibrated = true;
                            break;
                        }
                    }

                    if (calibrated) {
                        message.append(SwatchHelper.generateCalibrationFile(getActivity(), testInfo, false));

                        message.append("\n");

                        message.append("-------------------------------------------------");

                        message.append("\n");
                    }
                }

                sendEmail(getActivity(), message.toString());
                return true;
            });
        }


        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        list = view.findViewById(android.R.id.list);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ListViewUtil.setListViewHeightBasedOnChildren(list, 0);
    }

    public void sendEmail(Context context, String message) {
        try {
            String email = "devices@ternup.com";
            String subject = "Support request";
            final Intent emailIntent = new Intent(Intent.ACTION_SEND);
            emailIntent.setType("plain/text");
            emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{email});
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
            if (URI != null) {
                emailIntent.putExtra(Intent.EXTRA_STREAM, URI);
            }
            emailIntent.putExtra(Intent.EXTRA_TEXT, message);
            this.startActivity(Intent.createChooser(emailIntent, "Sending email..."));
        } catch (Throwable t) {
            Toast.makeText(context, "Request failed try again: " + t.toString(), Toast.LENGTH_LONG).show();
        }
    }
}
