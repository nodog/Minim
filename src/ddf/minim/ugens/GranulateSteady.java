package ddf.minim.ugens;

import ddf.minim.Minim;

public class GranulateSteady extends UGen
{    
	public UGenInput audio;
	public UGenInput grainLen;
	public UGenInput spaceLen;
	public UGenInput fadeLen;
	//public UGenInput amplitude;
	private Waveform sine = Waves.Sine;
	private boolean insideGrain;
	private float timeSinceGrainStart;
	private float timeSinceGrainStop;
	private float timeStep;
	
	private float grainLength = 0.010f;
	private float spaceLength = 0.020f;
	private float fadeLength = 0.0025f;
	
	public GranulateSteady()
	{
		this( 0.01f, 0.02f, 0.0025f );
	}
	
	public GranulateSteady(float grainLength, float spaceLength, float fadeLength)
	{
		super();
		// jam3: These can't be instantiated until the uGenInputs ArrayList
		//       in the super UGen has been constructed
		//audio = new UGenInput(InputType.AUDIO);
		audio = new UGenInput(InputType.AUDIO);
		grainLen = new UGenInput( InputType.CONTROL );
		spaceLen = new UGenInput( InputType.CONTROL );
		fadeLen = new UGenInput( InputType.CONTROL );
		//amplitude = new UGenInput(InputType.CONTROL);
		//value = gainVal;
		this.grainLength = grainLength;
		this.spaceLength = spaceLength;
		this.fadeLength = fadeLength;
		insideGrain = false;
		timeSinceGrainStart = 0.0f;
		timeSinceGrainStop = 0.0f;
		timeStep = 0.0f;
	}
	
	public void sampleRateChanged()
	{
		timeStep = 1.0f/sampleRate;
	}
	
	private void checkFadeLength()
	{
		if (fadeLength > grainLength/2.0)
		{
			fadeLength = grainLength/2.0f;
		}
	}
	
	@Override
	protected void uGenerate( float[] channels ) 
	{
		
		
		if ( insideGrain )
		{	
			float amp = 1.0f;
			
			// TODO protection for overlapping in and out fades
			if ( timeSinceGrainStart < fadeLength )
			{
				//amp = -0.5f*sine.value( timeSinceGrainStart/( 2.0f*fadeLength ) ) + 0.5f;
				amp = timeSinceGrainStart/fadeLength;
			}
			else if ( timeSinceGrainStart > ( grainLength - fadeLength ) )
			{
				//amp = sine.value( ( grainLength - timeSinceGrainStart )/( 4.0f*fadeLength ) );
				amp = (grainLength - timeSinceGrainStart)/fadeLength;
			}
			
			for(int i = 0; i < channels.length; i++)
			{
				channels[i] = amp*audio.getLastValues()[i];
			}
			timeSinceGrainStart += timeStep;
			if (timeSinceGrainStart > grainLength) 
			{
				timeSinceGrainStop = 0.0f;
				insideGrain = false;
			}
		}
		else
		{
			for(int i = 0; i < channels.length; i++)
			{
				channels[i] = 0.0f;
			}
			timeSinceGrainStop += timeStep;
			// only set the grain values at the beginning of a grain
			if (timeSinceGrainStop > spaceLength)
			{
				timeSinceGrainStart = 0.0f;
				insideGrain = true;
				if ((grainLen != null) && (grainLen.isPatched()))
				{
					grainLength = grainLen.getLastValues()[0];
					checkFadeLength();
				}
				if ((spaceLen != null) && (spaceLen.isPatched()))
				{
					spaceLength = spaceLen.getLastValues()[0];
				}
				if ((fadeLen != null) && (fadeLen.isPatched()))
				{
					fadeLength = fadeLen.getLastValues()[0];
					checkFadeLength();
				}
			}
		}
	} 
}