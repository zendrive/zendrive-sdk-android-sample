package com.zendrive.zendrivesdkdemo;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.zendrive.sdk.LocationPoint;
import com.zendrive.sdk.LocationPointWithTimestamp;
import com.zendrive.sdk.PhonePosition;
import com.zendrive.sdk.ZendriveAccidentConfidence;
import com.zendrive.sdk.ZendriveCollisionSeverity;
import com.zendrive.sdk.ZendriveDirectionOfImpact;
import com.zendrive.sdk.ZendriveDriveType;
import com.zendrive.sdk.ZendriveEventRatings;
import com.zendrive.sdk.ZendriveEventSeverity;
import com.zendrive.sdk.ZendriveStarRating;
import com.zendrive.sdk.ZendriveTurnDirection;
import com.zendrive.sdk.ZendriveUserMode;
import com.zendrive.sdk.ZendriveVehicleType;
import com.zendrive.sdk.testing.mockdrive.AggressiveAccelerationEventBuilder;
import com.zendrive.sdk.testing.mockdrive.CollisionEventBuilder;
import com.zendrive.sdk.testing.mockdrive.HardBrakeEventBuilder;
import com.zendrive.sdk.testing.mockdrive.HardTurnEventBuilder;
import com.zendrive.sdk.testing.mockdrive.MockDrive;
import com.zendrive.sdk.testing.mockdrive.MockEventBuilder;
import com.zendrive.sdk.testing.mockdrive.MockZendriveOperationResult;
import com.zendrive.sdk.testing.mockdrive.PhoneHandlingEventBuilder;
import com.zendrive.sdk.testing.mockdrive.PhoneScreenInteractionEventBuilder;
import com.zendrive.sdk.testing.mockdrive.PresetTripType;
import com.zendrive.sdk.testing.mockdrive.SpeedingEventBuilder;
import com.zendrive.sdk.testing.mockdrive.StopSignViolationEventBuilder;
import com.zendrive.zendrivesdkdemo.databinding.ActivityMainBinding;

import java.util.ArrayList;

import static com.zendrive.zendrivesdkdemo.Constants.LOG_TAG_DEBUG;

class MockDriveLayoutHandler implements LayoutHandler {

    private ActivityMainBinding binding;

    @Override
    public void setup(Context context, ActivityMainBinding binding) {
        this.binding = binding;
        setupUI(context);
    }

    private void setupUI(Context context) {
        ArrayAdapter<PresetTripType> presetArrayAdapter = new ArrayAdapter<>(context,
                android.R.layout.simple_spinner_item, PresetTripType.values());
        presetArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.mockDriveLayout.presetSpinner.setAdapter(presetArrayAdapter);
        binding.mockDriveLayout.presetSpinner.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    int selection = -1;

                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position,
                                               long id) {
                        if (selection == position) {
                            return;
                        }
                        selection = position;
                        binding.mockDriveLayout.presetSpinner.setSelection(selection);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        selection = -1;
                    }
                });
    }

    @Override
    public void onClick(View view) {
        Context context = view.getContext();
        if (view == binding.mockDriveLayout.simulatePresetButton) {
            String selectedPreset =
                    binding.mockDriveLayout.presetSpinner.getSelectedItem().toString();
            MockZendriveOperationResult result = simulatePresetMockDrive(
                    context, PresetTripType.valueOf(selectedPreset));
            if (!result.isSuccess()) {
                Log.d(LOG_TAG_DEBUG,
                        result.getErrorCode().name() + ": " + result.getErrorMessage());
            }
        }
    }

    private MockZendriveOperationResult simulatePresetMockDrive(Context context,
                                                          PresetTripType presetTripType) {
        MockDrive.Builder builder = MockDrive.Builder.presetMockDrive(context, presetTripType);
        MockDrive presetMockDrive = builder.build();
        return presetMockDrive.simulate(context, 10);
    }

    /**
     * To create a custom mock drive, we can start with an existing preset drive and
     * edit it to customize.
     */
    private MockZendriveOperationResult simulateModifiedPresetMockDrive(Context context,
                                                                PresetTripType presetTripType) {
        MockDrive.Builder builder = MockDrive.Builder.presetMockDrive(context, presetTripType);
        MockDrive modifiedPresetMockDrive = builder.setScore(54)
                .setOnDriveStartDelay(3000)
                .setOnDriveEndDelay(3000)
                .setOnDriveAnalyzedDelay(3000)
                .build();
        return modifiedPresetMockDrive.simulate(context, 10);
    }

    /**
     * To create a completely new MockDrive from scratch use the MockDrive.Builder API.
     */
    private MockZendriveOperationResult simulateCustomMockDrive(Context context) {
        long driveStartTimestamp = 1543870794382L;
        long driveEndTimestamp = 1543871176352L;

        ArrayList<LocationPointWithTimestamp> waypoints = new ArrayList<>();
        LocationPoint startLocationPoint = new LocationPoint(27.4293013, -82.5636051);
        LocationPoint endLocationPoint = new LocationPoint(27.4265938, -82.5306409);

        LocationPoint hardTurnStartLocation = new LocationPoint(27.429106, -82.530999);
        LocationPoint hardTurnEndLocation = new LocationPoint(27.428820, -82.530591);
        LocationPoint collisionLocation = new LocationPoint(27.427161, -82.530617);
        LocationPoint aggressiveAccelerationLocation = new LocationPoint(27.429283, -82.546828);
        LocationPoint hardBrakeLocation = new LocationPoint(27.429312, -82.548994);
        LocationPoint phoneHandlingStartLocation = new LocationPoint(27.427824, -82.530598);
        LocationPoint phoneHandlingEndLocation = new LocationPoint(27.426933, -82.530628);
        LocationPoint speedingStartLocationPoint = new LocationPoint(27.429316, -82.559140);
        LocationPoint speedingEndLocationPoint = new LocationPoint(27.429290, -82.557029);
        LocationPoint phoneScreenInteractionStartLocation = new LocationPoint(27.429330,
                -82.562356);
        LocationPoint phoneScreenInteractionEndLocation = new LocationPoint(27.429259, -82.560129);
        LocationPoint stopSignViolationLocationPoint = new LocationPoint(27.429191, -82.540527);

        waypoints.add(new LocationPointWithTimestamp(startLocationPoint, driveStartTimestamp));
        waypoints.add(new LocationPointWithTimestamp(phoneScreenInteractionStartLocation,
                1543870802367L));
        waypoints.add(new LocationPointWithTimestamp(phoneScreenInteractionEndLocation,
                1543870815396L));
        waypoints.add(new LocationPointWithTimestamp(speedingStartLocationPoint, 1543870828368L));
        waypoints.add(new LocationPointWithTimestamp(speedingEndLocationPoint, 1543870846391L));
        waypoints.add(new LocationPointWithTimestamp(hardBrakeLocation, 1543871082354L));
        waypoints.add(new LocationPointWithTimestamp(aggressiveAccelerationLocation,
                1543871124370L));
        waypoints.add(new LocationPointWithTimestamp(stopSignViolationLocationPoint, 1543871128383L));
        waypoints.add(new LocationPointWithTimestamp(hardTurnStartLocation, 1543871132397L));
        waypoints.add(new LocationPointWithTimestamp(hardTurnEndLocation, 1543871142367L));
        waypoints.add(new LocationPointWithTimestamp(phoneHandlingStartLocation, 1543871149359L));
        waypoints.add(new LocationPointWithTimestamp(phoneHandlingEndLocation, 1543871159381L));
        waypoints.add(new LocationPointWithTimestamp(collisionLocation, 1543871167418L));
        waypoints.add(new LocationPointWithTimestamp(endLocationPoint, driveEndTimestamp));

        MockEventBuilder aggressiveAccelerationEventBuilder =
                new AggressiveAccelerationEventBuilder(1543871082354L)
                .setLocation(aggressiveAccelerationLocation)
                .setSeverity(ZendriveEventSeverity.HIGH);

        MockEventBuilder collisionEventBuilder =
                new CollisionEventBuilder(1543871082354L, driveStartTimestamp, "accidentId",
                        ZendriveAccidentConfidence.HIGH, 70, ZendriveVehicleType.CAR,
                        ZendriveCollisionSeverity.HIGH, ZendriveDirectionOfImpact.FRONT)
                .setLocation(collisionLocation);

        MockEventBuilder hardBrakeEventBuilder =
                new HardBrakeEventBuilder(1543871082354L)
                .setLocation(hardBrakeLocation)
                .setSeverity(ZendriveEventSeverity.LOW);

        MockEventBuilder speedingEventBuilder =
                new SpeedingEventBuilder(1543871124370L, 1543871132397L)
                .setSpeedingInfo(35, 40, 25)
                .setLocation(speedingStartLocationPoint, speedingEndLocationPoint)
                .setSeverity(ZendriveEventSeverity.NOT_AVAILABLE);

        MockEventBuilder phoneScreenInteractionEventBuilder =
                new PhoneScreenInteractionEventBuilder(1543871124370L, 1543871132397L)
                .setLocation(phoneScreenInteractionStartLocation, phoneScreenInteractionEndLocation)
                .setSeverity(ZendriveEventSeverity.NOT_AVAILABLE);

        MockEventBuilder phoneHandlingEventBuilder =
                new PhoneHandlingEventBuilder(1543871124370L, 1543871132397L)
                .setLocation(phoneHandlingStartLocation, phoneHandlingEndLocation)
                .setSeverity(ZendriveEventSeverity.NOT_AVAILABLE);

        MockEventBuilder hardTurnEventBuilder =
                new HardTurnEventBuilder(1543871124370L, 1543871132397L, ZendriveTurnDirection.RIGHT)
                .setLocation(hardTurnStartLocation, hardTurnEndLocation)
                .setSeverity(ZendriveEventSeverity.NOT_AVAILABLE);

        MockEventBuilder stopSignViolationEventBuilder =
                new StopSignViolationEventBuilder(1543871128383L)
                .setLocation(stopSignViolationLocationPoint)
                .setSeverity(ZendriveEventSeverity.NOT_AVAILABLE);

        ZendriveEventRatings eventRatings = new ZendriveEventRatings();
        eventRatings.aggressiveAccelerationRating = ZendriveStarRating.FOUR;
        eventRatings.hardBrakeRating = ZendriveStarRating.THREE;
        eventRatings.speedingRating = ZendriveStarRating.TWO;
        eventRatings.hardTurnRating = ZendriveStarRating.FOUR;
        eventRatings.phoneHandlingRating = ZendriveStarRating.FOUR;

        MockDrive.Builder builder = MockDrive.Builder.newAutoDrive(driveStartTimestamp, driveEndTimestamp)
                .setWayPoints(waypoints)
                .setAverageSpeed(20)
                .setMaxSpeed(40)
                .setDistanceMeters(100)
                .setDriveType(ZendriveDriveType.DRIVE)
                .setPhonePosition(PhonePosition.UNKNOWN)
                .setUserMode(ZendriveUserMode.DRIVER)
                .setEventRatings(eventRatings)
                .setScore(54)
                .addEventBuilder(aggressiveAccelerationEventBuilder)
                .addEventBuilder(collisionEventBuilder)
                .addEventBuilder(hardBrakeEventBuilder)
                .addEventBuilder(speedingEventBuilder)
                .addEventBuilder(phoneScreenInteractionEventBuilder)
                .addEventBuilder(phoneHandlingEventBuilder)
                .addEventBuilder(hardTurnEventBuilder)
                .addEventBuilder(stopSignViolationEventBuilder);

        MockDrive customMockDrive = builder.build();
        return customMockDrive.simulate(context, 10);
    }
}
