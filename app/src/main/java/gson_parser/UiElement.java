package gson_parser;

import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import static com.example.mqtt_giga.MqttWork.noEmpty;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class UiElement{
	private static final String TAG = "UiElement" ;
	public  static final int DEF_COLOR = 0xFF37A93C  ;
  	public	static final int DEF_FNT_SIZE = 20	;
  	protected String  id     = ""   ;
	protected String  type   = ""   ;
	protected String  value  = ""   ;
	protected String  text   = ""   ;
 	protected String  label  = ""   ;
  	protected String  suffix = ""   ;
  	protected String  hint   = ""   ;
	protected String  align  = ""   ;
	protected String  icon   = ""   ;
	protected String  color  = ""   ;
	protected String  square = ""	;
	protected int     fsize  = 0    ;
	protected int     wwidth = 0    ;
	protected int     nolabel= 0    ;
	protected int     notab  = 0    ;
  	public 	  int 	  idView = 0	;
  	public String getType()   { return getStrValue(type )	;}
	public String getId()     { return getStrValue(id   )   ;}
	public String getText()   { return getStrValue(text )   ;}
	public String getValue()  { return "menu".equals(getType()) ? getText() : getStrValue(value)   ;}
  	public String getLabel()  { return getStrValue(label)   ;}
  	public String getSuffix() { return getStrValue(suffix)  ;}
  	public String getHint()   { return getStrValue(hint)    ;}
	public String getIcon()   { return getStrValue(icon )   ;}
	public int    getIValue() { return getIntValue(value,-1);}
	public int    getAlign()  { return getIntValue(align, 1);}
	public int    getColor()  { return getIntValue(color, 0);}
  	public int    getFsize()  { return fsize     ;}
    public int    getWwidth() { return wwidth    ;}
	public int    getNolabel(){ return nolabel   ;}
	public int    getNoTab()  { return notab     ;}
  	public boolean getSquare(){ return getBoolValue(square,false)	;}

  	public int getColorF()			{ return getColor() | 0xFF000000	;}
	public String getIcChar()		{ return getIcChar(icon)			;}
  	public int getTextAlignment()	{ return getTextAlignment(align)	;}
  	public boolean getChecked()		{ return getStrValue(value).equals("1");}
  	public void setType(String val)	{ type = getStrValue(val)			;}

  	public UiElement setNoTab(int val)  { notab = val	; return this	;}
  	public UiElement setIcon(String val){ icon  = val	; return this	;}
//=======================================================================
  public static String getIcChar(String val){
  	if(!noEmpty(val)) val = ""	;
	else val = new String(Character.toChars(Integer.parseInt(val,16)))	; // Переводим hex-строку в число
	return val												;}
  //=======================================================================
  public static int getIntValue(String str, int val){
	if(noEmpty(str))
		str = str.replaceAll("[^0-9]", "").trim()				;
	if(noEmpty(str))
		try{ val = Integer.parseInt(str);} catch(NumberFormatException e){}
	return val       ;}
  //=======================================================================
  public static boolean getBoolValue(String str, boolean val){
	try{ val = Integer.parseInt(str) == 1;} catch(NumberFormatException e){}
	return val       ;}
  //=======================================================================
  public static String getStrValue(String str){ if(str == null) str = ""  ; return str     ;}
//=======================================================================
public static int getTextAlignment(String align){
  int al = getIntValue(align, 1)					;
  return al == 0 ? TextView.TEXT_ALIGNMENT_VIEW_START :
		 al == 1 ? TextView.TEXT_ALIGNMENT_CENTER     :
		 al == 2 ? TextView.TEXT_ALIGNMENT_VIEW_END   : TextView.TEXT_ALIGNMENT_CENTER  ;}
//====================================================================================
  public UiElement(){}
  public UiElement(String _id,String _type,String _val,String _lbl,int _fz,int _wdt){
		id = _id		; type  = _type			;
		value = _val	; label = _lbl			;
		fsize = _fz		; wwidth = _wdt			;
  }
  //========================================================================
  public UiElement(String _id,JsonObject obj){
	// Заполнение полей для update!!!
	id    = _id;
	value = getStringValue(obj, "value")	;
	text  = getStringValue(obj, "text" )	;
	icon  = getStringValue(obj, "icon" )	;
	color = getStringValue(obj, "color")	;
	suffix= getStringValue(obj, "suffix")	;
	hint  = getStringValue(obj, "hint" )	;
	align = getStringValue(obj, "align")	;
	fsize = getIntValue	  (obj, "fsize")	;
	square= getStringValue(obj, "square")	;
	// Остальные поля остаются по умолчанию TODO TODO
  }
  private static String getStringValue(JsonObject obj, String key){
	String str = null	;
	if (obj.has(key)){
	  try{
		JsonElement element = obj.get(key)	;
		if(element.isJsonPrimitive())
		  str = element.getAsString()		;
		else str = element.toString()		; // Обработка нестроковых значений (например, чисел)
	  }catch(IllegalStateException | UnsupportedOperationException e){}	}
	return str	;}
  private static int getIntValue(JsonObject obj, String key){
	int val = 0	;
	String str = getStringValue(obj, key)	;
	try{ val = Integer.parseInt(str);} catch(NumberFormatException e){}
	return val	;}
  //========================================================================
  	public void update(UiElement updE){
	  if(id != null && getId().equals(updE.getId())){
		value = updE.value	; text  = updE.text	; label = updE.label	;
		align = updE.align	; icon  = updE.icon	; color = updE.color	;// !!!!! далее потом !!!! TODO
	  }
	}
//================================================================================
	public LinearLayout.LayoutParams getLayoutParams(){
	  	float wgt = wwidth > 0 ? wwidth : 1	;
  		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
		  ViewGroup.LayoutParams.WRAP_CONTENT,
		  ViewGroup.LayoutParams.WRAP_CONTENT, wgt );
  		return lp;}
  	public LinearLayout.LayoutParams getLayoutParams(int parentOrientation){
		return getLayoutParams(wwidth,parentOrientation)	;}
  	public static LinearLayout.LayoutParams getLayoutParams(int wwidth, int parentOrientation){
	  	float wgt = wwidth > 0 ? wwidth : 1	;
		int  wdth = parentOrientation == LinearLayout.HORIZONTAL ? 0 : ViewGroup.LayoutParams.MATCH_PARENT	;
		int  hgt =  parentOrientation == LinearLayout.HORIZONTAL ? ViewGroup.LayoutParams.MATCH_PARENT : ViewGroup.LayoutParams.WRAP_CONTENT	;
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(wdth,hgt, wgt );
	  	lp.setMargins(4,4,4,4);
	  return lp;}
}
