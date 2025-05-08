package com.example.mqtt_giga;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class DeviceManager {
    private final String TAG = "DEV_MNGR"  ;
    private static final String FILE_NAME = "devices.dat";
    private static List<Device> deviceList  ;
    private Context context;

//-----------------------------------------------------------
    public DeviceManager(Context context){
        this.context = context.getApplicationContext();
        this.deviceList = new ArrayList<>();
        loadDevListFromJson()       ;
    }
//-----------------------------------------------------------
    public boolean addDevice(Device val){
        boolean rzlt = false        ;
        if(val.isValid() && findByPrefix(val.prefix) == null) {
            deviceList.add(val)     ;
            saveDevListToJson()     ;
            rzlt = true             ;
        }
        return rzlt                 ;}
//-----------------------------------------------------------
    public Device addDevice(String prfx){
        Device dev = null                   ;
        if(prfx != null && findByPrefix(prfx) == null) {
            dev = new Device("",prfx,"")    ;
            deviceList.add(dev)             ;
            saveDevListToJson()             ;
        }
        return dev                          ;}
    //-----------------------------------------------------------
    public void removeDevice(int position){
        if(position >= 0 && position < deviceList.size()){
            deviceList.remove(position) ;
            saveDevListToJson()         ;
        }
    }
//-----------------------------------------------------------
    public static List<Device> getDeviceList(){ return new ArrayList<>(deviceList) ;}
//-----------------------------------------------------------
    public Device getDevice(int ix){
        return (ix<0 || ix>=deviceList.size()) ? null : deviceList.get(ix)  ;}
//-----------------------------------------------------------
//-----------------------------------------------------------
//-----------------------------------------------------------
    private Device findByPrefix(String val){
        for(Device dev : deviceList)
          if(dev.prefix.equals(val)) return dev ;

        return null ;}
//-----------------------------------------------------------
private void saveDevListToJson() {
    File   file = new File(context.getFilesDir(), "devices.json");
    Gson   gson = new Gson();
    String json = gson.toJson(deviceList);  // Преобразуем List<Device> в JSON-строку

    try (FileWriter writer = new FileWriter(file)) {
        writer.write(json);  // Записываем JSON в файл
    } catch (IOException e) {
        Log.e("JSON", "Ошибка сохранения: " + e.getMessage());
    }
}
    private void loadDevListFromJson() {
        File file = new File(context.getFilesDir(), "devices.json");
        if (!file.exists()) {
            deviceList = new ArrayList<>();  // Файла нет — создаём пустой список
            return;
        }

        try (FileReader reader = new FileReader(file)) {
            Gson gson = new Gson();
            // Указываем тип для корректной десериализации: List<Device>
            Type type = new TypeToken<List<Device>>() {}.getType();
            deviceList = gson.fromJson(reader, type);  // Читаем JSON в List<Device>

            if (deviceList == null) {
                deviceList = new ArrayList<>();  // Если файл пустой
            }
        } catch (IOException e) {
            Log.e("JSON", "Ошибка загрузки: " + e.getMessage());
            deviceList = new ArrayList<>();
        }
    }
    private void saveDevList(){
        Log.d("Serialization", "Saving list: " + deviceList);
        for (Device device : deviceList) {
            if (device == null) {
                Log.e("Serialization", "Found null Device in list!");
            }
        }

        FileOutputStream fos    ;
        ObjectOutputStream oos  ;
        File tempFile = new File(context.getFilesDir(), FILE_NAME + ".tmp");
        File finalFile = new File(context.getFilesDir(), FILE_NAME);
        try {
            fos = new FileOutputStream(tempFile);
            oos = new ObjectOutputStream(fos);
            oos.writeObject(new ArrayList<>(deviceList));
            oos.close();
            fos.close();
            tempFile.renameTo(finalFile); // Атомарная замена
        } catch (Exception e) {
            Log.e("Serialization", "Error saving device list", e);
        }
    }
//-----------------------------------------------------------
    private void loadDevList() {
        FileInputStream     fis     ;
        ObjectInputStream   ois     ;
        try {
            fis = new FileInputStream(new File(context.getFilesDir(), FILE_NAME));
            ois = new ObjectInputStream(fis)  ;
            deviceList = (List<Device>) ois.readObject()        ;
            ois.close()         ;
            fis.close()         ;
        } catch (Exception e) {
            Log.i(TAG,"exeptionnn: ",e);
//            e.printStackTrace();
        }
    }
//===========================================================
    public class Device implements Serializable{
        private static final long serialVersionUID = 1L;  // Рекомендуется

        public String   ix      ;
        public String   name    ;
        public String   prefix  ;
        public String   version ;
//-----------------------------------------------------------
        private boolean noValid(){
            return name.isEmpty() || prefix.isEmpty()         ;}
    //-----------------------------------------------------------
        private boolean isValid(){
            return !name.isEmpty() && !prefix.isEmpty()       ;}
    //-----------------------------------------------------------
        public Device(String _name,String prfx,String ver){
            name = _name    ; prefix = prfx ; version = ver ; ix = ""   ;}
    //-----------------------------------------------------------
        public String getIx()             { return ix     ;}
        public void setIx(String val)     { ix = val      ;}

        public String getName()           {return  name   ;}
        public void setName(String val)   { name = val    ;}

        public String getPrefix()         {return prefix  ;}
        public void setPrefix(String val) { prefix = val  ;}

        public String getVersion()        {return version ;}
        public void setVersion(String val){version = val  ;}
    }
//===========================================================
}
