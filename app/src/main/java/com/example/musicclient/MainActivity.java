package com.example.musicclient;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.musiccentral.IMusicService;

public class MainActivity extends AppCompatActivity {
    private IMusicService musicService;
    private boolean bound = false;

    private Spinner spinnerClips;
    private Button btnBind, btnPlay, btnPause, btnResume, btnStop, btnUnbind;

    private final ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            musicService = IMusicService.Stub.asInterface(service);
            bound = true;
            // Enable spinner & bind-dependent buttons
            spinnerClips.setEnabled(true);
            btnPlay.setEnabled(true);
            btnUnbind.setEnabled(true);

            // Populate spinner with clip labels
            try {
                int[] clipIds = musicService.listClips();
                String[] labels = new String[clipIds.length];
                for (int i = 0; i < clipIds.length; i++) {
                    labels[i] = "Clip " + clipIds[i];
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        MainActivity.this,
                        android.R.layout.simple_spinner_item,
                        labels
                );
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerClips.setAdapter(adapter);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(MainActivity.this, "Failed to list clips", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            bound = false;
            musicService = null;
            // Disable all controls
            spinnerClips.setEnabled(false);
            btnPlay.setEnabled(false);
            btnPause.setEnabled(false);
            btnResume.setEnabled(false);
            btnStop.setEnabled(false);
            btnUnbind.setEnabled(false);
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        spinnerClips = findViewById(R.id.spinner_clips);
        btnBind     = findViewById(R.id.btn_bind);
        btnPlay     = findViewById(R.id.btn_play);
        btnPause    = findViewById(R.id.btn_pause);
        btnResume   = findViewById(R.id.btn_resume);
        btnStop     = findViewById(R.id.btn_stop);
        btnUnbind   = findViewById(R.id.btn_unbind);

        // Initial UI state
        spinnerClips.setEnabled(false);
        btnPlay.setEnabled(false);
        btnPause.setEnabled(false);
        btnResume.setEnabled(false);
        btnStop.setEnabled(false);
        btnUnbind.setEnabled(false);

        // Bind Service button
        btnBind.setOnClickListener(v -> {
            Intent svc = new Intent();
            svc.setComponent(new ComponentName(
                    "com.example.musiccentral",
                    "com.example.musiccentral.MusicService"
            ));
            boolean ok = bindService(svc, conn, Context.BIND_AUTO_CREATE);
            Log.d("MusicClient", "bindService returned: " + ok);
            Toast.makeText(this, "bindService returned: " + ok, Toast.LENGTH_SHORT).show();

        });

        // Play button
        btnPlay.setOnClickListener(v -> {
            if (!bound) return;
            int clipPos = spinnerClips.getSelectedItemPosition();
            int clipId = clipPos + 1;
            try {
                musicService.play(clipId);
                // Update UI
                btnPlay.setEnabled(false);
                btnPause.setEnabled(true);
                btnStop.setEnabled(true);
                btnUnbind.setEnabled(false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // Pause button
        btnPause.setOnClickListener(v -> {
            if (!bound) return;
            try {
                musicService.pause();
                // Update UI
                btnPause.setEnabled(false);
                btnResume.setEnabled(true);
                btnStop.setEnabled(true);
                btnUnbind.setEnabled(false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // Resume button
        btnResume.setOnClickListener(v -> {
            if (!bound) return;
            try {
                musicService.resume();
                // Update UI
                btnResume.setEnabled(false);
                btnPause.setEnabled(true);
                btnStop.setEnabled(true);
                btnUnbind.setEnabled(false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // Stop button
        btnStop.setOnClickListener(v -> {
            if (!bound) return;
            try {
                musicService.stop();
                // Reset UI
                btnPlay.setEnabled(true);
                btnPause.setEnabled(false);
                btnResume.setEnabled(false);
                btnStop.setEnabled(false);
                btnUnbind.setEnabled(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // Unbind Service button
        btnUnbind.setOnClickListener(v -> {
            if (bound) {
                unbindService(conn);
                bound = false;
                // Reset UI
                spinnerClips.setEnabled(false);
                btnPlay.setEnabled(false);
                btnPause.setEnabled(false);
                btnResume.setEnabled(false);
                btnStop.setEnabled(false);
                btnUnbind.setEnabled(false);
            }
        });
    }

    @Override
    protected void onDestroy() {
        if (bound) unbindService(conn);
        super.onDestroy();
    }
}
