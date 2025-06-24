package gson_parser;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import static com.example.mqtt_giga.MqttWork.noEmpty;
import com.example.mqtt_giga.R;

public class UiButtonWidget extends UiWidget{
  private static final String DEF_ICHR_BTN = UiElement.getIcChar("f192")  ;
  protected TextView mainText				;
  public UiButtonWidget(Context _context, UiElement uiE, int parentOrientation){
	super(_context,uiE,parentOrientation)  ;}
  //----------------------------------------------------------------
  // Этот конструктор для созания виджета через макет!!!!
  public UiButtonWidget(Context context, AttributeSet attrs) {
	super(context, attrs)				;
	if(!noEmpty(mType)) mType = "label"	;
	createMainView()					;
	setOrientation(VERTICAL)			;
	addView(mainText,txtParams)			;
	setText(mText)						;
	if(!noTab)  setBackgroundResource(R.drawable.btn_device_bg)	;
  }
  //----------------------------------------------------------------
  @Override
  protected void createChildViews(UiElement uiE){
	super.createChildViews(uiE)				;
	createMainView()						;
	addView(mainText, txtParams)		    ;
  }
  //----------------------------------------------------------------
  private void createMainView(){
	// Параметры расположения
	txtParams = new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT,1);
	txtParams.setMargins(0,0,0,0)			;//dpToPx(20)			; // Отступ между текстами

	mainText = new TextView(context)		;
	mainText.setId(mainId)					;
	if(!mType.equals("label")) flSingleLine = true	;
	mainText.setSingleLine(flSingleLine)	;
	mainText.setPadding(0,0,0,botPadding)	;
	mainText.setTextAlignment(mAlign)		;

	if(mTextSize == 0) mTextSize = UiElement.DEF_FNT_SIZE	;
	setText(mText)							;
	setTextColor(mTextColor)				;
	setTextSize (mTextSize)					;
	setFocusable(true)						;

	if(mType.equals("button")){
	  if(icChar.isEmpty() && mText.isEmpty()) icChar = DEF_ICHR_BTN;
	  setFocusable(true)                    ;
	  setClickable(true)					;
	  setFocusable(true)					;
	  setOnClickListener(v->onClicked())  ;}
  }
  //----------------------------------------------------------------
  private void onClicked(){
	String _name = getTag().toString()	    ;
	if(onWorkListener != null) onWorkListener.onWork(mType,mUiId,"2");}
  //=========================================================================
// Методы для управления текстом
  @Override  public void setText(String text) {
	super.setText(text)					;
	if(fontAwesome != null && noEmpty(icChar) &&
			(mType.equals("label") || mType.equals("button"))){
	  if(mainText != null) mainText.setTypeface(fontAwesome)	;
	  text = icChar ;
	  if(noEmpty(mText)) text = icChar + " " + mText	;
	}
	if(mainText != null) mainText.setText(text)			;}
  //----------------------------------------------------------------
  @Override  public void setIcon(String val){
	super.setIcon(val)	;
	setText(mText)		;}
  //----------------------------------------------------------------
  @Override  public void setHint(String text){
	super.setHint(text)					;
	if(mainText != null) mainText.setHint(mHint)		;}
  //----------------------------------------------------------------
  @Override  public void setTextAlignment(int align){
	super.setTextAlignment(align)		;
	if(mainText != null) mainText.setTextAlignment(align)	;
  }
  //----------------------------------------------------------------
  @Override public void setTextColor(int color){
	super.setTextColor(color)			;
	if(mainText != null) mainText.setTextColor(color)		;}
  //----------------------------------------------------------------
  @Override public void setTextSize(int sizeDp){
	super.setTextSize(sizeDp)			;
	if(mainText != null) mainText.setTextSize(sizePx)		;}
  //----------------------------------------------------------------
  @Override public String getText(){ return mText   ;}
}
