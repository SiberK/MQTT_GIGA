package gson_parser;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.mqtt_giga.R;
import com.google.android.material.button.MaterialButton;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.lang.reflect.Type;
import java.util.List;

public class UiElementDeserializer implements JsonDeserializer<UiElement>{
  private static final String          	TAG         = "UI_DESER";
  @Override
  public UiElement deserialize(JsonElement json,Type typeOfT,JsonDeserializationContext context){
    JsonObject jsonObject = json.getAsJsonObject()              ;
    String     type       = jsonObject.get("type").getAsString();
    UiElement elt = new UiElement() ;

    switch (type) {
      case "button":
        elt = context.deserialize(json, ButtonElement.class)  ; break ;
      case "label": case "menu": case "title":
        elt = context.deserialize(json, LabelElement.class)   ; break ;
      case "row": case "col": case "ui":
        elt = context.deserialize(json, FrameElement.class)   ; break ;
      case "switch_t":
        elt = context.deserialize(json, SwitchElement.class)  ; break ;
      case "select":
        elt = context.deserialize(json, SelectElement.class)  ; break ;
      default:
        elt =  context.deserialize(json, LabelElement.class)  ; break ;
    }
    return elt  ;}
//------------------------------------------------------------------------
  public void updateView(UiElement updE, View view,Typeface fontAwesome){
    if(view == null) return ;

    if(view instanceof Switch){
      if(updE.value != null)
        ((Switch)view).setChecked(updE.getValue().equals("1")) ;
    }
    else if(view instanceof Spinner){
      Spinner sp = (Spinner) view       ;
      if(updE.value != null){
        int ix = updE.getIValue()       ;
        if(ix >= 0) sp.setSelection(ix) ;
      }
    }
    else if(view instanceof TextView){
      TextView vt = (TextView)view    ;
      if(updE.color != null) vt.setTextColor(updE.getColorF())    ;
      if(updE.icon  != null && fontAwesome != null){
        String str = updE.getIcChar()       ;
        if(updE.value != null) str += "  " + updE.getValue()      ;
        vt.setTypeface(fontAwesome)         ; vt.setText(str)     ;}
      else if(updE.value != null) vt.setText(updE.getValue())     ;
//    TODO TODO
    }
  }
  //====================================================================================
  public class FrameElement extends UiElement {
	private List<UiElement> data;
	private List<UiElement> controls;

	public int getOrientation() { return type.equals("row") ? LinearLayout.HORIZONTAL : LinearLayout.VERTICAL;}
	public List<UiElement> getChildren() { return data != null ? data : controls; }

	public LinearLayout createView(Context context,int viewId,int parentOrientation, Typeface fontAwesome){
	  float wgt = wwidth > 0 ? 1.0f/wwidth : 1.0f		;
	  int   wdth= ViewGroup.LayoutParams.MATCH_PARENT	;
	  int   hgt = ViewGroup.LayoutParams.WRAP_CONTENT	;
	  if(parentOrientation == LinearLayout.HORIZONTAL){ wgt = wwidth > 0 ? wwidth : 1	; wdth = 0	;}
	  LinearLayout ly = new LinearLayout(context)   	;
	  ly.setId(idView = viewId)                   		;
	  ly.setOrientation(getOrientation())           	;
//	  ly.setPadding(2,2,2,2)                        	;
	  LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(wdth,hgt,wgt)        ;
//	lp.setMargins(4,4,4,4);
	  ly.setLayoutParams(lp);
	  return ly;
	}
  }
  //====================================================================================
  public class LabelElement extends UiElement {
	public TextView createView(Context context,int viewId,int parentOrientation, Typeface fontAwesome){
	  int al = getAlign()   ;
	  int fz = getFsize()   ;
	  TextView tv = new TextView(context);
	  tv.setId(idView = viewId);
	  tv.setTag(getId())  	;
   	  tv.setLayoutParams(getLayoutParams(parentOrientation))  ;
	  int clr = getColorF() ;
	  if (clr != 0xFF000000) tv.setTextColor(clr);
	  if(fz > 0) tv.setTextSize(fz) ;
	  int algmnt = al == 0 ? TextView.TEXT_ALIGNMENT_VIEW_START :
				   al == 1 ? TextView.TEXT_ALIGNMENT_CENTER     :
				   al == 2 ? TextView.TEXT_ALIGNMENT_VIEW_END   : TextView.TEXT_ALIGNMENT_CENTER  ;
	  tv.setTextAlignment(algmnt)   ;
	  tv.setSingleLine()			;

	  if(fontAwesome != null && icon != null){
		String str = getIcChar()            ;
		if(!getValue().isEmpty()) str += "  " + getValue()      ;
		tv.setTypeface(fontAwesome)         ; tv.setText(str)   ;}
	  else tv.setText(getValue())           ;

      tv.setPadding(0,4,0,4)				;
	  if(notab == 0 && type.equals("label")){
	    tv.setBackgroundResource(R.drawable.btn_device_bg)	;}
	  if(type.equals("menu")) tv.setVisibility(View.GONE)	;
	  return tv ;
	}
  }
  //====================================================================================
  public class ButtonElement extends UiElement {
    public MaterialButton createView(Context context,int viewId,int parentOrientation, Typeface fontAwesome){
      MaterialButton btn = new MaterialButton(context,null, R.style.DeviceButtonStyle);
      int fz = getFsize()   	;
      int al = getAlign()   	;

      btn.setId(idView = viewId);
      btn.setText(getText())	;
      btn.setTag(getId())  	    ;
	  btn.setLayoutParams(getLayoutParams(parentOrientation))  ;
      btn.setTextSize(fz > 0 ? fz : 26) ;
      btn.setAllCaps(false)		;
      btn.setPadding(0,4,0,4)	;
      btn.setTextAlignment(MaterialButton.TEXT_ALIGNMENT_CENTER);
	  btn.setSingleLine()		;

      int clr = getColorF()      ;
      if(clr == 0xFF000000) clr = DEF_COLOR   ;
      btn.setTextColor(clr);
//      btn.setTextColor(ContextCompat.getColorStateList(context, R.color.black));
      btn.setBackgroundResource(R.drawable.btn_device_bg)	;
      btn.setBackgroundTintList(null)						; // очищаем стандартную заливку
      if(fontAwesome != null && !getIcon().isEmpty()){
        btn.setTypeface(fontAwesome)                        ;
        btn.setText(getIcChar())                            ;}
      return btn;
    }
  }
  //====================================================================================
  public class SwitchElement extends UiElement {
    public Switch createView(Context context,int viewId,int parentOrientation,Typeface fontAwesome){
      Switch sw = new Switch(context)               ;
      sw.setId(idView = viewId);
	  sw.setLayoutParams(getLayoutParams(parentOrientation))        ;
      sw.setText(getText())                         ;
      sw.setTag(getId())  	                        ;
      sw.setTextSize(fsize > 0 ? fsize : 26)        ;
      sw.setChecked(getValue().equals("1"))         ;
	  sw.setPadding(0,4,0,4)	;
	  if(notab == 0) sw.setBackgroundResource(R.drawable.btn_device_bg)	;
      return sw;
    }
  }
  //====================================================================================
  public class SelectElement extends UiElement {
	public Spinner createView(Context context,int viewId,int parentOrientation, Typeface fontAwesome){
	  Spinner sp = new Spinner(context)           ;
	  sp.setId(idView = viewId)                   ;
	  sp.setLayoutParams(getLayoutParams(parentOrientation))	  ;
	  String items[] = !getText().isEmpty() ?  getText().split(";") : new String[0]   ;
	  ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, R.layout.custom_spinner_item, items) {
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
		  int clr = getColorF()      ;
		  TextView view = (TextView) super.getView(position, convertView, parent);
		  view.setTextColor(clr)                  ;
		  view.setTextSize(getFsize())            ;
		  view.setLayoutParams(getLayoutParams()) ;
		  view.setGravity(Gravity.CENTER)		  ;
		  return view;}
		@Override
		public View getDropDownView(int position, View convertView, ViewGroup parent) {
		  TextView view = (TextView) super.getDropDownView(position, convertView, parent);
		  int clr = getColorF()                   ;
		  view.setTextColor(clr)                  ;
		  view.setTextSize(getFsize())        ;//-2
		  view.setGravity(Gravity.CENTER)		  ;
//          view.setLayoutParams(getLayoutParams()) ;
		  return view;
		}
	  };
	  try{ sp.setAdapter(adapter)                 ;
	  }catch(IllegalArgumentException e){/*Log.w(UiElement.TAG,"",e);*/}

	  sp.setTag(getId())  		;
	  sp.setPadding(0,4,0,4)	;
	  sp.setGravity(Gravity.CENTER)	;
	  if(notab == 0) sp.setBackgroundResource(R.drawable.btn_device_bg)	;
	  int ix = getIValue()  	;
	  if(ix >= 0 && ix < items.length) sp.setSelection(ix);

	  return sp;
	}
  }
  //====================================================================================
}
