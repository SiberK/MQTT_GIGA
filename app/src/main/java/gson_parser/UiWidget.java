package gson_parser;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import static com.example.mqtt_giga.MqttWork.noEmpty;
import com.example.mqtt_giga.R;

import static gson_parser.UiElement.DEF_FNT_SIZE;
import static gson_parser.UiElement.getIcChar;

public class UiWidget extends LinearLayout {
  public static Typeface fontAwesome		;
  private static final String TAG      = "UI_WD"	;
  private static final int DEF_LBL_SIZE = 12	;

  protected TextView   tvLabel				;
  protected TextView   tvSuffix				;
//  public	EditText	mainEText			;
//  protected 	Spinner		mSpinner		;

  protected String 		mType	= ""		;
  protected String 		mUiId	= ""		;
  protected	String 		icChar  = ""		;
  protected String 		mText   = ""		;
  protected String 		mValue  = ""		;
  protected String 		mHint 	= ""		;
  protected boolean     noLabel = false		;
  protected boolean 	noTab   = false		;
  protected boolean		square  = false		;
  protected int			wwidth  = 1			;
  protected int 		mTextColor= Color.BLACK		;
  protected int 		mTextSize = DEF_FNT_SIZE	;
  protected int 		mAlign	= TextView.TEXT_ALIGNMENT_CENTER 			;
  protected int 		cntItemsSp = 0		;
  protected	String 		spText	= ""		;// перечень для Spinner!!!
  protected Context		context				;
  public OnWorkListener onWorkListener		;
  protected	 int 		mainId				;
  protected LayoutParams txtParams			;
  protected int 		sizePx				;
  protected int			botPadding = dpToPx(12)	;
  protected boolean 	flSingleLine = true	;
  //=============================================================================
  public UiWidget(@NonNull Context _context,UiElement uiE, int parentOrientation){
	super(_context)					;
	context = _context				;
	setTag(uiE.getId()) 			;
	mAlign 		= uiE.getTextAlignment()   	;
	mType  		= uiE.getType()		;
	mUiId  		= uiE.getId()		;
	mText		= uiE.getValue()	;
	spText 		= uiE.getText()		;// перечень для Spinner!!!
	noTab		= uiE.getNoTab() == 1;
	noLabel		= uiE.getNolabel()==1;
	icChar		= uiE.getIcChar()	;
	wwidth      = uiE.getWwidth()	;
	mTextColor 	= uiE.color != null      ? uiE.getColorF() 	   :
				  mType.equals("button") ? UiElement.DEF_COLOR :
										   Color.BLACK 			;
	if(mType.equals("title")) noTab = noLabel = true  ;
	int fz  	= uiE.getFsize()	;
	if(fz == 0){
	  if(mType.equals("button") ||
		 mType.equals("label")  ||
		 mType.equals("title")) fz = DEF_FNT_SIZE*3/2	;
	  else  fz = DEF_FNT_SIZE		;}
	mTextSize  	= fz				;

	createChildViews(uiE)			;// Создаем элементы интерфейса

	setTextSize (mTextSize)			;
	setTextAlignment(mAlign)        ;
	setIcon(icChar)					;

	LinearLayout.LayoutParams lp = getLayoutParams(wwidth,parentOrientation)  ;
	setLayoutParams(lp)				;
  }
  //=======================================================================
  public static UiWidget create(Context context, UiElement uiE, int parentOrientation){
	UiWidget wd			;
	switch(uiE.getType()){
	  case "input": case "pass":
			   	wd = new UiInputWidget(context,uiE,parentOrientation)		; break	;
	  case "button": case "label": case "title":
				wd = new UiButtonWidget(context,uiE,parentOrientation)		; break	;
	  case "select":
				wd = new UiSelectWidget(context,uiE,parentOrientation)		; break	;
	  case "switch_t":
				wd = new UiSwitchWidget(context,uiE,parentOrientation)		; break	;
	  default: 	wd = new UiOtherWidget(context,uiE,parentOrientation)		; break	;
	}
	if(wd != null && !wd.noTab)  wd.setBackgroundResource(R.drawable.btn_device_bg)	;
  	return wd					;}
  //=======================================================================
  public void update(UiElement updE){
	if(updE.color != null) setTextColor(updE.getColorF())    	;
	if(updE.icon  != null) setIcon(updE.getIcChar())			;
	if(updE.value != null){ setText(updE.getValue())     		;
	  						setChecked(updE.getChecked())		;
							setValue(updE.getValue())			;}
	if(updE.fsize != 0)    setTextSize(updE.fsize)				;
	if(updE.align != null) setTextAlignment(updE.getTextAlignment());
  }
  //=======================================================================
  protected void createChildViews(UiElement uiE) {
	mainId = View.generateViewId()			;
	setPadding(0,0,0,0)						;//dpToPx(4), dpToPx(1), dpToPx(4), dpToPx(1));
	// Дополнительный текст
	tvLabel = new TextView(context)			;
	tvSuffix = new TextView(context)		;

	tvLabel.setId(View.generateViewId())	;
	tvLabel.setText(uiE.label != null ? uiE.label : uiE.getType());//"LABEL")				;
	tvLabel.setTextSize(DEF_LBL_SIZE)					;
	tvLabel.setTextColor(Color.GRAY)		;
	tvLabel.setPadding(0,0,0,0)				;//dpToPx(4), dpToPx(1), dpToPx(4), dpToPx(1));
	tvLabel.setTextAlignment(TEXT_ALIGNMENT_VIEW_START);

	tvSuffix.setId(View.generateViewId())	;
	tvSuffix.setTextColor(Color.GRAY)		;
	tvSuffix.setText(uiE.suffix != null ? uiE.suffix : "")					;
	tvSuffix.setTextSize(DEF_LBL_SIZE)		;
	tvSuffix.setVisibility(View.GONE)		;
	tvSuffix.setPadding(0,0,0,0)			;
	tvSuffix.setTextAlignment(TEXT_ALIGNMENT_VIEW_END);

	// Параметры расположения
	LayoutParams lblParams = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT,1);
	LayoutParams sfxParams = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT,1);
	txtParams = new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT,1);
	lblParams.setMargins(10,0,0,0)			;
	sfxParams.setMargins(0,0,10,0)			;
//	botPadding = dpToPx(12)					;

	setOrientation(VERTICAL)							;
	LinearLayout ll = new LinearLayout(context)			;
	ll.setOrientation(HORIZONTAL)						;

	// Добавляем элементы
	addView(ll)											;
	ll.addView(tvLabel , lblParams)						;
	ll.addView(tvSuffix, sfxParams)						;

	if(uiE.label  != null) setLabel (uiE.getLabel())	;
	if(uiE.hint   != null) setHint  (uiE.getHint())		;
	if(uiE.suffix != null) setSuffix(uiE.getSuffix())	;

	if(noLabel){ tvLabel.setVisibility(GONE)	; tvSuffix.setVisibility(GONE)	;}
  }
  //=========================================================================
  // Методы для управления текстом
  public String getText() 	{ return mText	;}
  //============================================================
  //--------------------------------------------------------------
  public void setText(String text) { if(text == null) text = ""	; mText = text	;}
  //--------------------------------------------------------------
  public void setValue(String val){}
  public void setValue(int val){ setValue("" + val)	;}
  //--------------------------------------------------------------
  public void setIcon(String val){ icChar = val	;}
  //--------------------------------------------------------------
  public void setChecked(boolean val){}
  //--------------------------------------------------------------
  public boolean isChecked()				{ return false				;}
  //--------------------------------------------------------------
  public void setLabel(String val) 			{ if(val == null) val = ""	;
											  tvLabel.setText(val)		;}
  //--------------------------------------------------------------
  public void setNoLabel(int val)			{ setNoLabel(val != 0)		;}
  //--------------------------------------------------------------
  public void setNoTab(boolean val)			{ noTab = val				;}
  //--------------------------------------------------------------
  public static void setFontAwesome(Typeface font){ fontAwesome = font	;}
  //--------------------------------------------------------------
  public void setNoLabel(boolean val){
	noLabel = val	;
	if(tvLabel != null) tvLabel.setVisibility  (noLabel ? GONE : VISIBLE);
	if(tvSuffix != null) tvSuffix.setVisibility(noLabel || tvSuffix.getText().toString().isEmpty() ? GONE : VISIBLE);
  }
  public void setHint(String val) {	mHint = val			;}
  //--------------------------------------------------------------
  public void setSuffix(String val) {
	if(val == null) val = ""	;
	tvSuffix.setText(val)								;
	tvSuffix.setVisibility(val.isEmpty() ? View.GONE : View.VISIBLE)	;}
  //--------------------------------------------------------------
  public void setTextAlignment(int align){super.setTextAlignment(align)	;}
  //--------------------------------------------------------------
  public void setTextColor(int color) {}
  //--------------------------------------------------------------
  public void setLabelColor(int color) { tvLabel.setTextColor(color)	;}
  //--------------------------------------------------------------
  public void setTextSize(int sizeDp) {
	if(sizeDp > 0){ mTextSize = sizeDp	; sizePx = dpToPx(mTextSize)	;}}
  //--------------------------------------------------------------
  public void setLabelSize(int sizePx) { if(sizePx > 0) tvLabel.setTextSize(sizePx)	;}
  //--------------------------------------------------------------
  public void setSingleLine(boolean val){}
  //--------------------------------------------------------------
  public static LinearLayout.LayoutParams getLayoutParams(int wwidth, int parentOrientation){
	float wgt = wwidth > 0 ? wwidth : 1	;
	int  wdth = parentOrientation == LinearLayout.HORIZONTAL ? 0 : ViewGroup.LayoutParams.MATCH_PARENT	;
	int  hgt =  parentOrientation == LinearLayout.HORIZONTAL ? ViewGroup.LayoutParams.MATCH_PARENT : ViewGroup.LayoutParams.WRAP_CONTENT	;
	LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(wdth,hgt, wgt );
	lp.setMargins(4,4,4,4)			;
	return lp						;}
  //============================================================
  // Вспомогательный метод для конвертации dp в пиксели
  public int dpToPx(int dp) {
	float density = 1;//getResources().getDisplayMetrics().density;
	return Math.round(dp * density)			;}
  //============================================================
  //============================================================
  public interface OnWorkListener{void onWork(String type, String name, String val)	;}
  //============================================================
  public void setOnWorkListener(OnWorkListener val){ onWorkListener = val	;}
  //============================================================
  //============================================================
  public String getLabel()  { return tvLabel.getText().toString()	;}
  public String getSuffix() { return tvSuffix.getText().toString()	;}
  public String getValue()	{ return mValue	;}
  public int 	getValueInt(){ return Integer.parseInt(mValue)		;}
  //============================================================
  public static void setFontAwesome(Context ctx){
	if(fontAwesome == null){
	  AssetManager mgr = ctx.getAssets();
	  try{fontAwesome = Typeface.createFromAsset(mgr, "fonts/fa_solid_900.ttf");
		UiWidget.setFontAwesome(fontAwesome)	;} catch(Exception e){Log.w(TAG, "", e);}}
  }
  //============================================================
  // Этот конструктор для созания виджета через макет!!!!
  public UiWidget(Context cntxt, AttributeSet attrs) {
	super(cntxt, attrs)				;
	init(cntxt, attrs)				;
	if(getLabel().isEmpty() && getSuffix().isEmpty()){
	  tvLabel .setVisibility(GONE)	;
	  tvSuffix.setVisibility(GONE)	;}
  }
  //============================================================
  protected void init(Context cntxt, AttributeSet attrs) {
	String str		;
	context = cntxt	;
//	botPadding = dpToPx(12)	;

	mainId = View.generateViewId()					;
	LayoutInflater.from(context).inflate(R.layout.ui_widget, this, true);	// Надуваем макет
	setOrientation(VERTICAL)						;
	LinearLayout llTop = findViewById(R.id.llTop)	;
	tvLabel  = llTop.findViewById(R.id.tvLabel)		;						// Получаем ссылки на view
	tvSuffix = llTop.findViewById(R.id.tvSuffix)	;

	if (attrs != null) {													// Обрабатываем кастомные атрибуты
	  TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.UiWidget);

	  // Устанавливаем значения из атрибутов
	  str = a.getString (R.styleable.UiWidget_uiId)		; if(noEmpty(str)) mUiId = str		;
	  str = a.getString (R.styleable.UiWidget_uiType)	; if(noEmpty(str)) mType = str		;
	  str = a.getString (R.styleable.UiWidget_uiText)	; if(noEmpty(str)) spText = mText = str		;
	  str = a.getString (R.styleable.UiWidget_uiValue)	; if(noEmpty(str)) mValue = str		;
	  str = a.getString (R.styleable.UiWidget_uiHint)	; if(noEmpty(str)) mHint = str		;
	  str = a.getString (R.styleable.UiWidget_uiLabel)	; if(noEmpty(str)) setLabel(str)	;
	  str = a.getString (R.styleable.UiWidget_uiSuffix)	; if(noEmpty(str)) setSuffix(str)	;
	  str = a.getString (R.styleable.UiWidget_uiIcon)	; if(noEmpty(str)) setIcon(getIcChar(str))	;

	  setLabelSize(a.getInt(R.styleable.UiWidget_labelSize,DEF_LBL_SIZE))					;
	  mTextColor = a.getInt(R.styleable.UiWidget_uiColor  ,Color.BLACK)						;
	  int 	  fz = a.getInt(R.styleable.UiWidget_uiFsize  , 0 )								;
	  mAlign     = a.getInt(R.styleable.UiWidget_uiAlign  ,TextView.TEXT_ALIGNMENT_CENTER )	;

	  setNoTab	  (a.getBoolean(R.styleable.UiWidget_uiNoTab  ,false))	;
	  setNoLabel  (a.getBoolean(R.styleable.UiWidget_uiNoLabel,false))	;
	  square	 = a.getBoolean(R.styleable.UiWidget_uiSquare ,false )	;
	  flSingleLine = a.getBoolean (R.styleable.UiWidget_uiSingleLine,true);

	  if(fz == 0){  if( mType.equals("button") ||
						mType.equals("title")) fz = DEF_FNT_SIZE*3/2	;
					else  fz = DEF_FNT_SIZE								;}
	  mTextSize  	= fz			;

	  a.recycle()		;
	}
  }
}
