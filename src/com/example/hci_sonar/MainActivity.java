//package com.example.hci_sonar;
//
//import java.io.IOException;
//
//import android.support.v8.app.ActionBarActivity;
//import android.support.v7.app.ActionBar;
//import android.support.v4.app.Fragment;
//import android.content.Context;
//import android.media.MediaPlayer;
//import android.media.MediaRecorder;
//import android.os.Bundle;
//import android.os.Environment;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.Menu;
//import android.view.MenuItem;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.Button;
//import android.widget.LinearLayout;
//import android.widget.Toast;
//import android.os.Build;
//
//public class MainActivity extends ActionBarActivity {
//
//	 private static final String LOG_TAG = "AudioRecordTest";
//	    private static String mFileName = null;
//
//	    private RecordButton mRecordButton = null;
//	    private MediaRecorder mRecorder = null;
//
//	    private PlayButton   mPlayButton = null;
//	    private MediaPlayer   mPlayer = null;
//
//	    private void onRecord(boolean start) {
//	        if (start) {
//	            startRecording();
//	        } else {
//	            stopRecording();
//	        }
//	    }
//
//	    private void onPlay(boolean start) {
//	        if (start) {
//	            startPlaying();
//	        } else {
//	            stopPlaying();
//	        }
//	    }
//
//	    private void startPlaying() {
//	        mPlayer = new MediaPlayer();
//	        try {
//	            mPlayer.setDataSource(mFileName);
//	            mPlayer.prepare();
//	            mPlayer.start();
//	        } catch (IOException e) {
//	            Log.e(LOG_TAG, "prepare() failed");
//	        }
//	    }
//
//	    private void stopPlaying() {
//	        mPlayer.release();
//	        mPlayer = null;
//	    }
//
//	    private void startRecording() {
//	    	Toast t1 = Toast.makeText(getApplicationContext(), "1", Toast.LENGTH_SHORT);
//	    	Toast t2 = Toast.makeText(getApplicationContext(), "2", Toast.LENGTH_SHORT);
//	    	Toast t3 = Toast.makeText(getApplicationContext(), "3", Toast.LENGTH_SHORT);
//	    	t1.show();
//	        mRecorder = new MediaRecorder();
//	        t2.show();
//	        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
//	        t3.show();
//	        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
//	        mRecorder.setOutputFile(mFileName);
//	        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
//
//	        try {
//	            mRecorder.prepare();
//	        } catch (IOException e) {
//	            Log.e(LOG_TAG, "prepare() failed");
//	        }
//
//	        mRecorder.start();
//	    }
//
//	    private void stopRecording() {
//	        mRecorder.stop();
//	        mRecorder.release();
//	        mRecorder = null;
//	    }
//
//	    class RecordButton extends Button {
//	        boolean mStartRecording = true;
//
//	        OnClickListener clicker = new OnClickListener() {
//	            public void onClick(View v) {
//	                onRecord(mStartRecording);
//	                if (mStartRecording) {
//	                    setText("Stop recording");
//	                } else {
//	                    setText("Start recording");
//	                }
//	                mStartRecording = !mStartRecording;
//	            }
//	        };
//
//	        public RecordButton(Context ctx) {
//	            super(ctx);
//	            setText("Start recording");
//	            setOnClickListener(clicker);
//	        }
//	    }
//
//	    class PlayButton extends Button {
//	        boolean mStartPlaying = true;
//
//	        OnClickListener clicker = new OnClickListener() {
//	            public void onClick(View v) {
//	                onPlay(mStartPlaying);
//	                if (mStartPlaying) {
//	                    setText("Stop playing");
//	                } else {
//	                    setText("Start playing");
//	                }
//	                mStartPlaying = !mStartPlaying;
//	            }
//	        };
//
//	        public PlayButton(Context ctx) {
//	            super(ctx);
//	            setText("Start playing");
//	            setOnClickListener(clicker);
//	        }
//	    }
//
//	    public MainActivity() {
//	        mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
//	        mFileName += "/audiorecordtest.3gp";
//	    }
//
//	
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        
//        LinearLayout ll = new LinearLayout(this);
//        mRecordButton = new RecordButton(this);
//        ll.addView(mRecordButton,
//            new LinearLayout.LayoutParams(
//                ViewGroup.LayoutParams.WRAP_CONTENT,
//                ViewGroup.LayoutParams.WRAP_CONTENT,
//                0));
//        mPlayButton = new PlayButton(this);
//        ll.addView(mPlayButton,
//            new LinearLayout.LayoutParams(
//                ViewGroup.LayoutParams.WRAP_CONTENT,
//                ViewGroup.LayoutParams.WRAP_CONTENT,
//                0));
//        setContentView(ll);
////        setContentView(R.layout.activity_main);
////
////        if (savedInstanceState == null) {
////            getSupportFragmentManager().beginTransaction()
////                    .add(R.id.container, new PlaceholderFragment())
////                    .commit();
////        }
//    }
//    
//    
//
//
//    @Override
//	protected void onPause() {
//		// TODO Auto-generated method stub
//		super.onPause();
//        if (mRecorder != null) {
//            mRecorder.release();
//            mRecorder = null;
//        }
//
//        if (mPlayer != null) {
//            mPlayer.release();
//            mPlayer = null;
//        }
//	}
//
//	@Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.main, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//        if (id == R.id.action_settings) {
//            return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }
//
//    /**
//     * A placeholder fragment containing a simple view.
//     */
//    public static class PlaceholderFragment extends Fragment {
//
//        public PlaceholderFragment() {
//        }
//
//        @Override
//        public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                Bundle savedInstanceState) {
//            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
//            return rootView;
//        }
//    }
//
// }
