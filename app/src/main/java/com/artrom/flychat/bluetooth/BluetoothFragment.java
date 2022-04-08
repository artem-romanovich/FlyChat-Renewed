package com.artrom.flychat.bluetooth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.artrom.flychat.R;

public class BluetoothFragment extends Fragment {

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_bluetooth, container, false);

        /*ViewPager viewPager = root.findViewById(R.id.viewpager);
        viewPager.setAdapter(new SampleFragmentPagerAdapter(requireActivity().getSupportFragmentManager(), requireContext()));

        TabLayout tabLayout = root.findViewById(R.id.sliding_tabs);
        tabLayout.setupWithViewPager(viewPager);*/

        return root;
    }
}