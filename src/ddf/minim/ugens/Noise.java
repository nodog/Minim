package ddf.minim.ugens;

public class Noise extends UGen 
{

	@Override
	protected void ugentick(float[] channels) 
	{
		float n = (float)Math.random()*2 - 1;
		for(int i = 0; i < channels.length; i++)
		{
			channels[i] = n;
		}
	}

}