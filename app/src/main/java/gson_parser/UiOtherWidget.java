package gson_parser;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

public class UiOtherWidget extends UiWidget{

  public UiOtherWidget(@NonNull Context _context,UiElement uiE, int parentOrientation){
	super(_context,uiE,parentOrientation)	;
  }
  @Override
  protected void createChildViews(UiElement uiE){
	super.createChildViews(uiE)	;
  }
}
