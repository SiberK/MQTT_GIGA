package gson_parser;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import static com.example.mqtt_giga.MqttWork.noEmpty;
import com.example.mqtt_giga.R;

import static gson_parser.UiElement.getIntValue;

public class UiSelectWidget extends UiWidget{
  private 	static final   String TAG      = "UI_SLCT"	;
  protected	volatile boolean flTouch = false;
  protected 	Spinner		mSpinner		;

  //----------------------------------------------------------------
  public UiSelectWidget(Context _context, UiElement uiE, int parentOrientation){
	super(_context,uiE,parentOrientation);}
  //----------------------------------------------------------------
  // Этот конструктор для созания виджета через макет!!!!
  public UiSelectWidget(Context context, AttributeSet attrs) {
	super(context, attrs)				;
	if(!noEmpty(mType)) mType = "select"	;
	createMainView()					;
	setOrientation(VERTICAL)			;
	addView(mSpinner,txtParams)			;
//    setText(mText)						;
	if(!noTab)  setBackgroundResource(R.drawable.btn_device_bg)	;
  }
  //----------------------------------------------------------------
  @Override
  protected void createChildViews(UiElement uiE){
	super.createChildViews(uiE)         ;
	createMainView()					;
	addView(mSpinner,txtParams)         ;
  }
  //----------------------------------------------------------------
  private void createMainView(){
	// Параметры расположения
	txtParams = new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT,1);
	txtParams.setMargins(0,0,0,0)			;//dpToPx(20)			; // Отступ между текстами

	mTextColor = Color.BLACK            ;
	mSpinner   = new Spinner(context)   ;
	mSpinner.setId(mainId)              ;
	//		mSpinner.setPadding(0,0,0,botPadding)			;
	initSpinner(spText)                 ;
  }
  //----------------------------------------------------------------
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

	mSpinner.setPadding(0,0,0,botPadding)	;
	mSpinner.setGravity(Gravity.CENTER)		;
	setValue(mValue)						;

	mSpinner.setOnTouchListener((v,event)->{
	  if(event.getAction() == MotionEvent.ACTION_UP){flTouch = true	;} return false	;});
	mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
	  @Override
	  public void onItemSelected(AdapterView<?> parent, View view, int position, long id){
		if(flTouch){ flTouch = false			;
		  String strPos = String.valueOf(position)	;
		  String strSel = mSpinner.getSelectedItem().toString()	;
		  if(onWorkListener   != null) onWorkListener.onWork(mType,mUiId,strPos)	;
		  if(onSelectListener != null) onSelectListener.onSelect(position,strSel)	;
		}}
	  @Override public void onNothingSelected(AdapterView<?> parent){flTouch = false		;}});
  }
  //----------------------------------------------------------------
  @Override public void setText(String text) {
	super.setText(text)         		;
	setValue(mText)						;}
  //----------------------------------------------------------------
  @Override public void setValue(String val){
	mValue = noEmpty(val) ? val : ""	;
	setValue(getIntValue(mValue,-1)	)	;}
  //----------------------------------------------------------------
  @Override public void setValue(int ix){
	if(mSpinner != null && ix >= 0 && ix < cntItemsSp)
	  mSpinner.setSelection(ix)			;}
  //----------------------------------------------------------------
  public int getValueInt(){ return mSpinner != null ? mSpinner.getSelectedItemPosition() : 0	;}
  //============================================================
  private OnSelectListener onSelectListener	;
  public interface OnSelectListener{void onSelect(int position, String val)	;}
  //============================================================
  public void setOnSelectListener(UiSelectWidget.OnSelectListener val){ onSelectListener = val	;}
  //============================================================
}
