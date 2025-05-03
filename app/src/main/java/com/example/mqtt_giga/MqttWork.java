package com.example.mqtt_giga;

import java.util.Stack;
//---------------------------------------------------------------------------------
public class MqttWork {
    private static final String TAG = "MQTT_WORK";
    private String HubTag[] = {
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
//---------------------------------------------------------------------------------
    public String   ReplaceTag(String Str){
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
    public boolean checkBracketsBalance(String input) {
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
}


