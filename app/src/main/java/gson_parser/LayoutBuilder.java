package gson_parser;

import android.content.Context;
import static android.content.Context.INPUT_METHOD_SERVICE;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import static androidx.core.content.ContextCompat.getSystemService;

import gson_parser.UiElementDeserializer.ButtonElement	;
import gson_parser.UiElementDeserializer.FrameElement	;
import gson_parser.UiElementDeserializer.LabelElement	;
import gson_parser.UiElementDeserializer.SelectElement	;
import gson_parser.UiElementDeserializer.SwitchElement	;
import gson_parser.UiElementDeserializer.MenuElement	;
import gson_parser.UiElementDeserializer.InputElement	;

import com.example.mqtt_giga.UiWidget;
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
  private SubMenu						subMenu					;

  public LayoutBuilder(Context context){
	this.context = context;
	AssetManager mgr = context.getAssets();
	try{
	  fontAwesome = Typeface.createFromAsset(mgr, "fonts/fa_solid_900.ttf");
	  UiWidget.setFontAwesome(fontAwesome)	;
	} catch(Exception e){Log.w(TAG, "", e);}
  }
  public void buildLayout(String json, FrameLayout container, SubMenu _subMenu){
	deserializer = new UiElementDeserializer();
	subMenu = _subMenu				;
	uiElements = new ArrayList<>()	;
	container.removeAllViews()		;
	try{
	  GsonBuilder gsonBuilder = new GsonBuilder().registerTypeAdapter(UiElement.class,
			  deserializer);
	  Gson        gson        = gsonBuilder.create();
	  UiElement   root        = gson.fromJson(json, UiElement.class);
	  parseElement(root, container,LinearLayout.VERTICAL);
	} catch(JsonSyntaxException | IllegalArgumentException e){
	  Log.w(TAG, "JsonSyntaxException ", e);
	}
  }
  private void parseElement(UiElement element, ViewGroup parent,int parentOrientation){
	int viewId = parent.generateViewId()				;

	if(element instanceof FrameElement){
	  FrameElement frame  = (FrameElement) element		;
	  LinearLayout layout = frame.createView(context,viewId,parentOrientation,fontAwesome);
	  parent.addView(layout)							;
	  parentOrientation = frame.getOrientation()		;

	  List<UiElement> children = frame.getChildren()	;
	  if(children != null){
		for(UiElement child : children)
		  parseElement(child, layout,parentOrientation)	;}
	}

	else if(element instanceof MenuElement){
	  uiElements.add(element)				;
	  ((MenuElement) element).create(context,viewId,subMenu, fontAwesome)	;}

	else if(element instanceof LabelElement){
		uiElements.add(element)				;
		UiWidget wg = ((LabelElement) element).createView(context,viewId,parentOrientation);
		parent.addView(wg)					;}

	else if(element instanceof InputElement){
	  	uiElements.add(element)				;
	  	UiWidget wd = ((InputElement) element).createView(context,viewId,parentOrientation);
	  	wd.setOnWorkListener((typ,nam,val)->MqttWork.onClickFaceElement(typ,nam,val))	;
	  	parent.addView(wd)					;}

	else if(element instanceof ButtonElement){
	  	uiElements.add(element)				;
		UiWidget wd = ((ButtonElement)element).createView(context,viewId,parentOrientation)	;
		wd.setOnWorkListener((typ,nam,val)->MqttWork.onClickFaceElement(typ,nam,val))	;
	  	parent.addView(wd)					;}

	else if(element instanceof SwitchElement){
		uiElements.add(element)				;
		UiWidget wd = ((SwitchElement)element).createView(context,viewId,parentOrientation)	;
		wd.setOnWorkListener((typ,nam,val)->MqttWork.onClickFaceElement(typ,nam,val))	;
		parent.addView(wd)					;}

	else if(element instanceof SelectElement){
	  	uiElements.add(element)				;
		UiWidget wd = ((SelectElement)element).createView(context,viewId,parentOrientation)	;
		wd.setOnWorkListener((typ,nam,val)->MqttWork.onClickFaceElement(typ,nam,val))	;
//		Spinner sp = ((SelectElement) element).createView(context,viewId,parentOrientation, fontAwesome);
//		sp.setOnTouchListener((v,event)->{
//		  if(event.getAction() == MotionEvent.ACTION_UP){flTouch = true	;} return false	;});
//		sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
//		  @Override
//		  public void onItemSelected(AdapterView<?> parent, View view, int position, long id){
//			if(flTouch){ flTouch = false			;
//			  String val = String.valueOf(position)	;
//			  MqttWork.onClickFaceElement("select",sp.getTag().toString(),val)	;}}
//		  @Override public void onNothingSelected(AdapterView<?> parent){flTouch = false	;}});
		parent.addView(wd)					;}
  }

  public void updateFace(String json, FrameLayout container){
	try{
	  JsonObject root = JsonParser.parseString(json).getAsJsonObject()	;
	  if(root.has("updates") && root.get("updates").isJsonObject() && deserializer != null){
		JsonObject updates = root.getAsJsonObject("updates")			;
		for(Map.Entry<String, JsonElement> entry : updates.entrySet()){
		  String elementId = entry.getKey()								;
		  if(entry.getValue().isJsonObject()){
			JsonObject elementData = entry.getValue().getAsJsonObject()	;
			UiElement updE = new UiElement(elementId,elementData)		;
			for(UiElement uiE : uiElements){
			  if(!uiE.getId().equals(updE.getId())) continue			;
			  updE.setType(uiE.getType())								;
			  updE.idView = uiE.idView									;
			  uiE.update(updE)											;// оно надо??
			  View view = container.findViewById(uiE.idView)			;
			  if(view != null)
				deserializer.updateView(updE,view,fontAwesome)			;
			  break	;
			}
		  }
		}
	  }
	} catch(IllegalStateException | JsonParseException | ClassCastException | NullPointerException |
			UnsupportedOperationException | IllegalArgumentException e){ Log.w(TAG, "", e)	;}
  }
}


