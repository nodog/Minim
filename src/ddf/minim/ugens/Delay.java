package ddf.minim.ugens;

public class Delay extends UGen
{
	
	float[] buffer;
	int limit;
	float decay;
	int j=0;
	
	public Delay(int interval, float dec)
	{
		buffer=new float[interval];
		limit=interval;
		decay=dec;
		
		//optional, as floats are initialized at zero
		for(int i=0;i<limit;i++)
		{
			buffer[i]=0f;
		}
		
	}
	
	/*
	 * Thoughts : every UGen should know the size of channels
	 * Here for the delay, or for the filter, it would be useful to know how
	 * many buffers we need to create.
	 * 
	 * For now it's mono
	 * 
	 * NB
	 * 
	 * 
	 * 
	 * 
	 * 2)
	 * The way noteoff is defined in the current test instrument makes the delay useless if placed before
	 * the gain (noteoff is basically saying gain=0 at the end of the note, which cancels any fadeout)
	 * */
	
	
	protected void uGenerate(float[] channels) 
	{
	
		buffer[j]=decay*channels[0];
		j++;
		j%=limit;
		for(int i = 0; i < channels.length; i++)
		{
			channels[i] += buffer[j];
		}
		
	}
}
