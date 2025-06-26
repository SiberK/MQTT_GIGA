package gson_parser;

import android.content.Context;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import androidx.annotation.NonNull;

import static com.example.mqtt_giga.MqttWork.noEmpty;
import com.example.mqtt_giga.R;

public class UiInputWidget extends UiWidget{
  protected static final int    typePass = 	InputType.TYPE_CLASS_TEXT |
		  									InputType.TYPE_TEXT_VARIATION_PASSWORD	;
  protected EditText	mainEText		;
  //----------------------------------------------------------------
  public UiInputWidget(@NonNull Context _context, UiElement uiE, int parentOrientation){
	super(_context,uiE,parentOrientation)	;}
  //----------------------------------------------------------------
  // Этот конструктор для созания виджета через макет!!!!
  public UiInputWidget(Context context, AttributeSet attrs) {
	super(context, attrs)				;
	if(!noEmpty(mType)) mType = "input"	;
	createMainView()					;
	setOrientation(VERTICAL)			;
	addView(mainEText,txtParams)		;
	setText(mText)						;
	if(!noTab)  setBackgroundResource(R.drawable.btn_device_bg)	;
  }
  //----------------------------------------------------------------
  @Override  protected void createChildViews(UiElement uiE){
	super.createChildViews(uiE)			;
	createMainView()					;
	addView(mainEText, txtParams)		;
  }
  //----------------------------------------------------------------
  private void createMainView(){
	// Параметры расположения
	txtParams = new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT,1);
	txtParams.setMargins(0,0,0,0);//dpToPx(20)			; // Отступ между текстами

	mainEText = new EditText(context)		;
	mainEText.setId(mainId)					;
	mainEText.setSingleLine()				;
	mainEText.setPadding(0,0,0,botPadding)	;
	mainEText.setTextAlignment(TEXT_ALIGNMENT_VIEW_START)	;

	if(mTextSize == 0) mTextSize = UiElement.DEF_FNT_SIZE	;
	setText(mText)							;
	setTextColor(mTextColor)				;
	setTextSize (mTextSize)					;
	setFocusable(true)						;

	if(mType.equals("pass"))
	  mainEText.setInputType(typePass)		;

	mainEText.setOnFocusChangeListener((v, hasFocus) -> {
	  if(!hasFocus) 						// Вызывается при потере фокуса
		if(onWorkListener != null) onWorkListener.onWork(
				mType,mUiId,mainEText.getText().toString())	;});

	mainEText.setOnEditorActionListener((v,actionId,event)->{
	  if (actionId == EditorInfo.IME_ACTION_DONE ||
			  (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
		if(onWorkListener != null) onWorkListener.onWork(
				mType,mUiId,mainEText.getText().toString())	;
		return true						;} // Событие обработано
	  return false						;});
  }
  //----------------------------------------------------------------
//  @Override   public void update(UiElement updE){
//	super.update(updE)	;
//  }
  //----------------------------------------------------------------
// Методы для управления текстом
  @Override  public void setText(String text) {
	super.setText(text)					;
	if(mainEText != null) mainEText.setText(text)				;}
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
	if(mainEText != null) mainEText.setTextAlignment(align)	;}
  //----------------------------------------------------------------
  @Override public void setTextColor(int color){
	super.setTextColor(color)			;
	if(mainEText != null) mainEText.setTextColor(color)		;}
  //----------------------------------------------------------------
  @Override public void setTextSize(int sizeDp){
	super.setTextSize(sizeDp)			;
	if(mainEText != null) mainEText.setTextSize(sizePx)		;}
  //----------------------------------------------------------------
  @Override public String getText(){
	return mainEText != null ? mainEText.getText().toString() : ""		;}
}
