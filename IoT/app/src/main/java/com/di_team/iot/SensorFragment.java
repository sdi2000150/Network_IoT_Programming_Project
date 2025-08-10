package com.di_team.iot;

import android.annotation.SuppressLint;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.di_team.iot.sensor.SensorConfig;
import com.di_team.iot.sensor.SensorStateManager;
import com.di_team.iot.sensor.SensorType;
import com.google.android.material.slider.Slider;

/**Generic fragment for each sensor type.*/
public class SensorFragment extends Fragment {
    private static final String ARG_SENSOR_TYPE = "SENSOR_TYPE";
    private Slider slider;
    private SensorStateManager sensorStateManager;
    private SensorType sensorType;
    private TextView title, description, sliderLabel, switchLabel;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch activeSwitch;

    /**Instantiates a new SensorFragment for the given sensor type.*/
    public static SensorFragment newInstance(SensorType sensorType) {
        SensorFragment fragment = new SensorFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_SENSOR_TYPE, sensorType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            sensorType = (SensorType) getArguments().getSerializable(ARG_SENSOR_TYPE);
        }
        sensorStateManager = SensorStateManager.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sensor, container, false);
        slider = view.findViewById(R.id.SensorSlider);
        sliderLabel = view.findViewById(R.id.SensorSliderLabel);
        title = view.findViewById(R.id.SensorTitle);
        description = view.findViewById(R.id.SensorDescription);
        activeSwitch = view.findViewById(R.id.SensorSwitch);
        switchLabel = view.findViewById(R.id.SensorSwitchLabel);

        initText();
        initSlider();
        initSwitch();

        return view;
    }

    /**Initializes the fragment's text components based on the sensor's type.*/
    private void initText()
    {
        Log.d("SensorFragment", "Sensor type: " + sensorType.toString());
        switch (sensorType) {
            case Gas:
                title.setText(R.string.gas_frag_title);
                description.setText(R.string.gas_frag_desc);
                break;
            case Smoke:
                title.setText(R.string.smoke_frag_title);
                description.setText(R.string.smoke_frag_desc);
                break;
            case Temperature:
                title.setText(R.string.temp_frag_title);
                description.setText(R.string.temp_frag_desc);
                break;
            default:
                title.setText(R.string.UV_frag_title);
                description.setText(R.string.UV_frag_desc);
        }
    }

    /**Initializes tha limits and value of slider based on the sensor's configuration.*/
    private void initSlider(){
        SensorConfig config = sensorStateManager.getConfig(sensorType);
        if(config != null) {
            slider.setValueFrom(config.getMinValue());
            slider.setValueTo(config.getMaxValue());
            slider.setValue(config.getProgress());

            //Map sensor type to color
            int sliderColor = switch (sensorType) {
                case Gas -> Color.BLUE;
                case Smoke -> Color.GREEN;
                case Temperature -> Color.RED;
                default -> Color.MAGENTA;
            };

            //Use a colorStateList to set the track colors
            ColorStateList colorStateList = ColorStateList.valueOf(sliderColor);
            slider.setTrackActiveTintList(colorStateList);
            slider.setTrackInactiveTintList(colorStateList);
        }else{
            Log.w("SensorFragment", "Config is null. Slider gets dummy values.");
            slider.setValueFrom(0);
            slider.setValueTo(100);
            slider.setValue(50);
        }

        float sensorValue = slider.getValue();
        String labelText = String.format(getResources().getString(R.string.sensor_slider_label), sensorValue);
        sliderLabel.setText(labelText);

        slider.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {

            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                float value = slider.getValue();
                if(config!= null) config.setProgress(value);
                String labelText = String.format(getResources().getString(R.string.sensor_slider_label), value);
                sliderLabel.setText(labelText);
            }
        });
    }

    /**Initializes tha switch's status and label based on the sensor's configuration.*/
    private void initSwitch() {
        // Initialize sensor's activity based on the state manager's data
        SensorConfig config = sensorStateManager.getConfig(sensorType);
        if(config != null){
            activeSwitch.setChecked(config.isActive());
            if (config.isActive()) {
                switchLabel.setText(R.string.sensor_active);
            }else {
                switchLabel.setText(R.string.sensor_inactive);
            }
        }else{
            activeSwitch.setChecked(false);
            Log.w("SensorFragment", "Config is null. Activity switch is disabled.");
        }
        // Attach listener that updates the corresponding sensor's active state
        activeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(config != null) config.setActive(isChecked);
            if (isChecked) {
                switchLabel.setText(R.string.sensor_active);
            }else {
                switchLabel.setText(R.string.sensor_inactive);
            }
        });
    }
}
