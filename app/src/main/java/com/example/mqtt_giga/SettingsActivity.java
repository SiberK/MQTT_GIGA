package com.example.mqtt_giga;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

import accountmanagerlib.AccManager;
import accountmanagerlib.AccManager.Account;
import accountmanagerlib.AccountUiManager;
import gson_parser.UiButtonWidget;
import gson_parser.UiElement;
import static gson_parser.UiElement.getIcChar;
import gson_parser.UiInputWidget;
import gson_parser.UiWidget;
//---------------------------------------------------------------
public class SettingsActivity extends AppCompatActivity
		implements AccountUiManager.AccountActionListener  {

  private static final String TAG = "M_SETT";
//  private TextView	tvAccount   ;
//  private TextView  tvStart		;
//  private EditText	etDevPfx    ;
//  private EditText	etCodeWord  ;
//  private TextView	tvUid   	;
//  private TextView	tvSound     ;

  private UiButtonWidget uiAccount	;
  private UiButtonWidget uiStart	;
  private UiButtonWidget uiSound	;
  private UiInputWidget  uiDevPfx	;
  private UiInputWidget  uiCodeWord	;

  private Uri		uriSound    ;
  private String	strSound    ;
  private String	soundTitle	;
  private String	user_uid    ;
  private boolean	flStartStop ;
  private  int		colorConn   ;
  private  int		colorDis    ;
  private AccountUiManager     	accUiManager;
  private AccManager.Account 	curAccount  ;
  private ActionBar  			actionBar   ;
  //private MaterialButton 		btnStart	;
  private MediaPlayer 			mediaPlayer	;
  private SoundPickerDialogFragment soundPickerDialog;
  //---------------------------------------------------------------
  private final BroadcastReceiver receiver = new BroadcastReceiver() {
	@Override
	public void onReceive(Context context, @NonNull Intent intent) {
	  TextView textView = findViewById(R.id.tvTopic)    ; textView.setText(intent.getStringExtra("topic"	))			;
	  textView = findViewById(R.id.tvMsg)               ; textView.setText(intent.getStringExtra("message"	))			;
	  textView = findViewById(R.id.tvStat)              ; textView.setText(intent.getStringExtra("state"	))			;
	  textView = findViewById(R.id.tvAlarm)             ; textView.setText("Alarm: " + intent.getStringExtra("alarm"))	;
	  if(intent.getStringExtra("user_uid") !=null){ uiAccount.setSuffix("ID: " + (user_uid = intent.getStringExtra("user_uid")))	;}
	}
  };

  //---------------------------------------------------------------
  @SuppressLint("UnspecifiedRegisterReceiverFlag")
  @Override
  protected void onCreate(Bundle savedInstanceState) {
	ViewGroup ly								;
	TextView  tv								;
	super.onCreate(savedInstanceState)			;
	setContentView(R.layout.activity_settings)	;
	flStartStop = false                         ;

	// Регистрация ресивера
	IntentFilter filter = new IntentFilter()    ;
	filter.addAction("FROM_MQTT_SERVICE")       ;
	if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU)
	  registerReceiver(receiver, filter)		            ;
	else registerReceiver(receiver, filter,RECEIVER_EXPORTED);

	// Инициализация элементов интерфейса
	//	Выбор аккаунта
	uiAccount = findViewById(R.id.uiAccount)	;
	uiStart   = findViewById(R.id.uiStart)		;
	uiStart.setOnClickListener(v->onClickStartStopService())	;

//	ly = findViewById(R.id.uiAccount)			;
//	ly.setOnClickListener(v->onClickAccountDlg())	;
//	tv    = ly.findViewById(R.id.tvLabel)		; tv.setText("ACCOUNT")		;
//	tvUid = ly.findViewById(R.id.tvSuffix)		; tvUid.setText("")			;
//	tvAccount = ly.findViewById(R.id.tvText)	;
//	// Старт/Стоп сервиса
//	ly = findViewById(R.id.uiStart)				;
//	ly.setOnClickListener(v->onClickStartStopService())				;
//	tv = ly.findViewById(R.id.tvLabel)			; tv.setText("SERVICE")		;
//	tvStart = ly.findViewById(R.id.tvText)		; tvStart.setText("START")	;

	// Поиск устройства
	uiDevPfx = findViewById(R.id.uiDevice)		;
//	uiDevPfx.setOnClickListener(v->onClickAddDevice())	;
	UiButtonWidget uiBtn = findViewById(R.id.uiFind)			;
	uiBtn.setOnClickListener(v->onClickAddDevice())		;

//	ly = findViewById(R.id.uiDevice)			;
//	tv = ly.findViewById(R.id.tvLabel)			; tv.setText("DEVICE")		;
//	etDevPfx = ly.findViewById(R.id.etText)		;
//	ly = findViewById(R.id.uiFind)				;
//	ly.setOnClickListener(v->onClickAddDevice())	;
//	tv = ly.findViewById(R.id.tvText)			; tv.setText("")			;
//	try{
//	  AssetManager mgr = getAssets()			;
//	  String icChar = getIcChar("f00e")			;
//	  Typeface fontAwesome = Typeface.createFromAsset(mgr, "fonts/fa_solid_900.ttf");
//	  tv.setTypeface(fontAwesome)				; tv.setText(icChar)		;
//	  tv.setTextSize(30)						;
//	} catch(Exception e){Log.w(TAG, "", e)		;}

	// Выбор отслеживаемого параметра
	uiCodeWord = findViewById(R.id.uiCodeword)	;
	uiBtn      = findViewById(R.id.uiOkKey)		;
	uiBtn.setOnClickListener(v->onClickOkCodeword(uiCodeWord.getText()))	;

//	ly = findViewById(R.id.uiCodeword)			;
//	tv = ly.findViewById(R.id.tvLabel)			; tv.setText("Отслеживать параметр")		;
//	etCodeWord = ly.findViewById(R.id.etText)	;
//	ly = findViewById(R.id.uiOkKey)				;
//	tv = ly.findViewById(R.id.tvText)			; tv.setText("OK")			;
//	ly.setOnClickListener(v->onClickOkCodeword(etCodeWord.getText()))	;

	// Выбор звука
	uiSound = findViewById(R.id.uiSelectSound)	;
	uiSound.setOnClickListener(v->onSoundDialog())	;
//	ly = findViewById(R.id.uiSelectSound)		;
//	tv = ly.findViewById(R.id.tvLabel)			; tv.setText("Звук уведомления")		;
//	tvSound = ly.findViewById(R.id.tvText)		;
//	ly.setOnClickListener(v->onSoundDialog())	;

//  TODO TODO
//	ly = findViewById(R.id.lySound)				;
//	UiElement uiE = new UiElement("uiSound","label","","",20,4)				;
////	uiE.setNoTab(1)				;
//	UiButtonWidget uiSound = (UiButtonWidget)UiWidget.create(this,uiE,LinearLayout.HORIZONTAL)		;
//	uiSound.setOnClickListener(v->onSoundDialog());
//	ly.addView(uiSound)	;

	actionBar = getSupportActionBar() 			;
	colorConn = ContextCompat.getColor(this, R.color.colorTitleConnected)     ; // Безопасное получение цветаColor.rgb(0x50,0x50,0xFF)	 		;
	colorDis  = ContextCompat.getColor(this, R.color.colorTitleDisconnected)  ; // Безопасное получение цветаColor.rgb(0xFF,0x50,0x50)			;

	accUiManager = new AccountUiManager(this,this);
	loadSettings()                              ; // Загрузка сохраненных настроек
	flStartStop = MQTTService.isRunning         ;
	uiStart.setText(flStartStop ? "СТОП" : "СТАРТ")    ;
	startStopColor()  ;
  }
  //-------------------------------------------------------------------
  private void onClickAccountDlg(){	if(!flStartStop) accUiManager.showAccountsList();}
  //-------------------------------------------------------------------
  private void onClickOkCodeword(String text){}	// TODO TODO
  //-------------------------------------------------------------------
  private void onClickStartStopService(){
	flStartStop ^= true                                 ;
	uiStart.setText(flStartStop ? "СТОП" : "СТАРТ")    ;
	saveSettings()  ;
	Intent broadcastIntent = new Intent("TO_MAIN")      ;// Отправка данных в активность
	broadcastIntent.putExtra("start_stop",flStartStop ? "start" : "stop")  ;
	sendBroadcast(broadcastIntent)                      ;
	startStopColor()  ;}
  private void startStopColor(){
	if(actionBar != null)
	  actionBar.setBackgroundDrawable(new ColorDrawable(flStartStop ? colorConn : colorDis))	;}
  //---------------------------------------------------------------
  private void onClickAddDevice() {
	String devPfx = uiDevPfx != null ? uiDevPfx.getText().toString() : ""   ;
	Intent broadcastIntent = new Intent("TO_MAIN")      ;// Отправка данных в активность
	broadcastIntent.putExtra("device",devPfx)           ;
	sendBroadcast(broadcastIntent)                      ;
  }
  //---------------------------------------------------------------
  @Override protected void onDestroy() {
	stopPlaying()				;
	soundPickerDialog = null	;
	saveSettings()              ;
	super.onDestroy()           ;
	if(receiver != null) unregisterReceiver(receiver)   ; // Отмена регистрации ресивера
  }
  //---------------------------------------------------------------
  private void onSoundDialog(){
	if (soundPickerDialog == null || !soundPickerDialog.isAdded()) {
	  soundPickerDialog = new SoundPickerDialogFragment();
	  // Устанавливаем listener через метод
	  soundPickerDialog.setOnSoundSelectedListener(new SoundPickerDialogFragment.OnSoundSelectedListener() {
		@Override public void onSoundSelected(Uri soundUri, String sndName) {
		  					handleSoundSelection(soundUri, sndName)		;}})	;

	  soundPickerDialog.show(getSupportFragmentManager(), "SoundPicker");
	}
  }
  private void handleSoundSelection(Uri sndUri, String sndName) {
	uriSound = sndUri				;
	strSound = uriSound.toString()	;
	soundTitle = sndName			;
	uiSound.setText(soundTitle)  	;
	saveSettings()					;
	Intent broadcastIntent = new Intent("TO_MQTT_SERVICE") 	;// Отправка данных в активность
	broadcastIntent.putExtra("SEL_RINGTONE",strSound)  		;
	sendBroadcast(broadcastIntent)          				;
	Log.i(TAG,"select sound: " + soundTitle)				;
  }
  private void playSound(Uri soundUri) {
	stopPlaying();
	try {
	  mediaPlayer = MediaPlayer.create(this, soundUri);
	  mediaPlayer.setOnCompletionListener(mp -> stopPlaying());
	  mediaPlayer.start();
	} catch (Exception e) {
	  Toast.makeText(this, "Ошибка воспроизведения", Toast.LENGTH_SHORT).show();
	}
  }

  private void stopPlaying() {
	if (mediaPlayer != null) {
	  mediaPlayer.release()		;
	  mediaPlayer = null		;
	}
  }
  //---------------------------------------------------------------
  private void saveSettings() {                       // Сохранение настроек
	List<String> listPing = new ArrayList<>()                       ;
	List<String> listPfx  = new ArrayList<>()                       ;

	for(DeviceManager.Device dev : DeviceManager.getDeviceList()){
	  listPfx.add(dev.getPrefix())                                ;
	  String strPing = MqttWork.Message.getPing(dev.getPrefix(),dev.getUID(),user_uid)   ;
	  if(!strPing.isEmpty()) listPing.add(strPing)                ;// соберём массив ping строк!
	}
	String strListPing = TextUtils.join("," , listPing)     ;
	String strListPfx  = TextUtils.join("," , listPfx)      ;

	if(uriSound != null)  strSound = uriSound.toString()    ;
	getSharedPreferences("MQTT_SETTINGS", MODE_PRIVATE)
			.edit()
			.putString("SRV_ADDRESS", curAccount.getServer())
			.putInt   ("PORT"       , Integer   .parseInt(curAccount.getPort()))
			.putString("LOGIN"      , curAccount.getLogin())
			.putString("PASSWORD"   , curAccount.getPassword())
			.putString("TOPIC_NAME" , uiDevPfx  .getText())
			.putString("CODE_WORD"  , uiCodeWord.getText())
			.putString("USER_UID"   , user_uid	)
			.putString("SEL_RINGTONE",strSound	)
			.putString("SOUND_TITLE" ,soundTitle)
			.putString("LIST_PING"  , strListPing)
			.putString("LIST_PFX"   , strListPfx)
			.putBoolean("START"     , flStartStop)
			.apply();
  }
  //---------------------------------------------------------------
  // Загрузка сохраненных настроек
  private void loadSettings() {
	SharedPreferences prefs = getSharedPreferences("MQTT_SETTINGS", MODE_PRIVATE);
	String serverAddress = prefs.getString("SRV_ADDRESS", "")       ;
	int port            = prefs.getInt   ("PORT"        , 0 )       ; // 1883 - стандартный порт MQTT
	flStartStop         = prefs.getBoolean("START"      ,false)     ;
	String login        = prefs.getString("LOGIN"       , "")       ;
	String password     = prefs.getString("PASSWORD"    , "")       ;
	String topicName    = prefs.getString("TOPIC_NAME"  , "")       ;
	String codeWord     = prefs.getString("CODE_WORD"   , "")       ;
	user_uid     		= prefs.getString("USER_UID"    , "")       ;
	strSound            = prefs.getString("SEL_RINGTONE", "")       ;
	soundTitle			= prefs.getString("SOUND_TITLE" , "")       ;
	uriSound            = Uri.parse(strSound);
	String strPort = String.valueOf(port)   ;

	curAccount = new Account(serverAddress, strPort, login, password)    ;
	uiAccount .setText(curAccount.toString())	;
	uiDevPfx  .setText(topicName) 				;
	uiCodeWord.setText(codeWord)  				;
	uiAccount .setSuffix("ID: " + user_uid)		;
	uiSound   .setText(soundTitle)  			;
  }
  //---------------------------------------------------------------
  @Override public void onAccountSelected(Account _account) {
	curAccount = _account  						;
	uiAccount.setText(curAccount.toString())	;}
  //---------------------------------------------------------------
  @Override  public void onAccountListDismissed() 	{} // Можно добавить дополнительную логику при закрытии списка
  //---------------------------------------------------------------
  @Override  public void onAccountUpdated() 		{} // Обновление UI при изменении аккаунтов
  //---------------------------------------------------------------
  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
	MenuInflater inflater = getMenuInflater()			;
	inflater.inflate(R.menu.main_menu, menu)			;

	// Получаем ColorStateList
	ColorStateList colorStateList = ContextCompat.getColorStateList(this, R.color.icon_color_selector);

	// Применяем к каждой иконке
	for (int i = 0; i < menu.size(); i++) {
	  MenuItem item = menu.getItem(i)					;
	  Drawable icon = item.getIcon()					;
	  if (icon != null) {
		icon = DrawableCompat.wrap(icon)				;
		DrawableCompat.setTintList(icon, colorStateList);
		item.setIcon(icon)								;}
	}
	return true;
  }
  //---------------------------------------------------------------
  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
	boolean rzlt = super.onOptionsItemSelected(item)	;
	// Обработка нажатий на пункты меню
	if(item.getItemId() == R.id.action_settings){
	  finish()		;
	  rzlt = true	;}
	return rzlt ;
  }
  //---------------------------------------------------------------
}