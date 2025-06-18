package gson_parser;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import static com.example.mqtt_giga.MqttWork.noEmpty;
import com.example.mqtt_giga.R;

import static gson_parser.UiElement.DEF_FNT_SIZE;
import static gson_parser.UiElement.getIntValue;

public class UiWidget extends ConstraintLayout {
  protected static Typeface fontAwesome		;
  private 	static final   String TAG      = "UI_WD"	;
  protected TextView   tvLabel				;
  protected TextView   tvSuffix				;
//  public	EditText	mainEText			;
//  protected 	Spinner		mSpinner		;

  protected final String mType				;
  protected final String mUiId				;
  protected boolean      noLabel = false	;
  protected boolean 	noTab   = false		;
  protected	String 		icChar   = ""		;
  protected String 		mText  = ""			;
  protected int 		mTextColor			;
  protected int 		mTextSize			;
  protected int 		mAlign				;
  protected int 		cntItemsSp = 0		;
  protected	String 		spText				;
  protected	volatile boolean flTouch  = false		;
  protected final     Context context		;

  public OnWorkListener onWorkListener		;
  protected	 int 		mainId				;
  protected LayoutParams txtParams			;
  protected String 		mHint = ""			;
  protected int 		sizePx				;
  protected int			botPadding          ;

  //=============================================================================
  public UiWidget(@NonNull Context _context,UiElement uiE, int parentOrientation){
	super(_context)					;
	context = _context				;

	setTag(uiE.getId()) 			;
	int fz  	= uiE.getFsize()	; if(fz == 0) fz = DEF_FNT_SIZE	;
	mTextSize  	= fz				;
	mAlign 		= uiE.getTextAlignment()   	;
	mType  		= uiE.getType()		;
	mUiId  		= uiE.getId()		;
	mText		= uiE.getValue()	;
	spText 		= uiE.getText()		;// перечень для Spinner!!!
	mTextColor 	= uiE.color == null ? Color.BLACK : uiE.getColorF()	;
	noTab		= uiE.getNoTab() == 1;
	noLabel		= uiE.getNolabel()==1;
	icChar		= uiE.getIcChar()	;

	createChildViews(uiE)			;// Создаем элементы интерфейса

	setTextSize (mTextSize)			;
	setTextAlignment(mAlign)        ;
	setIcon(icChar)					;

	LinearLayout.LayoutParams lp = getLayoutParams(uiE.getWwidth(),parentOrientation)  ;
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
	if(wd != null && !wd.noTab) wd.setBackgroundResource(R.drawable.btn_device_bg)	;
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
	botPadding = dpToPx(6)					;
	// Дополнительный текст
	tvLabel = new TextView(context)			;
	tvLabel.setId(View.generateViewId())	;
	tvLabel.setText(uiE.label != null ? uiE.label : uiE.getType());//"LABEL")				;
	tvLabel.setTextSize(12)					;
	tvLabel.setTextColor(Color.GRAY)		;
	tvLabel.setPadding(0,0,0,0)				;//dpToPx(4), dpToPx(1), dpToPx(4), dpToPx(1));
	tvSuffix = new TextView(context)		;
	tvSuffix.setId(View.generateViewId())	;
	tvSuffix.setTextColor(Color.GRAY)		;
	tvSuffix.setText(uiE.suffix != null ? uiE.suffix : "")					;
	tvSuffix.setTextSize(12)				;
	tvSuffix.setVisibility(View.GONE)		;
	tvSuffix.setPadding(0,0,0,0)			;

	// Параметры расположения
	LayoutParams lblParams = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
	LayoutParams sfxParams = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
	txtParams = new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);

	lblParams.topToTop     = LayoutParams.PARENT_ID		;
//	lblParams.bottomToTop  = mainId						;
	lblParams.startToStart = LayoutParams.PARENT_ID		;
//	lblParams.endToEnd 	= LayoutParams.PARENT_ID	;
	lblParams.setMargins(dpToPx(5),0,0,0)				;

	sfxParams.topToTop     	= LayoutParams.PARENT_ID	;
//	sfxParams.topToTop  	= label.getId()				;
//	sfxParams.startToStart = LayoutParams.PARENT_ID		;
	sfxParams.endToEnd 		= LayoutParams.PARENT_ID	;
	sfxParams.setMargins(0,0,dpToPx(5),0)				;

	txtParams.topToBottom = tvLabel.getId()				;
	txtParams.bottomToBottom 	= LayoutParams.PARENT_ID;
	txtParams.startToStart = LayoutParams.PARENT_ID		;
//	txtParams.endToEnd 	= LayoutParams.PARENT_ID	;
	txtParams.setMargins(0,0,0,0);//dpToPx(20)			; // Отступ между текстами

	// Добавляем элементы
	addView(tvLabel , lblParams)						;
	addView(tvSuffix, sfxParams)						;

	if(uiE.label  != null) setLabel (uiE.getLabel())	;
	if(uiE.hint   != null) setHint  (uiE.getHint())		;
	if(uiE.suffix != null) setSuffix(uiE.getSuffix())	;

	if(noLabel){ tvLabel.setVisibility(GONE)	; tvSuffix.setVisibility(GONE)	;}
  }
  //=========================================================================
  // Вспомогательный метод для конвертации dp в пиксели
  public int dpToPx(int dp) {
	float density = 1;//getResources().getDisplayMetrics().density;
	return Math.round(dp * density)			;}
  // Методы для управления текстом
  public CharSequence getText() 	{ return mText	;}
  //============================================================
  public void setText(String text) { if(text == null) text = ""	; mText = text	;}
 public void setValue(String val){}
  public void setIcon(String val){ icChar = val	;}
  public void setChecked(boolean val){}
  public boolean isChecked()				{ return false				;}
  public void setLabel(CharSequence text) 	{ tvLabel.setText(text)		;}
  public void setNoLabel(int val)			{ setNoLabel(val != 0)		;}
  public void setNoTab(boolean val)			{ noTab = val				;}
  public static void setFontAwesome(Typeface font){ fontAwesome = font	;}
  public void setNoLabel(boolean val){
	noLabel = val	;
	if(tvLabel != null) tvLabel.setVisibility  (noLabel ? GONE : VISIBLE);
	if(tvSuffix != null) tvSuffix.setVisibility(noLabel || tvSuffix.getText().toString().isEmpty() ? GONE : VISIBLE);
  }
  public void setHint(String val) {	mHint = val			;}
  public void setSuffix(String val) {
	tvSuffix.setText(val)								;
	tvSuffix.setVisibility(val.isEmpty() ? View.GONE : View.VISIBLE)	;}
  public void setTextAlignment(int align){super.setTextAlignment(align)	;}
  public void setTextColor(int color) {}
  public void setLabelColor(int color) { tvLabel.setTextColor(color)	;}
  public void setTextSize(int sizeDp) {
	if(sizeDp > 0){ mTextSize = sizeDp	; sizePx = dpToPx(mTextSize)	;}}
  public void setLabelSize(int sizePx) { if(sizePx > 0) tvLabel.setTextSize(sizePx)	;}
  public static LinearLayout.LayoutParams getLayoutParams(int wwidth, int parentOrientation){
	float wgt = wwidth > 0 ? wwidth : 1	;
	int  wdth = parentOrientation == LinearLayout.HORIZONTAL ? 0 : ViewGroup.LayoutParams.MATCH_PARENT	;
	int  hgt =  parentOrientation == LinearLayout.HORIZONTAL ? ViewGroup.LayoutParams.MATCH_PARENT : ViewGroup.LayoutParams.WRAP_CONTENT	;
	LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(wdth,hgt, wgt );
	lp.setMargins(4,4,4,4)			;
	return lp						;}
  //============================================================
  public interface OnWorkListener{void onWork(String type, String name, String val)	;}
  //============================================================
  public void setOnWorkListener(OnWorkListener val){ onWorkListener = val	;}
  //============================================================
  public CharSequence getLabel()  {return tvLabel.getText();}
  public CharSequence getSuffix() {return tvSuffix.getText();}
  //============================================================
  private void setupAttributes(Context context, AttributeSet attrs) {
	TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CompositeButton);
	try {
	  setText(a.getString(R.styleable.CompositeButton_mainText))	;// Основной текст
	  setLabel(a.getString(R.styleable.CompositeButton_extraText))	;// Дополнительный текст
	  if (a.hasValue(R.styleable.CompositeButton_mainTextColor))
		setTextColor(a.getColor(
				R.styleable.CompositeButton_mainTextColor,Color.WHITE))	;// Цвета текста
	  if (a.hasValue(R.styleable.CompositeButton_extraTextColor))
		setLabelColor(a.getColor(
				R.styleable.CompositeButton_extraTextColor,
				Color.argb(200, 255, 255, 255)))						;
	  setTextSize(a.getDimensionPixelSize(R.styleable.CompositeButton_mainTextSize,0));// Размеры текста
	  setLabelSize(a.getDimensionPixelSize(R.styleable.CompositeButton_extraTextSize,0));
	} finally {  a.recycle()	;}
  }
}