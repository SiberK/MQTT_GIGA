package gson_parser;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import gson_parser.UiElementDeserializer.ButtonElement;
import gson_parser.UiElementDeserializer.FrameElement;
import gson_parser.UiElementDeserializer.LabelElement;
import gson_parser.UiElementDeserializer.SelectElement;
import gson_parser.UiElementDeserializer.SwitchElement;

import com.example.mqtt_giga.MqttWork;
import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LayoutBuilder{
  private static final String          	TAG         = "LAY_BLDR";
  private final        Context         	context					;
  private              Typeface        	fontAwesome = null		;
  private              List<UiElement> 	uiElements				;
  private UiElementDeserializer 		deserializer			;
  private volatile 		boolean    		flTouch  = false		;

  public LayoutBuilder(Context context){
	this.context = context;
	AssetManager mgr = context.getAssets();
	try{
	  fontAwesome = Typeface.createFromAsset(mgr, "fonts/fa_solid_900.ttf");
	} catch(Exception e){Log.w(TAG, "", e);}
  }
  public void buildLayout(String json, FrameLayout container){
	deserializer = new UiElementDeserializer();

	uiElements = new ArrayList<>()	;
	container.removeAllViews()		;
	try{
	  GsonBuilder gsonBuilder = new GsonBuilder().registerTypeAdapter(UiElement.class,
			  deserializer);
	  Gson        gson        = gsonBuilder.create();
	  UiElement   root        = gson.fromJson(json, UiElement.class);
	  parseElement(root, container);
	} catch(JsonSyntaxException | IllegalArgumentException e){
	  Log.w(TAG, "JsonSyntaxException ", e);
	}
  }
  private void parseElement(UiElement element, ViewGroup parent){
	int viewId = parent.generateViewId()	;
	if(element instanceof FrameElement){
	  FrameElement frame  = (FrameElement) element;
	  LinearLayout layout = frame.createView(context,viewId,fontAwesome);
	  parent.addView(layout);

	  List<UiElement> children = frame.getChildren();
	  if(children != null){
		for(UiElement child : children)
		  parseElement(child, layout);
	  }
	}

	else if(element instanceof LabelElement){
	  TextView tv = ((LabelElement) element).createView(context,viewId, fontAwesome);
	  parent.addView(tv);
	  uiElements.add(element);
	}

	else if(element instanceof ButtonElement){
	  	MaterialButton btn = ((ButtonElement) element).createView(context,viewId, fontAwesome);
	  	btn.setOnClickListener(v->onButtonClicked(btn));
	  	parent.addView(btn);
	  	uiElements.add(element);
	}

	else if(element instanceof SwitchElement){
		Switch sw = ((SwitchElement) element).createView(context,viewId, fontAwesome);
		sw.setOnClickListener(v->onSwitchClicked(sw));
		parent.addView(sw);
		uiElements.add(element);
	}

	else if(element instanceof SelectElement){
		Spinner sp = ((SelectElement) element).createView(context,viewId, fontAwesome);
		sp.setOnTouchListener((v,event)->{
		  if(event.getAction() == MotionEvent.ACTION_UP){flTouch = true	;} return false	;});
		sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
		  @Override
		  public void onItemSelected(AdapterView<?> parent, View view, int position, long id){
			if(flTouch){ flTouch = false			;
			  onSpinnerItemSelected(sp, position)	;}}
		  @Override public void onNothingSelected(AdapterView<?> parent){flTouch = false	;}});
		parent.addView(sp);
		uiElements.add(element);
	}
  }
  private void onSpinnerItemSelected(Spinner sp, int position){
	String val = String.valueOf(position)	;
	boolean flEn = sp.isEnabled()			;
	Log.i(TAG, String.format("Select changed: %s  %d", sp.getTag().toString(), position));
	MqttWork.onClickFaceElement("select",sp.getTag().toString(),val)	;
//	  MqttWork.onItemSelect(sp.getTag().toString(),position)	;
  }
  private void onSwitchClicked(Switch sw){
	String val = sw.isChecked() ? "1" : "0"	;
	sw.setChecked(!sw.isChecked())	;
	Log.i(TAG, String.format("Switch click  %s %b", sw.getTag(), sw.isChecked()));
	MqttWork.onClickFaceElement("switch_t",sw.getTag().toString(),val)	;
  }
  private void onButtonClicked(MaterialButton btn){
	Log.i(TAG, "Button click  " + btn.getTag().toString());
	MqttWork.onClickFaceElement("button",btn.getTag().toString(),"2")	;
  }
  public void updateFace(String json, FrameLayout container){
	UiElement       updElement	;
	try{
	  JsonObject root = JsonParser.parseString(json).getAsJsonObject();
	  if(root.has("updates") && root.get("updates").isJsonObject()){
		JsonObject updates = root.getAsJsonObject("updates");
		for(Map.Entry<String, JsonElement> entry : updates.entrySet()){
		  String elementId = entry.getKey();

		  if(entry.getValue().isJsonObject()){
			JsonObject elementData = entry.getValue().getAsJsonObject();
			updElement = new UiElement();

			// Заполнение полей
			updElement.id    = elementId;
			updElement.value = getStringValue(elementData, "value");
			updElement.icon  = getStringValue(elementData, "icon");
			updElement.color = getStringValue(elementData, "color");
			updateFaceElement(container,updElement)					;
			// Остальные поля остаются по умолчанию
		  }
		}
	  }
	} catch(IllegalStateException | JsonParseException | ClassCastException | NullPointerException |
			UnsupportedOperationException | IllegalArgumentException e){
	  Log.w(TAG, "", e);
	}
  }
  private void updateFaceElement(FrameLayout container, UiElement updE){
	for(UiElement uiE : uiElements){
	  if(uiE.getId().equals(updE.getId())){
		updE.setType(uiE.getType())						;
		updE.idView = uiE.idView						;
		if(updE.icon  == null && updE.value != null) updE.icon  = uiE.icon	;// !!!! потому что иконка и текст идут вместе !!!!
		if(updE.value == null && updE.icon  != null) updE.value = uiE.value	;// !!!! потому что иконка и текст идут вместе !!!!
		uiE.update(updE)								; break	;}
	}
	if(updE.idView != 0 && deserializer != null){
	  View view = container.findViewById(updE.idView)	;
	  if(view != null) deserializer.updateView(updE,view,fontAwesome)		;
	}
  }
  private String getStringValue(JsonObject obj, String key){
	String str = null	;
	if (obj.has(key)){
	  try{
		JsonElement element = obj.get(key)	;
		if(element.isJsonPrimitive())
		  str = element.getAsString()		;
		else str = element.toString()		; // Обработка нестроковых значений (например, чисел)
	  }catch(IllegalStateException | UnsupportedOperationException e){}
	}
	return str	;
  }
}


