package gson_parser;

import android.content.Context;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import static com.example.mqtt_giga.MqttWork.noEmpty;

public class UiButtonWidget extends UiWidget{
  private static final String DEF_ICHR_BTN = UiElement.getIcChar("f192")  ;
  protected TextView mainText				;
  public UiButtonWidget(Context _context, UiElement uiE, int parentOrientation){
	super(_context,uiE,parentOrientation)  ;
  }
  @Override
  protected void createChildViews(UiElement uiE){
	if(uiE.getType().equals("button"))
	  mTextColor = uiE.color == null ? uiE.DEF_COLOR : uiE.getColorF()	;
	if(uiE.getType().equals("title")) noTab = noLabel = true  ;
	if(uiE.getFsize() == 0) mTextSize = uiE.DEF_FNT_SIZE *3/2	;

	super.createChildViews(uiE)				;

	mainText = new TextView(context)		;
	mainText.setId(mainId)					;
	mainText.setSingleLine()				;
	mainText.setPadding(0,0,0,botPadding)	;
	setTextSize(mTextSize)                	;

	if(mType.equals("button")){
	  if(icChar.isEmpty()) icChar = DEF_ICHR_BTN;
	  setFocusable(true)                    ;
	  setClickable(true)					;
	  setFocusable(true)					;
	  setOnClickListener(v->onClicked())  ;}

	setText(mText)							;
	setTextColor(mTextColor)				;
//    if(noLabel) tvLabel.setVisibility(GONE) ;
//    setTextAlignment(mAlign)                ;
	addView(mainText, txtParams)		    ;
  }
  //----------------------------------------------------------------
  private void onClicked(){
	String _name = getTag().toString()	    ;

	if(onWorkListener != null) onWorkListener.onWork(mType,mUiId,"2");
  }
  //=========================================================================
// Методы для управления текстом
  @Override  public void setText(String text) {
	super.setText(text)					;
	if(fontAwesome != null && noEmpty(icChar) &&
			(mType.equals("label") || mType.equals("button"))){
	  mainText.setTypeface(fontAwesome)	;
	  text = icChar ;
	  if(!mText.isEmpty()) text = icChar + " " + mText  ;
	}
	mainText.setText(text)				;}
  //----------------------------------------------------------------
  @Override  public void setIcon(String val){
	super.setIcon(val)	;
	setText(mText)		;
  }
  //----------------------------------------------------------------
  @Override  public void setHint(String text){
	super.setHint(text)					;
	if(mainText != null) mainText.setHint(mHint)		;}
  //----------------------------------------------------------------
  @Override  public void setTextAlignment(int align){
	super.setTextAlignment(align)		;
	mainText.setTextAlignment(align)	;
  }
  //----------------------------------------------------------------
  @Override public void setTextColor(int color){
	super.setTextColor(color)			;
	mainText.setTextColor(color)		;}
  //----------------------------------------------------------------
  @Override public void setTextSize(int sizeDp){
	super.setTextSize(sizeDp)			;
	mainText.setTextSize(sizePx)		;}
  //----------------------------------------------------------------
  @Override public CharSequence getText(){ return mText   ;}
}
