package ddf.minim.ugens;

public class Oscil extends UGen 
{

	
	// the waveform we will oscillate over
	private Waveform  wave;
	// the frequency at which we will oscillate
	private Frequency freq;
	// the amplitude at which we will oscillate
	private float 	  amp;
	// where we will sample our waveform, moves between [0,1]
	private float step;
	// the step size we will use to advance our step
	private float stepSize;
	
	
	//constructors
	

	
	public Oscil(float frequencyInHertz, float amplitude, Waveform waveform)
	{
		this(Frequency.ofHertz(frequencyInHertz), amplitude, waveform);
	}
	
	
	public Oscil(float frequencyInHertz, float amplitude)
	{
		this(Frequency.ofHertz(frequencyInHertz), amplitude);
	}

	
	//shortcut for building a sine wave
	public Oscil(Frequency frequency, float amplitude)
	{
		this(frequency, amplitude, Waves.Sine);
	}

	

	//standard constructor
	public Oscil(Frequency frequency, float amplitude, Waveform waveform)
	{

		wave = waveform;
		freq = frequency;
		amp = amplitude;
		step = 0f;
	}
	
	
	
	
	public void sampleRateChanged()
	{
		stepSize = freq.asHz() / sampleRate;
	}
	
	@Override
	protected void uGenerate(float[] channels) 
	{
		
		
		
		// figure out our sample value
		float sample = amp * wave.value(step);
		for(int i = 0; i < channels.length; i++)
		{
			channels[i] = sample;
		}
		step += stepSize;
		// make sure we don't exceed 1.0.
		// floor is less expensive than %?
		step -= (float)Math.floor(step);
	}
}
