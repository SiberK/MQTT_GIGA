package gson_parser;

import android.content.Context;
import android.graphics.Color;
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
  @Override
  protected void createChildViews(UiElement uiE){
    super.createChildViews(uiE)         ;

    mTextColor = Color.BLACK            ;
    mSpinner   = new Spinner(context)   ;
    mSpinner.setId(mainId)              ;
    //		mSpinner.setPadding(0,0,0,botPadding)			;
    initSpinner(spText)                 ;
    addView(mSpinner,txtParams)         ;
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
          if(onWorkListener != null) onWorkListener.onWork(mType,getTag().toString(),val)	;}}
      @Override public void onNothingSelected(AdapterView<?> parent){flTouch = false		;}});
  }
  @Override public void setText(String text) {
    super.setText(text)         ;
	setValue(mText)				;
  }
  @Override public void setValue(String val){
    int ix = getIntValue(val,-1)	;
    if(mSpinner != null && ix >= 0 && ix < cntItemsSp)
      mSpinner.setSelection(ix)		;
  }
}
