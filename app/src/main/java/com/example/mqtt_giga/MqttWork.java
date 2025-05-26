package com.example.mqtt_giga;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

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
import java.util.Stack;
//---------------------------------------------------------------------------------
public class MqttWork implements MqttCallbackExtended{
  private static final String TAG = "MQTT_WORK";
  private static final String HubTag[] = {
		  "api_v","id","client","type","update","updates","get","last","crc32","discover","name",
		  "prefix","icon","PIN","version","max_upl","http_t","ota_t","ws_port","modules","total",
		  "used","code","OK",
		  "ack","info","controls","ui","files","notice","alert","push","script","refresh","print",
		  "error","fs_err","ota_next","ota_done","ota_err","fetch_start","fetch_chunk","fetch_err",
		  "upload_next","upload_done","upload_err","ota_url_err","ota_url_ok",
		  "value","maxlen","rows","regex","align","min","max","step","dec","unit","fsize","action",
		  "nolabel","suffix","notab","square","disable","hint","len","wwidth","wheight","data",
		  "func","keep","exp",
		  "plugin","js","css","ui_file","stream","port","canvas","width","height","active","html",
		  "dummy","menu","gauge","gauge_r","gauge_l","led","log","table","image","text","display",
		  "text_f","label","title","dpad","joy","flags","tabs","switch_t","switch_i","button",
		  "color","select","spinner","slider","datetime","date","time","confirm","prompt","area",
		  "pass","input","hook","row","col","space","platform"};
  private static String BigStrMsg 	  = ""		;
  private static int 	ChckCount 	  = 0		;
  public  static boolean CheckBracket = false	;

  public  static volatile boolean isConnected = false   ;
  public static String 		srvAddr 	= ""   	;
  public static  int    	port    	= 1883	;
  public static String 		login   	= ""	;
  public static String   	password 	= ""	;
  public static String  	codeWord   	= ""	;
  public static String  	UserUID    	= ""	;
  private static MqttClient   mqttClient   		;
  public static List<String> listPing = null	;
  public static List<String> listPfx  = null	;
  private static boolean flReconnect = false	;
  private static Context	context = null		;
  public WorkCallback myCallback				;

  //---------------------------------------------------------------------------------
  public MqttWork(Context _con){ context = _con	;}
  //---------------------------------------------------------------------------------
  public static String   ReplaceTag(String Str){
	int SizeArr = HubTag.length     ;
	String  strT, strR              ;
	for(int ix=SizeArr-1;ix>=0;ix--){
	  strT = String.format("#%x",ix)      ;
	  strR = String.format("\"%s\"",HubTag[ix])           ;
	  Str = Str.replace(strT,strR);
	}
	Str = Str.replace("#","")       ;
	return Str   ;}
  //---------------------------------------------------------------------------------
  // Этот метод написал GIGAchat  !!!!
  public static boolean checkBracketsBalance(String input) {
	if (input == null || input.isEmpty())
	  return true;

	Stack<Character> stack = new Stack<>();

	for (char ch : input.toCharArray()) {
	  switch(ch) {
		case '(':
		case '[':
		case '{':
		  stack.push(ch); // Открывающая скобка помещается в стек
		  break;

		case ')':
		  if (!stack.isEmpty() && stack.peek() == '(')
			stack.pop(); // Закрывающая соответствует открывающей
		  else
			return false; // Нарушение баланса
		  break;

		case ']':
		  if (!stack.isEmpty() && stack.peek() == '[')
			stack.pop();
		  else
			return false;
		  break;

		case '}':
		  if (!stack.isEmpty() && stack.peek() == '{')
			stack.pop();
		  else
			return false;
		  break;
	  }
	}

	return stack.isEmpty(); // Если стек пуст — значит всё сбалансировано
  }
  //---------------------------------------------------------------------------------
  public static String prework(String strMsg){
	// меняем теги (типа #1a, #69...) на ключевые слова, это надо для JSON разбора!
	strMsg = ReplaceTag(strMsg)      ;
	// проверим баланс скобок
	CheckBracket = checkBracketsBalance(strMsg)  ;
	if(CheckBracket || ChckCount > 5){
	  BigStrMsg = ""          ; ChckCount = 0 ;}
	if(!CheckBracket) {
	  BigStrMsg += strMsg     ;// если в сообщении нарушен баланс скобок, то
	  ChckCount++             ;// попытаемся "склеить" несколько сообщений в одно
	  CheckBracket = checkBracketsBalance(BigStrMsg)  ;
	  if(CheckBracket){
		strMsg = BigStrMsg  ;// если удалось "склеить" несколько сообщений в одно
		BigStrMsg = ""      ; ChckCount = 0 ;
	  }
	}
	return strMsg	;
  }
  //---------------------------------------------------------------------------------
  public void findDevice(String pfx) {
	if(pfx != null && !pfx.isEmpty()) publishMessage(pfx,UserUID) ;}
  //---------------------------------------------------------------
  private void subscribeTopics(){
	if(mqttClient.isConnected() && listPfx != null)
	  for(String pfx : listPfx) subscribeToDevice(pfx)	;}
  //---------------------------------------------------------------
  private void subscribeToDevice(String pfx) {
	if(pfx != null && !pfx.isEmpty()) subscribeToTopic(pfx + "/#")  ;}
  //---------------------------------------------------------------
  public void start(){
	if(mqttClient == null || !mqttClient.isConnected()){
	  DeviceAlarm.Work(null, null, null)			;
//	  fillListPingPfx("fill_list")					;

	  try{ connectToBroker()						;
	  } catch(MqttException e){
		Log.e(TAG, "Ошибка подключения к брокеру", e);
	  }
	}
  }
  //---------------------------------------------------------------------------------
  // Подключение к брокеру
  private void connectToBroker() throws MqttException{
	MemoryPersistence persistence = new MemoryPersistence();
	String            brokerUrl   = "tcp://" + srvAddr + ":" + port;

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
	  connOpts.setPassword(password.toCharArray())		;}
	mqttClient.connect(connOpts)						;
	mqttClient.setCallback(this)						;
	subscribeTopics()									;
	Log.d(TAG, "Connected to broker")					;
	reportBr("user_uid",UserUID,"state","Connected to broker")	;
  }
  //---------------------------------------------------------------
  // Отключение от брокера
  public void disconnectFromBroker() {
	try { if(mqttClient.isConnected()) mqttClient.disconnect()	;
	  reportBr("state","Disconnect from broker")			;
	} catch (MqttException e) {Log.e(TAG, "Ошибка отключения от брокера", e);}
  }
  //---------------------------------------------------------------
  // Подписка на тему
  private void subscribeToTopic(String topic) {
	try { if(mqttClient.isConnected())	mqttClient.subscribe(topic, 0);
	} catch (MqttException e) {
	  Log.e(TAG, "Ошибка подписки на тему", e)				;
	  reportBr("message","Ошибка подписки на тему " + e)	;
	}
  }
  //---------------------------------------------------------------
  // Отправка сообщения
  public void publishMessage(String topic, String message) {
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
  public void connectComplete(boolean reconnect, String serverURI){
	Log.d(TAG, "Connect Complete" + serverURI)			;
	if(listPfx != null) for(String pfx : listPfx) subscribeToDevice(pfx)		;

	reportBr("state","Соединение завершено " + serverURI)          ;
	reportW(String.format("Соединение завершено  %s",mqttClient.isConnected() ? "conn" : "no conn"))	;
//	LogW("Соединение завершено "+ (mqttClient.isConnected() ? "conn" : "no conn"))	;
  }
  //---------------------------------------------------------------------------------
  @Override
  public void connectionLost(Throwable cause){
	Log.w(TAG, "Потеря соединения " + cause.getMessage(), cause);
	flReconnect = true	;
	reportBr("state",String.format("Потеря соединения  %s",mqttClient.isConnected() ? "conn" : "no conn"))	;
	reportW(String.format("Потеря соединения  %s",mqttClient.isConnected() ? "conn" : "no conn"))	;

//	  try{
//         mqttClient.reconnect()    ;
//		 if(listPfx != null) for(String pfx : listPfx) subscribeToDevice(pfx)		;
//	  } catch(MqttException e){
//          Log.e(TAG, "Ошибка reconnect", e);
//	  }
  }
  //---------------------------------------------------------------------------------
  @Override
  public void messageArrived(String topic, MqttMessage message) throws Exception{
	String 	StrMsg = message.toString() ;
	String 	StrAlarm = "", report = ""  ;
	String 	devPfx = ""                 ;
	boolean flAlarm = false         		;

	StrMsg = prework(StrMsg)		;
	// если CheckBracket == true
	String LogStrMsg = String.format("(%d,%b) %s",StrMsg.length(),CheckBracket,StrMsg) ;
	Log.i(TAG,LogStrMsg)               	;

	try{
	  if(!StrMsg.isEmpty() && StrMsg.length() < 5000 && CheckBracket){
		Message msg = new Message(topic,StrMsg)  			;// определим тип сообщения
		report = msg.report()	;
		if(msg.getType() == Message.TypeMsg.update){
		  devPfx   = msg.getDevPfx()               		;
		  StrAlarm = new MyParserJson(StrMsg).GetKey("updates,"+codeWord)	;
		  flAlarm  = DeviceAlarm.Work(devPfx,codeWord,StrAlarm)           ;}
	  }
	}catch(IllegalArgumentException e){
	}

	// TODOOO
	if (flAlarm && myCallback != null) myCallback.onAlarm()	;
//			!strSelRing.isEmpty()) {
//	  Uri soundUri = Uri.parse(strSelRing)        		;
//	  if (mediaPlayer == null)
//		mediaPlayer = MediaPlayer.create(this, soundUri)	;
//	  mediaPlayer.start()                         		;}

	reportW(report)		;

	// Отправка данных в активность
	if(CheckBracket){
	  reportBr("topic",topic,"alarm",StrAlarm,"message",StrMsg)    	;
	}
  }
  //---------------------------------------------------------------------------------
  @Override  public void deliveryComplete(IMqttDeliveryToken token){ }
  //---------------------------------------------------------------------------------
  public void fillListPingPfx(String cmd){
	if(cmd == null || cmd.isEmpty() ||
			UserUID == null || UserUID.isEmpty()) return  	;

	listPing  = new ArrayList<>()                       ;
	listPfx   = new ArrayList<>()                       ;

	for(DeviceManager.Device dev : DeviceManager.getDeviceList()){
	  listPfx.add(dev.getPrefix())                    ;
	  String strPing = Message.getPing(dev.getPrefix(),dev.getUID(),UserUID)   ;
	  if(!strPing.isEmpty()) listPing.add(strPing)   ;// соберём массив ping строк!
	}
  }
  //---------------------------------------------------------------
  public void workMqtt(String pfx,String typeMsg, String devUID){
	if(pfx != null && typeMsg != null && UserUID != null
			&& !pfx.isEmpty() && !typeMsg.isEmpty() && !UserUID.isEmpty()){
	  Message msg = new Message(pfx, typeMsg, UserUID, devUID);
	  publishMessage(msg.getTopic(), msg.getMsg());
	}
  }
  //---------------------------------------------------------------
  private void reportBr(@NonNull String ... str){
	Intent broadcastIntent = new Intent()               ;
	broadcastIntent.setAction("FROM_MQTT_SERVICE")  	;

	List<String> listBr = new ArrayList<>()				;
	for(String s : str) listBr.add(s)					;
	for(int ix=0;ix<listBr.size();ix+=2){
	  if(!listBr.get(ix+1).isEmpty())
	    broadcastIntent.putExtra(listBr.get(ix),listBr.get(ix+1));}
	context.sendBroadcast(broadcastIntent)              ;}
  //---------------------------------------------------------------
  private void reportW(String str){
	if(myCallback != null){
	  StackTraceElement ste = Thread.currentThread().getStackTrace()[3];
	  str = String.format("[%s:%-4d] %s",ste.getMethodName(),ste.getLineNumber(),str)		;
	  myCallback.onReportW(str);	;}
  }
  //---------------------------------------------------------------

  //---------------------------------------------------------------------------------
  public static class Message{
  private static final String TAG = "MSG";
  private String  topic		;
  private String  msg		;
  private TypeMsg type		;
  private String devPfx		;
  private String devName	;
  private String dstUid		;
  private String srcUid		;
//-------------------------------------------------------------------
  // ЗАПРОСЫ
  public Message(String pfx, String typeMsg, String userUID, String devUID){
	if(!typeMsg.isEmpty()){
	  try{
		type = TypeMsg.valueOf(typeMsg);
	  } catch(IllegalArgumentException e){type = TypeMsg.isEmpty;}
	}
	set(pfx, userUID, devUID);
  }

  public static String getPing(String pfx,String devUid,String userUid){
	String ping = ""	;// topic: Boil_9140/a470ab51/d760bb65/ping
	if(!pfx.isEmpty() && !userUid.isEmpty() && !devUid.isEmpty())
	  ping = java.lang.String.format("%s/%s/%s/ping",pfx, devUid, userUid)	;
	return ping	;}

  //-------------------------------------------------------------------
  public static String getFind(String prefix, String userUID){
	String find = ""	;// topic: Boil_9140 	msg: d760bb65

	return find			;}

  //-------------------------------------------------------------------
  public void set(String pfx, String userUid, String devUid){
	msg     = ""	;topic = ""	;
	switch(type){
	  case find:	if(!pfx.isEmpty() && !userUid.isEmpty()){
		topic = pfx	;msg   = userUid;}
		break	;

	  case ping:	if(!pfx.isEmpty() && !userUid.isEmpty() && !devUid.isEmpty()){
		  				topic = pfx + "/" + devUid + "/" + userUid + "/ping";}
		break	;

	  case get_ui:	if(!pfx.isEmpty() && !userUid.isEmpty() && !devUid.isEmpty()){
						topic = pfx + "/" + devUid + "/" + userUid + "/ui";}
		break	;
	}
  }
//-------------------------------------------------------------------
  // ОТВЕТЫ
  public Message(String tpc, String ms){
	topic = tpc		; msg  = ms		; type  = TypeMsg.isEmpty	;
	devName = ""	; devPfx = ""	; dstUid = ""	; srcUid = ""	;

	String[] 	parts   = null	;
	MyParserJson parser = new MyParserJson(msg)		;
	String     	strType = parser.getValue("type")	;

	if(!strType.isEmpty()){
	  try{ type = TypeMsg.valueOf(strType)			;
	  } catch(IllegalArgumentException e){}}

	if(!topic.isEmpty()) parts = topic.split("/")	;
	if(parts != null){
	  if(parts.length > 1) devPfx = parts[0]		;

	  if(parts.length > 3){
	  	if(parts[1].equals("hub")){
			dstUid = parts[2]	; srcUid = parts[3]	;}
	  	if(parts[3].equals("status")){
			srcUid = parts[2]	;
			if(ms.equals("offline")) type = TypeMsg.offline	;
			if(ms.equals("online" )) type = TypeMsg.online	;}
	  }
	}

	switch(type){
	  case OK:
		break;
	  case discover:
						devName = parser.getValue("name");
		break;
	}
	Log.i(TAG, "topic:" + topic + "  type:" + type);
  }

  //---------------------------------------------------------------------------------
  public String getTopic()	{ return topic  == null ? "" : topic	;}
  public String getMsg()	{ return msg    == null ? "" : msg		;}
  public String getDevPfx()	{ return devPfx == null ? "" : devPfx	;}
  public String getDevName(){ return devName== null ? "" : devName	;}
  public String getDstUid()	{ return dstUid	== null ? "" : dstUid	;}
  public String getSrcUid()	{ return srcUid	== null ? "" : srcUid	;}
  public TypeMsg getType()	{ return type		;}

	public String report(){
		return java.lang.String.format("%s:%s (%d bytes)",getDevPfx(),getType(),getMsg().length())	;}

	//---------------------------------------------------------------------------------
  public enum TypeMsg{
	// ответ на ping
	OK,                // topic: Boil_9140/hub/d760bb65/a470ab51  	msg: {"id":"a470ab51","type":"OK"}
	// ответ на get_ui
	ui,                // topic: Boil_9140/hub/d760bb65/a470ab51 	msg: {"id":"a470ab51","type":"ui","controls":[{"id":"_menu","type":................"icon":"f150","color":3647804}]}]}
	// ответ на
	update,            // topic: Boil_9140/hub						msg: {"updates":{"alarm":{"value":"0"},"t_in":{"value":"ОШБК"},"t_out......,"btn_mll_dn":{"color":3647804,"icon":"f150"}},"id":"a470ab51","type":"update"}
	// ответ на get_discover и на find
	discover,          // topic: Boil_9140/hub/d760bb65/a470ab51 	msg: {"id":"a470ab51","type":"discover","name":"Boil40","prefix":"Boil_9140","icon":"f7e4","PIN":0,"version":"5.0.07","platform":"ESP32","max_upl":512,"api_v":1,"http_t":0,"ota_t":"bin","ws_port":81,"modules":16256}
	offline,           // topic: Boil_9140/hub/a470ab51/status  	msg: offline
	online,            // topic: Boil_9140/hub/a470ab51/status  	msg: online

	// запросы
	get_discover,      // topic: Boil_9140/a470ab51			 		msg: d760bb65
	find,              // topic: Boil_9140						 	msg: d760bb65
	ping,              // topic: Boil_9140/a470ab51/d760bb65/ping  				msg:
	unfocus,           // topic: Boil_9140/a470ab51/d760bb65/unfocus 			msg:
	unix,              // topic: Boil_9140/a470ab51/d760bb65/unix/1746720627	msg:
	get_ui,            // topic: Boil_9140/a470ab51/d760bb65/ui				msg:
	isEmpty;
  }
}
  //-----------------------------------------------------------------
  private static class DeviceAlarm{
	private String pfx      ;
	private String codeW    ;
	private String alrm     ;

	private static List<DeviceAlarm> list    ;

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
  public interface WorkCallback{
	void onReportW(String str)	;
	void onAlarm()				;
  }
}

