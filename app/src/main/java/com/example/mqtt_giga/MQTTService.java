package com.example.mqtt_giga;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

//---------------------------------------------------------------
public class MQTTService extends Service{
    private static final String    TAG         = "M_SRV";
  	private static final int 		ID_SERVICE = 101	;
  	private static final   String  CHANNEL_ID  = "ForegroundServiceChannel";
    public static volatile boolean isRunning   = false  ;
	public static volatile boolean flRestarted = false	;
  	public static volatile boolean 	flCheck    = false	;
  	private static Intent 			intentRestart		;
  	private volatile boolean isServiceRunning  = false	;
    private static         String  strSelRing = ""		;
    private static MediaPlayer 		mediaPlayer 		;
//    private Timer   timerPing       ;
	private static final int TIME_CHECK = (5*60000)		;
	private static boolean flReconnect = false			;
	private volatile InetAddress remoteAddress = null		;
    private static final int UDP_PORT 	= 3001			;
    private static final int TCP_PORT  = 3000			;
	private  OutputStream    tcpOut    = null			;
  	private  Thread          threadTcp = null			;
  	private  Thread 		 threadUdp = null			;
	private MqttWork 		workerMqtt = null			;
  	private Notification.Builder notiBuilder = null 	;
  	private NotificationManager  notiManager = null		;

  //---------------------------------------------------------------
	@SuppressLint("UnspecifiedRegisterReceiverFlag")
	@Override
	public void onCreate() {
	  super.onCreate()            	;

  // Регистрация ресивера
	  IntentFilter filter = new IntentFilter("TO_MQTT_SERVICE");
	  if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU)
		registerReceiver(receiver, filter)		                ;
	  else registerReceiver(receiver, filter,RECEIVER_EXPORTED);
	  StartForeground()           ;
	}
  //---------------------------------------------------------------
  @Override
  public void onDestroy() {
	super.onDestroy()           ;
	if(workerMqtt != null)
	  workerMqtt.disconnectFromBroker()      ;
//	stopTimerPing()             ;
	isRunning = false           ;
	isServiceRunning = false	;
	if(threadTcp != null) threadTcp.interrupt();	;
	threadTcp = null			;
	if(threadUdp != null) threadUdp.interrupt();	;
	threadUdp = null			;

	if (mediaPlayer != null) {
	  mediaPlayer.release()   	;
	  mediaPlayer = null      	;}
	unregisterReceiver(receiver);
  }
  //---------------------------------------------------------------
  //---------------------------------------------------------------
  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
//        Log.d(TAG, "onStartCommand");
	String 	str	;
	int 	val	;
	if (isServiceRunning){ Log.i(TAG,"check"); LogW("check") ;  return START_STICKY	;}

//	if(flRestarted){ flRestarted = false			; LogW("restarted!")	;} else LogW("onStart")	;
	isServiceRunning = true	;
	isRunning = true        ;
	if(workerMqtt == null){
	  workerMqtt = new MqttWork(this)				;
	  workerMqtt.myCallback = new MqttWork.WorkCallback(){
				@Override public void onReportW(String str)	{ ReportW(str)	;}
				@Override public void onAlarm()				{ Alarm()		;}};
	}

	SharedPreferences prefs         = getSharedPreferences("MQTT_SETTINGS", MODE_PRIVATE);
	val = prefs.getInt   ("PORT"        , 0 )       ; if(val != 0) 	  MqttWork.port 	= val	;
	str = prefs.getString("SRV_ADDRESS" , "")       ; if(str != null) MqttWork.srvAddr 	= str	;
	str = prefs.getString("LOGIN"       , "")       ; if(str != null) MqttWork.login 	= str	;
	str = prefs.getString("PASSWORD"    , "")       ; if(str != null) MqttWork.password	= str	;
	str = prefs.getString("CODE_WORD"   , "")       ; if(str != null) MqttWork.codeWord	= str	;
	str = prefs.getString("USER_UID"    , "")       ; if(str != null) MqttWork.UserUID 	= str	;
    str = prefs.getString("SEL_RINGTONE", "")       ; if(str != null) strSelRing 		= str	;

	str = prefs.getString("LIST_PING"   , "")		;
	if(str != null && !str.isEmpty()) MqttWork.listPing = Arrays.asList(str.split(","))			;
	str = prefs.getString("LIST_PFX"   , "")		;
	if(str != null && !str.isEmpty()) MqttWork.listPfx  = Arrays.asList(str.split(","))			;

	workerMqtt.start()	;

	if(threadUdp != null) threadUdp.interrupt()		;
	threadUdp = new Thread(()-> startUdpServer())	;
	threadUdp.start()		;
//	  stopTimerPing()         ;
//	  timerPing = new Timer() ;
//        if(mqttClient.isConnected()){
//            try{ timerPing.schedule(pingTask, 1000, TIM_PING);
//            } catch(NullPointerException | IllegalStateException | IllegalArgumentException e){
//            }
//        }
//	  	executeTimerTask()	;
//	  	scheduleNextAlarm()	;
	return START_STICKY ;
  }
  //-----------------------------------------------------------------------------
  @Override  public IBinder onBind(Intent intent) { return null	;}
  //---------------------------------------------------------------
  private void StartForeground(){
	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
	  Intent activityIntent = new Intent(this, MainActivity.class);
	  PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
			  activityIntent,PendingIntent.FLAG_IMMUTABLE);

	  notiBuilder = new Notification.Builder(this,CHANNEL_ID)
//			  .setSmallIcon(R.mipmap.ic_launcher)
			  .setSmallIcon(R.drawable.ic_notification)
			  .setContentTitle("My Awesome App")
			  .setContentText("Doing some work...")
			  .setOngoing(true)
			  .setContentIntent(pendingIntent);
	  Notification notification = notiBuilder.build();
	  notiManager = getSystemService(NotificationManager.class);
	  NotificationChannel serviceChannel = new NotificationChannel(
			  CHANNEL_ID, "Foreground Service Channel",
			  NotificationManager.IMPORTANCE_DEFAULT);

	  notiManager.createNotificationChannel(serviceChannel);

	  startForeground(ID_SERVICE, notification);
	  scheduleServiceCheck(this,true)	;
	}
  }
  //---------------------------------------------------------------
  public void updateNotification(String title, String text) {
	  if(title != null && text != null && !title.isEmpty() &&
			  notiBuilder != null && notiManager != null){
		notiBuilder.setContentTitle(title).setContentText(text)		;
		notiManager.notify(ID_SERVICE, notiBuilder.build())			;}
  }
  //---------------------------------------------------------------
  private synchronized void setOutputStream(OutputStream stream) {this.tcpOut = stream; }
  //---------------------------------------------------------------
  private synchronized OutputStream getOutputStream() {	return tcpOut;}
  //---------------------------------------------------------------
  private void startUdpServer() {
	try (DatagramSocket socket = new DatagramSocket(UDP_PORT)) {
	  socket.setBroadcast(true);
	  byte[] buffer = new byte[1024];

	  while (isServiceRunning && !Thread.currentThread().isInterrupted()) {
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
		socket.setSoTimeout(5000); // Таймаут 1 секунда
		try{ socket.receive(packet)	;
		} catch (IOException e) {
		  if (Thread.currentThread().isInterrupted()) { break	;}
		  continue				;}

		remoteAddress = packet.getAddress();
//		LogW("UDP "+remoteAddress.toString())         ;
		restartTcpThread(remoteAddress)	;
	  }
	} catch (IOException e) {
	  Log.e(TAG, "UDP server error", e);
	}
  }
  //-----------------------------------------------------------------------------
  private synchronized void restartTcpThread(InetAddress address) {
	if (threadTcp != null) threadTcp.interrupt()	;
	threadTcp = new Thread(() -> connectTcpClient(address));
	threadTcp.start();
  }
  //-----------------------------------------------------------------------------
  private void connectTcpClient(InetAddress address){
	byte[] recvBuffer = new byte[1024]			;
	OutputStream out	= null					;
	try (Socket socket = new Socket(address, TCP_PORT)) {
	  out = socket.getOutputStream()			;
	  InputStream  in  = socket.getInputStream()			;
	  setOutputStream(out)	;
	  // Ждем окончания TCP-соединения (например, читаем данные пока не закроется)
	  socket.setSoTimeout(5000)					; // Таймаут 1 секунда
	  while (isServiceRunning && !Thread.currentThread().isInterrupted()) {
		try{
		  if(flRestarted){ flRestarted = false	; LogW("restarted!")	;}
		  if(flCheck)	 { flCheck 	   = false	; LogW("Check")			;}
		  int read = in.read(recvBuffer);
		  if(read == -1) break; // соединение закрыто
		  // можно обработать данные здесь
		} catch (SocketTimeoutException e) { continue	;// Продолжить цикл при таймауте
		} catch (IOException e) {
		  Log.e(TAG, "Read error", e);
		  break;
		}
	  }
	} catch (IOException e) {if (isServiceRunning) Log.e(TAG, "TCP error", e);}
	finally{
	  try { if(out != null) out.close(); } catch (IOException ignored) {}
	  setOutputStream(null)	; // Важно сбросить ссылку
	}
  }
  //---------------------------------------------------------------
  void ReportW(String str){
	OutputStream localOut = getOutputStream();
	if (localOut != null){
	try{ localOut.write(str.getBytes())		; localOut.flush()	;
	} catch (IOException e) { Log.e(TAG,"",e)		;}}
  }
  //-----------------------------------------------------------------------------
  private void LogW(String str) { OutputStream localOut = getOutputStream();
	if (localOut == null) return ;
	try{
	  StackTraceElement ste = Thread.currentThread().getStackTrace()[3];
	  str = String.format("[%s:%-4d] %s",ste.getMethodName(),ste.getLineNumber(),str)		;
	  localOut.write(str.getBytes())				; localOut.flush()		;
	} catch (IOException e) { Log.e(TAG,"",e)		;}
  }
//-----------------------------------------------------------------------------
  private void executeTimerTask(){
	PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
	PowerManager.WakeLock wakeLock = powerManager.newWakeLock(
						PowerManager.PARTIAL_WAKE_LOCK,	"MyApp::TimerWakeLock");
	try {
	  wakeLock.acquire(1000L)	; // Удерживаем 1 секунд
	  PingTask()				;// Ваша логика таймера здесь
	} finally { if (wakeLock.isHeld()) wakeLock.release()	;}}
  //---------------------------------------------------------------
  public static void scheduleServiceCheck(Context context,boolean flStart) {
	long triggerTime = SystemClock.elapsedRealtime() + TIME_CHECK;
	AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
	Intent intent = new Intent(context, ServiceRestartReceiver.class)	;
	int flags = PendingIntent.FLAG_UPDATE_CURRENT			;
	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
	  flags |= PendingIntent.FLAG_IMMUTABLE					;

	PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent,flags);
	// Проверка разрешения для Android 13+
	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
	  if (!alarmManager.canScheduleExactAlarms()) {
// Если нет разрешения - вызываем диалоговое окно - запрос разрешения!
		Intent intentEx = new Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
//		context.startActivity(intentEx)				;// чё-то падает :(
		return	;}}
// Установка/сброс будильника
	try{
	  if(flStart){
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
		  alarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP,triggerTime,pendingIntent);
		else alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerTime, pendingIntent);
	  }
	  else alarmManager.cancel(pendingIntent)	;
	} catch(Exception e){	Log.w(TAG, "", e)	;}
  }
  //---------------------------------------------------------------
  //---------------------------------------------------------------
  private void PingTask(){
//	if(mqttClient.isConnected() && listPing.size()>0){
//	  String msg = ""		;
//	  // если был реконнект, то переподписаться на топики!
//	  if(flReconnect){ flReconnect = false	; msg = "reconnect"	;
//		/*for(String pfx : listPfx) subscribeToDevice(pfx)		;*/}
////	  for(String ping : listPing) publishMessage(ping,msg)     	;
//	}
  }
  //---------------------------------------------------------------
  private void Alarm(){
	if(strSelRing != null && !strSelRing.isEmpty()){
	  try{
		Uri soundUri = Uri.parse(strSelRing);
		if(mediaPlayer == null) mediaPlayer = MediaPlayer.create(this, soundUri);
		mediaPlayer.start();
	  }catch(Exception e){ Log.e(TAG, "MediaPlayer error", e);}
	}
  }
  //---------------------------------------------------------------
 //  	private TimerTask pingTask = new TimerTask(){
//    	@Override
//        public void run(){ PingTask()	;}};
	//---------------------------------------------------------------
//	private void stopTimerPing(){ if(timerPing != null){ timerPing.cancel()   ; timerPing = null  ;}}
	//---------------------------------------------------------------
	private final BroadcastReceiver receiver = new BroadcastReceiver() {
	  @Override
	  public void onReceive(Context context, @NonNull Intent intent) {
		if(intent.getAction().equals("TO_MQTT_SERVICE")) {
//                subscribeToDevice(intent.getStringExtra("prefix"));
		  String devUid = intent.getStringExtra("dev_uid")			;
		  String devPfx = intent.getStringExtra("prefix")			;
		  String typeMsg= intent.getStringExtra("type_msg")			;
		  if(typeMsg == null || typeMsg.isEmpty()) return			;

		  switch(typeMsg){
			case "find": workerMqtt.findDevice(devPfx)		; break	;
			default: workerMqtt.workMqtt(typeMsg,devPfx,devUid)	; break	;
		  }

		  workerMqtt.fillListPingPfx(intent.getStringExtra("fill_list_ping"))  ;

		  updateNotification(intent.getStringExtra("noti_title"),
				  			 intent.getStringExtra("noti_text"))	;
		}
	  }
	};
  //---------------------------------------------------------------
  public static class ServiceRestartReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
	  SharedPreferences prefs = context.getSharedPreferences("MQTT_SETTINGS", MODE_PRIVATE);
	  boolean flStart = prefs.getBoolean("START"      ,false)     ;

	  if (!isServiceRunning(context)){
		if(flStart){
		  flRestarted = true	;
		  Intent intentSrv = new Intent(context, MQTTService.class)	;
		  Log.w(TAG, "Service not running! Restarting...");
		  if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
			context.startForegroundService(intentSrv)		;
		  else context.startService(intentSrv)				;}
	  }
	  else{ flCheck = true	; Log.i(TAG,"Check: Service running!")	;}
	  scheduleServiceCheck(context,flStart);
	}

	private boolean isServiceRunning(Context context) {
	  ActivityManager manager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
	  for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
		if (MQTTService.class.getName().equals(service.service.getClassName())) {
		  return true;
		}
	  }
	  return false;
	}
  }
  //---------------------------------------------------------------
}