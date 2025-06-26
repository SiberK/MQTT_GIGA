package gson_parser;

import android.content.Context;
import android.util.Log;
import android.widget.Switch;
import android.widget.TextView;

import static com.example.mqtt_giga.MqttWork.noEmpty;

public class UiSwitchWidget extends UiWidget{
  private static final String TAG = "SWTH_WD"	;
  protected Switch mSwitch						;

  //----------------------------------------------------------------
  public UiSwitchWidget(Context _context, UiElement uiE, int parentOrientation){
	super(_context,uiE,parentOrientation);}
  //----------------------------------------------------------------
  @Override
  protected void createChildViews(UiElement uiE){
	super.createChildViews(uiE)				;

	mSwitch = new Switch(context)			;
	mSwitch.setId(mainId)					;
	mSwitch.setPadding(0,0,0,botPadding)	;
	mSwitch.setTextColor(mTextColor)		;
	mSwitch.setTextSize(mTextSize)			;
	mSwitch.setOnClickListener(v->onClicked());
	mSwitch.setChecked(uiE.getChecked())	;

	setTextSize (mTextSize)					;
	addView(mSwitch, txtParams)		        ;
  }
  //----------------------------------------------------------------
  private void onClicked(){
	String _value = mSwitch.isChecked() ? "1" : "0"	;
	if(onWorkListener != null) onWorkListener.onWork(mType,mUiId,_value);
  }
  //=========================================================================
  @Override public void setTextSize(int sizeDp){
	super.setTextSize(sizeDp)				;
	mSwitch.setTextSize(sizePx)				;}
  //----------------------------------------------------------------
  @Override  public void setChecked(boolean val){if(mSwitch != null) mSwitch.setChecked(val)	;}
  //----------------------------------------------------------------
  @Override  public boolean isChecked(){ return mSwitch != null ? mSwitch.isChecked() : false	;}
  //----------------------------------------------------------------
  @Override public String getText(){ return mText   ;}
  //----------------------------------------------------------------
}
