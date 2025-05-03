package com.example.mqtt_giga;

import android.util.Log;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

public class MyParserJson {
    private static final String TAG = "MQTT_PARSER";
    private final JsonObject root;

    /**
     * Конструктор, принимающий JSON строку и парсящий её в объект JsonObject.
     *
     * @param jsonString входящая JSON строка
     */
    public MyParserJson(String jsonString) {
        JsonParser parser = new JsonParser();
        JsonObject obj = null            ;
        try {
            obj  = parser.parseString(jsonString).getAsJsonObject();
        }catch(JsonParseException | IllegalStateException e){
//            Log.i(TAG,"***MyParserJson*** Exception: IllegalState",e)    ;
            obj = null  ;
        }
        root = obj       ;
    }

    /**
     * Метод для доступа к значениям по цепочке ключей.
     *
     * @param keys последовательность ключей для поиска значения
     * @return найденное значение или "", если путь не существует
     */
    public String GetKey(String keys) {
        String      result  = "", key = ""              ;
        if(root != null) {
            JsonObject current = root;
            String[] arrKeys = keys.split(",");

            try {
                for (int ix = 0; ix < arrKeys.length; ix++) {
                    key = arrKeys[ix];
                    Log.i(TAG, key);
                    if (current != null && current.isJsonObject() && current.get(key).isJsonObject()) {
                        current = current.getAsJsonObject(key);
                    } else break; // Путь не существует
                }
                if (current != null && !key.isEmpty()) {
                    if (current.isJsonObject())
                        result = current.toString();
                    else result = current.get(key).getAsString();
                }
            } catch (UnsupportedOperationException | IllegalStateException | ClassCastException |
                     java.lang.NullPointerException e) {
                result = "";
            }
        }
        return result       ; // Возвращаем конечное значение
    }

    /**
     * Метод для доступа к значениям по цепочке ключей.
     *
     * @param keys последовательность ключей для поиска значения
     * @return найденное значение или "", если путь не существует
     */
    public String get(String... keys) {
        String      result  = "", strKey = ""           ;
        JsonObject  current = root                      ;

        for (String key : keys) {
            strKey = key    ; Log.i(TAG, key)           ;
            if (current != null && current.isJsonObject() && current.get(key).isJsonObject()) {
                current = current.getAsJsonObject(key)  ;
            } else break                                ; // Путь не существует
        }
        try {
            if (current != null && !strKey.isEmpty()) {
                if (current.isJsonObject())
                    result = current.toString()      ;
                else result = current.get(strKey).getAsString();
            }
        } catch (UnsupportedOperationException | IllegalStateException e) { result = "" ;}

        return result       ; // Возвращаем конечное значение
    }
}
