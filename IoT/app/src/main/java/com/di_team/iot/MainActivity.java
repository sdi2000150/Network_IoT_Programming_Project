package com.di_team.iot;

import static com.di_team.iot.sensor.SensorType.Temperature;
import static com.di_team.iot.sensor.SensorType.UV;

import android.os.Bundle;
import android.widget.Button;
import android.view.View;
import android.content.Intent;

import com.di_team.iot.sensor.SensorStateManager;
import com.di_team.iot.sensor.SensorType;

public class MainActivity extends BaseActivity {
    private Button temperatureSensorButton;
    private Button ultravioletRadiationSensorButton;

    private SensorStateManager sensorStateManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        //initialize to default sensor state: gas and smoke exist
        sensorStateManager = SensorStateManager.getInstance();

        temperatureSensorButton = findViewById(R.id.temperature_sensor_button);
        ultravioletRadiationSensorButton = findViewById(R.id.ultraviolet_radiation_sensor_button);
        updateButtonStates();
    }
    @Override
    public void onResume(){
        super.onResume();
        updateButtonStates();
    }

    /**Disables buttons with no corresponding sensor.*/
    public void updateButtonStates() {
        //check only for non-default sensor types
        temperatureSensorButton.setEnabled(sensorStateManager.sensorTypeExists(Temperature));
        ultravioletRadiationSensorButton.setEnabled(sensorStateManager.sensorTypeExists(UV));
    }

    public void startMenuActivity(View view) {
        Intent intent = new Intent(this, MenuActivity.class);
        startActivity(intent);
    }

    public void startSensorActivity(View view) {
        Intent intent = new Intent(this, SensorActivity.class);

        // Determine which sensor button was clicked
        SensorType sensorType;
        if (view.getId() == R.id.smoke_sensor_button) {
            sensorType = SensorType.Smoke;
        } else if (view.getId() == R.id.gas_sensor_button) {
            sensorType = SensorType.Gas;
        } else if (view.getId() == R.id.temperature_sensor_button) {
            sensorType = Temperature;
        } else {
            sensorType = SensorType.UV;
        }
        // Pass the selected sensor type to SensorActivity
        intent.putExtra("SENSOR_TYPE", sensorType);
        startActivity(intent);
    }

}
