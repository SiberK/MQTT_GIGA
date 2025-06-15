package gson_parser;

import android.content.Context;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.example.mqtt_giga.UiWidget;
import static com.example.mqtt_giga.MqttWork.noEmpty;
import com.example.mqtt_giga.R;
import com.google.android.material.button.MaterialButton;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.lang.reflect.Type;
import java.util.List;

import static gson_parser.UiElement.getIntValue;

public class UiElementDeserializer implements JsonDeserializer<UiElement>{
  private static final String          	TAG         = "UI_DESER";

  @Override
  public UiElement deserialize(JsonElement json,Type typeOfT,JsonDeserializationContext context){
    JsonObject jsonObject = json.getAsJsonObject()              ;
    String     type       = jsonObject.get("type").getAsString();
    UiElement elt ;

    switch (type) {
      case "button":
        		elt = context.deserialize(json, ButtonElement.class)  ; break ;
	  case "label": case "title":
				elt = context.deserialize(json, LabelElement.class)   ; break ;
	  case "menu":
				elt = context.deserialize(json, MenuElement.class)   ; break ;
      case "row": case "col": case "ui":
        		elt = context.deserialize(json, FrameElement.class)   ; break ;
      case "switch_t":
        		elt = context.deserialize(json, SwitchElement.class)  ; break ;
      case "select":
        		elt = context.deserialize(json, SelectElement.class)  ; break ;
	  case "input": case "pass":
				elt = context.deserialize(json, InputElement.class)  ; break ;
      default:
        		elt =  context.deserialize(json, LabelElement.class)  ; break ;
    }
    return elt  ;}
//------------------------------------------------------------------------
private static int getTextAlignment(String align){
  int al = getIntValue(align, 1)					;
  return 	al == 0 ? TextView.TEXT_ALIGNMENT_VIEW_START :
			al == 1 ? TextView.TEXT_ALIGNMENT_CENTER     :
			al == 2 ? TextView.TEXT_ALIGNMENT_VIEW_END   : TextView.TEXT_ALIGNMENT_CENTER  ;}
  //====================================================================================
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
    }

    else if(view instanceof UiWidget){
	  UiWidget wd = (UiWidget) view    ;
	  if(updE.color != null) wd.setTextColor(updE.getColorF())    	;
	  if(updE.icon  != null) wd.setIcon(updE.getIcChar())			;
	  if(updE.value != null) wd.setText(updE.getValue())     		;
	  if(updE.fsize != 0)    wd.setTextSize(updE.fsize)				;
	  if(updE.align != null) wd.setTextAlignment(getTextAlignment(updE.align));
	}
//    TODO TODO
  }
  //====================================================================================
  public class FrameElement extends UiElement {//	"row","col","ui"
	private List<UiElement> data			;
	private List<UiElement> controls		;
	//------------------------------------------------------
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
	//------------------------------------------------------
	public int getOrientation() { return type.equals("row") ? LinearLayout.HORIZONTAL : LinearLayout.VERTICAL;}
	//------------------------------------------------------
	public List<UiElement> getChildren() { return data != null ? data : controls; }
  }
  //====================================================================================
  public class InputElement extends UiElement {
	public UiWidget createView(Context context, int viewId, int parentOrientation){
	  int al = getAlign()   ;
	  int fz = getFsize()   ; if(fz == 0) fz = DEF_FNT_SIZE	;
	  int clr = getColorF() 						;
	  UiWidget wd = new UiWidget(context,getType(),clr,fz)	;
	  wd.setId(idView = viewId)						;
	  wd.setTag(getId())  							;
	  wd.setLayoutParams(getLayoutParams(parentOrientation))  ;
	  wd.setText(getValue())						;
	  if(label != null)  wd.setLabel(getLabel())	;
	  if(hint  != null)  wd.setHint(getHint())		;
	  if(suffix != null) wd.setSuffix(getSuffix())	;
//	  wd.setTextSize(fz)							;
//	  wd.setTextColor(clr)							;
	  wd.setNoLabel(getNolabel()!=0)				;

	  if(notab == 0)
		wd.setBackgroundResource(R.drawable.btn_device_bg)	;
	  return wd;
	}
  }
  //====================================================================================
  public class MenuElement extends  UiElement{
	void create(Context context, int viewId, SubMenu subMenu,Typeface fontAwesome){
	  int fz = getFsize()	; if(fz == 0) fz = DEF_FNT_SIZE		;
	  String items[] = !getText().isEmpty() ?  getText().split(";") : new String[0]   ;
	  MenuItem	mi,miMenu = null	;
	  int id = 0, order=0, groupId = Menu.NONE		;
	  if(subMenu != null){
		miMenu = subMenu.getItem()	;
		if(miMenu != null){
		  miMenu.setTitle(getId())		;
		  groupId = miMenu.getItemId()	;
		}
		for(String it : items){
		  if(noEmpty(it)){
			id = View.generateViewId();
			mi = subMenu.add(groupId, id,order++, it);
			setMenuItemTextSize(context,mi, fz);
		  }
		}
	  }
	}
	private void setMenuItemTextSize(Context context,MenuItem item, float sizeInSp) {
	  SpannableString spanString = new SpannableString(item.getTitle());
	  spanString.setSpan(
			  new AbsoluteSizeSpan((int) TypedValue.applyDimension(
					TypedValue.COMPLEX_UNIT_SP, sizeInSp,
					context.getResources().getDisplayMetrics())),0,
			  		spanString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
	  item.setTitle(spanString);
	}
  }
  //====================================================================================
  public class LabelElement extends UiElement {
	public UiWidget createView(Context context, int viewId, int parentOrientation){
	  int fz = getFsize()   ;
	  if(fz == 0)
		switch(type){
		  case "label" : fz = DEF_FNT_SIZE * 3/2	; break	;
		  case "title":  fz = DEF_FNT_SIZE * 3/2	; break	;
		}
	  int clr = getColorF() 								;
	  UiWidget wd = new UiWidget(context,getType(),clr,fz)	;
	  wd.setId(idView = viewId)								;
	  wd.setTag(getId())  									;
	  wd.setLayoutParams(getLayoutParams(parentOrientation));
	  wd.setText(getValue())								;
	  if(label != null)  wd.setLabel(getLabel())			;
	  if(hint  != null)  wd.setHint(getHint())				;
	  if(suffix != null) wd.setSuffix(getSuffix())			;
//	  wd.setTextSize(fz)									;
//	  wd.setTextColor(clr)									;
	  wd.setTextAlignment(getTextAlignment(align))   		;
	  wd.setIcon(getIcChar())								;
	  wd.setNoLabel(getNolabel() != 0 || type.equals("title"))	;
	  if(notab == 0 && type.equals("label"))
		wd.setBackgroundResource(R.drawable.btn_device_bg)	;
	  return wd;
	}
  }
  //====================================================================================
  public class ButtonElement extends UiElement{
	public UiWidget createView(Context context, int viewId, int parentOrientation){
	  int fz = getFsize();
	  if(fz == 0) switch(type){
		case "button":
		  fz = DEF_FNT_SIZE * 3 / 2;
		  break;
	  }
	  int clr = getColorF();
	  if(clr == 0xFF000000) clr = DEF_COLOR;
	  UiWidget wd = new UiWidget(context, getType(),clr,fz);
	  wd.setId(idView = viewId);
	  wd.setTag(getId());
	  wd.setLayoutParams(getLayoutParams(parentOrientation));
	  wd.setText(getValue());
	  wd.setTextAlignment(MaterialButton.TEXT_ALIGNMENT_CENTER);
	  if(label != null) wd.setLabel(getLabel());
	  if(hint != null) wd.setHint(getHint());
	  if(suffix != null) wd.setSuffix(getSuffix());
	  wd.setTextAlignment(getTextAlignment(align));
	  String icChar = noEmpty(icon) ? getIcChar() : getIcChar("f192");
	  wd.setIcon(icChar);
	  wd.setNoLabel(getNolabel() != 0);
//	  wd.setTextSize(fz);
//	  wd.setTextColor(clr);
	  if(notab == 0) wd.setBackgroundResource(R.drawable.btn_device_bg);
	  return wd;
	}
}
  //====================================================================================
  public class SwitchElement extends UiElement {
	public UiWidget createView(Context context,int viewId,int parentOrientation){
	  int fz = getFsize();
	  if(fz == 0)  fz = DEF_FNT_SIZE * 3 / 2				;
	  int clr = getColorF()									;
	  if(clr == 0xFF000000) clr = DEF_COLOR					;
	  UiWidget wd = new UiWidget(context,getType(),clr,fz)	;
	  wd.setId(idView = viewId)								;
	  wd.setTag(getId())									;
	  wd.setLayoutParams(getLayoutParams(parentOrientation));
	  if(label  != null) wd.setLabel(getLabel())			;
	  if(hint   != null) wd.setHint(getHint())				;
	  if(suffix != null) wd.setSuffix(getSuffix())			;
	  wd.setNoLabel(getNolabel() != 0)						;
//	  wd.setTextSize(fz);
	  wd.setChecked(getValue().equals("1"))         		;
	  if(notab == 0) wd.setBackgroundResource(R.drawable.btn_device_bg);
	  return wd	;
	}
  }
  //====================================================================================
  public class SelectElement extends UiElement {
	public UiWidget createView(Context context, int viewId, int parentOrientation){
	  int fz = getFsize();
	  if(fz == 0)  fz = DEF_FNT_SIZE * 3 / 2				;
	  int clr = getColorF()									;
	  if(clr == 0xFF000000) clr = DEF_COLOR					;
	  UiWidget wd = new UiWidget(context,getType(),clr,fz,getText())	;
	  wd.setId(idView = viewId)								;
	  wd.setTag(getId())									;
	  wd.setLayoutParams(getLayoutParams(parentOrientation));
	  if(label  != null) wd.setLabel(getLabel())			;
	  if(hint   != null) wd.setHint(getHint())				;
	  if(suffix != null) wd.setSuffix(getSuffix())			;
	  wd.setNoLabel(getNolabel() != 0)						;
	  wd.setValue(getValue())	;
	  if(notab == 0) wd.setBackgroundResource(R.drawable.btn_device_bg);
	  return wd	;
	}
	public Spinner createView(Context context,int viewId,int parentOrientation, Typeface fontAwesome){
	  Spinner sp = new Spinner(context)           		;
	  sp.setId(idView = viewId)                   		;
	  sp.setLayoutParams(getLayoutParams(parentOrientation))	  ;
	  String items[] = !getText().isEmpty() ?  getText().split(";") : new String[0]   ;
	  ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, R.layout.custom_spinner_item, items) {
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
		  int clr = getColorF()      					;
		  TextView view = (TextView) super.getView(position, convertView, parent);
		  int fz = getFsize()	; if(fz == 0) fz = DEF_FNT_SIZE	;
		  view.setTextColor(clr)                  		;
		  view.setTextSize(fz)            				;
		  view.setLayoutParams(getLayoutParams()) 		;
//		  view.setGravity(Gravity.CENTER)		  		;
//	  	  if(notab == 0) view.setBackgroundResource(R.drawable.btn_device_bg)	;
		  view.setPadding(4,0,32,0)	; return view		;}
		@Override
		public View getDropDownView(int position, View convertView, ViewGroup parent) {
		  TextView view = (TextView) super.getDropDownView(position, convertView, parent);
		  int clr = getColorF()                   		;
		  int fz = getFsize()	; if(fz == 0) fz = DEF_FNT_SIZE	;
		  view.setTextColor(clr)                  		;
		  view.setTextSize(fz)        					;//-2
		  view.setGravity(Gravity.CENTER)		  		;
//          view.setLayoutParams(getLayoutParams()) 	;
		  return view	;}
	  };
	  try{ sp.setAdapter(adapter)                 ;
	  }catch(IllegalArgumentException e){/*Log.w(UiElement.TAG,"",e);*/}

//	  sp.setLayoutParams(getLayoutParams()) ;
	  sp.setTag(getId())  		;
	  sp.setPadding(0,4,0,4)	;
	  sp.setGravity(Gravity.CENTER)	;
	  if(notab == 0) sp.setBackgroundResource(R.drawable.spinner_bg)	;
	  int ix = getIValue()  	;
	  if(ix >= 0 && ix < items.length) sp.setSelection(ix);

	  return sp;
	}
  }
  //====================================================================================
}
