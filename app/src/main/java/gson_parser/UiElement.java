package gson_parser;

import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class UiElement{
	private static final String TAG = "UiElement" ;
	public  static final int DEF_COLOR = 0xFF37A93C  ;
  	protected String  id     = ""   ;
	protected String  type   = ""   ;
	protected String  value  = ""   ;
	protected String  text   = ""   ;
	protected String  label  = ""   ;
	protected String  align  = ""   ;
	protected String  icon   = ""   ;
	protected String  color  = ""   ;
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
	public String getIcon()   { return getStrValue(icon )   ;}
	public int    getIValue() { return getIntValue(value,-1);}
	public int    getAlign()  { return getIntValue(align, 1);}
	public int    getColor()  { return getIntValue(color, 0);}
  	public int    getFsize()  { return fsize     ;}
    public int    getWwidth() { return wwidth    ;}
	public int    getNolabel(){ return nolabel   ;}
	public int    getNotab()  { return notab     ;}

  	public int getColorF(){return getColor() | 0xFF000000	;}
  	public void setType(String val){type = getStrValue(val)	;}
  	public String getIcChar(){
	  int codePoint = Integer.parseInt(icon, 16)          	; // Переводим hex-строку в число
	  return new String(Character.toChars(codePoint))		;}
    private int getIntValue(String str, int val){
	  try{ val = Integer.parseInt(str);} catch(NumberFormatException e){}
	  return val       ;}
	private String getStrValue(String str){ if(str == null) str = ""  ; return str     ;}

	public UiElement(){}
  	public void update(UiElement updE){
	  if(id != null && getId().equals(updE.getId())){
		value = updE.value	;
		text  = updE.text	;
		label = updE.label	;
		align = updE.align	;
		icon  = updE.icon	;
		color = updE.color	;// !!!!! далее потом !!!! TODO
	  }
	}
//================================================================================
	public LinearLayout.LayoutParams getLayoutParams(){
  		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
		  ViewGroup.LayoutParams.WRAP_CONTENT,
		  ViewGroup.LayoutParams.WRAP_CONTENT, wwidth > 0 ? wwidth : 1 );
  		lp.gravity = Gravity.CENTER ;
//	lp.setMargins(2,2,2,2);
  		return lp;}
  	public LinearLayout.LayoutParams getLayoutParams0(){
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
			0,
			ViewGroup.LayoutParams.WRAP_CONTENT, wwidth > 0 ? wwidth : 1 );
//	lp.setMargins(2,2,2,2);
		return lp;}
}
