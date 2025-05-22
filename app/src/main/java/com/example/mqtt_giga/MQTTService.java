package com.example.mqtt_giga;

import android.annotation.SuppressLint;
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
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.NonNull;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import com.example.mqtt_giga.MqttWork.Message;
import com.example.mqtt_giga.DeviceManager.Device;
//---------------------------------------------------------------
public class MQTTService extends Service implements MqttCallbackExtended {
    private static final String    TAG         = "M_SRV";
  	private static final int ID_SERVICE = 101;
  	private static final String CHANNEL_ID = "ForegroundServiceChannel";
    public  static volatile boolean isRunning   = false   ;
    public  static volatile boolean isConnected = false   ;
    private static String   srvAddr     = ""   	;
	private static int      port        = 1883	;
    private static String   login   	= ""	;
    private static String   password 	= ""	;
    private static String  	codeWord   	= ""	;
    private static String  	UserUID    	= ""	;
    private static String  	strSelRing 	= ""	;
    private static MediaPlayer 		mediaPlayer 		;
    private static MqttClient 		mqttClient   		;
    private static List<String> 	listPing = null		;
    private static List<String> 	listPfx  = null		;
//    private Timer   timerPing       ;
	private static final int TIM_PING = 20000			;
	private static boolean flReconnect = false			;
	private static InetAddress remoteAddress = null		;
    private static final int UDP_PORT = 3001			;
    private static final int TCP_PORT = 3000			;
	private static OutputStream out = null				;
  	private Thread threadTcp		;

  //---------------------------------------------------------------
	@SuppressLint("UnspecifiedRegisterReceiverFlag")
	@Override
	public void onCreate() {
	  super.onCreate()            	;
	  listPing  = new ArrayList<>() ;
	  listPfx   = new ArrayList<>() ;

	  // Регистрация ресивера
	  IntentFilter filter = new IntentFilter("TO_MQTT_SERVICE");
	  if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU)
		registerReceiver(receiver, filter)		                ;
	  else registerReceiver(receiver, filter,RECEIVER_NOT_EXPORTED);

	  StartForeground()           ;
	  new Thread(this::startUdpServer).start();
	  isRunning = true            ;
	}
  //---------------------------------------------------------------
  private void startUdpServer() {
	try (DatagramSocket socket = new DatagramSocket(UDP_PORT)) {
	  socket.setBroadcast(true);
	  byte[] buffer = new byte[1024];

	  while (isRunning) {
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
		socket.receive(packet);

		remoteAddress = packet.getAddress();
//		LogW("UDP "+remoteAddress.toString())         ;
		if(threadTcp != null) threadTcp.stop()		;
		threadTcp = new Thread(){
		  @Override  public void run(){ connectTcpClient(remoteAddress);}};
		threadTcp.start()	;
	  }
	} catch (IOException e) {
	  e.printStackTrace();
	}
  }
  //-----------------------------------------------------------------------------
  private boolean LogW(String str)
  {if(out == null) return false	;
	try{											//https://j2w.blogspot.com/2008/10/java_1133.html
	  StackTraceElement ste = Thread.currentThread().getStackTrace()[3];
	  str = String.format("[%s:%-4d] %s",ste.getMethodName(),ste.getLineNumber(),str)		;
	  out.write(str.getBytes())		; out.flush()		;
	} catch (IOException e) { e.printStackTrace()		;}
   return true	;}
//-----------------------------------------------------------------------------
  private void connectTcpClient(InetAddress address){
	try (Socket tcpSocket = new Socket(address, TCP_PORT)) {
	  out = tcpSocket.getOutputStream();
	  InputStream  in  = tcpSocket.getInputStream();
	  // Ждем окончания TCP-соединения (например, читаем данные пока не закроется)
	  byte[] recvBuffer = new byte[1024];
	  while (isRunning) {
		int read = in.read(recvBuffer);
		if (read == -1) break; // соединение закрыто
		// можно обработать данные здесь
	  }
	  out = null				;
	} catch (IOException e) {e.printStackTrace()		;}
  }
  //---------------------------------------------------------------
  @Override
  public void onDestroy() {
	isRunning = false           ;
	super.onDestroy()           ;
//	stopTimerPing()             ;
	disconnectFromBroker()      ;
	if(threadTcp != null) threadTcp.stop()		;
	threadTcp = null			;

	if (mediaPlayer != null) {
	  mediaPlayer.release()   	;
	  mediaPlayer = null      	;}
	unregisterReceiver(receiver);
  }
  //---------------------------------------------------------------
	private void StartForeground(){
	  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
		Intent activityIntent = new Intent(this, MainActivity.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
				activityIntent,PendingIntent.FLAG_IMMUTABLE);

		Notification.Builder notificationBuilder = new Notification.Builder(this,CHANNEL_ID)
				.setSmallIcon(R.mipmap.ic_launcher)
				.setContentTitle("My Awesome App")
				.setContentText("Doing some work...")
				.setOngoing(true)
				.setContentIntent(pendingIntent);
		Notification notification = notificationBuilder.build();

		NotificationManager manager = getSystemService(NotificationManager.class);

		NotificationChannel serviceChannel = new NotificationChannel(
				CHANNEL_ID, "Foreground Service Channel",
				NotificationManager.IMPORTANCE_DEFAULT);

		manager.createNotificationChannel(serviceChannel);

		startForeground(ID_SERVICE, notification);
	  }
	}
  //---------------------------------------------------------------
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
//---------------------------------------------------------------
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        Log.d(TAG, "onStartCommand");
		String 	str	;
		int 	val	;
		if(mqttClient == null){
		  // Получаем параметры из интента
		  val = intent.getIntExtra("PORT", 0)			; if(val != 0) port = val;
		  str = intent.getStringExtra("SERVER_ADDRESS")	; if(str != null) srvAddr 	= str	;
		  str = intent.getStringExtra("LOGIN")			; if(str != null) login 	= str	;
		  str = intent.getStringExtra("PASSWORD")		; if(str != null) password 	= str	;
		  str = intent.getStringExtra("CODE_WORD")		; if(str != null) codeWord 	= str	;
		  str = intent.getStringExtra("USER_UID")		; if(str != null) UserUID 	= str	;
		  str = intent.getStringExtra("SEL_RINGTONE")	; if(str != null) strSelRing = str	;
		}
	  	if(mqttClient == null || !mqttClient.isConnected()){
		  DeviceAlarm.Work(null, null, null)			;
		  fillListPingPfx("fill_list")					;

		  try{ connectToBroker()						;
		  } catch(MqttException e){
			Log.e(TAG, "Ошибка подключения к брокеру", e);
		  }
		}
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

  private void executeTimerTask(){
	PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
	PowerManager.WakeLock wakeLock = powerManager.newWakeLock(
						PowerManager.PARTIAL_WAKE_LOCK,	"MyApp::TimerWakeLock");
	try {
	  wakeLock.acquire(1000L)	; // Удерживаем 1 секунд
	  PingTask()				;// Ваша логика таймера здесь
	} finally { if (wakeLock.isHeld()) wakeLock.release()	;}}
  //---------------------------------------------------------------
  private void scheduleNextAlarm() {
	long nextTriggerTime = SystemClock.elapsedRealtime() + TIM_PING;
	AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
	Intent intent = new Intent(this, AlarmReceiver.class)	;
	int flags = PendingIntent.FLAG_UPDATE_CURRENT			;
	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
	  flags |= PendingIntent.FLAG_IMMUTABLE					;

	PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent,flags);
	// Проверка разрешения для Android 13+
	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
	  if (!alarmManager.canScheduleExactAlarms()) {
		requestScheduleExactAlarmPermission()	; return	;}}
// Установка будильника
	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
	  alarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP,
			  nextTriggerTime, pendingIntent)			;}
	else { alarmManager.setExact( AlarmManager.ELAPSED_REALTIME_WAKEUP,
			  nextTriggerTime, pendingIntent )			;}
  }
  //---------------------------------------------------------------
  private void requestScheduleExactAlarmPermission(){
	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
	  Intent intent = new Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
	  startActivity(intent);
	}
  }
  //---------------------------------------------------------------
  private void PingTask(){
	if(mqttClient.isConnected() && listPing.size()>0){
	  String msg = ""		;
	  // если был реконнект, то переподписаться на топики!
	  if(flReconnect){ flReconnect = false	; msg = "reconnect"	;
		/*for(String pfx : listPfx) subscribeToDevice(pfx)		;*/}
	  for(String ping : listPing) publishMessage(ping,msg)     	;
	}
  }
  //---------------------------------------------------------------
//  	private TimerTask pingTask = new TimerTask(){
//    	@Override
//        public void run(){ PingTask()	;}};
	//---------------------------------------------------------------
//	private void stopTimerPing(){ if(timerPing != null){ timerPing.cancel()   ; timerPing = null  ;}}
	//---------------------------------------------------------------
	private void fillListPingPfx(String cmd){
  		if(cmd == null || cmd.isEmpty() ||
		  UserUID == null || UserUID.isEmpty()) return  	;

  		listPing  = new ArrayList<>()                       ;
  		listPfx   = new ArrayList<>()                       ;

  		for(Device dev : DeviceManager.getDeviceList()){
			listPfx.add(dev.getPrefix())                    ;
			String strPing = Message.getPing(dev.getPrefix(),dev.getUID(),UserUID)   ;
			if(!strPing.isEmpty()) listPing.add(strPing)   ;// соберём массив ping строк!
  		}
	}
  //---------------------------------------------------------------
    private void findDevice(String pfx) {
        if(pfx != null) publishMessage(pfx,UserUID) ;
    }
    //---------------------------------------------------------------
    private void subscribeToDevice(String pfx) {
        if(pfx != null && !pfx.isEmpty()){
            subscribeToTopic(pfx + "/#")        ;
        }
    }
//---------------------------------------------------------------
private void workMqtt(String pfx,String typeMsg, String devUID){
	if(pfx != null && typeMsg != null && UserUID != null
            && !pfx.isEmpty() && !typeMsg.isEmpty() && !UserUID.isEmpty()){
	  Message msg = new Message(pfx, typeMsg, UserUID, devUID);
	  publishMessage(msg.getTopic(), msg.getMsg());
	}
}
//---------------------------------------------------------------
	private final BroadcastReceiver receiver = new BroadcastReceiver() {
	  @Override
	  public void onReceive(Context context, @NonNull Intent intent) {
		if(intent.getAction().equals("TO_MQTT_SERVICE")) {
//                subscribeToDevice(intent.getStringExtra("prefix"));
		  findDevice(intent.getStringExtra("find"));
		  fillListPingPfx(intent.getStringExtra("fill_list_ping"))  ;
		  workMqtt(intent.getStringExtra("prefix"),
				  intent.getStringExtra("type_msg"),
				  intent.getStringExtra("dev_uid"));
		}
	  }
	};
  //---------------------------------------------------------------
    // Подключение к брокеру
    private void connectToBroker() throws MqttException {
      MemoryPersistence persistence = new MemoryPersistence();
	  String brokerUrl   = "tcp://" + srvAddr + ":" + port;

        if(UserUID == null || UserUID.length() < 5){
            // !!! сделал так потому-что MqttClient.generateClientId() генерирует очень длинный ClientId !!!!
            UserUID = String.format("%08x", MqttClient.generateClientId().hashCode())  ;}

        if(mqttClient == null)
           mqttClient = new MqttClient(brokerUrl, UserUID, null);// persistence);

        if(mqttClient.isConnected()) return ;

        MqttConnectOptions connOpts = new MqttConnectOptions();
        connOpts.setCleanSession(true)						;
        connOpts.setAutomaticReconnect(true)				;
        if(login != null && password != null && !login.isEmpty() && !password.isEmpty()){
            connOpts.setUserName(login)						;
            connOpts.setPassword(password.toCharArray())	;}
        mqttClient.connect(connOpts)						;
        Log.d(TAG, "Connected to broker")					;

        Intent broadcastIntent = new Intent()               ;
        broadcastIntent.setAction("FROM_MQTT_SERVICE")  	;
        broadcastIntent.putExtra("state","MQTT broker: Connected to broker")          ;
        broadcastIntent.putExtra("user_uid",UserUID)        ;
        sendBroadcast(broadcastIntent)                      ;

        mqttClient.setCallback(this)						;
		if(mqttClient.isConnected() && listPfx != null)
		  for(String pfx : listPfx) subscribeToDevice(pfx)	;
    }
    //---------------------------------------------------------------
    // Отключение от брокера
    private void disconnectFromBroker() {
        try {
            if(mqttClient.isConnected())
               mqttClient.disconnect();

            Intent broadcastIntent = new Intent()               ;
            broadcastIntent.setAction("FROM_MQTT_SERVICE")      ;
            broadcastIntent.putExtra("state","MQTT broker: Disconnect from broker")          ;
            sendBroadcast(broadcastIntent)                      ;
        } catch (MqttException e) {
            Log.e(TAG, "Ошибка отключения от брокера", e);
        }
    }
    //---------------------------------------------------------------
    // Подписка на тему
    private void subscribeToTopic(String topic) {
        try {
            if(mqttClient.isConnected())
               mqttClient.subscribe(topic, 0);
        } catch (MqttException e) {
            Log.e(TAG, "Ошибка подписки на тему", e);

            Intent broadcastIntent = new Intent()               ;
            broadcastIntent.setAction("FROM_MQTT_SERVICE")  ;
            broadcastIntent.putExtra("message","Ошибка подписки на тему " + e)          ;
            sendBroadcast(broadcastIntent)                      ;
        }
    }
    //---------------------------------------------------------------
    // Отправка сообщения
    private void publishMessage(String topic,String message) {
	    if(topic.isEmpty()) return	;
        try {
            MqttMessage mqttMessage = new MqttMessage(message.getBytes("UTF-8"));
            mqttMessage.setRetained(false);
            mqttMessage.setQos(0);
            if(mqttClient.isConnected())
                mqttClient.publish(topic, mqttMessage);
        } catch (UnsupportedEncodingException | MqttException e) {
            Log.e(TAG, "Ошибка отправки сообщения", e);
        }
    }
    //---------------------------------------------------------------
    // Реализация коллбэков MQTT
    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
	  String 	StrMsg = message.toString() ;
	  String 	StrAlarm = "", report = ""  ;
	  String 	devPfx = ""                 ;
	  boolean flAlarm = false         		;

	  StrMsg = MqttWork.prework(StrMsg)		;
	  // если CheckBracket == true
	  String LogStrMsg = String.format("(%d,%b) %s",StrMsg.length(),MqttWork.CheckBracket,StrMsg) ;
	  Log.i(TAG,LogStrMsg)               	;

	  try{
		if(!StrMsg.isEmpty() && StrMsg.length() < 5000 && MqttWork.CheckBracket){
		  Message msg = new Message(topic,StrMsg)  			;// определим тип сообщения
		  report = msg.report()	;
		  if(msg.getType() == Message.TypeMsg.update){
			devPfx   = msg.getDevPfx()               		;
			StrAlarm = new MyParserJson(StrMsg).GetKey("updates,"+codeWord)	;
			flAlarm  = DeviceAlarm.Work(devPfx,codeWord,StrAlarm)           ;}
		}
	  }catch(IllegalArgumentException e){
	  }

	  if (flAlarm && !strSelRing.isEmpty()) {
		Uri soundUri = Uri.parse(strSelRing)        		;
		if (mediaPlayer == null)
		  mediaPlayer = MediaPlayer.create(this, soundUri)	;
		mediaPlayer.start()                         		;}

	  LogW(report)		;

        // Отправка данных в активность
	  if(MqttWork.CheckBracket){
		Intent broadcastIntent = new Intent()               ;
		broadcastIntent.setAction("FROM_MQTT_SERVICE")      ;
		broadcastIntent.putExtra("topic"  ,topic)           ;
		if(!StrMsg.isEmpty())
		  broadcastIntent.putExtra("message",StrMsg)      	;
		if(!StrAlarm.isEmpty())
		  broadcastIntent.putExtra("alarm"  ,StrAlarm)    	;
		sendBroadcast(broadcastIntent)                      ;}
    }

    //---------------------------------------------------------------
    @Override
    public void connectionLost(Throwable cause) {
        Log.w(TAG, "Потеря соединения " + cause.getMessage(), cause);
		flReconnect = true	;
	  LogW(String.format("reconnect %s",mqttClient.isConnected() ? "conn" : "no conn"))	;
//	  try{
//         mqttClient.reconnect()    ;
//		 if(listPfx != null) for(String pfx : listPfx) subscribeToDevice(pfx)		;
//	  } catch(MqttException e){
//          Log.e(TAG, "Ошибка reconnect", e);
//	  }
//	  LogW(String.format("after reconnect %s",mqttClient.isConnected() ? "conn" : "no conn"))	;
	  Intent broadcastIntent = new Intent()               ;
      broadcastIntent.setAction("FROM_MQTT_SERVICE")  ;
      broadcastIntent.putExtra("state","Потеря соединения" + cause.getMessage())          ;
      sendBroadcast(broadcastIntent)                      ;
    }
    //---------------------------------------------------------------
    @Override
    public void connectComplete(boolean reconnect, String serverURI) {
        Log.d(TAG, "Connect Complete" + serverURI)			;
	  	if(listPfx != null) for(String pfx : listPfx) subscribeToDevice(pfx)		;

        Intent broadcastIntent = new Intent()               ;
        broadcastIntent.setAction("FROM_MQTT_SERVICE")  	;
        broadcastIntent.putExtra("state","Соединение завершено " + serverURI)          ;
        sendBroadcast(broadcastIntent)                      ;
		LogW("Соединение завершено "+ (mqttClient.isConnected() ? "conn" : "no conn"))	;
    }
  //---------------------------------------------------------------
  @Override
  public void deliveryComplete(IMqttDeliveryToken token) {Log.i(TAG, "Сообщение доставлено успешно");}
  //============================================
    private static class DeviceAlarm{
        private String pfx      ;
        private String codeW    ;
        private String alrm     ;

        private static List<DeviceAlarm>    list    ;

        public DeviceAlarm(String devPfx, String codeWord, String strAlarm){
            pfx = devPfx    ; codeW = codeWord  ; alrm = strAlarm   ;}

        public static boolean Work(String devPfx, String codeWord, String strAlarm){
          boolean fl = false, isNew = true    ;
          if(devPfx == null || codeWord == null || strAlarm == null){
              list = new ArrayList<>()   ; return false ;}

          for(DeviceAlarm da:list){
                if(da.pfx.equals(devPfx) && da.codeW.equals(codeWord)){
                    isNew = false                       ;// такой dewPfx,codeWord уже есть!
                    fl = !da.alrm.equals(strAlarm)      ;// strAlarm не совпадает!!! ТРЕВоГА
                    if(fl) da.alrm = strAlarm           ;
                    break   ;
                }
          }
          if(isNew) list.add(new DeviceAlarm(devPfx,codeWord,strAlarm)) ;
          return fl   ;}
    }
}