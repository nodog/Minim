package ddf.minim.ugens;

import ddf.minim.ugens.UGen.InputType;
import ddf.minim.ugens.UGen.UGenInput;



public class Ring extends UGen 
{

	/**
	 * Ring modulator
	 * 
	 */
	public UGenInput audio;
	public UGenInput product;
	
	public Ring()
	{
		product = new UGenInput(InputType.CONTROL);
		audio = new UGenInput(InputType.AUDIO);
	}
	
	
	
	protected void uGenerate(float[] channels) 
	{

		if ((product != null) && (product.isPatched()))
		
		for(int i = 0; i < channels.length; i++)
		{
			channels[i] = 0;
		}

	}
		
}
