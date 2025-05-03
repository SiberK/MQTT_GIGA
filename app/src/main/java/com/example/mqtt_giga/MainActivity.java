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
public class MainActivity extends AppCompatActivity
        implements AccountUiManager.AccountActionListener {
    private static final String TAG = "MAIN";
    private TextView    tvAccount   ;
    private EditText    etTopicName ;
    private EditText    etCodeWord  ;
    private TextView    tvUID       ;
    private Button      btnStart    ;
    private Button      btnStop     ;
    private TextView    tvSound     ;
    private Uri         uriSound    ;
    private String      strSound    ;
    private AccountUiManager accountUiManager;
    private Account     account     ;
    private static final int RINGTONE_PICKER_REQUEST = 1;
    //---------------------------------------------------------------
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, @NonNull Intent intent) {
            TextView textView = findViewById(R.id.tvTopic)    ; textView.setText(intent.getStringExtra("topic"));
            textView = findViewById(R.id.tvMsg)               ; textView.setText(intent.getStringExtra("message"));
            textView = findViewById(R.id.tvStat)              ; textView.setText(intent.getStringExtra("state"));
            textView = findViewById(R.id.tvAlarm)             ; textView.setText("Alarm: " + intent.getStringExtra("alarm"));
            if(intent.getStringExtra("user_uid") !=null) { tvUID.setText(intent.getStringExtra("user_uid"))  ;}
        }
    };
    //---------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Регистрация ресивера
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.example.MY_ACTION"); // Уникальное действие
        registerReceiver(receiver, filter);

        // Инициализация элементов интерфейса
        tvAccount   = findViewById(R.id.tvAccount)  ;
        etTopicName = findViewById(R.id.etTopicName);
        etCodeWord  = findViewById(R.id.etCodeWord) ;
        tvUID       = findViewById(R.id.tvUID)      ;
        btnStart    = findViewById(R.id.btnStart)   ;
        btnStop     = findViewById(R.id.btnStop)    ;
        tvSound     = findViewById(R.id.tvSound)    ;

        tvAccount.setOnClickListener(v->accountUiManager.showAccountsList());
        accountUiManager = new AccountUiManager(this,this);

        // Обработчик кнопки выбора звука
        findViewById(R.id.tvSound).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { showSoundPicker()   ;}
        });
        //---------------------------------------------------------------
        // Обработчики кнопок запуска и остановки сервиса
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              saveSettings();
              startMqttService();
            }
        });
        //---------------------------------------------------------------
        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopMqttService();
            }
        });

        loadSettings(); // Загрузка сохраненных настроек
    }
    //---------------------------------------------------------------
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver); // Отмена регистрации ресивера
    }
    //---------------------------------------------------------------
    private void showSoundPicker() {
        Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE,
                        RingtoneManager.TYPE_NOTIFICATION);// | RingtoneManager.TYPE_ALARM)      ;
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Выберите звук");
//        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, uriSound);
        startActivityForResult(intent, RINGTONE_PICKER_REQUEST);
    }
    //---------------------------------------------------------------
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RINGTONE_PICKER_REQUEST && resultCode == RESULT_OK) {
            uriSound = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            if (uriSound != null) {
                Log.i(TAG,"Звук выбран" + uriSound);
                Toast.makeText(this, "Звук выбран", Toast.LENGTH_SHORT).show();
            }
            else{
                Log.i(TAG,"Звук не выбран")                     ;
                Toast.makeText(this,"Звук не выбран",Toast.LENGTH_SHORT).show();
            }
        }
        super.onActivityResult(requestCode, resultCode, data)   ;
        strSound = uriSound.toString()    ;
        tvSound.setText(strSound)       ;
    }
    //---------------------------------------------------------------
    private void saveSettings() {                       // Сохранение настроек
        if(uriSound != null)  strSound = uriSound.toString()    ;
        getSharedPreferences("MQTT_SETTINGS", MODE_PRIVATE)
                .edit()
                .putString("SRV_ADDRESS", account.getServer())
                .putInt   ("PORT"       , Integer.parseInt(account.getPort()))
                .putString("LOGIN"      , account.getLogin())
                .putString("PASSWORD"   , account.getPassword())
                .putString("TOPIC_NAME" , etTopicName .getText().toString())
                .putString("CODE_WORD"  , etCodeWord  .getText().toString())
                .putString("USER_UID"   , tvUID       .getText().toString())
                .putString("SEL_RINGTONE",strSound)
                .apply();
    }
    //---------------------------------------------------------------
    // Загрузка сохраненных настроек
    private void loadSettings() {
        SharedPreferences prefs = getSharedPreferences("MQTT_SETTINGS", MODE_PRIVATE);
        String serverAddress = prefs.getString("SRV_ADDRESS", "test.mosquitto.org");
        int port            = prefs.getInt   ("PORT"        , 1883)     ; // 1883 - стандартный порт MQTT
        String login        = prefs.getString("LOGIN"       , "")       ;
        String password     = prefs.getString("PASSWORD"    , "")       ;
        String topicName    = prefs.getString("TOPIC_NAME"  , "test512");
        String codeWord     = prefs.getString("CODE_WORD"   , "")       ;
        String user_uid     = prefs.getString("USER_UID"    , "")       ;
        strSound = prefs.getString("SEL_RINGTONE", "")       ;
        uriSound = Uri.parse(strSound)   ;

        account = new Account(serverAddress,String.valueOf(port),login,password)    ;
        tvAccount.setText(account.toString());

        etTopicName .setText(topicName) ;
        etCodeWord  .setText(codeWord)  ;
        tvUID       .setText(user_uid)  ;
        tvSound     .setText(strSound)  ;
    }
    //---------------------------------------------------------------
    // Запуск сервиса
    private void startMqttService() {
        Intent serviceIntent = new Intent(MainActivity.this, MQTTService.class);
        serviceIntent.putExtra("SERVER_ADDRESS" , account.getServer());
        serviceIntent.putExtra("PORT"           , Integer.parseInt(account.getPort()));
        serviceIntent.putExtra("LOGIN"          , account.getLogin());
        serviceIntent.putExtra("PASSWORD"       , account.getPassword());
        serviceIntent.putExtra("TOPIC_NAME"     , etTopicName.getText().toString());
        serviceIntent.putExtra("CODE_WORD"      , etCodeWord.getText().toString());
        serviceIntent.putExtra("USER_UID"       , tvUID.getText().toString());
        serviceIntent.putExtra("SEL_RINGTONE"   , strSound);
//        ContextCompat.startForegroundService(this, serviceIntent);
        startService(serviceIntent) ;
        Toast.makeText(MainActivity.this, "Сервис запущен", Toast.LENGTH_SHORT).show();
    }
    //---------------------------------------------------------------
    // Остановка сервиса
    private void stopMqttService() {
        Intent serviceIntent = new Intent(MainActivity.this, MQTTService.class);
        stopService(serviceIntent);
        Toast.makeText(MainActivity.this, "Сервис остановлен", Toast.LENGTH_SHORT).show();
    }
    //---------------------------------------------------------------
    @Override
    public void onAccountSelected(Account _account) {
        account = _account  ;
        tvAccount.setText(account.toString());
    }
    //---------------------------------------------------------------
    @Override
    public void onAccountListDismissed() {
        // Можно добавить дополнительную логику при закрытии списка
    }
    //---------------------------------------------------------------
    @Override
    public void onAccountUpdated() {
        // Обновление UI при изменении аккаунтов
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
            startActivity(new Intent(this, SettingsActivity.class));
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