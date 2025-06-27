package com.example.androidmobileapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * PUBLIC_INTERFACE
 * Main activity that handles alarm scheduling and UI interaction
 */
public class MainActivity extends AppCompatActivity {
    
    private static final String PREFS_NAME = "AlarmPrefs";
    private static final String KEY_ALARMS_ENABLED = "alarms_enabled";
    
    private Button toggleButton;
    private TextView statusText;
    private TextView nextAlarmText;
    private AlarmManager alarmManager;
    private SharedPreferences prefs;
    private boolean alarmsEnabled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        initializeViews();
        setupAlarmManager();
        loadPreferences();
        updateUI();
        
        // Request exact alarm permission for Android 12+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requestExactAlarmPermission();
        }
    }

    /**
     * Initialize UI components
     */
    private void initializeViews() {
        toggleButton = findViewById(R.id.toggleButton);
        statusText = findViewById(R.id.statusText);
        nextAlarmText = findViewById(R.id.nextAlarmText);
        
        toggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleAlarms();
            }
        });
    }

    /**
     * Setup alarm manager and preferences
     */
    private void setupAlarmManager() {
        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Load saved preferences
     */
    private void loadPreferences() {
        alarmsEnabled = prefs.getBoolean(KEY_ALARMS_ENABLED, false);
    }

    /**
     * Save preferences
     */
    private void savePreferences() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(KEY_ALARMS_ENABLED, alarmsEnabled);
        editor.apply();
    }

    /**
     * Toggle alarm state
     */
    private void toggleAlarms() {
        if (alarmsEnabled) {
            stopAlarms();
        } else {
            startAlarms();
        }
        alarmsEnabled = !alarmsEnabled;
        savePreferences();
        updateUI();
    }

    /**
     * Start daily alarms
     */
    private void startAlarms() {
        scheduleAlarm(5, 45); // 5:45 AM
        scheduleAlarm(6, 0);  // 6:00 AM
    }

    /**
     * Stop daily alarms
     */
    private void stopAlarms() {
        cancelAlarm(5, 45);
        cancelAlarm(6, 0);
    }

    /**
     * Schedule an alarm for specific time
     */
    private void scheduleAlarm(int hour, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        
        // If the time has already passed today, schedule for tomorrow
        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        
        Intent intent = new Intent(this, AlarmReceiver.class);
        int requestCode = hour * 100 + minute; // Unique request code
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            this, requestCode, intent, 
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        // Set repeating alarm
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP, 
            calendar.getTimeInMillis(), 
            pendingIntent
        );
    }

    /**
     * Cancel an alarm
     */
    private void cancelAlarm(int hour, int minute) {
        Intent intent = new Intent(this, AlarmReceiver.class);
        int requestCode = hour * 100 + minute;
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            this, requestCode, intent, 
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        alarmManager.cancel(pendingIntent);
    }

    /**
     * Update UI based on current state
     */
    private void updateUI() {
        if (alarmsEnabled) {
            toggleButton.setText(R.string.stop_alarms);
            statusText.setText(R.string.alarms_active);
            showNextAlarm();
        } else {
            toggleButton.setText(R.string.start_alarms);
            statusText.setText(R.string.alarms_inactive);
            nextAlarmText.setVisibility(View.GONE);
        }
    }

    /**
     * Show next alarm time
     */
    private void showNextAlarm() {
        Calendar now = Calendar.getInstance();
        Calendar next545 = Calendar.getInstance();
        Calendar next600 = Calendar.getInstance();
        
        // Set to 5:45 AM
        next545.set(Calendar.HOUR_OF_DAY, 5);
        next545.set(Calendar.MINUTE, 45);
        next545.set(Calendar.SECOND, 0);
        
        // Set to 6:00 AM
        next600.set(Calendar.HOUR_OF_DAY, 6);
        next600.set(Calendar.MINUTE, 0);
        next600.set(Calendar.SECOND, 0);
        
        // If times have passed today, set for tomorrow
        if (next545.getTimeInMillis() <= now.getTimeInMillis()) {
            next545.add(Calendar.DAY_OF_MONTH, 1);
        }
        if (next600.getTimeInMillis() <= now.getTimeInMillis()) {
            next600.add(Calendar.DAY_OF_MONTH, 1);
        }
        
        // Find the next alarm
        Calendar nextAlarm = next545.getTimeInMillis() < next600.getTimeInMillis() ? next545 : next600;
        
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault());
        String nextAlarmTime = sdf.format(nextAlarm.getTime());
        
        nextAlarmText.setText(getString(R.string.next_alarm, nextAlarmTime));
        nextAlarmText.setVisibility(View.VISIBLE);
    }

    /**
     * Request exact alarm permission for Android 12+
     */
    private void requestExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (!alarmManager.canScheduleExactAlarms()) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivity(intent);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPreferences();
        updateUI();
    }
}
