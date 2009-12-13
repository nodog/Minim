package ddf.minim.ugens;

import ddf.minim.AudioOutput;

public class Keyboard implements Instrument {
	
	
	/**
	 * 
	 * attempt at merging a large number of Oscils into a single effects chain
	 * 
	 * @author nb
	 * 
	 * 
	 */
	AudioOutput out;
	Oscil [] keys ;//do it with array
	int newoscil=0;
	
	public Keyboard(Waveform waveform, int poly, AudioOutput output)
	{
		out = output;
		keys = new Oscil[poly]; 
		for(int i=0;i<poly;i++)
		{
			keys[i]= new Oscil(100,1,waveform);
		}
	}
	
	
	
	
	/**
	 * Start playing a note.
	 */
	public void noteOn()
	{
		
	}
	
	/**
	 * Stop playing a note.
	 */
	public void noteOff()
	{
		
	}


	
	
	
	
	
	
	
	
	
	
	
	
	protected void uGenerate(float[] channels)
	{
		/*
		for(int i = 0; i < channels.length; i++)
		{
			float a= amount*channels[i];
			channels[i]= (Math.abs(a)> 1f)? Math.signum(a) : shape.value(a/2+0.5f);
			channels[i]/=amount;
		}
		*/
	}

}
