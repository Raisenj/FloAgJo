package com.example.hci_sonar;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.TextView;

public class MainSonar extends Activity {

	private static final int RECORDING_TIME = 3;
	private static final int SANCTION_TIME = 10;

	private static final int IDLE = 99;
	private static final int RECORDING = 98;
	private static final int SANCTIONED = 97;

	private final Handler guiHandler = new Handler();

	private Button b_record;
	private TextView tv_recording;
	private TextView tv_info_sanction;
	private TextView tv_seconds_to_next_recording;
	private TextView tv_counter_recording;

	private Timer timer_recording;
	private Timer timer_sanction;
	private int STATE;
	private int seconds_recorded;
	private int seconds_sanction;

	private Runnable changeGUIToSanction = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub

			updateGUISanctioned();
		}

	};

	private Runnable backToIdle = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			updateGUIIdle();
		}

	};

	final Runnable decreaseCounter = new Runnable() {

		@Override
		public void run() {
			tv_counter_recording.setText(String.valueOf((Integer
					.parseInt((String) tv_counter_recording.getText()) - 1)));
		}
	};
	final Runnable decreaseSecondsSanction = new Runnable() {

		@Override
		public void run() {
			tv_seconds_to_next_recording
					.setText(String.valueOf((Integer
							.parseInt((String) tv_seconds_to_next_recording
									.getText()) - 1)));
		}
	};

	private OnTouchListener buttonListener = new OnTouchListener() {

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			// TODO Auto-generated method stub
			switch (STATE) {
			case IDLE:
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					startRecording();
					return true;
				}
				break;
			case RECORDING:
				if (event.getAction() == MotionEvent.ACTION_UP) {
					stopRecording(false);
					return true;
				}
				break;
			}
			return false;
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_main);

		initVariables();
		initGuiElements();
	}

	protected void stopRecording(boolean fromThread) {
		// TODO Auto-generated method stub
		STATE = SANCTIONED;
		this.seconds_recorded = 0;
		this.seconds_sanction = 0;
		if (fromThread) {
			guiHandler.post(changeGUIToSanction);
		} else {
			updateGUISanctioned();
		}
		this.timer_recording.cancel();
		this.timer_recording.purge();
		this.timer_recording = null;
		this.timer_sanction = new Timer(true);
		this.timer_sanction.schedule(new SanctionCounterTask(), 1000, 1000);
	}

	private void updateGUISanctioned() {
		// TODO Auto-generated method stub
		this.b_record.setEnabled(false);
		this.b_record.setBackground(getApplicationContext().getResources()
				.getDrawable(R.drawable.buttonshapedisabled));
		this.tv_recording.setVisibility(TextView.INVISIBLE);
		this.tv_info_sanction.setVisibility(TextView.VISIBLE);
		this.tv_seconds_to_next_recording
				.setText(String.valueOf(SANCTION_TIME));
		this.tv_seconds_to_next_recording.setVisibility(TextView.VISIBLE);
		this.tv_counter_recording.setText(String.valueOf(RECORDING_TIME));
		this.tv_counter_recording.setVisibility(TextView.INVISIBLE);
	}

	private void updateGUIIdle() {
		this.b_record.setEnabled(true);
		this.b_record.setBackground(getApplicationContext().getResources()
				.getDrawable(R.drawable.buttonshape));
		this.tv_recording.setVisibility(TextView.INVISIBLE);
		this.tv_info_sanction.setVisibility(TextView.INVISIBLE);
		this.tv_seconds_to_next_recording
				.setText(String.valueOf(SANCTION_TIME));
		this.tv_seconds_to_next_recording.setVisibility(TextView.INVISIBLE);
		this.tv_counter_recording.setText(String.valueOf(RECORDING_TIME));
		this.tv_counter_recording.setVisibility(TextView.INVISIBLE);
	}

	private void updateGUIRecording() {
		this.b_record.setBackground(getApplicationContext().getResources()
				.getDrawable(R.drawable.buttonshapeactive));
		this.tv_recording.setVisibility(TextView.VISIBLE);
		this.tv_info_sanction.setVisibility(TextView.INVISIBLE);
		this.tv_seconds_to_next_recording
				.setText(String.valueOf(SANCTION_TIME));
		this.tv_seconds_to_next_recording.setVisibility(TextView.INVISIBLE);
		this.tv_counter_recording.setText(String.valueOf(RECORDING_TIME));
		this.tv_counter_recording.setVisibility(TextView.VISIBLE);
	}

	protected void startRecording() {
		// TODO Auto-generated method stub
		STATE = RECORDING;
		this.seconds_recorded = 0;
		this.updateGUIRecording();
		this.timer_recording = new Timer(true);
		this.timer_recording.schedule(new RecordingCounterTask(), 1000, 1000);
	}

	private void initVariables() {
		// TODO Auto-generated method stub
		this.STATE = IDLE;
		this.seconds_recorded = 0;
		this.seconds_sanction = 0;

	}

	private void initGuiElements() {
		// TODO Auto-generated method stub
		this.b_record = (Button) findViewById(R.id.Brecord);
		this.b_record.setOnTouchListener(buttonListener);
		this.tv_recording = (TextView) findViewById(R.id.TVRecording);
		this.tv_info_sanction = (TextView) findViewById(R.id.TVInfoSanction);
		this.tv_seconds_to_next_recording = (TextView) findViewById(R.id.TVSecondsToNextRecording);
		this.tv_counter_recording = (TextView) findViewById(R.id.TVCounterRecording);

		this.updateGUIIdle();
	}

	private class SanctionCounterTask extends TimerTask {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			if (seconds_sanction < SANCTION_TIME) {
				seconds_sanction++;
				guiHandler.post(decreaseSecondsSanction);

			} else {
				STATE = IDLE;
				seconds_sanction = 0;
				guiHandler.post(backToIdle);
				timer_sanction.cancel();
				timer_sanction.purge();
				timer_sanction = null;
			}
		}

	}

	private class RecordingCounterTask extends TimerTask {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			if (STATE == RECORDING) {
				if (seconds_recorded < RECORDING_TIME) {
					seconds_recorded++;
					guiHandler.post(decreaseCounter);

				} else {
					seconds_recorded = 0;
					stopRecording(true);
				}
			}

		}
	};

}
