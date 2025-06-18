package gson_parser;

import android.content.Context;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

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
	  case "menu":	elt = context.deserialize(json, MenuElement.class)  ; break ;
	  case "row": case "col": case "ui":
					elt = context.deserialize(json, FrameElement.class) ; break ;
      case "button": case "label": case "title":
      case "switch_t": case "select": case "input": case "pass":
					elt = context.deserialize(json, HubElement.class)  	; break ;
      default:		elt = context.deserialize(json, OtherElement.class) ; break ;
    }
    return elt  ;}
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
//			setMenuItemTextSize(context,mi, fz);
			SpannableString spanString = new SpannableString(mi.getTitle());
			spanString.setSpan(
					new AbsoluteSizeSpan((int) TypedValue.applyDimension(
							TypedValue.COMPLEX_UNIT_SP, fz,
							context.getResources().getDisplayMetrics())),0,
					spanString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			mi.setTitle(spanString);
		  }
		  }
		}
	  }
	}
	private void setMenuItemTextSize(Context context,MenuItem item, float sizeInSp) {
  }
  //====================================================================================
  public class OtherElement extends UiElement {
  }
  //====================================================================================
  public class HubElement extends UiElement {
  }
  //====================================================================================
}
