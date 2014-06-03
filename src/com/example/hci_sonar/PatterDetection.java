package com.example.hci_sonar;

import java.util.ArrayList;

import android.util.Log;
import be.hogent.tarsos.dsp.MicrophoneAudioDispatcher;
import be.hogent.tarsos.dsp.filters.HighPass;
import be.hogent.tarsos.dsp.pitch.Goertzel;
import be.hogent.tarsos.dsp.pitch.Goertzel.FrequenciesDetectedHandler;
import be.hogent.tarsos.dsp.pitch.PitchDetectionResult;

public class PatterDetection {

	public static boolean hasPattern(String pattern) {

		Log.d("HCI-EEEEEE", "\n eieieieieiei");
		// my testfile
		// File file = new File("/home/florian/workspace/Sonar/signal3.wav"); //
		// expected
		// System.out.println(AudioSystem.getAudioFileTypes().);
		// AudioInputStream inputStream =
		// AudioSystem.getAudioInputStream(inputFile);
		MicrophoneAudioDispatcher dispatcher = new MicrophoneAudioDispatcher(
				44100, 2048, 1024);

		// here i cut away the frequencies below 18000 Hz
		dispatcher.addAudioProcessor(new HighPass(18000, 44100));

		// this line can be deleted, i just store the result after I have cut
		// away the frequencies

		// the frequencies for which i am looking for, the strange thing is
		// that, even though I have cut away everything
		// below 18000 Hz it has 9500. So if there is a a frequency of 19000 Hz
		// a 9500 Hz sound remains. It works .
		double[] frequencies = { 9500 };

		final ArrayList<Boolean> results = new ArrayList<Boolean>();

		// Here the Goertzel object is added to the dispatcher

		dispatcher.addAudioProcessor(new Goertzel(44100, 2048, frequencies,
				new FrequenciesDetectedHandler() {
					@Override
					public void handleDetectedFrequencies(
							final double[] frequencies, final double[] powers,
							final double[] allFrequencies,
							final double allPowers[]) {

						for (double a : frequencies) {
							// System.out.print(a + " ");
							results.add(true);
						}
						Log.d("HCI-EEEEEE", "\n" + results.size());

					}
				}));
		PitchProcessor pitch = new PitchProcessor(44100, 2048);
		dispatcher.addAudioProcessor(pitch);
		(new Thread(dispatcher)).start();

		if (pattern.equals(createCode(pitch.getResults()))) {
			return true;
		}

		return false;

	}

	private static String createCode(ArrayList<PitchDetectionResult> resultList) {
		int current = 0;
		String output = "";
		for (PitchDetectionResult pitch : resultList) {
			if (pitch.getPitch() == -1.0) {
				if (current == 1) {
					output += "0";
					current = 0;

				}
			} else if (pitch.getPitch() > 0) {
				if (current == 0) {
					output += "1";
					current = 1;
				}
			}
		}
		return output;
	}

}
