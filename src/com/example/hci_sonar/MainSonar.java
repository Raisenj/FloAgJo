package com.example.hci_sonar;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.io.FileUtils;
import org.kobjects.base64.Base64;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

public class MainSonar extends Activity {

	private static final int RECORDER_BPP = 16;
	private static final String AUDIO_RECORDER_FILE_EXT_WAV = ".wav";
	private static final String AUDIO_RECORDER_TEMP_FILE = "record_temp.raw";
	private static final int RECORDER_SAMPLERATE = 44100;
	private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_STEREO;
	private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
	private static final String AUDIO_RECORDER_FOLDER = "AudioRecorder";

	private static final int RECORDING_TIME = 5;
	private static final int SANCTION_TIME = 20;

	private static final int IDLE = 99;
	private static final int RECORDING = 98;
	private static final int SANCTIONED = 97;
	private static final String AUDIOFILENAME = Environment
			.getExternalStorageDirectory().getAbsolutePath()
			+ "/audiorecordtest";

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

	private MediaRecorder mRecorder = null;
	private AudioRecord recorder;
	private int bufferSize = 0;
	private Thread recordingThread = null;
	private Thread processSignalT = null;

	private ProgressDialog processingSignal;

	public final static String URL = "http://147.83.200.117:8080/HCI_DetectSonar/services/SonarDetection?wsdl";
	public static final String NAMESPACE = "http://detect.hci";
	public static final String SOAP_ACTION_PREFIX = "/";
	private static final String METHOD = "checkForPattern";

	private AsyncTaskRunner runner;

	private Runnable changeGUIToSanction = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub

			updateGUISanctioned();
		}

	};

	private Runnable showBonus = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			Dialog bonusDialog = new Dialog(MainSonar.this);
			bonusDialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
			bonusDialog.setContentView(getLayoutInflater().inflate(
					R.layout.bonus_layout, null));
			bonusDialog.setCancelable(true);
			bonusDialog.show();
		}

	};

	private Runnable showFail = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			Dialog bonusDialog = new Dialog(MainSonar.this);
			bonusDialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
			bonusDialog.setContentView(getLayoutInflater().inflate(
					R.layout.fail_layout, null));
			bonusDialog.setCancelable(true);
			bonusDialog.show();
		}

	};

	private Runnable processSignalR = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			boolean patternFound = false;
			patternFound = searchForPattern();
			processingSignal.dismiss();
			processingSignal.cancel();
			processingSignal = null;
			if (patternFound) {
				guiHandler.post(showBonus);

			} else {
				guiHandler.post(showFail);
			}

		}

		private boolean searchForPattern() {
			// TODO Auto-generated method stub
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return false;
		}
	};

	private Runnable backToIdle = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			updateGUIIdle();
		}

	};

	private Runnable decreaseCounter = new Runnable() {

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

	protected void stopRecordingAudio2() {

		Log.d("FILTERED", "is recording false");
		if (null != recorder) {
			Log.d("FILTERED", "is recording false");

			int i = recorder.getState();
			if (i == 1)
				recorder.stop();
			recorder.release();

			recorder = null;
			recordingThread = null;
		}

		copyWaveFile(getTempFilename(), getFilename());
		deleteTempFile();
	}

	private String getFilename() {
		String filepath = Environment.getExternalStorageDirectory().getPath();
		File file = new File(filepath, AUDIO_RECORDER_FOLDER);

		if (!file.exists()) {
			file.mkdirs();
		}

		// return (file.getAbsolutePath() + "/" + System.currentTimeMillis() +
		// AUDIO_RECORDER_FILE_EXT_WAV);
		Log.d("AAAAAAAA", file.getAbsolutePath() + "/" + "Output"
				+ AUDIO_RECORDER_FILE_EXT_WAV);
		Log.d("AAAAAAAA", file.getAbsolutePath() + "/" + "Output"
				+ AUDIO_RECORDER_FILE_EXT_WAV);
		Log.d("AAAAAAAA", file.getAbsolutePath() + "/" + "Output"
				+ AUDIO_RECORDER_FILE_EXT_WAV);
		Log.d("AAAAAAAA", file.getAbsolutePath() + "/" + "Output"
				+ AUDIO_RECORDER_FILE_EXT_WAV);
		Log.d("AAAAAAAA", file.getAbsolutePath() + "/" + "Output"
				+ AUDIO_RECORDER_FILE_EXT_WAV);
		Log.d("AAAAAAAA", file.getAbsolutePath() + "/" + "Output"
				+ AUDIO_RECORDER_FILE_EXT_WAV);
		return (file.getAbsolutePath() + "/" + "Output" + AUDIO_RECORDER_FILE_EXT_WAV);
	}

	private void deleteTempFile() {
		File file = new File(getTempFilename());

		file.delete();
	}

	private void copyWaveFile(String inFilename, String outFilename) {
		FileInputStream in = null;
		FileOutputStream out = null;
		long totalAudioLen = 0;
		long totalDataLen = totalAudioLen + 36;
		long longSampleRate = RECORDER_SAMPLERATE;
		int channels = 2;
		long byteRate = RECORDER_BPP * RECORDER_SAMPLERATE * channels / 8;

		byte[] data = new byte[bufferSize];

		try {
			in = new FileInputStream(inFilename);
			out = new FileOutputStream(outFilename);
			totalAudioLen = in.getChannel().size();
			totalDataLen = totalAudioLen + 36;

			// AppLog.logString("File size: " + totalDataLen);

			WriteWaveFileHeader(out, totalAudioLen, totalDataLen,
					longSampleRate, channels, byteRate);

			while (in.read(data) != -1) {
				out.write(data);
			}

			in.close();
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void WriteWaveFileHeader(FileOutputStream out, long totalAudioLen,
			long totalDataLen, long longSampleRate, int channels, long byteRate)
			throws IOException {

		byte[] header = new byte[44];

		header[0] = 'R'; // RIFF/WAVE header
		header[1] = 'I';
		header[2] = 'F';
		header[3] = 'F';
		header[4] = (byte) (totalDataLen & 0xff);
		header[5] = (byte) ((totalDataLen >> 8) & 0xff);
		header[6] = (byte) ((totalDataLen >> 16) & 0xff);
		header[7] = (byte) ((totalDataLen >> 24) & 0xff);
		header[8] = 'W';
		header[9] = 'A';
		header[10] = 'V';
		header[11] = 'E';
		header[12] = 'f'; // 'fmt ' chunk
		header[13] = 'm';
		header[14] = 't';
		header[15] = ' ';
		header[16] = 16; // 4 bytes: size of 'fmt ' chunk
		header[17] = 0;
		header[18] = 0;
		header[19] = 0;
		header[20] = 1; // format = 1
		header[21] = 0;
		header[22] = (byte) channels;
		header[23] = 0;
		header[24] = (byte) (longSampleRate & 0xff);
		header[25] = (byte) ((longSampleRate >> 8) & 0xff);
		header[26] = (byte) ((longSampleRate >> 16) & 0xff);
		header[27] = (byte) ((longSampleRate >> 24) & 0xff);
		header[28] = (byte) (byteRate & 0xff);
		header[29] = (byte) ((byteRate >> 8) & 0xff);
		header[30] = (byte) ((byteRate >> 16) & 0xff);
		header[31] = (byte) ((byteRate >> 24) & 0xff);
		header[32] = (byte) (2 * 16 / 8); // block align
		header[33] = 0;
		header[34] = RECORDER_BPP; // bits per sample
		header[35] = 0;
		header[36] = 'd';
		header[37] = 'a';
		header[38] = 't';
		header[39] = 'a';
		header[40] = (byte) (totalAudioLen & 0xff);
		header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
		header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
		header[43] = (byte) ((totalAudioLen >> 24) & 0xff);

		out.write(header, 0, 44);
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
		// stopRecordingAudio();

		stopRecordingAudio2();
		processingSignal = ProgressDialog.show(MainSonar.this,
				"Processing Signal", "analizing...");

		// processSignalT = new Thread(processSignalR);
		// processSignalT.start();
		runner = new AsyncTaskRunner();
		runner.execute();
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
		// startRecordingAudio();
		startRecordingAudio2();
	}

	private void startRecordingAudio2() {
		recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
				RECORDER_SAMPLERATE, RECORDER_CHANNELS,
				RECORDER_AUDIO_ENCODING, bufferSize);

		int i = recorder.getState();
		if (i == 1)
			recorder.startRecording();

		recordingThread = new Thread(new Runnable() {

			@Override
			public void run() {
				writeAudioDataToFile();
			}
		}, "AudioRecorder Thread");

		recordingThread.start();

	}

	private String getTempFilename() {
		String filepath = Environment.getExternalStorageDirectory().getPath();
		File file = new File(filepath, Environment
				.getExternalStorageDirectory().getAbsolutePath());

		if (!file.exists()) {
			file.mkdirs();
		}

		File tempFile = new File(filepath, AUDIO_RECORDER_TEMP_FILE);

		if (tempFile.exists())
			tempFile.delete();

		return (file.getAbsolutePath() + "/" + AUDIO_RECORDER_TEMP_FILE);
	}

	private void writeAudioDataToFile() {
		byte data[] = new byte[bufferSize];
		String filename = getTempFilename();
		FileOutputStream os = null;

		try {
			os = new FileOutputStream(filename);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		int read = 0;

		if (null != os) {
			while (STATE == RECORDING) {
				read = recorder.read(data, 0, bufferSize);

				if (AudioRecord.ERROR_INVALID_OPERATION != read) {
					try {
						os.write(data);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}

			try {
				os.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@SuppressWarnings("deprecation")
	private void initVariables() {
		// TODO Auto-generated method stub
		this.STATE = IDLE;
		this.seconds_recorded = 0;
		this.seconds_sanction = 0;

		bufferSize = AudioRecord.getMinBufferSize(8000,
				AudioFormat.CHANNEL_CONFIGURATION_MONO,
				AudioFormat.ENCODING_PCM_16BIT);

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

	private class AsyncTaskRunner extends AsyncTask<String, String, String> {

		private String resp;

		@Override
		protected String doInBackground(String... params) {
			publishProgress("Loading contents..."); // Calls onProgressUpdate()
			try {
				// SoapEnvelop.VER11 is SOAP Version 1.1 constant
				SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
						SoapEnvelope.VER11);
				SoapObject request = new SoapObject(NAMESPACE, METHOD);
				// bodyOut is the body object to be sent out with this envelope
				File sdcard = Environment.getExternalStorageDirectory();
				File file = new File(getFilename());
				// File file = new File(sdcard, "Audiotesten1.wav");

				byte[] bytes = FileUtils.readFileToByteArray(file);

				String encoded = Base64.encode(bytes);// .encode(bytes);//.encodeBase64String(bytes);

				PropertyInfo pa = new PropertyInfo();
				pa.setName("a");
				pa.setValue(encoded);
				request.addProperty(pa);

				envelope.bodyOut = request;
				HttpTransportSE transport = new HttpTransportSE(URL);
				try {
					transport.call(NAMESPACE + SOAP_ACTION_PREFIX + METHOD,
							envelope);
				} catch (IOException e) {
					e.printStackTrace();
				} catch (XmlPullParserException e) {
					e.printStackTrace();
				}
				// bodyIn is the body object received with this envelope
				if (envelope.bodyIn != null) {
					// getProperty() Returns a specific property at a certain
					// index.
					SoapPrimitive resultSOAP = (SoapPrimitive) ((SoapObject) envelope.bodyIn)
							.getProperty(0);
					resp = resultSOAP.toString();
				}
			} catch (Exception e) {
				e.printStackTrace();
				resp = e.getMessage();
			}
			return resp;
		}

		/**
		 * 
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		@Override
		protected void onPostExecute(String result) {
			// execution of result of Long time consuming operation
			// In this example it is the return value from the web service
			// textView.setText(result);
			processingSignal.dismiss();
			processingSignal.cancel();
			processingSignal = null;
			if (result == null) {
				guiHandler.post(showFail);
			} else if (result.equalsIgnoreCase("wrong pattern")) {
				guiHandler.post(showFail);
			} else if (result.equalsIgnoreCase("right pattern")) {
				guiHandler.post(showBonus);
			}
		}
	}

	/**
	 * 
	 * @see android.os.AsyncTask#onPreExecute()
	 */
	protected void onPreExecute() {
		// Things to be done before execution of long running operation. For
		// example showing ProgessDialog
		processingSignal = ProgressDialog.show(MainSonar.this,
				"Processing Signal", "analizing...");

	}

	/**
	 * 
	 * @see android.os.AsyncTask#onProgressUpdate(Progress[])
	 */
	protected void onProgressUpdate(String... text) {
		// textView.setText(text[0]);
		// Things to be done while execution of long running operation is in
		// progress. For example updating ProgessDialog
	}
}
