/*
 *  Copyright (C) Stichting Akvo (Akvo Foundation)
 *
 *  This file is part of Akvo Caddisfly
 *
 *  Akvo Caddisfly is free software: you can redistribute it and modify it under the terms of
 *  the GNU Affero General Public License (AGPL) as published by the Free Software Foundation,
 *  either version 3 of the License or any later version.
 *
 *  Akvo Caddisfly is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU Affero General Public License included below for more details.
 *
 *  The full license text can also be seen at <http://www.gnu.org/licenses/agpl.html>.
 */

package org.akvo.caddisfly.ui;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.adapter.SwatchesAdapter;
import org.akvo.caddisfly.app.CaddisflyApp;
import org.akvo.caddisfly.model.Swatch;
import org.akvo.caddisfly.util.PreferencesUtils;

import java.util.ArrayList;

@SuppressWarnings("WeakerAccess")
public class SwatchFragment extends ListFragment {

    public SwatchFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_swatch, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getActivity().setTitle(R.string.swatches);

        CaddisflyApp caddisflyApp = CaddisflyApp.getApp();

        if (caddisflyApp != null) {
            ArrayList<Swatch> swatchList = new ArrayList<>();

            if (caddisflyApp.currentTestInfo.getRanges().size() > 0) {
                int startValue = (int) (caddisflyApp.currentTestInfo.getRange(0).getValue() * 10);
                int endValue = (int) (caddisflyApp.currentTestInfo.getRange(caddisflyApp.currentTestInfo.getRanges().size() - 1).getValue() * 10);
                for (int i = startValue; i <= endValue; i += 1) {
                    String key = String.format("%s-%.2f", caddisflyApp.currentTestInfo.getCode(), (i / 10f));
                    Swatch range = new Swatch((double) i / 10,
                            PreferencesUtils.getInt(getActivity(), key, 0));
                    swatchList.add(range);
                }
                Swatch[] colorArray = swatchList.toArray(new Swatch[swatchList.size()]);

                SwatchesAdapter swatchesAdapter = new SwatchesAdapter(getActivity(), colorArray);
                setListAdapter(swatchesAdapter);
            }
        }

    }
}
