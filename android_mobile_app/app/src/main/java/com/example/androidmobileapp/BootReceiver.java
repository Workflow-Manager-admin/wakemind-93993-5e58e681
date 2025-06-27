package com.example.androidmobileapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import java.util.Calendar;

/**
 * PUBLIC_INTERFACE
 * Boot receiver that reschedules alarms after device restart
 */
public class BootReceiver extends BroadcastReceiver {
    
    private static final String PREFS_NAME = "AlarmPrefs";
    private static final String KEY_ALARMS_ENABLED = "alarms_enabled";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction()) ||
            Intent.ACTION_MY_PACKAGE_REPLACED.equals(intent.getAction()) ||
            Intent.ACTION_PACKAGE_REPLACED.equals(intent.getAction())) {
            
            // Check if alarms were enabled before reboot
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            boolean alarmsEnabled = prefs.getBoolean(KEY_ALARMS_ENABLED, false);
            
            if (alarmsEnabled) {
                // Reschedule both alarms
                scheduleAlarm(context, 0, 0);
                scheduleAlarm(context, 0, 15);
            }
        }
    }
    
    /**
     * Schedule an alarm for specific time
     */
    private void scheduleAlarm(Context context, int hour, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        
        // If the time has already passed today, schedule for tomorrow
        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent alarmIntent = new Intent(context, AlarmReceiver.class);
        int requestCode = hour * 100 + minute;
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            context, requestCode, alarmIntent, 
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP, 
                calendar.getTimeInMillis(), 
                pendingIntent
            );
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP, 
                calendar.getTimeInMillis(), 
                pendingIntent
            );
        }
    }
}
