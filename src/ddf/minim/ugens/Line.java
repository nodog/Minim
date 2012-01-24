package ddf.minim.ugens;

import java.util.Arrays;

import ddf.minim.Minim;
import ddf.minim.UGen;

/**
 * A UGen that starts at a value
 * and changes linearly to another value over a specified time.
 * @author nodog
 *
 */
public class Line extends UGen
{
	// jam3: define the inputs to Oscil
	// the initial amplitude
	private float begAmp;
	// the ending amplitude
	private float endAmp;
	// the current amplitude
	private float amp;
	// the time from begAmp to endAmp
	private float lineTime;
	// the current size of the step
	private float timeStepSize;
	// the current time
	private float lineNow;
	// the damp has been activated
	private boolean isActivated;
	
	/**
	 * Constructs a Line that starts a 1 and transitions to 0 over 1 second.
	 */
	public Line()
	{
		this(1.0f, 1.0f, 0.0f);
	}
	
	/**
	 * Constructs a Line that starts at 1 and transtions to 0 over dT seconds.
	 * 
	 * @param dT how long it should take, in seconds, to transtion from 1 to 0.
	 */
	public Line(float dT)
	{
		this(dT, 1.0f, 0.0f);
	}
	
	/**
	 * Constructs a Line that starts at beginningAmplitude and transtions to 0 over dT seconds.
	 * 
	 * @param dT how long it should take, in seconds, to transition to 0.
	 * @param beginningAmplitude what value to begin at.
	 */
	public Line(float dT, float beginningAmplitude)
	{
		this(dT, beginningAmplitude, 0.0f);
	}
	
	/**
	 * Constructs a Line that starts at begAmplitude and transitions to endAmplitude over dT seconds.
	 * 
	 * @param dT how long it should take, in seconds, to transition
	 * @param begAmplitude the value to start at
	 * @param endAmplitude the value to end at
	 */
	public Line(float dT, float begAmplitude, float endAmplitude)
	{
		super();
		lineTime = dT;
		begAmp = begAmplitude;
		amp = begAmp;
		endAmp = endAmplitude;
		lineNow = 0f;
		isActivated = false;
		Minim.debug(" dampTime = " + lineTime + " begAmp = " + begAmp + " now = " + lineNow);
	}
	
	/**
	 * Start the Line's transition.
	 *
	 */
	public void activate()
	{
		lineNow = 0f;
		amp = begAmp;
		isActivated = true;
	}
	
	public void activate( float duration, float beginAmp, float endingAmp )
	{
		begAmp = beginAmp;
		endAmp = endingAmp;
		lineTime = duration;
		activate();
	}
	
	/**
	 * Has the line completed its lerp.
	 */
	public boolean isAtEnd()
	{
		return (lineNow >= lineTime);
	}
	
	/**
	 * Set the ending value of the Line's transition
	 *
	 * @param newEndAmp
	 */
	public void setEndAmp( float newEndAmp )
	{
		endAmp = newEndAmp;
	}
	
	/**
	 * Set the length of this Line's transition
	 * @param newLineTime the new transition time (in seconds)
	 */
	public void setLineTime( float newLineTime )
	{
		lineTime = newLineTime;
	}
	
	/**
	 * Change the timeStepSize when sampleRate changes.
	 */
	@Override
	protected void sampleRateChanged()
	{
		timeStepSize = 1/sampleRate();
	}
	
	/**
	 * Generate one sampleframe for this UGen.
	 */
	@Override
	protected void uGenerate(float[] channels) 
	{
		//Minim.debug(" dampTime = " + dampTime + " begAmp = " + begAmp + " now = " + now);
		if (!isActivated)
		{
			Arrays.fill( channels, begAmp );
		} 
		else if (lineNow >= lineTime)
		{
			Arrays.fill( channels, endAmp );
		} 
		else 
		{
			amp += ( endAmp - amp )*timeStepSize/( lineTime - lineNow );
			//Minim.debug(" dampTime = " + dampTime + " begAmp = " + begAmp + " amp = " + amp + " dampNow = " + dampNow);
			Arrays.fill( channels, amp );
			lineNow += timeStepSize;
		}
	}
}