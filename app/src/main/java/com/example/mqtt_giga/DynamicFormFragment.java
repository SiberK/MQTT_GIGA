package com.example.mqtt_giga;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import gson_parser.LayoutBuilder;

public class DynamicFormFragment extends Fragment{
  private static final String        TAG = "DY_FORM"	;
  private              FrameLayout   container			;
  private              LayoutBuilder layoutBuilder		;
  private              Context       context				;

  public DynamicFormFragment(){}	// Required empty public constructor
  public static DynamicFormFragment newInstance() {
	return new DynamicFormFragment()			;}
  @Override
  public void onCreate(Bundle savedInstanceState){super.onCreate(savedInstanceState)	;}
  @Override
  public void onAttach(@NonNull Context _context) {
	super.onAttach(_context)	;
	context = _context			;}
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState){
	Log.i(TAG,"onCreateView")	;
	// Создаем новый FrameLayout как корневой вид фрагмента
	FrameLayout root = new FrameLayout(context);
	root.setLayoutParams(new FrameLayout.LayoutParams(
			FrameLayout.LayoutParams.MATCH_PARENT,
			FrameLayout.LayoutParams.MATCH_PARENT));

	container = root; // Сохраняем ссылку
	return root;}
  @Override
  public void onViewCreated(View view, Bundle savedInstanceState){
	super.onViewCreated(view, savedInstanceState)	;
	layoutBuilder = new LayoutBuilder(context)		;
	Log.i(TAG,"onViewCreated")	;
  }
  @Override
  public void onDestroyView(){
	super.onDestroyView()	;
	clear()					;
  }
  //============================================================================================
  public void clear(){if(container != null){ container.removeAllViews()	; container = null	;}}
  public void buildFace(String json, SubMenu subMenu){
  	if(json != null && !json.isEmpty() && layoutBuilder != null
			&& container != null && context != null){
	    container.setVisibility(View.VISIBLE)				;
  		layoutBuilder.buildLayout(json, container,subMenu)	;}
  }
  public void updateFace(String json){
	if(json != null && !json.isEmpty() && layoutBuilder != null
			&& container != null && context != null){
	  container.setVisibility(View.VISIBLE)			;
      layoutBuilder.updateFace(json,container)		;}
  }

  public void removeAllViews(){if(container != null){
	container.removeAllViews()	; container.setVisibility(View.GONE)	;}}
  //============================================================================================
}