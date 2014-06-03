package com.example.hci_sonar;

/*
 *      _______                       _____   _____ _____  
 *     |__   __|                     |  __ \ / ____|  __ \ 
 *        | | __ _ _ __ ___  ___  ___| |  | | (___ | |__) |
 *        | |/ _` | '__/ __|/ _ \/ __| |  | |\___ \|  ___/ 
 *        | | (_| | |  \__ \ (_) \__ \ |__| |____) | |     
 *        |_|\__,_|_|  |___/\___/|___/_____/|_____/|_|     
 *                                                         
 * -----------------------------------------------------------
 *
 *  TarsosDSP is developed by Joren Six at 
 *  The School of Arts,
 *  University College Ghent,
 *  Hoogpoort 64, 9000 Ghent - Belgium
 *  
 * -----------------------------------------------------------
 *
 *  Info: http://tarsos.0110.be/tag/TarsosDSP
 *  Github: https://github.com/JorenSix/TarsosDSP
 *  Releases: http://tarsos.0110.be/releases/TarsosDSP/
 *  
 *  TarsosDSP includes modified source code by various authors,
 *  for credits and info, see README.
 * 
 */

import java.util.ArrayList;

import be.hogent.tarsos.dsp.AudioEvent;
import be.hogent.tarsos.dsp.AudioProcessor;
import be.hogent.tarsos.dsp.pitch.DynamicWavelet;
import be.hogent.tarsos.dsp.pitch.FastYin;
import be.hogent.tarsos.dsp.pitch.McLeodPitchMethod;
import be.hogent.tarsos.dsp.pitch.PitchDetectionResult;
import be.hogent.tarsos.dsp.pitch.Yin;

/**
 * Is responsible to call a pitch estimation algorithm. It also calculates
 * progress. The underlying pitch detection algorithm must implement the
 * {@link PitchDetector} interface.
 * 
 * @author Joren Six
 */
public class PitchProcessor implements AudioProcessor {

	private ArrayList<PitchDetectionResult> results = new ArrayList<PitchDetectionResult>();
	int i = 1;

	/**
	 * A list of pitch estimation algorithms.
	 * 
	 * @author Joren Six
	 */
	public enum PitchEstimationAlgorithm {
		/**
		 * See {@link Yin} for the implementation. Or see <a href=
		 * "http://recherche.ircam.fr/equipes/pcm/cheveign/ps/2002_JASA_YIN_proof.pdf"
		 * >the YIN article</a>.
		 */
		YIN,
		/**
		 * See {@link McLeodPitchMethod}. It is described in the article "<a
		 * href=
		 * "http://miracle.otago.ac.nz/postgrads/tartini/papers/A_Smarter_Way_to_Find_Pitch.pdf"
		 * >A Smarter Way to Find Pitch</a>".
		 */
		MPM,
		/**
		 * A YIN implementation with a faster {@link FastYin} for the
		 * implementation. Or see <a href=
		 * "http://recherche.ircam.fr/equipes/pcm/cheveign/ps/2002_JASA_YIN_proof.pdf"
		 * >the YIN article</a>.
		 */
		FFT_YIN,
		/**
		 * An implementation of a dynamic wavelet pitch detection algorithm (See
		 * {@link DynamicWavelet}), described in a paper by Eric Larson and Ross
		 * Maddox <a href= http://online.physics.uiuc
		 * .edu/courses/phys498pom/NSF_REU_Reports/2005_reu/Real
		 * -Time_Time-Domain_Pitch_Tracking_Using_Wavelets.pdf">"Real-Time
		 * Time-Domain Pitch Tracking Using Wavelets</a>
		 */
		DYNAMIC_WAVELET,
		/**
		 * A pitch extractor that extracts the Average Magnitude Difference
		 * (AMDF) from an audio buffer. This is a good measure of the Pitch (f0)
		 * of a signal.
		 */
		AMDF;

		/**
		 * Returns a new instance of a pitch detector object based on the
		 * provided values.
		 * 
		 * @param sampleRate
		 *            The sample rate of the audio buffer.
		 * @param bufferSize
		 *            The size (in samples) of the audio buffer.
		 * @return A new pitch detector object.
		 */

	};

	/**
	 * The underlying pitch detector;
	 */
	private final PitchDetector detector;

	/**
	 * Initialize a new pitch processor.
	 * 
	 * @param algorithm
	 *            An enum defining the algorithm.
	 * @param sampleRate
	 *            The sample rate of the buffer (Hz).
	 * @param bufferSize
	 *            The size of the buffer in samples.
	 * @param handler
	 *            The handler handles detected pitch.
	 */
	public PitchProcessor(float sampleRate, int bufferSize) {
		detector = new AMDF(sampleRate, bufferSize);

	}

	@Override
	public boolean process(AudioEvent audioEvent) {
		float[] audioFloatBuffer = audioEvent.getFloatBuffer();

		be.hogent.tarsos.dsp.pitch.PitchDetectionResult result = detector
				.getPitch(audioFloatBuffer);

		PitchDetectionResult res2 = new PitchDetectionResult();
		res2.setPitch(result.getPitch());
		res2.setProbability((float) audioEvent.getTimeStamp());// dirty solution
		results.add(res2);
		return true;
	}

	@Override
	public void processingFinished() {
	}

	public ArrayList<PitchDetectionResult> getResults() {
		return results;
	}
}