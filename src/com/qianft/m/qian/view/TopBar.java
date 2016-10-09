package com.qianft.m.qian.view;

import com.qianft.m.qian.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

public class TopBar extends LinearLayout{

	public TopBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		LayoutInflater.from(context).inflate(R.layout.top_bar, this);
	}

}
