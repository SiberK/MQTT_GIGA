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
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.mqtt_giga.DeviceManager.Device;
import com.example.mqtt_giga.MqttWork.Message;
import static com.example.mqtt_giga.MqttWork.noEmpty;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static gson_parser.UiElement.getIntValue;
import gson_parser.UiWidget;

//---------------------------------------------------------------
public class MainActivity extends AppCompatActivity{
    private static final String TAG = "MAIN";
//    private SettingsActivity 	settingsActivity   ;
    private DeviceManager 		devManager      	;
    private Device 				curDevice  			;
//    private String 				StrTopic     	;
//    private String 				StrMessage   	;
//    private String            	StrDevice   	;
  	private TableLayout 		btnContainer		;
	private DynamicFormFragment faceContainer		;
  	private String 				mqttUid	= ""		;
	private String				currDevUid = ""		;
	private String				currDevPfx = ""		;
	private String				currDevName= ""		;
	private static MainActivity	Instance = null		;
  	private boolean 			flStartMqtt = false	;
  	private Intent 				serviceIntent		;
	private TextView 			titleTextView = null;
	private MenuItem			miSettings			;
  	private MenuItem			miCancel			;
	private MenuItem			miMenu				;
  	private MenuItem			miRfrsh				;
  	private SubMenu 			subMenu				;
  	private ActionBar 			actionBar			;
  	private int 				colorConn 			;
  	private int					colorDis  			;
    private int   				mTimPing = 0		;
	private Timer 				timerPing       	;
  //---------------------------------------------------------------
	@Override
	protected void onCreate(Bundle savedInstanceState) {
	  Instance = this								;
	  super.onCreate(savedInstanceState)          	;
	  setContentView(R.layout.activity_main)      	;
	  receiverRegister()							;// Регистрация ресивера
	  devManager = new DeviceManager(this)        	;
	  btnContainer = findViewById(R.id.contButtons)	;
	  faceContainer= DynamicFormFragment.newInstance()		;

	  UiWidget.setFontAwesome(this)					;

	  actionBar	= getSupportActionBar() 			;
	  colorConn = ContextCompat.getColor(this, R.color.colorTitleConnected)		; // Безопасное получение цвета Color.rgb(0x50,0x50,0xFF)	 		;
	  colorDis  = ContextCompat.getColor(this, R.color.colorTitleDisconnected)	; // Color.rgb(0xFF,0x50,0x50)			;

	  FrameLayout     	  layF            = findViewById(R.id.contFace)			;
	  FragmentManager     fragmentManager = getSupportFragmentManager()			;
	  FragmentTransaction transaction     = fragmentManager.beginTransaction()	;
	  transaction.replace(layF.getId(),faceContainer);// Добавляем фрагмент в контейнер
	  transaction.commit()							;// Фиксируем изменения
	  createDeviceMatButtons()						;

	  SharedPreferences prefs = getSharedPreferences("MQTT_SETTINGS", MODE_PRIVATE);
	  flStartMqtt   = prefs.getBoolean("START",false)     	;
	  mTimPing		= prefs.getInt("TIME_PING",0)			;
	  mqttUid  		= prefs.getString("USER_UID", "")       ;

	  serviceIntent = new Intent(this, MQTTService.class)	;
	  startStopServiceMqtt(flStartMqtt)						;
	}
  //=====================================================================
  @Override protected void onDestroy() {
	super.onDestroy()           ;
	if(receiver != null) unregisterReceiver(receiver)   ; // Отмена регистрации ресивера
	startStopTimerPing(false)	;
  }
  //=====================================================================
  private void startStopServiceMqtt(boolean flStart){
	if(serviceIntent != null){
	  if(flStart){	startService(serviceIntent)	; Log.i(TAG,"Сервис старт")				;
					Toast.makeText(getApplicationContext(), "Сервис старт", Toast.LENGTH_SHORT).show()	;
					if(actionBar != null)
		  			   actionBar.setBackgroundDrawable(new ColorDrawable(colorConn))	;}
	  else{			stopService(serviceIntent)	; Log.i(TAG,"Сервис стоп")				;
					Toast.makeText(getApplicationContext(), "Сервис стоп", Toast.LENGTH_SHORT).show()	;
					if(actionBar != null)
					   actionBar.setBackgroundDrawable(new ColorDrawable(colorDis))		;}
	}
  }
  //=====================================================================
  private void onClickRst(){
	if(faceContainer != null) faceContainer.removeAllViews()	; currDevPfx = ""	;
	createDeviceMatButtons()									;
	if(miCancel != null) miCancel.setVisible(false)				;
	if(miMenu   != null) miMenu  .setVisible(false)				;
	if(miSettings != null) miSettings.setVisible(true)			;
	setTitle("")	;
	startStopTimerPing(false)	;
  }
  private void onClickBuild(){// это для теста
	if(btnContainer != null)	btnContainer.removeAllViews()		; // Очистка предыдущих кнопок
	if(faceContainer != null)	faceContainer.buildFace(strTest1,subMenu)	;}
  private void onClickUpdate(){// это для теста
	if(btnContainer != null)	btnContainer.removeAllViews()		; // Очистка предыдущих кнопок
	if(faceContainer != null) 	faceContainer.updateFace(strUpdate)	;}
//=====================================================================
  @SuppressLint("UnspecifiedRegisterReceiverFlag")
  private void receiverRegister(){

	IntentFilter filter = new IntentFilter()    	;
	filter.addAction("FROM_MQTT_SERVICE")       	;// Уникальное действие
	filter.addAction("TO_MAIN")                 	;// Уникальное действие

	// Регистрация ресивера
	if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU)
	  registerReceiver(receiver, filter)		                ;
	else registerReceiver(receiver, filter,RECEIVER_EXPORTED);
  }
  //---------------------------------------------------------------
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, @NonNull Intent intent) {
            messageParse(intent.getStringExtra("topic"),intent.getStringExtra("message"))   ;
            addDevice(intent.getStringExtra("device"))      	;
			setMqttUid(intent.getStringExtra("user_uid"))		;
			setPingSelect(intent.getStringExtra("PingSelect"))	;

		    String onOff = intent.getStringExtra("start_stop")	;
			if(noEmpty(onOff)){ flStartMqtt = onOff.equals("start")	;
				startStopServiceMqtt(flStartMqtt)				;}}
    };
  //=====================================================================
    private void setPingSelect(String val){
	  if(val != null)
		mTimPing = getIntValue(val,0)	;
	}
  //---------------------------------------------------------------
  private void PingTask(){
	  Log.i(TAG,"PingTask " + currDevPfx +" " + currDevUid)	;
	  if(noEmpty(currDevPfx) && noEmpty(currDevUid))
	  		sendBroadcastTo(this,"TO_MQTT_SERVICE","type_msg","ping",
													"dev_uid",currDevUid,
													"prefix" ,currDevPfx)	;
  }
  //---------------------------------------------------------------
 //---------------------------------------------------------------
  private void startStopTimerPing(boolean start){
	if(start && timerPing == null && mTimPing > 1){
	  	timerPing = new Timer() ;
		try{ timerPing.schedule(new TimerTask(){
		  		@Override public void run(){ PingTask()	;}}, 1000, mTimPing * 1000);
		} catch(NullPointerException | IllegalStateException | IllegalArgumentException e){ }
  	}
	else if(timerPing != null){ timerPing.cancel()   ; timerPing = null  ;}
  }
 //=====================================================================
    private void messageParse(String topic, String message) {
        if(topic != null && message != null){
		    boolean flRe = false	;// сохранить список устройств
		    Button btn   = null		;
            Message msg     = new Message(topic,message)		;
//		  	Toolbar toolbar = findViewById(R.id.toolbar)		;

			switch(msg.getType()){
			  case discover	:
				String devName = msg.getDevName()				;
				String devPfx  = msg.getDevPfx()				;
				Device dvc = devManager.findByPfx(devPfx)		;
				if(dvc != null){
				  flRe |= dvc.setName(devName)					;
				  flRe |= dvc.setUID(msg.getSrcUid())			;// здесь - deviceUid!!!
				  btn = findViewById(dvc.getBtnId())			;}
				if(btn != null) btn.setText(devName)			;
			  break	;

			  case ui :
				if(!msg.getMsg().isEmpty() && msg.getDstUid().equals(mqttUid)){
				  currDevUid = msg.getSrcUid()	; currDevPfx = msg.getDevPfx()	;
				  curDevice  = devManager.findByPfx(currDevPfx)					;
				  currDevName =curDevice != null ? curDevice.getNamePfx() : ""	;
				  setTitle(currDevName)											;
				  if (actionBar != null)
					actionBar.setBackgroundDrawable(new ColorDrawable(colorConn))	;
				  //				  setTitleColor(R.color.colorTitleConnected)					;
				  if(subMenu  	  != null) subMenu.clear()						;
				  if(miMenu   	  != null) miMenu.setVisible(true)				;
				  if(miCancel 	  != null) miCancel.setVisible(true)			;
				  if(miSettings   != null) miSettings.setVisible(false)			;
				  if(btnContainer != null) btnContainer.removeAllViews()		; // Очистка предыдущих кнопок
				  if(faceContainer!= null) faceContainer.buildFace(msg.getMsg(),subMenu)	;
//				  invalidateOptionsMenu()	;
				}
			  break	;

			  case update:
				if(!msg.getMsg().isEmpty() && msg.getDevPfx().equals(currDevPfx)){
				  if(btnContainer != null){	btnContainer.removeAllViews()			; // Очистка предыдущих кнопок
											btnContainer.setVisibility(View.GONE)	;}
				  if(faceContainer != null) faceContainer.updateFace(msg.getMsg())	;}
			  break	;
			}

			if(flRe){		// была дополнена информация по устройству?
			  devManager.saveDevListToJson()					;
				fillListPing()	;}
        }
    }
  //---------------------------------------------------------------
  public static String getCurrDevUid(){return Instance == null ? "" : Instance.currDevUid	;}
  public static String getCurrDevPfx(){return Instance == null ? "" : Instance.currDevPfx	;}
  //---------------------------------------------------------------
  private void setMqttUid(String val){ if(noEmpty(val))	mqttUid = val	;}
  //---------------------------------------------------------------
  private void fillListPing(){ sendBroadcastTo(this,"TO_MQTT_SERVICE","fill_list_ping","fill_list_ping")	;}
  //---------------------------------------------------------------
    private void addDevice(String pfx) {
	  curDevice = devManager.addDevice(pfx)   ;// если pfx не новый, то вернёт null
	  if(curDevice != null){
		createDeviceMatButtons()				;
		subscribeTo(pfx)                    	;
		findDevice(pfx)                     	;
		fillListPing()							;}
    }
  //---------------------------------------------------------------
	private void removeDevice(String pfx){
		if(devManager.removeDevice(pfx)){
			sendBroadcastTo(this,"TO_MQTT_SERVICE","unsubscribe",pfx)	;}
	  	createDeviceMatButtons()				;
		fillListPing()							;}
  //---------------------------------------------------------------
    private void findDevice(String pfx) { sendBroadcastTo(this,"TO_MQTT_SERVICE","type_msg","find","prefix",pfx)	;}
    //---------------------------------------------------------------
    private void subscribeTo(String pfx){ sendBroadcastTo(this,"TO_MQTT_SERVICE","subscribe",pfx)	;}
//---------------------------------------------------------------
  private void createDeviceMatButtons() {
	MaterialButton 	btn			;
	TableRow       	row = null	;
	Device 			device		;
	String			ttl			;

	List<Device> devices = DeviceManager.getDeviceList()	;
	if(btnContainer == null || devices == null || devices.size() == 0){
	  Log.e(TAG, "GridLayout или список устройств равен null"); return	;}

	btnContainer.removeAllViews()							; // Очистка предыдущих кнопок
	btnContainer.setVisibility(View.VISIBLE);

	TableRow.LayoutParams params = new TableRow.LayoutParams(0,//TableRow.LayoutParams.MATCH_PARENT,
															 TableRow.LayoutParams.WRAP_CONTENT,1.0f);
	params.setMargins(8, 8, 8, 8);

	for (int i = 0; i < devices.size(); i++) {
	  if(i % 2 == 0){ row = new TableRow(this)				;
		btnContainer.addView(row)							;}

	  device = devices.get(i)	; if(device == null) break	;

	  ttl = device.getName()								;
	  if(ttl.isEmpty()) ttl = device.getPrefix()			;
	  int btnId = View.generateViewId()						;
	  device.setBtnId(btnId)								;

	  btn = createMatButton(ttl,btnId,device)				;
	  if(btn != null) row.addView(btn,params)				;
	}
	if(devices.size() % 2 != 0 && row != null){
	  View spacer = new View(this)							;
	  row.addView(spacer,params)							;}
  }
  //----------------------------------------------------------------------------
  private MaterialButton createMatButton(String ttl, int btnId, Device device){
	MaterialButton btn = new MaterialButton(this,null, R.style.DeviceButtonStyle);
	btn.setText(ttl)				;
	btn.setHint(device.getPrefix())	;
	btn.setId(btnId)				;
	btn.setTextSize(26)				;
	btn.setSingleLine()				;
	btn.setAllCaps(false)			;
	btn.setPadding(8,8,8,8)			;

	// Настройка внешнего вида кнопки
	btn.setTextColor(ContextCompat.getColorStateList(this, R.color.black));
	btn.setBackgroundResource(R.drawable.btn_device_bg)	;
	btn.setBackgroundTintList(null)						; // очищаем стандартную заливку

	// Добавляем иконку ВНУТРИ кнопки
	btn.setIcon(ContextCompat.getDrawable(this, R.drawable.gear));
	btn.setIconGravity(MaterialButton.ICON_GRAVITY_END)	;
	btn.setIconSize(46)	; // размер в пикселях
	btn.setChecked(true);
	btn.setIconPadding(4)	; // отступ между текстом и иконкой

	// Обработчик нажатия
	btn.setOnClickListener(v -> onDeviceClicked(device))	;
	btn.setIconTint(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.purple_200)));
	// Отдельный обработчик только для иконки
	btn.setOnTouchListener(new View.OnTouchListener(){
	  @Override
	  public boolean onTouch(View v, MotionEvent event){
		if(event.getAction() == MotionEvent.ACTION_DOWN){
		  Rect  bounds   = ((MaterialButton) v).getIcon().getBounds();
		  float iconLeft = v.getWidth() - v.getPaddingRight() - bounds.width();

		  if(event.getX() > iconLeft){// Клик по иконке ?
			MainActivity.this.showAlertDialog(device);
			return true			;}}
		return false			;}});

  	return btn	;}
  //----------------------------------------------------------------------------
  // AlertDialog
  private void showAlertDialog(Device device) {
//	Toast.makeText(this, "Информация: " + device.getName(), Toast.LENGTH_SHORT).show();
	// Создаем кастомный макет для диалога
	View dialogView = LayoutInflater.from(this)
									.inflate(R.layout.dialog_device_actions, null);
	dialogView.setBackgroundResource(R.drawable.btn_device_bg)	;
	// Находим элементы управления
	ImageView ivDel   = dialogView.findViewById(R.id.ivDelete)	;
	ImageView ivCheck = dialogView.findViewById(R.id.ivCheck)	;
	TextView  tvDev   = dialogView.findViewById(R.id.tvDevPfx)	;
	if(tvDev != null && device != null)
	  tvDev.setText(device.getPrefix())			;

	// Создаем и настраиваем диалог
	AlertDialog dialog = new MaterialAlertDialogBuilder(this)
//			.setTitle("Действия с устройством")
			.setView(dialogView)
//			.setNegativeButton("Отмена", null)
			.create();

	// Обработчики кликов по иконкам
	if(ivDel != null)
	  ivDel.setOnClickListener(v -> {
	  removeDevice(device.getPrefix())	;
	  dialog.dismiss();
	});
	if(ivCheck != null)
	  ivCheck.setOnClickListener(v -> {
//	  toggleDeviceState(device);
	  dialog.dismiss();
	});

	dialog.show();}
	//----------------------------------------------------------------------------
	private void onDeviceClicked(Device device) {  // Обработка нажатия на устройство
  		Toast.makeText(this, "Выбрано: " + device.getPrefix(), Toast.LENGTH_SHORT).show();
		sendBroadcastTo(this,"TO_MQTT_SERVICE","prefix" ,device.getPrefix(),"dev_uid",device.getUID(),"type_msg","get_ui");
	  	startStopTimerPing(true)	;
	}
  //---------------------------------------------------------------
  public static void sendBroadcastTo(Context cntx,String Addr,String ... params){
	Intent brcIntent = new Intent(Addr)		;// Отправка данных в активность
	for(int ix=0;ix<params.length;ix+=2){
	  if(noEmpty(params[ix]))
		brcIntent.putExtra(params[ix],params[ix+1])	;
	}
	cntx.sendBroadcast(brcIntent)					;
  }
  //---------------------------------------------------------------
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater()			;
        inflater.inflate(R.menu.main_menu, menu)			;

        // Получаем ColorStateList
        ColorStateList colorStateList = ContextCompat.getColorStateList(this, R.color.icon_color_selector);

        // Применяем к каждой иконке
        for (int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.getItem(i)			;
            Drawable icon = item.getIcon()			;
            if (icon != null) {
                icon = DrawableCompat.wrap(icon)	;
                DrawableCompat.setTintList(icon, colorStateList);
                item.setIcon(icon)					;
            }
		  if(item.getItemId() == R.id.action_settings) miSettings = item	;
		  if(item.getItemId() == R.id.action_cancel  ) miCancel   = item	;
		  if(item.getItemId() == R.id.action_rfrsh   ) miRfrsh    = item	;
		  if(item.getItemId() == R.id.action_menu    ) miMenu     = item	;
		  if(miMenu != null && miMenu.hasSubMenu()) subMenu = miMenu.getSubMenu()	;
		  if(miCancel != null) miCancel.setVisible(false)	; setTitle("")	;
		}
        return true;
    }
    //---------------------------------------------------------------
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean rzlt = super.onOptionsItemSelected(item);
		int order = item.getOrder()		;
		int groupId = item.getGroupId()	;
        // Обработка нажатий на пункты меню
        if(item.getItemId() == R.id.action_settings){
            Intent intent = new Intent(this, SettingsActivity.class) ;
            startActivity(intent)   ; rzlt = true   			;}
        else if(item.getItemId() == R.id.action_cancel){
		  	onClickRst()			; rzlt = true   			;}
        else if(item.getItemId() == R.id.action_rfrsh){
            PingTask()				; rzlt = true   			;}
		else if(miMenu != null && groupId == miMenu.getItemId()){
		  String strId = (String) miMenu.getTitle();
		  MqttWork.onClickFaceElement("menu",strId,String.valueOf(order))	;
		  rzlt = true	;
		}
    return rzlt ;
    }
    //---------------------------------------------------------------
	private final String strTest1 = "{\"id\":\"a470ab51\",\"type\":\"ui\",\"controls\":[{\"id\":\"_menu\",\"type\":\"menu\",\"value\":\"0\",\"text\":\"КОТЁЛ\"},{\"type\":\"row\",\"wwidth\":1,\"data\":[{\"id\":\"_n1\",\"type\":\"title\",\"value\":\"Теплоноситель\",\"fsize\":30,\"wwidth\":4,\"align\":0},{\"id\":\"t_rssi\",\"type\":\"label\",\"label\":\"5.0.07\",\"wwidth\":1,\"fsize\":16,\"color\":3199024,\"notab\":1,\"align\":2,\"value\":\"-46dBm\"}]},{\"type\":\"row\",\"wwidth\":1,\"data\":[{\"type\":\"col\",\"wwidth\":1,\"data\":[{\"type\":\"row\",\"wwidth\":1,\"data\":[{\"id\":\"_n2\",\"type\":\"label\",\"value\":\"ВЫХОД\",\"nolabel\":1,\"wwidth\":1,\"fsize\":20,\"notab\":1},{\"id\":\"t_out\",\"type\":\"label\",\"nolabel\":1,\"wwidth\":1,\"fsize\":30,\"color\":13314105,\"value\":\"35°C\"}]},{\"type\":\"row\",\"wwidth\":1,\"data\":[{\"id\":\"_n3\",\"type\":\"label\",\"value\":\"ВХОД\",\"label\":\"обратка\",\"wwidth\":1,\"fsize\":20,\"notab\":1},{\"id\":\"t_in\",\"type\":\"label\",\"nolabel\":1,\"wwidth\":1,\"fsize\":30,\"color\":2718669,\"value\":\"28°C\"}]}]},{\"type\":\"col\",\"wwidth\":1,\"data\":[{\"type\":\"row\",\"wwidth\":1,\"data\":[{\"id\":\"_n4\",\"type\":\"label\",\"value\":\"УСТАВКА\",\"nolabel\":1,\"wwidth\":1,\"fsize\":20,\"notab\":1},{\"id\":\"t_trg\",\"type\":\"label\",\"nolabel\":1,\"wwidth\":1,\"fsize\":30,\"color\":3647804,\"value\":\"58°C\"}]},{\"type\":\"row\",\"wwidth\":1,\"data\":[{\"id\":\"_n5\",\"type\":\"label\",\"value\":\"\",\"nolabel\":1,\"wwidth\":2,\"fsize\":20,\"notab\":1},{\"id\":\"_n6\",\"type\":\"button\",\"nolabel\":1,\"wwidth\":1,\"fsize\":40,\"icon\":\"f151\"},{\"id\":\"_n7\",\"type\":\"button\",\"nolabel\":1,\"wwidth\":1,\"fsize\":40,\"icon\":\"f150\"}]}]}]},{\"id\":\"_n8\",\"type\":\"title\",\"value\":\"Режим работы\",\"fsize\":30,\"align\":0},{\"type\":\"row\",\"wwidth\":1\n" + ",\"data\":[{\"id\":\"slc_m\",\"type\":\"select\",\"value\":\"1\",\"text\":\"M1;M2;M3;M4\",\"nolabel\":1,\"wwidth\":1,\"fsize\":35,\"value\":2},{\"id\":\"rgm_1\",\"type\":\"label\",\"nolabel\":1,\"wwidth\":1,\"fsize\":30,\"color\":3647804,\"value\":1},{\"id\":\"rgm_2\",\"type\":\"label\",\"nolabel\":1,\"wwidth\":1,\"fsize\":30,\"color\":13158600,\"value\":2}]},{\"type\":\"row\",\"wwidth\":1,\"data\":[{\"id\":\"w_ttl\",\"type\":\"title\",\"value\":\"Работа\",\"wwidth\":2,\"fsize\":30,\"align\":0},{\"id\":\"_n9\",\"type\":\"label\",\"value\":\"STOP\",\"nolabel\":1,\"wwidth\":2,\"fsize\":20,\"notab\":1,\"align\":2},{\"id\":\"sw_onoff\",\"type\":\"switch_t\",\"value\":\"0\",\"nolabel\":1,\"wwidth\":1},{\"id\":\"_n10\",\"type\":\"label\",\"value\":\"START\",\"nolabel\":1,\"wwidth\":2,\"fsize\":20,\"notab\":1,\"align\":0}]},{\"type\":\"row\",\"wwidth\":1,\"data\":[{\"id\":\"r_fan\",\"type\":\"label\",\"nolabel\":1,\"wwidth\":1,\"fsize\":20,\"icon\":\"f863\",\"color\":13129882,\"value\":\"0%\"},{\"id\":\"r_fdr\",\"type\":\"label\",\"nolabel\":1,\"wwidth\":1,\"fsize\":20,\"icon\":\"f159\",\"color\":13129882,\"value\":\"\"},{\"id\":\"r_mll\",\"type\":\"label\",\"nolabel\":1,\"wwidth\":1,\"fsize\":20,\"icon\":\"f159\",\"color\":13129882,\"value\":\"\"}]},{\"id\":\"_n11\",\"type\":\"title\",\"value\":\"Ручное управление\",\"fsize\":30,\"align\":0},{\"type\":\"row\",\"wwidth\":1,\"data\":[{\"id\":\"btn_fan_up\",\"type\":\"button\",\"nolabel\":1,\"wwidth\":1,\"fsize\":40,\"icon\":\"f151\",\"color\":3647804},{\"id\":\"btn_fdr_up\",\"type\":\"button\",\"nolabel\":1,\"wwidth\":1,\"fsize\":40,\"icon\":\"f151\",\"color\":3647804},{\"id\":\"btn_mll_up\",\"type\":\"button\",\"nolabel\":1,\"wwidth\":1,\"fsize\":40,\"icon\":\"f151\",\"color\":3647804}]},{\"type\":\"row\",\"wwidth\":1,\"data\":[{\"id\":\"_n12\",\"type\":\"label\",\"value\":\"Поддув\"\n" + ",\"nolabel\":1,\"wwidth\":1,\"wheight\":20,\"fsize\":17,\"notab\":1,\"align\":0},{\"id\":\"_n13\",\"type\":\"label\",\"value\":\"Шнек\",\"nolabel\":1,\"wwidth\":1,\"wheight\":20,\"fsize\":17,\"notab\":1,\"align\":0},{\"id\":\"_n14\",\"type\":\"label\",\"value\":\"Питатель\",\"nolabel\":1,\"wwidth\":1,\"wheight\":20,\"fsize\":17,\"notab\":1,\"align\":0}]},{\"type\":\"row\",\"wwidth\":1,\"data\":[{\"id\":\"btn_fan_dn\",\"type\":\"button\",\"nolabel\":1,\"wwidth\":1,\"fsize\":40,\"icon\":\"f150\",\"color\":3647804},{\"id\":\"btn_fdr_dn\",\"type\":\"button\",\"nolabel\":1,\"wwidth\":1,\"fsize\":40,\"icon\":\"f150\",\"color\":3647804},{\"id\":\"btn_mll_dn\",\"type\":\"button\",\"nolabel\":1,\"wwidth\":1,\"fsize\":40,\"icon\":\"f150\",\"color\":3647804}]}]}";
	private final String strUpdate = "{\"updates\":{\"alarm\":{\"value\":\"0\"},\"t_in\":{\"value\":\"ОШБК\"},\"t_out\":{\"value\":\"ОШБК\"},\"t_trg\":{\"value\":\"56°C\"},\"rgm_1\":{\"color\":3647804},\"rgm_2\":{\"color\":13158600},\"r_fan\":{\"color\":13129882,\"value\":\"0%\"},\"r_fdr\":{\"color\":13129882,\"value\":\"\"},\"r_mll\":{\"color\":13129882,\"value\":\"\"},\"m_fdr\":{\"color\":13129882},\"m_mll\":{\"color\":13129882},\"w_ttl\":{\"value\":\"Обрыв шлейфа\"},\"sw_onoff\":{\"value\":1},\"slc_m\":{\"value\":0},\"t_rssi\":{\"color\":3199024,\"value\":\"-43dBm\"},\"b_fan10\":{\"color\":13158600},\"btn_fan_up\":{\"color\":3647804},\"btn_fan_dn\":{\"color\":13158600},\"btn_fdr_up\":{\"color\":3647804,\"icon\":\"f152\"},\"btn_fdr_dn\":{\"color\":3647804,\"icon\":\"f153\"},\"btn_mll_up\":{\"color\":3647804,\"icon\":\"f151\"},\"btn_mll_dn\":{\"color\":3647804,\"icon\":\"f150\"}},\"id\":\"a470ab51\",\"type\":\"update\"}";
}