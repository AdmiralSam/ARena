package com.samuel.arena;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.qualcomm.vuforia.DataSet;
import com.qualcomm.vuforia.ObjectTracker;
import com.qualcomm.vuforia.STORAGE_TYPE;
import com.qualcomm.vuforia.TrackerManager;
import com.qualcomm.vuforia.Vuforia;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GLSurfaceView glSurfaceView = new MainGlSurfaceView(this);
        setContentView(glSurfaceView);
        Vuforia.setInitParameters(this, Vuforia.GL_20, "AU+DQTz/////AAAAAXHG40QU6Ej7u9oN6+ZymNddgsDbcUjCkKpnw2UFv3l6mYUyeqyNf7gdnsqLsKYqkAS4JiWa+LdamrlSXnj2W6+CCgd0KNUbUVCX2B4wLng4M+NipFKYotd2HK3F+It44qw70DULIYXSQW9XALuD2uGpwiGIOJMo3kn4tbQwEIvrJC8Pcfx8J1BFhacrlcEAAue43he6YBGOuKqM6Kvy0QhFAjS8jMHMPCf6BrVgnKD9WK/9LY5v1MBgDeb1kgxVhGE7w2vXDJgqjBk7boCXSBmd83gWdl4lQHpnPRtfZEkBKaTmYfitQbPYqPY1vMdV9dq+ohaQkS8f1B9SONTCeGO44ItjGzbHwOjGI5w4GsWa");
        Vuforia.init();
        TrackerManager trackerManager = TrackerManager.getInstance();
        ObjectTracker objectTracker = (ObjectTracker) trackerManager.initTracker(ObjectTracker.getClassType());
        DataSet dataSet = objectTracker.createDataSet();
        dataSet.load("StonesAndChips.xml", STORAGE_TYPE.STORAGE_APPRESOURCE);
        objectTracker.activateDataSet(dataSet);
    }
}
