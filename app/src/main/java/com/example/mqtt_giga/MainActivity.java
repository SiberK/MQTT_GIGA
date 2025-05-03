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
    //---------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Регистрация ресивера
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.example.MY_ACTION"); // Уникальное действие
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