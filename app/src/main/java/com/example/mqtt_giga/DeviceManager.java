package com.example.mqtt_giga;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
//-----------------------------------------------------------
public class DeviceManager {
    private final String TAG = "MNGR_DEV"  ;
    private static final String FILE_NAME = "devices";
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
    public void saveDevListToJson() {
        if(deviceList == null) return   ;

        File   file = new File(context.getFilesDir(), FILE_NAME +".json");
        File tempFile = new File(context.getFilesDir(), FILE_NAME + ".tmp");// Сначала пишем во временный файл

        try (OutputStreamWriter writer = new OutputStreamWriter(
            new FileOutputStream(tempFile), StandardCharsets.UTF_8)) {
            new Gson().toJson(deviceList, writer)                       ;// Сериализация и запись
            writer.close()                                              ;// Закрываем writer перед переименованием
            if(tempFile.renameTo(file)) Log.i(TAG,"Save DevList OK")    ;
            else Log.w(TAG,"Не удалось переименовать временный файл")   ;
        } catch (IOException | JsonIOException e) {
            Log.e(TAG, "Ошибка сохранения DevList: " + e.getMessage())  ;
        }finally{
            if (tempFile.exists() && !tempFile.delete()) {
                Log.w(TAG, "Не удалось удалить временный файл")         ;}
        }
    }
//----------------------------------------------------------------------------------------
    private void loadDevListFromJson() {
        deviceList = null   ;
        File file = new File(context.getFilesDir(), FILE_NAME +".json");
        if(file.exists() && file.length() != 0){
            try(InputStreamReader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)){
                Gson gson = new Gson()                                  ;
                Type type = new TypeToken<List<Device>>(){}.getType()   ;// Указываем тип для корректной десериализации: List<Device>
                deviceList = gson.fromJson(reader, type)                ;  // Читаем JSON в List<Device>
            } catch(IOException | JsonSyntaxException | JsonIOException e){
                Log.e("JSON", "Ошибка загрузки: " + e.getMessage())     ;}
            Log.i(TAG,"Load DevList OK")    ;
        }
        if (deviceList == null) deviceList = new ArrayList<>()          ;  // Если файл пустой
        for(Device dvc : deviceList){
            if(dvc.uid == null) dvc.uid = ""    ;
        }
    }
//----------------------------------------------------------------------------------------
    public int setDevName(String devPfx, String devName){
        int id = -1 ;
        Device dvc = findByPfx(devPfx)  ;
        if(dvc != null && !devName.isEmpty()){
            dvc.name = devName  ;
            id = dvc.btnId      ;// чтоб найти кнопку на форме
        }
        return id   ;}
//----------------------------------------------------------------------------------------
    public Device findByPfx(String devPfx){
        Device dvc = null   ;
        if(!devPfx.isEmpty()){
            for(Device devIx : deviceList){
                if(devIx.prefix.equals(devPfx)){
                    dvc = devIx   ; break   ;}}
        }
        return dvc  ;}
    //===========================================================
    public class Device {
        private static final long serialVersionUID = 1L;  // Рекомендуется

        private String   ix      ;
        private String   name    ;
        private String   prefix  ;
        private String   uid     ;
        private String   version ;
        private int      btnId   ;
//-----------------------------------------------------------
        private boolean noValid(){
            return name.isEmpty() || prefix.isEmpty()         ;}
    //-----------------------------------------------------------
        private boolean isValid(){
            return !name.isEmpty() && !prefix.isEmpty()       ;}
    //-----------------------------------------------------------
        public Device(String _name,String prfx,String ver){
            name = _name    ; prefix = prfx ; version = ver ;
            ix = ""         ; uid = ""      ; btnId = 0   ;}
    //-----------------------------------------------------------
        public String getIx()             { return ix     ;}
        public void setIx(String val)     { ix = val      ;}

        public String getUID()             { return uid   ;}
        public boolean setUID(String val) { boolean fl = !val.equals(uid) ; uid = val    ; return fl  ;}

        public String getName()           {return  name   ;}
        public boolean setName(String val){ boolean fl = !val.equals(name) ; name = val    ; return fl  ;}

        public String getPrefix()         {return prefix    ;}
        public void setPrefix(String val) { prefix = val    ;}

        public String getVersion()        {return version   ;}
        public void setVersion(String val){version = val    ;}

        public void setBtnId(int val)     { btnId = val     ;}
        public int  getBtnId()            { return btnId    ;}
    }
//===========================================================
}
