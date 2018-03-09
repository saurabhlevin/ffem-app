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

import android.app.Fragment;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.common.ConstantKey;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.sensor.titration.TitrationTestActivity;
import org.akvo.caddisfly.util.ListViewUtil;
import org.akvo.caddisfly.viewmodel.TestListViewModel;

/**
 * A simple {@link Fragment} subclass.
 */
public class DebuggingPreferenceFragment extends PreferenceFragment {

    private ListView list;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_debugging);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.card_row, container, false);
        view.setBackgroundColor(Color.rgb(255, 240, 220));

        setupTitrationPreference();
        return view;
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

    private void setupTitrationPreference() {
        final Preference titrationPreference = findPreference("titration");
        if (titrationPreference != null) {
            titrationPreference.setOnPreferenceClickListener(preference -> {
                Intent intent = new Intent(getActivity(), TitrationTestActivity.class);

                final TestListViewModel viewModel =
                        ViewModelProviders.of((FragmentActivity) getActivity()).get(TestListViewModel.class);
                TestInfo testInfo = viewModel.getTestInfo("52ec4ca0-d691-4f2b-b17a-232c2966974a");

                intent.putExtra(ConstantKey.TEST_INFO, testInfo);
                startActivity(intent);
                return true;
            });
        }
    }
}
