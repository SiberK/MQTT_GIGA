package com.example.mqtt_giga;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.annotation.SuppressLint;
import android.media.RingtoneManager;
import android.net.Uri;

import org.w3c.dom.Text;

import accountmanagerlib.Account;
import accountmanagerlib.AccountUiManager;
//---------------------------------------------------------------
public class MainActivity extends AppCompatActivity{
    private static final String TAG = "MAIN";
    private SettingsActivity settingsActivity   ;
    private DeviceManager devManager            ;
    private DeviceManager.Device device         ;

    private String StrTopic     ;
    private String StrMessage   ;
    private String StrDevice    ;
    //---------------------------------------------------------------
    private BroadcastReceiver receiver = new BroadcastReceiver() {
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
            MqttWork.Message msg = new MqttWork.Message(topic,message)  ;
        }
    }
    //---------------------------------------------------------------
    private void addDevice(String pfx) {
        if(pfx != null && !pfx.isEmpty()){
            device = devManager.addDevice(pfx)     ;// если pfx не новый, то вернёт null
            if(device != null){
            }
            subscribeTo(pfx)                    ;
            pingDevice(pfx)                     ;
        }
    }
    //---------------------------------------------------------------
    private void pingDevice(String pfx) {
        Intent broadcastIntent = new Intent("TO_MQTT_SERVICE") ;// Отправка данных в активность
        broadcastIntent.putExtra("ping",pfx)    ;
        sendBroadcast(broadcastIntent)          ;}
    //---------------------------------------------------------------
    private void subscribeTo(String pfx) {
        Intent broadcastIntent = new Intent("TO_MQTT_SERVICE") ;// Отправка данных в активность
        broadcastIntent.putExtra("prefix",pfx)  ;
        sendBroadcast(broadcastIntent)          ;}
    //---------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState)          ;
        setContentView(R.layout.activity_main)      ;

        // Регистрация ресивера
        IntentFilter filter = new IntentFilter()    ;
        filter.addAction("FROM_MQTT_SERVICE")       ;// Уникальное действие
        filter.addAction("TO_MAIN")                 ;// Уникальное действие
        registerReceiver(receiver, filter)          ;

        devManager = new DeviceManager(this)        ;
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
            Toast.makeText(this, "action_settings", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(this, SettingsActivity.class) ;
//            intent.putExtra("key", "Значение для передачи");
            startActivity(intent)   ;

            rzlt = true   ;
        }
        else if(item.getItemId() == R.id.action_search){
            Toast.makeText(this, "action_search", Toast.LENGTH_SHORT).show();
            rzlt = true   ;
        }
        else if(item.getItemId() == R.id.action_help){
            Toast.makeText(this, "action_help", Toast.LENGTH_SHORT).show();
            rzlt = true   ;
        }
    return rzlt ;
    }
    //---------------------------------------------------------------
}