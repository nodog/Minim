package ddf.minim;

import java.util.ArrayList;
import java.util.Arrays;


/**
 * The UGen class is an abstract class which provides the basis for all
 * UGens in Minim. UGen is short for Unit Generator, which is simply something
 * that either generates a sample value, or transforms the sample value produced by
 * another UGen. Since everything is a UGen, there is a common interface for
 * patching things together. For instance, you might have a line of code that
 * looks like this:
 * 
 * <pre>
 * osc.patch( filter ).patch( adsr ).patch( output );
 * </pre>
 * 
 * You can read this code left to right. It says that the output of an Oscil
 * should be sent through a filter (perhaps a LowPass) and the output of the
 * filter should be sent through an ADSR envelope, which should then be sent to
 * an AudioOutput. It's incredibly clear what the signal path is and it can 
 * be stated concisely.
 * <p>
 * UGens might also have UGenInputs. Oscil, for example, has a UGenInput called
 * <code>frequency</code>. UGenInputs can be patched to, just like UGens, which
 * means you might have a line of code like this:
 * 
 * <pre>
 * line.patch( osc.frequency );
 * </pre>
 * 
 * This says that a Line UGen should control the value of the Oscil's frequency.
 * You may have created a Line that changes it's value from 440 to 880 over 2
 * seconds. The audible result, when you call <code>activate()</code> on the Line, 
 * is that the Oscil will sweep upwards in frequency and then hold there until you activate the
 * Line again. All of this control happens on a sample-by-sample basis, which
 * means (hopefully) no clicks and pops.
 * 
 * @example Basics/SynthesizeSound
 * 
 * @author Damien Di Fede, Anderson Mills
 */
public abstract class UGen
{
	/**
	 * This enum is used to specify the InputType of the UGenInput.
	 * An AUDIO UGenInput will have a last values array that conforms
	 * to the channel count of the UGen that owns it, whereas a CONTROL 
	 * UGenInput will always have only one channel.
	 * 
	 * @author Anderson Mills
	 * @nosuperclasses
	 */
	// jam3: enum is automatically static so it can't be in the nested class
	public enum InputType
	{
		CONTROL, AUDIO
	};

	// ddf: UGen class members are before the UGenInput definition because the
	// UGenInput class
	// refers to some of these. I think it's clearer to see these before reading
	// the
	// UGenInput code.

	// list of UGenInputs connected to this UGen
	private ArrayList<UGenInput>	m_allInputs;

	// last values generated by this UGen
	private float[]					m_lastValues;
	// m_sampleRate of this UGen
	private float					m_sampleRate;
	// number of outputs connected to this UGen
	private int						m_nOutputs;
	// counter for the m_currentTick with respect to the number of Outputs
	private int						m_currentTick;

	/**
	 * A UGenInput represents parameter of the UGen that can be 
	 * controlled by other UGens by patching to it. When not patched,
	 * a UGenInput produces a constant value, which can be changed at 
	 * any time by calling setLastValue.
	 * <p>
	 * A UGenInput will have an InputType of either AUDIO or CONTROL.
	 * An AUDIO input will always have the same number of channels 
	 * as the owning UGen, in other words the length of the array 
	 * returned by getLastValues will have a length equal to 
	 * channel count. A CONTROL input will always have one channel 
	 * and its value can be conveniently queried by calling getLastValue().
	 * 
	 * @example Basics/PatchingAnInput
	 * @author Anderson Mills
	 */
	public final class UGenInput
	{
		private UGen		m_incoming;
		private InputType	m_inputType;
		private float[]		m_lastValues;

		/**
		 * Create a UGenInput with a particular type.
		 * 
		 * @param type the InputType of this UGenInput
		 */
		public UGenInput(InputType type)
		{
			m_inputType = type;
			m_allInputs.add( this );
			// assume one channel. good for controls and mono audio.
			m_lastValues = new float[1];
		}
		
		/**
		 * Create a UGenInput of the specified type with an initial value.
		 * 
		 * @param type 	the InputType of this UGenInput
		 * @param value the initial value used for all last values
		 */
		public UGenInput( InputType type, float value )
		{
			m_inputType = type;
			m_allInputs.add( this );
			m_lastValues = new float[1];
			m_lastValues[0] = value;
		}

		/**
		 * Set the number of channels this input should generate.
		 * This will be called by the owning UGen if this input 
		 * is an AUDIO input.
		 * 
		 * @param numberOfChannels
		 *  		float: how many channels this input should generate
		 */
		public void setChannelCount(int numberOfChannels)
		{
			if ( m_lastValues.length != numberOfChannels )
			{
				// make sure we keep the value we already had when 
				// our channel count changes.
				float val = m_lastValues.length > 0 ? m_lastValues[0] : 0;
				m_lastValues = new float[numberOfChannels];
				Arrays.fill(m_lastValues, val);
			}
			
			// make sure our incoming UGen knows about this
			if ( m_inputType == InputType.AUDIO && m_incoming != null )
			{
				m_incoming.setChannelCount( numberOfChannels );
			}
		}
		
		/**
		 * @return int: how many channels this input generates 
		 */
		public int channelCount()
		{
			return m_lastValues.length;
		}

		/**
		 * @return InputType: either AUDIO or CONTROL
		 */
		public InputType getInputType()
		{
			return m_inputType;
		}

		/**
		 * The outer UGen is the UGen that owns this input.
		 * For instance, calling this on the frequency UGenInput
		 * member of an Oscil will return the Oscil.
		 * 
		 * @return the UGen that owns this UGenInput
		 */
		public UGen getOuterUGen()
		{
			return UGen.this;
		}

		/**
		 * The incoming UGen is the UGen that is patched to 
		 * this UGenInput. When this input is ticked, it 
		 * will tick the incoming UGen and store the result
		 * in its last values.
		 * 
		 * @return the UGen that is patched to this UGenInput
		 */
		public UGen getIncomingUGen()
		{
			return m_incoming;
		}

		/**
		 * This method is called when a UGen is patched to this input.
		 * Typically you will not call this method directly, instead 
		 * using UGen's patch method instead.
		 * 
		 * @param in the UGen being patched to this input
		 */
		public void setIncomingUGen(UGen in)
		{
			m_incoming = in;
			if ( m_incoming != null && m_inputType == InputType.AUDIO )
			{
				m_incoming.setChannelCount( m_lastValues.length );
			}
		}

		/**
		 * @return true if a UGen is patched to this UGenInput
		 */
		public boolean isPatched()
		{
			return ( m_incoming != null );
		}

		/**
		 * @return float[]: the last values generated by this input
		 */
		public float[] getLastValues()
		{
			return m_lastValues;
		}

		/**
		 * Returns the first value in the array of last values. This is meant to
		 * make code that gets values from CONTROL inputs easier to read.
		 * 
		 * @return float: the last value generated by this input
		 */
		// TODO (ddf) change these two to getValue and setValue?
		public float getLastValue()
		{
			return m_lastValues[0];
		}

		/**
		 * Sets all values in the last values array to the provided value. If
		 * you want to set last values in the different channels of this input
		 * to different values, you should use getLastValues to do so. For
		 * example:
		 * 
		 * <pre>
		 * ugen.anInput.getLastValues()[0] = 1.f;
		 * ugen.anInput.getLastValues()[1] = 0.f;
		 * </pre>
		 * 
		 * @param value
		 *            float: the value to set all last values to
		 */
		public void setLastValue(float value)
		{
			for ( int i = 0; i < m_lastValues.length; ++i )
			{
				m_lastValues[i] = value;
			}
		}

		// this will be called by the owning UGen *only* when something is
		// patched to this input.
		void tick()
		{
			if ( m_incoming != null )
			{
				m_incoming.tick( m_lastValues );
			}
		}

		/**
		 * @return the InputType as a string (for debugging)
		 */
		public String getInputTypeAsString()
		{
			String typeLabel = null;
			switch ( m_inputType )
			{
			case AUDIO:
				typeLabel = "AUDIO";
				break;
			case CONTROL:
				typeLabel = "CONTROL";
				break;
			}
			return typeLabel;
		}

		/**
		 * Print information about this UGenInput (for debugging)
		 */
		public void printInput()
		{
			Minim.debug( "UGenInput: " + " signal = " + getInputTypeAsString() + " " + ( m_incoming != null ) );
		}
	} // ends the UGenInput inner class

	/**
	 * Constructor for a UGen.
	 */
	public UGen()
	{
		m_allInputs 	= new ArrayList<UGenInput>();
		m_lastValues 	= new float[0];
		m_nOutputs 		= 0;
		m_currentTick 	= 0;
	}

	/**
	 * Patching a UGen to another UGen, UGenInput, or AudioOutput will 
	 * cause the signal from that UGen to pass through the object it
	 * is patched to. 
	 * 
	 * @example Basics/PatchingAnInput
	 * 
	 * @param connectToUGen
	 *            The UGen to patch to.
	 * @return When patching to a UGen or UGenInput, the UGen being patched to is returned 
	 * 		   so that you can chain patch calls. For example:
	 * 
	 * <pre>
	 * sine.patch( gain ).patch( out );
	 * </pre>
	 */
	// ddf: this is final because we never want people to override it.
	public final UGen patch(UGen connectToUGen)
	{
		setSampleRate( connectToUGen.m_sampleRate );
		// jam3: connecting to a UGen is the same as connecting to it's first
		// input
		connectToUGen.addInput( this );
		// TODO jam3: m_nOutputs should only increase when this chain will be
		// ticked!
		m_nOutputs += 1;
		Minim.debug( "m_nOutputs = " + m_nOutputs );
		return connectToUGen;
	}

	/**
	 * Connect the output of this UGen to a specific UGenInput of a UGen.
	 * 
	 * @param connectToInput
	 * 			The UGenInput to patch to.
	 * @return the UGen that owns connectToInput
	 */
	public final UGen patch(UGenInput connectToInput)
	{
		setSampleRate( connectToInput.getOuterUGen().m_sampleRate );
		connectToInput.setIncomingUGen( this );
		// TODO jam3: m_nOutputs should only increase when this chain will be
		// ticked!
		m_nOutputs += 1;
		Minim.debug( "m_nOutputs = " + m_nOutputs );

		return connectToInput.getOuterUGen();
	}
	
	/**
	 * Patch the output of this UGen to the provided AudioOuput. Doing so will
	 * immediately result in this UGen and all UGens patched into it to begin
	 * generating audio.
	 * 
	 * @param audioOutput
	 *            The AudioOutput you want to connect this UGen to.
	 */
	public final void patch(AudioOutput audioOutput)
	{
		Minim.debug( "Patching " + this + " to the output " + audioOutput + "." );
		setSampleRate( audioOutput.sampleRate() );
		setChannelCount( audioOutput.getFormat().getChannels() );
		patch( audioOutput.bus );
	}

	/**
	 * If you want to do something other than the default behavior when your
	 * UGen is patched to, you can override this method in your derived class.
	 * Summer, for instance, keeps a list of all the UGens that have been
	 * patched to it, so that it can tick them and sum the results when it
	 * uGenerates.
	 * 
	 * @param input
	 */
	// ddf: Protected because users of UGens should never call this directly.
	// Sub-classes can override this to control what happens when something
	// is patched to them. See the Summer class.
	protected void addInput(UGen input)
	{
		// jam3: This default behavior is that the incoming signal will be added
		// to the first input in the m_allInputs list.
		Minim.debug( "UGen addInput called." );
		// TODO change input checking to an Exception?
		if ( m_allInputs.size() > 0 )
		{
			Minim.debug( "Initializing default input on something" );
			this.m_allInputs.get( 0 ).setIncomingUGen( input );
		}
		else
		{
			System.err.println( "Trying to connect to UGen with no default input." );
		}
	}

	/**
	 * Unpatch this UGen from an AudioOutput or other UGen.
	 * This causes this UGen and all UGens patched into it to stop generating audio
	 * if they are not patched to an AudioOuput somewhere else in the chain.
	 * 
	 * @param audioOutput
	 *            The AudioOutput this UGen should be disconnected from.
	 */
	public final void unpatch( AudioOutput audioOutput )
	{
		Minim.debug( "Unpatching " + this + " from the output " + audioOutput + "." );
		unpatch( audioOutput.bus );
	}

	/**
	 * Remove this UGen as the input to the connectToUGen.
	 * 
	 * @param fromUGen
	 * 			The UGen to unpatch from.
	 * 
	 */
	public final void unpatch( UGen fromUGen )
	{
		fromUGen.removeInput( this );
		// TODO m_nOutputs needs to be updated as the converse of patch above.
		m_nOutputs -= 1;
		Minim.debug( "m_nOutputs = " + m_nOutputs );
	}

	/**
	 * If you need to do something specific when something is unpatched from
	 * your UGen, you can override this method.
	 * 
	 * @param input
	 */
	// This currently does nothing, but is overridden in Summer.
	protected void removeInput(UGen input)
	{
		Minim.debug( "UGen removeInput called." );
		// see if any of our ugen inputs currently have input as the incoming
		// ugen
		// set their incoming ugen to null if that's the case
		for ( int i = 0; i < m_allInputs.size(); i++ )
		{
			if ( m_allInputs.get( i ).getIncomingUGen() == input )
			{
				this.m_allInputs.get( i ).setIncomingUGen( null );
			}
		}
	}

	/**
	 * Generates one sample frame for this UGen.
	 * 
	 * @param channels
	 *            An array that represents one sample frame. To generate a mono
	 *            signal, pass an array of length 1, if stereo an array of
	 *            length 2, and so on. How a UGen deals with multi-channel sound
	 *            will be implementation dependent.
	 */
	public final void tick(float[] channels)
	{
		if ( m_nOutputs > 0 )
		{
			// only tick once per sampleframe when multiple outputs
			m_currentTick = ( m_currentTick + 1 ) % ( m_nOutputs );
		}

		if ( 0 == m_currentTick )
		{
			for ( int i = 0; i < m_allInputs.size(); ++i )
			{
				m_allInputs.get( i ).tick();
			}

			// and then uGenerate for this UGen
			uGenerate( channels );

			for( int i = 0; i < channels.length && i < m_lastValues.length; ++i )
			{
				m_lastValues[i] = channels[i];
			}
		}
		else
		{
			for( int i = 0; i < channels.length && i < m_lastValues.length; ++i )
			{
				channels[i] = m_lastValues[i];
			}
		}
	}

	/**
	 * Implement this method when you extend UGen. It will be called when your
	 * UGen needs to generate one sample frame of audio. It is expected that you
	 * will assign values to the array and <em>not</em> simply modify the
	 * existing values. In the case where you write a UGen that takes audio
	 * input and modifies it, the pattern to follow is to have the first
	 * UGenInput you create be your audio input and then in uGenerate you will
	 * use the <code>getLastValues</code> method of your audio UGenInput to
	 * retrieve the audio you want to modify, which you will then modify however
	 * you need to, assigning the result to the values in <code>channels</code>.
	 * 
	 * @param channels
	 *            an array representing one sample frame.
	 */
	protected abstract void uGenerate(float[] channels);

	/**
	 * Return the last values generated by this UGen. This will most often be
	 * used by sub-classes when pulling data from their inputs.
	 * 
	 */
	public final float[] getLastValues()
	{
		return m_lastValues;
	}

	/**
	 * Returns the sample rate of this UGen.
	 */
	public final float sampleRate()
	{
		return m_sampleRate;
	}

	/**
	 * Override this method in your derived class to receive a notification when
	 * the sample rate of your UGen has changed. You might need to do this to
	 * recalculate sample rate dependent values, such as the step size for an
	 * oscillator.
	 * 
	 */
	protected void sampleRateChanged()
	{
		// default implementation does nothing.
	}

	/**
	 * Set the sample rate for this UGen.
	 * 
	 * @param newSampleRate
	 *            the sample rate this UGen should generate at.
	 */
	// ddf: changed this to public because Summer needs to be able to call it
	// on all of its UGens when it has its sample rate set by being connected
	// to an AudioOuput. Realized it's not actually a big deal for people to
	// set the sample rate of any UGen they create whenever they want. In fact,
	// could actually make total sense to want to do this with something playing
	// back a chunk of audio loaded from disk. Made this final because it should
	// never be overridden. If sub-classes need to know about sample rate
	// changes
	// the should override sampleRateChanged()
	public final void setSampleRate(float newSampleRate)
	{
		if ( m_sampleRate != newSampleRate )
		{
			m_sampleRate = newSampleRate;
			sampleRateChanged();

			// these are guaranteed to have an incoming UGen
			// if one doesn't it's probably a bug!
			for ( int i = 0; i < m_allInputs.size(); ++i )
			{
				UGen inputIncoming = m_allInputs.get( i ).getIncomingUGen();
				if ( inputIncoming != null )
				{
					inputIncoming.setSampleRate( newSampleRate );
				}
			}
		}
	}

	/**
	 * Let this UGen know how many channels of audio you will be asking it for.
	 * This will be called automatically when a UGen is patched to an AudioOuput
	 * and propagated to all UGenInputs of type AUDIO.
	 * 
	 * @param numberOfChannels
	 *            how many channels of audio you will be generating with this
	 *            UGen
	 */
	public void setChannelCount(int numberOfChannels)
	{
		for ( int i = 0; i < m_allInputs.size(); ++i )
		{
			UGenInput input = m_allInputs.get( i );
			if ( input.getInputType() == InputType.AUDIO )
			{
				input.setChannelCount( numberOfChannels );
			}
		}
		
		if ( m_lastValues.length != numberOfChannels )
		{
			m_lastValues = new float[numberOfChannels];
			channelCountChanged();
		}
	}
	
	/**
	 * Returns the number of channels this UGen has been configured to generate.
	 */
	public int channelCount() { return m_lastValues.length; }
	
	/**
	 * This method is only called when setChannelCount results in the channel count
	 * of this UGen actually changing. Override this function in
	 * sub-classes of UGen if you need to reconfigure things
	 * when the channel count changes.
	 */
	protected void channelCountChanged() {}

	/**
	 * Prints all inputs connected to this UGen (for debugging)
	 */
	public void printInputs()
	{
		for ( int i = 0; i < m_allInputs.size(); i++ )
		{
			Minim.debug( "m_allInputs " + i + " " );
			if ( m_allInputs.get( i ) == null )
			{
				Minim.debug( "null" );
			}
			else
			{
				m_allInputs.get( i ).printInput();
			}
		}
	}
	
	protected UGenInput addControl()
	{
		return new UGenInput( InputType.CONTROL );
	}
	
	protected UGenInput addControl( float initialValue )
	{
		return new UGenInput( InputType.CONTROL, initialValue );
	}
	
	protected UGenInput addAudio()
	{
		return new UGenInput( InputType.AUDIO );
	}
}
