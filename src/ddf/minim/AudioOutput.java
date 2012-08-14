/*
 *  Copyright (c) 2007 - 2008 by Damien Di Fede <ddf@compartmental.net>
 *
 *   This program is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU Library General Public License as published
 *   by the Free Software Foundation; either version 2 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Library General Public License for more details.
 *
 *   You should have received a copy of the GNU Library General Public
 *   License along with this program; if not, write to the Free Software
 *   Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

package ddf.minim;

import ddf.minim.spi.AudioOut;
import ddf.minim.ugens.DefaultInstrument;
import ddf.minim.ugens.Frequency;
import ddf.minim.ugens.Instrument;
import ddf.minim.ugens.Summer;

/**
 * An <code>AudioOutput</code> is used to generate audio with
 * <code>AudioSignal</code>s. Well, strictly speaking, the
 * <code>AudioSynthesizer</code> it is constructed with generates the signals
 * and <code>AudioOutput</code> merely delegates to the synth when signals are
 * added. You can get an <code>AudioOutput</code> from <code>Minim</code> by
 * calling one of the <code>getLineOut</code> methods.
 * 
 * @author Damien Di Fede
 * 
 */
public class AudioOutput extends AudioSource implements Polyphonic
{
	// the synth attach our signals to
	private AudioOut	synth;
	// the signals added by the user
	private SignalChain	signals;
	// the note manager for this output
	private NoteManager	noteManager;
	// the Bus for UGens used by this output
	Summer bus;

	private class SampleGenerator implements AudioSignal
	{
		public void generate(float[] signal)
		{
			if ( signals.size() > 0 )
			{
				signals.generate( signal );
			}

			float[] tick = new float[1];
			for ( int i = 0; i < signal.length; ++i )
			{
				noteManager.tick();
				bus.tick( tick );
				signal[i] += tick[0];
			}
		}

		public void generate(float[] left, float[] right)
		{
			if ( signals.size() > 0 )
			{
				signals.generate( left, right );
			}

			float[] tick = new float[2];
			for ( int i = 0; i < left.length; ++i )
			{
				noteManager.tick();
				bus.tick( tick );
				left[i] += tick[0];
				right[i] += tick[1];
			}
		}
	}

	/**
	 * Constructs an <code>AudioOutput</code> that will subscribe its buffers to
	 * <code>synthesizer</code> and be able to control the <code>DataLine</code>
	 * the synthesizer uses for output. If the synth does not have an associated
	 * <code>DataLine</code>, then calls to <code>Controller</code>'s methods
	 * will result in a <code>NullPointerException</code>.
	 * 
	 * @param out
	 *            the <code>AudioOut</code> that does most of our work
	 *            
	 * @invisible
	 */
	public AudioOutput(AudioOut out)
	{
		super( out );
		synth = out;
		signals = new SignalChain();
		noteManager = new NoteManager( getFormat().getSampleRate() );
		bus = new Summer();
		// configure it
		bus.setSampleRate( getFormat().getSampleRate() );
		bus.setChannelCount( getFormat().getChannels() );

		synth.setAudioSignal( new SampleGenerator() );
	}

	/** @deprecated */
	public void addSignal(AudioSignal signal)
	{
		signals.add( signal );
	}

	/** @deprecated */
	public AudioSignal getSignal(int i)
	{
		// get i+1 because the bus is signal 0.
		return signals.get( i );
	}

	/** @deprecated */
	public void removeSignal(AudioSignal signal)
	{
		signals.remove( signal );
	}

	/** @deprecated */
	public AudioSignal removeSignal(int i)
	{
		// remove i+1 because the bus is 1
		return signals.remove( i );
	}

	/** @deprecated */
	public void clearSignals()
	{
		signals.clear();
	}

	/** @deprecated */
	public void disableSignal(int i)
	{
		// disable i+1 because the bus is 0
		signals.disable( i );
	}

	/** @deprecated */
	public void disableSignal(AudioSignal signal)
	{
		signals.disable( signal );
	}

	/** @deprecated */
	public void enableSignal(int i)
	{
		signals.enable( i );
	}

	/** @deprecated */
	public void enableSignal(AudioSignal signal)
	{
		signals.enable( signal );
	}

	/** @deprecated */
	public boolean isEnabled(AudioSignal signal)
	{
		return signals.isEnabled( signal );
	}

	/** @deprecated */
	public boolean isSounding()
	{
		for ( int i = 1; i < signals.size(); i++ )
		{
			if ( signals.isEnabled( signals.get( i ) ) )
			{
				return true;
			}
		}
		return false;
	}

	/** @deprecated */
	public void noSound()
	{
		for ( int i = 1; i < signals.size(); i++ )
		{
			signals.disable( i );
		}
	}

	/** @deprecated */
	public int signalCount()
	{
		return signals.size();
	}

	/** @deprecated */
	public void sound()
	{
		for ( int i = 1; i < signals.size(); i++ )
		{
			signals.enable( i );
		}
	}

	/** @deprecated */
	public boolean hasSignal(AudioSignal signal)
	{
		return signals.contains( signal );
	}

	/**
	 * Play a note startTime seconds from now, for the given duration, using the
	 * given instrument.
	 * 
	 * @param startTime
	 * @param duration
	 * @param instrument
	 */
	public void playNote(float startTime, float duration, Instrument instrument)
	{
		noteManager.addEvent( startTime, duration, instrument );
	}

	public void playNote(float startTime, float duration, float hz)
	{
		noteManager.addEvent( startTime, duration, new DefaultInstrument( hz, this ) );
	}

	public void playNote(float startTime, float duration, String pitchName)
	{
		noteManager.addEvent( startTime, duration, new DefaultInstrument( Frequency.ofPitch( pitchName ).asHz(), this ) );
	}

	public void playNote(float startTime, float hz)
	{
		noteManager
				.addEvent( startTime, 1.0f, new DefaultInstrument( hz, this ) );
	}

	public void playNote(float startTime, String pitchName)
	{
		noteManager.addEvent( startTime, 1.0f, new DefaultInstrument( Frequency.ofPitch( pitchName ).asHz(), this ) );
	}

	public void playNote(float hz)
	{
		noteManager.addEvent( 0.0f, 1.0f, new DefaultInstrument( hz, this ) );
	}

	public void playNote(String pitchName)
	{
		noteManager.addEvent( 0.0f, 1.0f, new DefaultInstrument( Frequency.ofPitch( pitchName ).asHz(), this ) );
	}

	public void playNote()
	{
		noteManager.addEvent( 0.0f, 1.0f, new DefaultInstrument( Frequency.ofPitch( "" ).asHz(), this ) );
	}

	public void setTempo(float tempo)
	{
		noteManager.setTempo( tempo );
	}

	public void setNoteOffset(float noteOffset)
	{
		noteManager.setNoteOffset( noteOffset );
	}

	public void setDurationFactor(float durationFactor)
	{
		noteManager.setDurationFactor( durationFactor );
	}

	public void pauseNotes()
	{
		noteManager.pause();
	}

	public void resumeNotes()
	{
		noteManager.resume();
	}

}
