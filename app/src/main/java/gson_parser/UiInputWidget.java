package gson_parser;

import android.content.Context;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import androidx.annotation.NonNull;

import static com.example.mqtt_giga.MqttWork.noEmpty;

public class UiInputWidget extends UiWidget{
  protected static final int    typePass = 	InputType.TYPE_CLASS_TEXT |
		  									InputType.TYPE_TEXT_VARIATION_PASSWORD	;
  private 	EditText	mainEText			;
  //----------------------------------------------------------------
  public UiInputWidget(@NonNull Context _context, UiElement uiE, int parentOrientation){
	super(_context,uiE,parentOrientation)	;}
  //----------------------------------------------------------------
  @Override
  protected void createChildViews(UiElement uiE){
	super.createChildViews(uiE)				;

	mainEText = new EditText(context)		;
	mainEText.setId(mainId)					;
	mainEText.setSingleLine()				;
	mainEText.setPadding(0,0,0,botPadding)	;

	if(mTextSize == 0) mTextSize = uiE.DEF_FNT_SIZE	;
	setText(mText)							;
	setTextColor(mTextColor)				;
	setTextSize (mTextSize)					;
	setFocusable(true)						;

	if(mType.equals("pass"))
	  mainEText.setInputType(typePass)		;

	mainEText.setOnFocusChangeListener((v, hasFocus) -> {
	  if(!hasFocus) 						// Вызывается при потере фокуса
		if(onWorkListener != null) onWorkListener.onWork(
				mType,getTag().toString(),mainEText.getText().toString())	;});

	mainEText.setOnEditorActionListener((v,actionId,event)->{
	  if (actionId == EditorInfo.IME_ACTION_DONE ||
			  (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
		if(onWorkListener != null) onWorkListener.onWork(
				mType,getTag().toString(),mainEText.getText().toString())	;
		return true						;} // Событие обработано
	  return false						;});

	addView(mainEText, txtParams)		;
  }
  //----------------------------------------------------------------
//  @Override   public void update(UiElement updE){
//	super.update(updE)	;
//  }
  //----------------------------------------------------------------
// Методы для управления текстом
  @Override  public void setText(String text) {
	super.setText(text)					;
	mainEText.setText(text)				;}
  //----------------------------------------------------------------
  @Override  public void setIcon(String val){
	super.setIcon(val)					;
	if(noEmpty(icChar)) mainEText.setTypeface(fontAwesome)		;}
  //----------------------------------------------------------------
  @Override  public void setHint(String text){
	super.setHint(text)					;
	if(mainEText != null) mainEText.setHint(mHint)			;}
  //----------------------------------------------------------------
  @Override  public void setTextAlignment(int align){
	super.setTextAlignment(align)		;
	mainEText.setTextAlignment(align)	;}
  //----------------------------------------------------------------
  @Override public void setTextColor(int color){
	super.setTextColor(color)			;
	mainEText.setTextColor(color)		;}
  //----------------------------------------------------------------
  @Override public void setTextSize(int sizeDp){
	super.setTextSize(sizeDp)			;
	mainEText.setTextSize(sizePx)		;}
  //----------------------------------------------------------------
  @Override public CharSequence getText(){
	return mainEText.getText()			;}
}
