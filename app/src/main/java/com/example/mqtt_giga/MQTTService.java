package com.example.mqtt_giga;

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
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.mqtt_giga.MqttWork.Message;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

//---------------------------------------------------------------
public class MQTTService extends Service implements MqttCallbackExtended {
    private static final String TAG = "M_SRV";
    private String  serverAddress   ;
	private int     port            ;
    private String  login           ;
    private String  password        ;
    private String  codeWord        ;
    private String  UserUID         ;
    private String  strSelRing      ;
    private MediaPlayer mediaPlayer ;
    private String  BigStrMsg       ;
    private int     ChckCount       ;
    private MqttClient mqttClient   ;
    private List<String> listPing   ;
    private List<String> listPfx    ;
    private Timer   timerPing       ;

    //---------------------------------------------------------------
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
//---------------------------------------------------------------
    private static final int ID_SERVICE = 101;
    private static final String CHANNEL_ID = "ForegroundServiceChannel";
    private static String prevAlarm = ""    ;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");

        // Получаем параметры из интента
        serverAddress = intent.getStringExtra("SERVER_ADDRESS");
        port      = intent.getIntExtra("PORT", 1883)        ;
        login     = intent.getStringExtra("LOGIN")          ;
        password  = intent.getStringExtra("PASSWORD")       ;
//        topicName = intent.getStringExtra("TOPIC_NAME")     ;
        codeWord  = intent.getStringExtra("CODE_WORD")      ;
        UserUID   = intent.getStringExtra("USER_UID")       ;
        strSelRing=intent.getStringExtra("SEL_RINGTONE")    ;
        stopTimerPing()         ;
        timerPing = new Timer() ;
        fillListPing("fill_list")                           ;

        DeviceAlarm.Work(null,null,null)                    ;

        try {
            connectToBroker()   ;
        } catch (MqttException e) {
            Log.e(TAG, "Ошибка подключения к брокеру", e)   ;}
        if(mqttClient.isConnected()){
            for(String pfx : listPfx) subscribeToDevice(pfx);
            try{
                timerPing.schedule(pingTask, 5000, 5000);
            } catch(NullPointerException | IllegalStateException | IllegalArgumentException e){
            }
        }
        return START_STICKY ;
    }
 //---------------------------------------------------------------
    private void fillListPing(String cmd){
        if(cmd == null || cmd.isEmpty()) return             ;

        listPing  = new ArrayList<>()                       ;
        listPfx   = new ArrayList<>()                       ;
        for(DeviceManager.Device dev : DeviceManager.getDeviceList()){
            listPfx.add(dev.getPrefix())                    ;
            String strPing = Message.getPing(dev.getPrefix(),dev.getUID(),UserUID)   ;
            if(!strPing.isEmpty()) listPing.add(strPing)   ;// соберём массив ping строк!
        }
    }
    //---------------------------------------------------------------
    private TimerTask pingTask = new TimerTask(){
    @Override
        public void run(){
          if(mqttClient.isConnected() && listPing.size()>0){
              for(String ping : listPing)
                    publishMessage(ping,"")     ;
          }
        }
    };
//---------------------------------------------------------------
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
    @Override
        public void onReceive(Context context, @NonNull Intent intent) {
            if(intent.getAction().equals("TO_MQTT_SERVICE")) {
//                subscribeToDevice(intent.getStringExtra("prefix"));
                pingDevice(intent.getStringExtra("ping"));
                fillListPing(intent.getStringExtra("fill_list_ping"))  ;
				workMqtt(intent.getStringExtra("prefix"),
						 intent.getStringExtra("type_msg"),
						 intent.getStringExtra("dev_uid"));
            }
        }
    };
    //---------------------------------------------------------------
    private void pingDevice(String pfx) {
        if(pfx != null && !pfx.isEmpty()) publishMessage(pfx,UserUID) ;
    }
    //---------------------------------------------------------------
    private void subscribeToDevice(String pfx) {
        if(pfx != null && !pfx.isEmpty()){
            subscribeToTopic(pfx + "/#")        ;
        }
    }
//---------------------------------------------------------------
private void workMqtt(String pfx,String typeMsg, String devUID){
	if(!pfx.isEmpty() && !typeMsg.isEmpty()){
	  Message msg = new Message(pfx, typeMsg, UserUID, devUID);
	  if(!msg.getTopic().isEmpty()){
		publishMessage(msg.getTopic(), msg.getMsg());
	  }
	}
}
//---------------------------------------------------------------
//    @SuppressLint("UnspecifiedRegisterReceiverFlag")
	@Override
    public void onCreate() {
        super.onCreate()            ;

        // Регистрация ресивера
        IntentFilter filter = new IntentFilter("TO_MQTT_SERVICE");
        registerReceiver(receiver, filter,RECEIVER_NOT_EXPORTED);

        StartForeground()           ;
        BigStrMsg = ""              ;
        ChckCount = 0               ;
    }
    //---------------------------------------------------------------
    @Override
    public void onDestroy() {
        super.onDestroy()           ;
        stopTimerPing()             ;
        disconnectFromBroker()      ;
        if (mediaPlayer != null) {
            mediaPlayer.release()   ;
            mediaPlayer = null      ;}
        unregisterReceiver(receiver);
    }
    //---------------------------------------------------------------
    private void stopTimerPing(){ if(timerPing != null){ timerPing.cancel()   ; timerPing = null  ;}}

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
    // Подключение к брокеру
    private void connectToBroker() throws MqttException {
      MemoryPersistence persistence = new MemoryPersistence();
	  String brokerUrl   = "tcp://" + serverAddress + ":" + port;

        if(UserUID.length() < 5)
            UserUID = MqttClient.generateClientId()             ;

        if(mqttClient == null)
           mqttClient = new MqttClient(brokerUrl, UserUID, persistence);

        if(mqttClient.isConnected()) return ;

        MqttConnectOptions connOpts = new MqttConnectOptions();
        connOpts.setCleanSession(true);
        connOpts.setAutomaticReconnect(true);
        if(login != null && password != null && !login.isEmpty() && !password.isEmpty()){
            connOpts.setUserName(login);
            connOpts.setPassword(password.toCharArray());}
        mqttClient.connect(connOpts);
        Log.d(TAG, "Connected to broker");

        Intent broadcastIntent = new Intent()               ;
        broadcastIntent.setAction("FROM_MQTT_SERVICE")  ;
        broadcastIntent.putExtra("state","MQTT broker: Connected to broker")          ;
        broadcastIntent.putExtra("user_uid",UserUID)        ;
        sendBroadcast(broadcastIntent)                      ;

        mqttClient.setCallback(this);
 //       subscribeToTopic(topicName);
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
        String StrMsg = message.toString()  ;
        String StrAlarm = ""                ;
        String devPfx = ""                  ;
        boolean     isJSON = true           ;
        boolean     flAlarm = false         ;
        // меняем теги (типа #1a, #69...) на ключевые слова, это надо для JSON разбора!
        StrMsg = new MqttWork().ReplaceTag(StrMsg)      ;
        // проверим баланс скобок
        boolean CheckBracket = new MqttWork().checkBracketsBalance(StrMsg)  ;
        if(CheckBracket || ChckCount > 5){
            BigStrMsg = ""          ; ChckCount = 0 ;}
        if(!CheckBracket) {
            BigStrMsg += StrMsg     ;// если в сообщении нарушен баланс скобок, то
            ChckCount++             ;// попытаемся "склеить" несколько сообщений в одно
            CheckBracket = new MqttWork().checkBracketsBalance(BigStrMsg)  ;
            if(CheckBracket){
                StrMsg = BigStrMsg  ;// если удалось "склеить" несколько сообщений в одно
                BigStrMsg = ""      ; ChckCount = 0 ;
            }
        }
        // если CheckBracket == true
        String LogStrMsg =  String.format("(%d,%b) %s",StrMsg.length(),CheckBracket,StrMsg) ;
        Log.i(TAG,LogStrMsg)               ;

        try{
            if(!StrMsg.isEmpty() && StrMsg.length() < 5000){
                Message msg = new Message(topic,StrMsg)                         ;// определим тип сообщения
                if(msg.getType() == Message.TypeMsg.update){
                   devPfx   = msg.getDevPfx()                                       ;
                   StrAlarm = new MyParserJson(StrMsg).GetKey("updates,"+codeWord)  ;
                   flAlarm  = alarmWork(devPfx,codeWord,StrAlarm)                   ;
                }
            }
            else isJSON = false  ;
        }catch(IllegalArgumentException e){
            isJSON = false  ;
        }

        if(flAlarm){
            if (!strSelRing.isEmpty() && !prevAlarm.isEmpty()) {
                Uri soundUri = Uri.parse(strSelRing)        ;
                if (mediaPlayer == null)
                    mediaPlayer = MediaPlayer.create(this, soundUri);
                mediaPlayer.start()                         ;
            }
            prevAlarm = StrAlarm                            ;
        }

        // Отправка данных в активность
        Intent broadcastIntent = new Intent()               ;
        broadcastIntent.setAction("FROM_MQTT_SERVICE")      ;
        broadcastIntent.putExtra("topic"  ,topic)           ;
        if(!StrMsg.isEmpty())
            broadcastIntent.putExtra("message",StrMsg)      ;
        if(!StrAlarm.isEmpty())
            broadcastIntent.putExtra("alarm"  ,StrAlarm)    ;
        sendBroadcast(broadcastIntent)                      ;
    }

    //---------------------------------------------------------------
    private boolean alarmWork(String devPfx, String codeWord, String strAlarm){
        boolean fl = DeviceAlarm.Work(devPfx, codeWord,strAlarm)  ;
        return  fl          ;}

    //---------------------------------------------------------------
    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        Log.d(TAG, "Сообщение доставлено успешно");
    }
    //---------------------------------------------------------------
    @Override
    public void connectionLost(Throwable cause) {
        Log.w(TAG, "Потеря соединения " + cause.getMessage(), cause);

	  try{
          if(mqttClient.isConnected())
		     mqttClient.disconnect()    ;
	  } catch(MqttException e){
          Log.e(TAG, "Ошибка отключения от брокера", e);
	  }

	  Intent broadcastIntent = new Intent()               ;
        broadcastIntent.setAction("FROM_MQTT_SERVICE")  ;
        broadcastIntent.putExtra("state","MQTT broker: Потеря соединения" + cause.getMessage())          ;
        sendBroadcast(broadcastIntent)                      ;
    }
    //---------------------------------------------------------------
    @Override
    public void connectComplete(boolean reconnect, String serverURI) {
        Log.d(TAG, "Connect Complete" + serverURI);

        Intent broadcastIntent = new Intent()               ;
        broadcastIntent.setAction("FROM_MQTT_SERVICE")  ;
        broadcastIntent.putExtra("state","MQTT broker: Соединение завершено " + serverURI)          ;
        sendBroadcast(broadcastIntent)                      ;
    }

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