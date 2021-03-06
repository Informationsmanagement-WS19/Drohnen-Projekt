package com.dji.sdk.sample.demo.CustomViews;


import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;


import androidx.annotation.NonNull;

import com.dji.sdk.sample.R;
import com.dji.sdk.sample.internal.controller.DJISampleApplication;
import com.dji.sdk.sample.internal.utils.DialogUtils;
import com.dji.sdk.sample.internal.utils.ModuleVerificationUtil;
import com.dji.sdk.sample.internal.utils.ToastUtils;
import com.dji.sdk.sample.internal.view.PresentableView;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import dji.common.error.DJIError;
import dji.common.flightcontroller.simulator.InitializationData;
import dji.common.flightcontroller.simulator.SimulatorState;
import dji.common.flightcontroller.virtualstick.FlightControlData;
import dji.common.flightcontroller.virtualstick.FlightCoordinateSystem;
import dji.common.flightcontroller.virtualstick.RollPitchControlMode;
import dji.common.flightcontroller.virtualstick.VerticalControlMode;
import dji.common.flightcontroller.virtualstick.YawControlMode;
import dji.common.model.LocationCoordinate2D;
import dji.common.util.CommonCallbacks;
import dji.keysdk.FlightControllerKey;
import dji.keysdk.KeyManager;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.flightcontroller.Simulator;

// Imports Camera/ Video
import android.graphics.SurfaceTexture;
import android.view.TextureView;
import dji.sdk.camera.VideoFeeder;
import dji.sdk.codec.DJICodecManager;
import dji.common.camera.SettingsDefinitions;


/**
 * Class for virtual stick.
 */
public class FlightCustomExtendedView extends RelativeLayout
        implements View.OnClickListener, CompoundButton.OnCheckedChangeListener, PresentableView, TextureView.SurfaceTextureListener {

    //Variables buttons
    private ToggleButton btnSimulator;
    private ToggleButton btnControlModes;
    private ToggleButton btnVirtualStick;
    private ToggleButton btnCameraMode;
    private ToggleButton btnRecordVideo;
    private Button btnTakeOff;
    private Button btnLand;
    private Button btnShootPhoto;
    private Button btnMissions;
    private Button btnMedia;

    //Variables Camera
    private VideoFeeder.VideoDataListener videoDataListener = null;
    private DJICodecManager codecManager = null;

    //Variables video function
    private Timer timer = new Timer();
    private long timeCounter = 0;
    private long hours = 0;
    private long minutes = 0;
    private long seconds = 0;
    private String time = "";

    //Variables screen texts
    private TextView textView;
    private TextView textView_Video;

    private Timer sendVirtualStickDataTimer;
    private SendVirtualStickDataTask sendVirtualStickDataTask;

    FlightController flightController = ModuleVerificationUtil.getFlightController();

    private float pitch;
    private float roll;
    private float yaw;
    private float throttle;
    private FlightControllerKey isSimulatorActived;


    public FlightCustomExtendedView(Context context) {
        super(context);
        init(context);
    }

    @NonNull
    @Override
    public String getHint() {
        return this.getClass().getSimpleName() + ".java";
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        setUpListeners();

        if (isModuleAvailable()) {

            DJISampleApplication.getProductInstance()
                    .getCamera()
                    .setMode(SettingsDefinitions.CameraMode.SHOOT_PHOTO,
                            new CommonCallbacks.CompletionCallback() {
                                @Override
                                public void onResult(DJIError djiError) {

                                }
                            });
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        if (null != sendVirtualStickDataTimer) {
            if (sendVirtualStickDataTask != null) {
                sendVirtualStickDataTask.cancel();

            }
            sendVirtualStickDataTimer.cancel();
            sendVirtualStickDataTimer.purge();
            sendVirtualStickDataTimer = null;
            sendVirtualStickDataTask = null;
        }
        tearDownListeners();
        super.onDetachedFromWindow();
    }

    private void init(Context context) {
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Service.LAYOUT_INFLATER_SERVICE);
        layoutInflater.inflate(R.layout.view_flight_custom_extended, this, true);

        initAllKeys();
        initUI();

    }

    private void initAllKeys() {
        isSimulatorActived = FlightControllerKey.create(FlightControllerKey.IS_SIMULATOR_ACTIVE);
    }

    //Start initUI-------------------------------------------------------------
    private void initUI() {
        btnTakeOff = (Button) findViewById(R.id.btn_take_off);
        btnLand = (Button) findViewById(R.id.btn_land);
        btnShootPhoto = (Button) findViewById(R.id.btn_shoot_photo);
        btnMissions = (Button) findViewById(R.id.btn_missions);
        btnMedia = (Button) findViewById(R.id.btn_media);

        btnSimulator = (ToggleButton) findViewById(R.id.btn_start_simulator);
        btnControlModes = (ToggleButton) findViewById(R.id.btn_control_modes);
        btnVirtualStick = (ToggleButton) findViewById(R.id.btn_virtual_stick);
        btnCameraMode = (ToggleButton) findViewById(R.id.btn_camera_mode);
        btnRecordVideo = (ToggleButton) findViewById(R.id.btn_record_video);

        textView = (TextView) findViewById(R.id.textview_simulator);
        textView_Video = (TextView) findViewById(R.id.textview_video_record);


        btnTakeOff.setOnClickListener(this);
        btnLand.setOnClickListener(this);
        btnSimulator.setOnCheckedChangeListener(FlightCustomExtendedView.this);

        btnShootPhoto.setOnClickListener(this);
        btnMissions.setOnClickListener(this);
        btnMedia.setOnClickListener(this);



        Boolean isSimulatorOn = (Boolean) KeyManager.getInstance().getValue(isSimulatorActived);
        if (isSimulatorOn != null && isSimulatorOn) {
            btnSimulator.setChecked(true);
            textView.setText("Simulator is On.");
        }



        // Toggle Button: Control Modes
        btnControlModes.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                   disableControlModes();
                } else {
                    enableControlModes();
                }
            }
        });



        // Toggle Button: Virtual Stick
        btnVirtualStick.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    disableVirtualStick();
                } else {
                    enableVirtualStick();
                }
            }
        });



        //Toggle Button: Camera Mode
        btnCameraMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    toVideoMode();
                } else {
                    toPhotoMode();
                }
            }
        });



        // Toggle Button: Video Record
        btnRecordVideo.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    startRecording();
                } else {
                    stopRecording();
                }
            }
        });


        // Camera Feed
        TextureView mVideoSurface = (TextureView) findViewById(R.id.video_previewer);

        if (null != mVideoSurface) {
            mVideoSurface.setSurfaceTextureListener((TextureView.SurfaceTextureListener) this);

            // This callback is for

            videoDataListener = new VideoFeeder.VideoDataListener() {
                @Override
                public void onReceive(byte[] bytes, int size) {
                    if (null != codecManager) {
                        codecManager.sendDataToDecoder(bytes, size);
                    }
                }
            };
        }
        initSDKCallback();
        toPhotoMode();
        enableVirtualStick();
        enableControlModes();
    }// END initUI------------------------------------------------------------


    private boolean isModuleAvailable() {
        return (null != DJISampleApplication.getProductInstance()) && (null != DJISampleApplication.getProductInstance()
                .getCamera());
    }

    private void initSDKCallback() {
        try {
            VideoFeeder.getInstance().getPrimaryVideoFeed().addVideoDataListener(videoDataListener);
        } catch (Exception ignored) {
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        if (codecManager == null) {
            codecManager = new DJICodecManager(getContext(), surface, width, height);
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        if (codecManager != null) {
            codecManager.cleanSurface();
            codecManager = null;
        }
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    private void setUpListeners() {
        Simulator simulator = ModuleVerificationUtil.getSimulator();
        if (simulator != null) {
            simulator.setStateCallback(new SimulatorState.Callback() {
                @Override
                public void onUpdate(@NonNull final SimulatorState simulatorState) {
                    ToastUtils.setResultToText(textView,
                            "Yaw : "
                                    + simulatorState.getYaw()
                                    + ","
                                    + "X : "
                                    + simulatorState.getPositionX()
                                    + "\n"
                                    + "Y : "
                                    + simulatorState.getPositionY()
                                    + ","
                                    + "Z : "
                                    + simulatorState.getPositionZ());
                }
            });
        } else {
            ToastUtils.setResultToToast("Disconnected!");
        }

    }

    private void tearDownListeners() {
        Simulator simulator = ModuleVerificationUtil.getSimulator();
        if (simulator != null) {
            simulator.setStateCallback(null);
        }
    }

    //OnClick cases for each button
    @Override
    public void onClick(View v) {
        FlightController flightControllerClick = ModuleVerificationUtil.getFlightController();
        if (flightControllerClick == null) {
            return;
        }
        switch (v.getId()) {

            case R.id.btn_take_off:
                takeOff();
                break;

            case R.id.btn_land:
                autoLand();
                break;

            case R.id.btn_shoot_photo:
                shootPhoto();
                break;

            case R.id.btn_missions:
                Intent intent = new Intent(getContext(), LoadMission.class);
                getContext().startActivity(intent);
                break;

            case R.id.btn_media:
                ToastUtils.setResultToToast("Not implemented yet");
                break;

            default:
                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if (compoundButton == btnSimulator) {
            onClickSimulator(b);
        }
    }

    private void onClickSimulator(boolean isChecked) {
        Simulator simulator = ModuleVerificationUtil.getSimulator();
        if (simulator == null) {
            return;
        }
        if (isChecked) {

            textView.setVisibility(VISIBLE);

            simulator.start(InitializationData.createInstance(new LocationCoordinate2D(23, 113), 10, 10),
                    new CommonCallbacks.CompletionCallback() {
                        @Override
                        public void onResult(DJIError djiError) {

                        }
                    });
        } else {

            textView.setVisibility(INVISIBLE);

            simulator.stop(new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(DJIError djiError) {

                }
            });
        }
    }


    @Override
    public int getDescription() {
        return R.string.flight_controller_listview_virtual_stick;
    }

    private class SendVirtualStickDataTask extends TimerTask {

        @Override
        public void run() {
            if (ModuleVerificationUtil.isFlightControllerAvailable()) {
                DJISampleApplication.getAircraftInstance()
                        .getFlightController()
                        .sendVirtualStickFlightControlData(new FlightControlData(pitch,
                                        roll,
                                        yaw,
                                        throttle),
                                new CommonCallbacks.CompletionCallback() {
                                    @Override
                                    public void onResult(DJIError djiError) {

                                    }
                                });
            }
        }
    }


    //Shoot single photo
    private void shootPhoto() {

        if (isModuleAvailable()) {
            DJISampleApplication.getProductInstance()
                    .getCamera()
                    .startShootPhoto(new CommonCallbacks.CompletionCallback() {
                        @Override
                        public void onResult(DJIError djiError) {
                            if (null == djiError) {
                                ToastUtils.setResultToToast("Photo taken");
                            } else {
                                ToastUtils.setResultToToast(djiError.getDescription());
                            }
                        }
                    });
        }
    }



    // Activate Video Mode
    private void toVideoMode(){
        if (ModuleVerificationUtil.isCameraModuleAvailable()) {
            DJISampleApplication.getProductInstance()
                    .getCamera()
                    .setMode(SettingsDefinitions.CameraMode.RECORD_VIDEO,
                            new CommonCallbacks.CompletionCallback() {
                                @Override
                                public void onResult(DJIError djiError) {
                                    ToastUtils.setResultToToast("SetCameraMode to recordVideo");
                                }
                            });
        }
        btnShootPhoto.setClickable(false);
        btnRecordVideo.setClickable(true);
    }


    // Activate Photo Mode
    private void toPhotoMode(){
        if (ModuleVerificationUtil.isCameraModuleAvailable()) {
            DJISampleApplication.getProductInstance()
                    .getCamera()
                    .setMode(SettingsDefinitions.CameraMode.SHOOT_PHOTO,
                            new CommonCallbacks.CompletionCallback() {
                                @Override
                                public void onResult(DJIError djiError) {
                                    ToastUtils.setResultToToast("SetCameraMode to shootPhoto");
                                }
                            });
        }
        btnRecordVideo.setClickable(false);
        btnShootPhoto.setClickable(true);
    }



    // Start recording a video
    private void startRecording(){
        setRecordTime("Recording: 00:00:00");
        btnCameraMode.setClickable(false);
        if (ModuleVerificationUtil.isCameraModuleAvailable()) {
            DJISampleApplication.getProductInstance()
                    .getCamera()
                    .startRecordVideo(new CommonCallbacks.CompletionCallback() {
                        @Override
                        public void onResult(DJIError djiError) {
                            //success so, start recording
                            if (null == djiError) {
                                ToastUtils.setResultToToast("Start record");
                                timer = new Timer();
                                timer.schedule(new TimerTask() {
                                    @Override
                                    public void run() {
                                        timeCounter = timeCounter + 1;
                                        hours = TimeUnit.MILLISECONDS.toHours(timeCounter);
                                        minutes =
                                                TimeUnit.MILLISECONDS.toMinutes(timeCounter) - (hours * 60);
                                        seconds = TimeUnit.MILLISECONDS.toSeconds(timeCounter) - ((hours
                                                * 60
                                                * 60) + (minutes * 60));
                                        time = String.format("%02d:%02d:%02d", hours, minutes, seconds);
                                        setRecordTime("Recording:" + time);
                                    }
                                }, 0, 1);
                            }
                        }
                    });
        }
    }



    //Stop recording the video
    private void stopRecording(){
        if (ModuleVerificationUtil.isCameraModuleAvailable()) {
            DJISampleApplication.getProductInstance()
                    .getCamera()
                    .stopRecordVideo(new CommonCallbacks.CompletionCallback() {
                        @Override
                        public void onResult(DJIError djiError) {
                            ToastUtils.setResultToToast("Recording stopped");
                            setRecordTime("00:00:00");
                            timer.cancel();
                            timeCounter = 0;
                        }
                    });
        }
        btnCameraMode.setClickable(true);
    }



    // Enable virtual stick
    private void enableVirtualStick(){
        flightController.setVirtualStickModeEnabled(true, new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {
                ToastUtils.setResultToToast("Virtual Stick enabled");
            }
        });
    }



    // Disable virtual stick
    private void disableVirtualStick(){
        flightController.setVirtualStickModeEnabled(false, new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {
                ToastUtils.setResultToToast("Virtual Stick disabled");
            }
        });
    }



    // Enable control modes
    private void enableControlModes(){
        flightController.setRollPitchControlMode(RollPitchControlMode.VELOCITY);
        flightController.setYawControlMode(YawControlMode.ANGULAR_VELOCITY);
        flightController.setVerticalControlMode(VerticalControlMode.VELOCITY);
        flightController.setRollPitchCoordinateSystem(FlightCoordinateSystem.BODY);

        ToastUtils.setResultToToast("Control Modes enabled");
    }



    // Disable control modes
    private void disableControlModes(){
        flightController.setRollPitchControlMode(RollPitchControlMode.ANGLE);
        flightController.setYawControlMode(YawControlMode.ANGLE);
        flightController.setVerticalControlMode(VerticalControlMode.POSITION);
        flightController.setRollPitchCoordinateSystem(FlightCoordinateSystem.GROUND);

        ToastUtils.setResultToToast("Control Modes disabled");
    }



    // Take off
    private void takeOff(){
        flightController.startTakeoff(new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {
                DialogUtils.showDialogBasedOnError(getContext(), djiError);
                ToastUtils.setResultToToast("Take Off");
            }
        });
    }



    // Land
    private void autoLand(){
        flightController.startLanding(new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {
                DialogUtils.showDialogBasedOnError(getContext(), djiError);
                ToastUtils.setResultToToast("Start Landing");
            }
        });
    }



    private void setRecordTime(final String recordTime) {
        post(new Runnable() {
            @Override
            public void run() {
                textView_Video.setText(recordTime);
            }
        });
    }
}