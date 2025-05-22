package com.example.mqtt_giga;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.example.mqtt_giga.DeviceManager.Device;
import com.example.mqtt_giga.MqttWork.Message;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;

import static com.google.android.material.internal.ViewUtils.dpToPx;

//---------------------------------------------------------------
public class MainActivity extends AppCompatActivity{
    private static final String TAG = "MAIN";
    private SettingsActivity settingsActivity   ;
    private DeviceManager devManager            ;
    private Device curDevice         			;

    private String StrTopic     ;
    private String StrMessage   ;
    private       String            StrDevice    ;
    //---------------------------------------------------------------
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, @NonNull Intent intent) {
            messageParse(intent.getStringExtra("topic"),intent.getStringExtra("message"))   ;
            addDevice(intent.getStringExtra("device"))      ;
//            textView = findViewById(R.id.tvStat)          ; textView.setText(intent.getStringExtra("state"));
//            textView = findViewById(R.id.tvAlarm)         ; textView.setText("Alarm: " + intent.getStringExtra("alarm"));
//            if(intent.getStringExtra("user_uid") !=null) { tvUID.setText(intent.getStringExtra("user_uid"))  ;}
        }
    };
    //---------------------------------------------------------------
    private void messageParse(String topic, String message) {
        if(topic != null && message != null){
		    boolean flRe = false	;// сохранить список устройств
		    Button btn   = null		;
            Message msg  = new Message(topic,message)			;

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
			}

			if(flRe){		// была дополнена информация по устройству?
			  devManager.saveDevListToJson()					;
				fillListPing()	;}
        }
    }
  //---------------------------------------------------------------
  private void fillListPing(){
	Intent broadcastIntent = new Intent("TO_MQTT_SERVICE") 		;// Отправка данных в активность
	broadcastIntent.putExtra("fill_list_ping","fill_list_ping")   ;
	sendBroadcast(broadcastIntent)          			;
  }
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
		devManager.removeDevice(pfx)    		;
	  	createDeviceMatButtons()				;
		fillListPing()							;}
  //---------------------------------------------------------------
    private void findDevice(String pfx) {
        Intent broadcastIntent = new Intent("TO_MQTT_SERVICE") ;// Отправка данных в активность
        broadcastIntent.putExtra("find",pfx)    ;
        sendBroadcast(broadcastIntent)          ;}
    //---------------------------------------------------------------
    private void subscribeTo(String pfx) {
        Intent broadcastIntent = new Intent("TO_MQTT_SERVICE") ;// Отправка данных в активность
        broadcastIntent.putExtra("prefix",pfx)  ;
        sendBroadcast(broadcastIntent)          ;}
//---------------------------------------------------------------
  private void createDeviceMatButtons() {
	MaterialButton 	btn			;
	TableRow       	row = null	;
	Device 			device		;
	String			ttl			;
	TableLayout cont = findViewById(R.id.cont)				;

	List<Device> devices = DeviceManager.getDeviceList()	;
	if(cont == null || devices == null || devices.size() == 0){
	  Log.e(TAG, "GridLayout или список устройств равен null");
	  return	;}
	cont.removeAllViews()										; // Очистка предыдущих кнопок

	TableRow.LayoutParams params = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,
															 TableRow.LayoutParams.WRAP_CONTENT,1.0f);
	params.setMargins(8, 8, 8, 8);

	for (int i = 0; i < devices.size(); i++) {
	  if(i % 2 == 0){ row = new TableRow(this)				;
		cont.addView(row)						;}

	  device = devices.get(i)	; if(device == null) break	;

	  ttl = device.getName()								;
	  if(ttl.isEmpty()) ttl = device.getPrefix()			;
	  int btnId = View.generateViewId()						;
	  device.setBtnId(btnId)								;

	  btn = createMatButton(ttl,btnId,device)				;
	  if(btn != null) row.addView(btn,params)				;
	}
	if(devices.size() % 2 != 0 && row != null){
	  View spacer = new View(this)			;
	  row.addView(spacer,params)			;
	}
  }
  //----------------------------------------------------------------------------
  private MaterialButton createMatButton(String ttl, int btnId, Device device){
	MaterialButton btn = new MaterialButton(this,null, R.style.DeviceButtonStyle);
	btn.setText(ttl)				;
	btn.setHint(device.getPrefix())	;
	btn.setId(btnId)				;
	btn.setTextSize(26)				;
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
private void onDeviceClicked(Device device) {
  // Обработка нажатия на устройство
  Toast.makeText(this, "Выбрано: " + device.getPrefix(), Toast.LENGTH_SHORT).show();

  Intent broadcastIntent = new Intent("TO_MQTT_SERVICE") 	;// Отправка данных в активность
  broadcastIntent.putExtra("prefix",device.getPrefix())		;
  broadcastIntent.putExtra("type_msg","find")    			;
  broadcastIntent.putExtra("dev_uid" ,device.getUID())   	;
  sendBroadcast(broadcastIntent)          					;
}
  //---------------------------------------------------------------
  @SuppressLint("UnspecifiedRegisterReceiverFlag")
  @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState)          ;
        setContentView(R.layout.activity_main)      ;

        // Регистрация ресивера
        IntentFilter filter = new IntentFilter()    ;
        filter.addAction("FROM_MQTT_SERVICE")       ;// Уникальное действие
        filter.addAction("TO_MAIN")                 ;// Уникальное действие

	// Регистрация ресивера
	if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU)
	  registerReceiver(receiver, filter)		                ;
	else registerReceiver(receiver, filter,RECEIVER_NOT_EXPORTED);

        devManager = new DeviceManager(this)        ;
		createDeviceMatButtons()					;
  }
    //---------------------------------------------------------------
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        // Получаем ColorStateList
        ColorStateList colorStateList = ContextCompat.getColorStateList(this, R.color.icon_color_selector);

        // Применяем к каждой иконке
        for (int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.getItem(i);
            Drawable icon = item.getIcon();
            if (icon != null) {
                icon = DrawableCompat.wrap(icon);
                DrawableCompat.setTintList(icon, colorStateList);
                item.setIcon(icon);
            }
        }
        return true;
    }
    //---------------------------------------------------------------
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean rzlt = super.onOptionsItemSelected(item);
        // Обработка нажатий на пункты меню
        if(item.getItemId() == R.id.action_settings){
//            Toast.makeText(this, "action_settings", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, SettingsActivity.class) ;
            startActivity(intent)   ;
            rzlt = true   ;
        }
//        else if(item.getItemId() == R.id.action_search){
//            Toast.makeText(this, "action_search", Toast.LENGTH_SHORT).show();
//            rzlt = true   ;
//        }
//        else if(item.getItemId() == R.id.action_help){
//            Toast.makeText(this, "action_help", Toast.LENGTH_SHORT).show();
//            rzlt = true   ;
//        }
    return rzlt ;
    }
    //---------------------------------------------------------------
}