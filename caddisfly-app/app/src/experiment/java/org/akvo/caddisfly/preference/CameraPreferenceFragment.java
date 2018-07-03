package org.akvo.caddisfly.preference;

import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.util.ListViewUtil;
import org.akvo.caddisfly.util.PreferencesUtil;

import io.ffem.experiment.ResolutionListPreference;

public class CameraPreferenceFragment extends PreferenceFragment {

    private ListView list;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_camera);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.card_row, container, false);
        view.setBackgroundColor(Color.rgb(255, 240, 220));

        setupZoomPreference();

        setupOffsetPreference();

        setupCameraPreference();

        return view;
    }

    private void setupOffsetPreference() {
        final SeekBarPreference seekBarPreference =
                (SeekBarPreference) findPreference(getString(R.string.cameraCenterOffsetKey));

        if (seekBarPreference != null) {
            int offset = PreferenceManager.getDefaultSharedPreferences(this.getActivity())
                    .getInt(getString(R.string.cameraCenterOffsetKey), 0);

            seekBarPreference.setSummary(String.valueOf(offset));

            seekBarPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                seekBarPreference.setSummary(String.valueOf(newValue));
                return false;
            });
        }
    }

    private void setupZoomPreference() {

        final SeekBarPreference seekBarPreference =
                (SeekBarPreference) findPreference(getString(R.string.cameraZoomPercentKey));

        if (seekBarPreference != null) {
            int zoomValue = PreferenceManager.getDefaultSharedPreferences(this.getActivity())
                    .getInt(getString(R.string.cameraZoomPercentKey), 0);

            seekBarPreference.setSummary(String.valueOf(zoomValue));

            seekBarPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                seekBarPreference.setSummary(String.valueOf(newValue));
                return false;
            });
        }
    }

    private void setupCameraPreference() {
        final ResolutionListPreference resolutionListPreference =
                (ResolutionListPreference) findPreference(getString(R.string.cameraResolutionKey));

        if (resolutionListPreference != null) {
            String resolution = PreferenceManager.getDefaultSharedPreferences(this.getActivity())
                    .getString(getString(R.string.cameraResolutionKey), "640-480");

            resolutionListPreference.setSummary(String.valueOf(resolution));

            resolutionListPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                resolutionListPreference.setSummary(String.valueOf(newValue));
                PreferencesUtil.setString(getActivity(), R.string.cameraResolutionKey, newValue.toString());
                return false;
            });
        }
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
}
