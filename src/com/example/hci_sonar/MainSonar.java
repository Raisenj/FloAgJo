package com.example.hci_sonar;

import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;

public class MainSonar extends Activity {
	
	private Button b_record;

	private OnTouchListener buttonListener = new OnTouchListener() {
		
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			// TODO Auto-generated method stub
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				b_record.setBackground(getApplicationContext().getResources().getDrawable(R.drawable.buttonshapeactive));
				
			} else if (event.getAction() == MotionEvent.ACTION_UP) {
				b_record.setBackground(getApplicationContext().getResources().getDrawable(R.drawable.buttonshape));
			}
			return false;
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_main);
		
		initGuiElements();
	}

	private void initGuiElements() {
		// TODO Auto-generated method stub
		this.b_record = (Button) findViewById(R.id.Brecord);
		this.b_record.setOnTouchListener(buttonListener);
	}
	
	

}
