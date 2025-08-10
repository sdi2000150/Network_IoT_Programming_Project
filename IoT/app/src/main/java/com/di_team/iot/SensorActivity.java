package com.di_team.iot;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.di_team.iot.sensor.SensorConfig;
import com.di_team.iot.sensor.SensorStateManager;
import com.di_team.iot.sensor.SensorType;

import com.google.android.material.tabs.TabLayout;
import androidx.viewpager2.widget.ViewPager2;

import java.util.List;

public class SensorActivity extends BaseActivity {

    private ViewPager2 viewPager;   // Widget used to swipe between fragments
    private TabLayout tabLayout;    // UI component that allows users to switch between different tabs,
    private SensorStateManager sensorStateManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sensor_activity);
        sensorStateManager = SensorStateManager.getInstance();

        // Set the title of the activity (this will change the title at the top)
        setTitle("Available Sensors Tabs");

        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);

        // Get the sensor type from the intent
        SensorType sensorType = (SensorType) getIntent().getSerializableExtra("SENSOR_TYPE");

        // Handle case where no extra was passed (normally impossible)
        if (sensorType == null) {
            sensorType = SensorType.Gas;
        }

        // Set up the adapter for the ViewPager2
        SensorAdapter sensorAdapter = new SensorAdapter(this, sensorStateManager.getSensorConfigs());
        viewPager.setAdapter(sensorAdapter);

        // Link TabLayout and ViewPager2
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        // Link ViewPager2 to TabLayout
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                tabLayout.selectTab(tabLayout.getTabAt(position));
            }
        });

        // Set the initial tab based on the passed sensor type
        viewPager.setCurrentItem(sensorType.ordinal());
    }


    /** Adapter to handle different fragments (tabs) for each sensor**/
    public static class SensorAdapter extends FragmentStateAdapter {
        private final List<SensorConfig> sensorConfigs; //list of currently existing sensors
        public SensorAdapter(@NonNull AppCompatActivity activity, List<SensorConfig> sensorConfigs) {
            super(activity);
            this.sensorConfigs = sensorConfigs;
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            //Return a new fragment for each position, using the corresponding type ordinal
            SensorType type = SensorType.values()[position];
            return SensorFragment.newInstance(type);
        }

        @Override
        public int getItemCount() {
            // number of tabs corresponds to number of existing sensors
            return sensorConfigs.size();
        }
    }
}
