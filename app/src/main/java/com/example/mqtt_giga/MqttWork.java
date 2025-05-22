package com.example.mqtt_giga;

import android.util.Log;

import java.util.Stack;
//---------------------------------------------------------------------------------
public class MqttWork {
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
  private static String BigStrMsg 	= ""	;
  private static int ChckCount 		= 0		;
  public  static boolean CheckBracket = false;

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
	strMsg = MqttWork.ReplaceTag(strMsg)      ;
	// проверим баланс скобок
	CheckBracket = MqttWork.checkBracketsBalance(strMsg)  ;
	if(CheckBracket || ChckCount > 5){
	  BigStrMsg = ""          ; ChckCount = 0 ;}
	if(!CheckBracket) {
	  BigStrMsg += strMsg     ;// если в сообщении нарушен баланс скобок, то
	  ChckCount++             ;// попытаемся "склеить" несколько сообщений в одно
	  CheckBracket = MqttWork.checkBracketsBalance(BigStrMsg)  ;
	  if(CheckBracket){
		strMsg = BigStrMsg  ;// если удалось "склеить" несколько сообщений в одно
		BigStrMsg = ""      ; ChckCount = 0 ;
	  }
	}
	return strMsg	;
  }
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
	  ping = String.format("%s/%s/%s/ping",pfx, devUid, userUid)	;
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
		return String.format("%s:%s (%d bytes)",getDevPfx(),getType(),getMsg().length())	;}

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
}

