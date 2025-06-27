package com.example.androidmobileapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import java.util.Calendar;

/**
 * PUBLIC_INTERFACE
 * Broadcast receiver that handles alarm triggers and reschedules repeating alarms
 */
public class AlarmReceiver extends BroadcastReceiver {
    
    @Override
    public void onReceive(Context context, Intent intent) {
        // Start the alarm service
        Intent serviceIntent = new Intent(context, AlarmService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent);
        } else {
            context.startService(serviceIntent);
        }
        
        // Launch challenge activity
        Intent challengeIntent = new Intent(context, ChallengeActivity.class);
        challengeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | 
                                Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(challengeIntent);
        
        // Reschedule the alarm for tomorrow
        rescheduleAlarm(context);
    }
    
    /**
     * Reschedule the alarm for the next day
     */
    private void rescheduleAlarm(Context context) {
        Calendar now = Calendar.getInstance();
        int currentHour = now.get(Calendar.HOUR_OF_DAY);
        int currentMinute = now.get(Calendar.MINUTE);
        
        // Determine which alarm triggered and reschedule it
        if ((currentHour == 0 && currentMinute == 0) || 
            (currentHour == 0 && currentMinute >= 0 && currentMinute < 15)) {
            scheduleAlarmForTomorrow(context, 0, 0);
        } else if ((currentHour == 0 && currentMinute == 15) || 
                   (currentHour == 0 && currentMinute >= 15 && currentMinute < 30)) {
            scheduleAlarmForTomorrow(context, 0, 15);
        }
    }
    
    /**
     * Schedule alarm for tomorrow at specified time
     */
    private void scheduleAlarmForTomorrow(Context context, int hour, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        int requestCode = hour * 100 + minute;
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            context, requestCode, intent, 
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP, 
            calendar.getTimeInMillis(), 
            pendingIntent
        );
    }
}
