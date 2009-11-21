package ddf.minim.ugens;

public class Ring extends UGen 
{

	/**
	 * Ring modulator
	 * 
	 */
	
	
	
	
	
	protected void ugentick(float[] channels) 
	{

		for(int i = 0; i < channels.length; i++)
		{
			channels[i] = sample;
		}

}
