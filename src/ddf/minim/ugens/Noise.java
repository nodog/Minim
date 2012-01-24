package ddf.minim.ugens;

import ddf.minim.UGen;


/**
 * Provides a UGen which generates noise.
 * @author Anderson Mills, Damien Di Fede
 *
 */
public class Noise extends UGen 
{
	/**
	 * enumeration used to specify the tint of the noise
	 */
	public enum Tint { WHITE, PINK, RED, BROWN };
	
	/**
	 * Patch to this to control the amplitude of the noise with another UGen.
	 */
	public UGenInput amplitude;
	
	/**
	 * Patch to this to offset the value of the noise by a fixed value. 
	 */
	public UGenInput offset;

	// the type of noise
	private Tint	tint;
	// the last output value
	private float	lastOutput;
	// cutoff frequency for brown/red noise
	private float brownCutoffFreq = 100.0f;
	// alpha filter coefficient for brown/red noise
	private float brownAlpha;
	// amplitude correction for brown noise;
	private float brownAmpCorr = 6.2f;

	/**
	 * Constructor for white noise with an amplitude of 1.0.
	 */
	public Noise()
	{
		this( 1.0f, 0.f, Tint.WHITE );
	}
	/**
	 * Constructor for white noise of the specified amplitude.
	 * @param amplitude
	 */
	public Noise( float amplitude )
	{
		this( amplitude, 0.f, Tint.WHITE ) ;
	}
	/**
	 * Constructor for noise of the specified tint with an amplitude of 1.0.
	 * @param noiseType
	 * 		specifies the tint of the noise: WHITE, PINK, RED, BROWN
	 */
	public Noise( Tint noiseType )
	{
		this( 1.0f, 0.f, noiseType ) ;
	}
	/**
	 * Constructor for noise of a specific tint with a specified amplitude.
	 * @param amplitudeValue
	 * @param noiseType
	 * 		specifies the tint of the noise: WHITE, PINK, RED, BROWN
	 */
	public Noise(float amplitudeValue, Tint noiseType)
	{
		this(amplitudeValue, 0.f, noiseType);
	}
	/**
	 * Constructor for noise of a specific tint with a specified amplitude and offset.
	 * @param amplitudeValue 
	 * @param offsetValue
	 * @param noiseType
	 */
	public Noise(float amplitudeValue, float offsetValue, Tint noiseType)
	{
		amplitude = new UGenInput(InputType.CONTROL);
		amplitude.setLastValue(amplitudeValue);
		offset = new UGenInput(InputType.CONTROL);
		offset.setLastValue(offsetValue);
		lastOutput = 0f;
		tint = noiseType;
		if ( tint == Tint.PINK )
		{
			initPink();
		}
	}
	
	public void setTint( Tint noiseType )
	{
	    if ( tint != noiseType )
	    {
	        if ( noiseType == Tint.PINK )
	        {
	            initPink();
	        }
	        tint = noiseType;
	    }
	}
	
	public final Tint getTint()
	{
	    return tint;
	}
	
	@Override
	protected void sampleRateChanged()
	{
		float dt = 1.0f/sampleRate();
		float RC = 1.0f/( 2.0f*(float)Math.PI*brownCutoffFreq );
		brownAlpha = dt/( RC + dt );
	}
	
	@Override
	protected void uGenerate(float[] channels) 
	{
		// start with our base amplitude
		float outAmp = amplitude.getLastValue();
		
		float n;
		switch (tint) 
		{
		// BROWN is a 1/f^2 spectrum (20db/decade, 6db/octave).
		// There is some disagreement as to whether
		// brown and red are the same, but here they are.
		case BROWN :
		case RED :
			// I admit that I'm using the filter coefficients and 
			// amplitude correction from audacity, a great audio editor.  
			n = outAmp*(2.0f*(float)Math.random() - 1.0f);
			n = brownAlpha*n + ( 1 - brownAlpha )*lastOutput;
			lastOutput = n;
			n *= brownAmpCorr;
			break;
		// PINK noise has a 10db/decade (3db/octave) slope
		case PINK :
			n = outAmp*pink();
			break;
		case WHITE :
		default :
			n = outAmp*(2.0f*(float)Math.random() - 1.0f);
			break;
		}
		n += offset.getLastValue();
		for(int i = 0; i < channels.length; i++)
		{
			channels[i] = n;
		}
	}
	
	// The code below (including comments) is taken directly from ddf's old PinkNoise.java code
	// This is the Voss algorithm for creating pink noise
	private int maxKey, key, range;
	private float whiteValues[];
	private float maxSumEver;

	private void initPink()
	{
	    maxKey = 0x1f;
	    range = 128;
	    maxSumEver = 90;
	    key = 0;
	    whiteValues = new float[6];
	    for (int i = 0; i < 6; i++)
	      whiteValues[i] = ((float) Math.random() * Long.MAX_VALUE) % (range / 6);
	}

	// return a pink noise value
	private float pink()
	{
	  int last_key = key;
	  float sum;

	  key++;
	  if (key > maxKey) key = 0;
	  // Exclusive-Or previous value with current value. This gives
	  // a list of bits that have changed.
	  int diff = last_key ^ key;
	  sum = 0;
	  for (int i = 0; i < 6; i++)
	  {
	    // If bit changed get new random number for corresponding
	    // white_value
	    if ((diff & (1 << i)) != 0)
	    {
	      whiteValues[i] = ((float) Math.random() * Long.MAX_VALUE) % (range / 6);
	    }
	    sum += whiteValues[i];
	  }
	  if (sum > maxSumEver) maxSumEver = sum;
	  sum = 2f * (sum / maxSumEver) - 1f;
	  return sum;
	}
	
}