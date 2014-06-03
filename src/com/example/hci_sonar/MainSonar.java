package com.example.hci_sonar;

import android.app.Activity;
import android.opengl.Visibility;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.TextView;

public class MainSonar extends Activity {
	
	private Button b_record;
	private TextView tv_recording;
	private TextView tv_info_sanction;
	private TextView tv_seconds_to_next_recording;

	private OnTouchListener buttonListener = new OnTouchListener() {
		
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			// TODO Auto-generated method stub
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				b_record.setBackground(getApplicationContext().getResources().getDrawable(R.drawable.buttonshapeactive));
				tv_recording.setVisibility(TextView.VISIBLE);
				
			} else if (event.getAction() == MotionEvent.ACTION_UP) {
				b_record.setBackground(getApplicationContext().getResources().getDrawable(R.drawable.buttonshape));
				tv_recording.setVisibility(TextView.INVISIBLE);
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
		
		this.tv_recording = (TextView) findViewById(R.id.TVRecording);
		this.tv_recording.setVisibility(TextView.INVISIBLE);
		this.tv_info_sanction = (TextView) findViewById(R.id.TVInfoSanction);
		this.tv_info_sanction.setVisibility(TextView.INVISIBLE);
		this.tv_seconds_to_next_recording = (TextView) findViewById(R.id.TVSecondsToNextRecording);
		this.tv_seconds_to_next_recording.setVisibility(TextView.INVISIBLE);
	}
	
	

}
