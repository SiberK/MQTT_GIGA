package com.example.mqtt_giga;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.InputType;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import static com.example.mqtt_giga.MqttWork.noEmpty;

import static gson_parser.UiElement.getIntValue;

public class UiWidget extends ConstraintLayout {
  private static Typeface fontAwesome		;
  private static final String TAG = "UI_WD"	;
  private static final int typePass = InputType.TYPE_CLASS_TEXT |
		  							  InputType.TYPE_TEXT_VARIATION_PASSWORD	;

  private	TextView 	label				;
  private	TextView 	suffix				;
  private	TextView 	mainText			;
  public	EditText	mainEText			;
  public	Switch   	mSwitch				;
  private 	Spinner		mSpinner			;

  private 	final String   	type			;
  private 	boolean 	noLabel = false		;
//  private 	boolean 	noTab   = false		;
  private 	String 		icChar   = ""		;
  private	String 		mText  = ""			;
  private 	int			mTextColor			;
  private 	int			mTextSize			;
  private 	int			cntItemsSp = 0		;
  private 	String 		spText				;
  private 	volatile boolean flTouch  = false		;
  private 	final  Context context			;

  public OnWorkListener onWorkListener	;
//=============================================================================
  public UiWidget(@NonNull Context _context,String _type,int txtColor,int txtSize,String spTxt){
  	super(_context)			;
  	context = _context		;
  	type 	= _type			;
  	spText 	= spTxt			;// перечень для Spinner!!!
  	mTextColor = txtColor	; mTextSize = txtSize	;
  	createChildViews()		;// Создаем элементы интерфейса
  }
  public UiWidget(@NonNull Context _context,String _type,int txtColor,int txtSize) {
  	super(_context)			;
  	context = _context		;
  	type 	= _type			;
  	mTextColor = txtColor	; mTextSize = txtSize	;
  	createChildViews()		;// Создаем элементы интерфейса
  }
  //=======================================================================
  private void createChildViews() {
	int mainId = View.generateViewId()		;
	setPadding(0,0,0,0)						;//dpToPx(4), dpToPx(1), dpToPx(4), dpToPx(1));
	// Дополнительный текст
	label = new TextView(context)			;
	label.setId(View.generateViewId())		;
	label.setText("LABEL")					;
	label.setTextSize(12)					;
	label.setTextColor(Color.GRAY)			;
	label.setPadding(0,0,0,0)				;//dpToPx(4), dpToPx(1), dpToPx(4), dpToPx(1));
	suffix = new TextView(context)			;
	suffix.setId(View.generateViewId())		;
	suffix.setTextColor(Color.GRAY)			;
	suffix.setText("")						;
	suffix.setTextSize(12)					;
	suffix.setVisibility(View.GONE)			;
	suffix.setPadding(0,0,0,0)				;

	// Основной текст
	switch(type){
	  case "input" : case "pass" :
		mainEText = new EditText(context)		;
		mainEText.setId(mainId)					;
		mainEText.setText("")					;
		mainEText.setTextColor(mTextColor)		;
		mainEText.setTextSize(mTextSize)		;
		mainEText.setSingleLine()				;
		mainEText.setPadding(0,0,0, dpToPx(6))	;
		setFocusable(true)						;
		if(type.equals("pass"))
		  mainEText.setInputType(typePass)		;
		mainEText.setOnFocusChangeListener((v, hasFocus) -> {
		  if(!hasFocus) // Вызывается при потере фокуса
			if(onWorkListener != null) onWorkListener.onWork(type,getTag().toString(),mainEText.getText().toString())	;});

		mainEText.setOnEditorActionListener((v,actionId,event)->{
		  if (actionId == EditorInfo.IME_ACTION_DONE ||
				  (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
			if(onWorkListener != null) onWorkListener.onWork(type,getTag().toString(),mainEText.getText().toString())	;
			return true;} // Событие обработано
		  return false	;});

	  break	;
	  case "label" :
		mainText = new TextView(context)		;
		mainText.setId(mainId)					;
		mainText.setText("")					;
		mainText.setTextColor(mTextColor)		;
		mainText.setTextSize(mTextSize)			;
		mainText.setPadding(0,0,0,0);// dpToPx(6))	;
		mainText.setSingleLine()				;
	  break	;
	  case "button":
		mainText = new TextView(context)		;
		mainText.setId(mainId)					;
		mainText.setTextColor(mTextColor)		;
		mainText.setTextSize(mTextSize)			;
		mainText.setSingleLine()				;
		mainText.setPadding(0,0,0, dpToPx(6))	;
		setClickable(true)						;
		setFocusable(true)						;
		setOnClickListener(v->onClicked());
	  break	;
	  case "switch_t"	:
		mSwitch = new Switch(context)			;
		mSwitch.setId(mainId)					;
		mSwitch.setPadding(0,0,0,0)				;
		mSwitch.setTextColor(mTextColor)		;
		mSwitch.setTextSize(mTextSize)			;
		mSwitch.setOnClickListener(v->onClicked());
	  break;
	  case "select":
		mTextColor = Color.BLACK				;
		mSpinner = new Spinner(context)			;
		mSpinner.setId(mainId)					;
//		mSpinner.setPadding(0,0,0,0)			;
		initSpinner(spText)						;
	  break	;
	  default:
		mainText = new TextView(context)		;
		mainText.setId(mainId)					;
		mainText.setText("")					;
		mainText.setTextColor(mTextColor)		;
		mainText.setTextSize(mTextSize)			;
		mainText.setPadding(0,0,0, dpToPx(6))	;
		mainText.setSingleLine()				;
		label.setVisibility(GONE)				;
		suffix.setVisibility(GONE)				;
		noLabel = true	;
	  break	;
	}

	// Параметры расположения
	LayoutParams lblParams = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
	LayoutParams sfxParams = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
	LayoutParams txtParams = new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);

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

	txtParams.topToBottom = label.getId()				;
	txtParams.bottomToBottom 	= LayoutParams.PARENT_ID;
	txtParams.startToStart = LayoutParams.PARENT_ID		;
//	txtParams.endToEnd 	= LayoutParams.PARENT_ID	;
	txtParams.setMargins(0,0,0,0);//dpToPx(20)			; // Отступ между текстами

	// Добавляем элементы
	addView(label	 , lblParams)						;
	addView(suffix	 , sfxParams)						;
	if(mainEText != null) addView(mainEText, txtParams)	;
	if(mainText  != null) addView(mainText , txtParams)	;
	if(mSwitch   != null) addView(mSwitch  , txtParams)	;
	if(mSpinner  != null) addView(mSpinner , txtParams)	;
  }
  //=========================================================================
  private void onClicked(){
	String _name = getTag().toString()	;
	String _value = ""	;
	switch(type){
	  case "button" : _value = "2"		; break	;
	  case "switch_t": _value = mSwitch != null && mSwitch.isChecked() ? "1" : "0"	; break	;
	}
//	if(mSwitch != null) mSwitch.setChecked(!mSwitch.isChecked())	;
	if(onWorkListener != null) onWorkListener.onWork(type,_name,_value);
  }
  //=========================================================================
  private void setupAttributes(Context context, AttributeSet attrs) {
	TypedArray a = context.obtainStyledAttributes(attrs,R.styleable.CompositeButton);
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

  // Вспомогательный метод для конвертации dp в пиксели
  private int dpToPx(int dp) {
	float density = 1;//getResources().getDisplayMetrics().density;
	return Math.round(dp * density)			;}

  // Методы для управления текстом
  public void setText(String text) {
	if(text == null) text = ""	; mText = text			;
	if(noEmpty(icChar)){ text = icChar	; if(noEmpty(mText)) text += " " + mText		;}
	if(mainEText != null) mainEText.setText(text)		;
	if(mainText  != null) mainText.setText(text)		;
	if(mSwitch   != null) mSwitch.setChecked(text.equals("1")) ;
	if(mSpinner  != null) setValue(text)				;
  }
 public void setValue(String val){
   	int ix = getIntValue(val,-1)	;
	if(mSpinner != null && ix >= 0 && ix < cntItemsSp)
	  mSpinner.setSelection(ix)		;
 }
  private void initSpinner(String text){
	String items[] = !text.isEmpty() ?  text.split(";") : new String[0]   ;
	cntItemsSp = items.length	;
	ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, R.layout.custom_spinner_item, items) {
	  @Override
	  public View getView(int position, View convertView, ViewGroup parent) {
		TextView view = (TextView) super.getView(position, convertView, parent);
		view.setTextColor(mTextColor)            		;
		view.setTextSize(mTextSize)      				;
//		view.setLayoutParams(getLayoutParams()) 		;
		view.setPadding(4,0,32,0)	; return view		;}//
	  @Override
	  public View getDropDownView(int position, View convertView, ViewGroup parent) {
		TextView view = (TextView) super.getDropDownView(position, convertView, parent);
		view.setTextColor(mTextColor)            		;
		view.setTextSize(mTextSize)  					;//-2
		view.setGravity(Gravity.CENTER)		  			;
		return view	;}};
	try{ if(mSpinner != null) mSpinner.setAdapter(adapter)	;}
	catch(IllegalArgumentException e){Log.w(TAG,"",e)	;}

	mSpinner.setPadding(0,4,0,4)	;
	mSpinner.setGravity(Gravity.CENTER)	;
//	if(!noTab)
//	  mSpinner.setBackgroundResource(R.drawable.spinner_bg)	;
	mSpinner.setSelection(0);

	mSpinner.setOnTouchListener((v,event)->{
	  if(event.getAction() == MotionEvent.ACTION_UP){flTouch = true	;} return false	;});
	mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
	  @Override
	  public void onItemSelected(AdapterView<?> parent, View view, int position, long id){
		if(flTouch){ flTouch = false			;
		  String val = String.valueOf(position)	;
		  if(onWorkListener != null) onWorkListener.onWork(type,getTag().toString(),val)	;}}
	  @Override public void onNothingSelected(AdapterView<?> parent){flTouch = false		;}});
  }
  public void setIcon(String val)			{
    if(fontAwesome != null && !val.isEmpty() &&
			(type.equals("label") || type.equals("button"))){
	  icChar = val	;
	  if(mainEText != null) mainEText.setTypeface(fontAwesome)		;
	  if(mainText  != null) mainText.setTypeface(fontAwesome)		;
	  setText(mText)	;
	}
	else icChar = ""	;
  }
  public void setChecked(boolean val){if(mSwitch != null) mSwitch.setChecked(val)	;}
  public boolean isChecked(){ return mSwitch != null ? mSwitch.isChecked() : false	;}
  public void setLabel(CharSequence text) 	{ label.setText(text)	;}
  public void setNoLabel(int val)			{ setNoLabel(val != 0)	;}
  public static void setFontAwesome(Typeface font){ fontAwesome = font	;}
  public void setNoLabel(boolean val){
	noLabel = val	;
	if(label != null) label.setVisibility  (noLabel ? GONE : VISIBLE);
	if(suffix != null) suffix.setVisibility(noLabel || suffix.getText().toString().isEmpty() ? GONE : VISIBLE);
  }
  public void setHint(String text) {
	if(mainEText != null) mainEText.setHint(text)	;
	if(mainText  != null) mainText.setHint(text)	;}
  public void setSuffix(String val) {
	suffix.setText(val)								;
	suffix.setVisibility(val.isEmpty() ? View.GONE : View.VISIBLE)	;
  }
  public void setTextAlignment(int align){
	super.setTextAlignment(align)							;
	if(mainEText != null) mainEText.setTextAlignment(align)	;
	if(mainText  != null) mainText.setTextAlignment(align)	;}
  public void setTextColor(int color) {
	if(mainEText != null) mainEText.setTextColor(color)		;
	if(mainText  != null) mainText .setTextColor(color)		;}
  public void setLabelColor(int color) { label.setTextColor(color)	;}
  public void setTextSize(int sizePx) {
	mTextSize = sizePx										;
	if(sizePx > 0){ sizePx = dpToPx(sizePx)					;
	  if(mainEText != null) mainEText.setTextSize(sizePx)	;
	  if(mainText  != null) mainText .setTextSize(sizePx)	;}
  }
  public void setLabelSize(int sizePx) {
	if(sizePx > 0) label.setTextSize(sizePx);
  }
  // Геттеры
  public CharSequence getText() {
	if(mainEText != null) mText = mainEText.getText().toString()	;
	return mText	;}

  public CharSequence getLabel()  {return label.getText();}
  public CharSequence getSuffix() {return suffix.getText();}
  //============================================================
  public interface OnWorkListener{
	void onWork(String type, String name, String val)	;
  }
  public void setOnWorkListener(OnWorkListener val){ onWorkListener = val	;}
}