package com.example.mqtt_giga;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

// AlarmReceiver.java
public class AlarmReceiver extends BroadcastReceiver{
  @Override
  public void onReceive(Context context, Intent intent) {
	Intent serviceIntent = new Intent(context, MQTTService.class);
	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
	  context.startForegroundService(serviceIntent)			;
	} else  context.startService(serviceIntent)				;
  }
}
//---------------------------------------------------------------

