package ddf.minim.ugens;

import ddf.minim.AudioOutput;
import ddf.minim.Minim;
import java.util.ArrayList;

public abstract class UGen
{
	// jam3: enum is automatically static so it can't be in the nested class
	public enum InputType {CONTROL, AUDIO};
	
	// jam3: declaring an inner nested class here
	public class UGenInput
	{
		private UGen incoming;
		private InputType inputType;
		
	    UGenInput()
	    {
	    	// jam3: default to audio input
	    	this(InputType.AUDIO);
	    }
	    
	    public UGenInput(InputType it)
	    {
	    	inputType = it;
	    	//try
	    	//{
	    		uGenInputs.add(this);
	    	//} catch (ArrayIndexOutOfBoundsException e) 
	    	//{
	    	 //   System.err.println("Caught ArrayIndexOutOfBoundsException: " 
	    	  //  		+ e.getMessage());
	    	//}
	    }
	    InputType getInputType()
	    {
	    	return inputType;
	    }
	    UGen getOuterUGen()
	    {
	    	return UGen.this;
	    }
	    UGen getIncomingUGen()
	    {
	    	return incoming;
	    }
	    void setIncomingUGen(UGen in)
	    {
	    	incoming = in;
	    }
	    
	    public boolean isPatched()
	    {
	    	return (incoming != null);
	    }
	    
			public float[] getLastValues()
			{
				return getIncomingUGen().getLastValues();
			}
	    
	    String getInputTypeAsString()
	    {
	    	String typeLabel = null;
	    	switch (inputType)
	    	{
	    	case AUDIO :
	    		typeLabel = "AUDIO";
	    		break;
	    	case CONTROL :
	    		typeLabel = "CONTROL";
	    		break;	
	    	}
	    	return typeLabel;
	    }
	    void printInput()
	    {
	    	Minim.debug("UGenInput: " 
	    			+ " signal = " + getInputTypeAsString() + " " 
	    			+ isPatched() );
	    }
	}
	
    private ArrayList<UGenInput> uGenInputs;
	private float[] lastValues;
	protected float sampleRate;
	private int nOutputs;
	private int currentTick;
	
	public UGen()
	{
		uGenInputs = new ArrayList<UGenInput>();
		// TODO How to set length of last values appropriately?
		// jam3: Using "2" here is wrong.  Could make ArrayList and set size with tick?
		lastValues = new float[2];
		nOutputs = 0;
		currentTick = 0;
	}
	
	// TODO describe how this patching stuff works.
	/**
	 * Connect the output of this UGen to the input of connectTo. Doing so will chain these 
	 * two UGens together, causing them to generate sound at the same time when the end of 
	 * chain is patched to an AudioOutput.
	 * 
	 * @param connectTo The UGen to connect to.
	 * @return connectTo is returned so that you can chain patch calls. For example: 
	 * <pre>
	 * sine.patch(gain).patch(out);
	 * </pre>
	 */
	// ddf: this is final because we never want people to override it.
	public final UGen patch(UGen connectToUGen)
	{
		// jam3: connecting to a UGen is the same as connecting to it's "mainAudio" input
		connectToUGen.addInput(this);
		nOutputs += 1;
		System.out.println("nOutputs = " + nOutputs);
		return connectToUGen;
	}
	
	public final UGen patch(UGenInput connectToInput)
	{
		connectToInput.setIncomingUGen(this);
		nOutputs +=1;
		System.out.println("nOutputs = " + nOutputs);
		// TODO setSampleRate(sampleRate);
		return connectToInput.getOuterUGen();
	}
	
	// ddf: Protected because users of UGens should never call this directly.
	//      Sub-classes can override this to control what happens when something
	//      is patched to them. See the Bus class.
	protected void addInput(UGen input)
	{
		// jam3: This default behavior is that the incoming signal will be added
		// 		to some input called "audio" if it exists.
		Minim.debug("UGen addInput called.");
		// TODO change input checking to an Exception?
		if (uGenInputs.size() > 0)
		{
			Minim.debug("Initializing default input on something");	
			this.uGenInputs.get(0).setIncomingUGen(input);
		}  else
		{
			System.err.println("Trying to connect to UGen with no default input.");
		}
	}
	
	/**
	 * Patch the output of this UGen to the provided AudioOuput. Doing so will immediately 
	 * result in this UGen and all UGens patched into it to begin generating audio.
	 * 
	 * @param out The AudioOutput you want to connect this UGen to.
	 */
	public final void patch(AudioOutput output)
	{
		Minim.debug("Patching " + this + " to the output " + output + ".");
		patch(output.bus);
		setSampleRate(output.sampleRate());
	}
	
	/**
	 * Generates one sample frame for this UGen. 
	 * 
	 * @param channels
	 *    An array that represents one sample frame. To generate a mono signal, 
	 *    pass an array of length 1, if stereo an array of length 2, and so on.
	 *    How a UGen deals with multi-channel sound will be implementation dependent.
	 */
	public void tick(float[] channels)
	{
		if (nOutputs > 0)
		{
			currentTick = (currentTick + 1)%(nOutputs);
			//System.out.println("currentTick = " + currentTick
				//	+ " nOutputs = " + nOutputs );
		}
		if (0 == currentTick) 
		{			
			if (uGenInputs.size() > 0)
			{
				for(int i=0; i<uGenInputs.size(); i++)
				{		
					if ((uGenInputs.get(i) != null) && (uGenInputs.get(i).isPatched()))
					{
						float[] tmp;
						switch (uGenInputs.get(i).inputType)
						{
						case CONTROL :
							tmp = new float[1];
							break;
						default : // includes AUDIO
							tmp = new float[channels.length];
							break;
						}
						//float[] tmp = new float[channels.length];
						uGenInputs.get(i).getIncomingUGen().tick(tmp);
					}
				}
			}
		uGenerate(channels);
		//Minim.debug(" ticking : value = " + channels[0]);
		System.arraycopy(channels, 0, lastValues, 0, channels.length);
		}
	}
	
	/**
	 * Implement this method when you extend UGen.
	 * @param channels
	 */
	protected abstract void uGenerate(float[] channels);
		
	float[] getLastValues()
	{
		return lastValues;
	}
	
	/**
	 * Override this method in your derived class to receive a notification
	 * when the sample rate of your UGen has changed. You might need to do 
	 * this to recalculate sample rate dependent values, such as the 
	 * step size for an oscillator.
	 *
	 */
	protected void sampleRateChanged()
	{
		// default implementation does nothing.
	}
	
	/**
	 * Set the sample rate for this UGen.
	 * 
	 * @param newSampleRate the sample rate this UGen should generate at.
	 */
	// ddf: changed this to public because Bus needs to be able to call it
	//      on all of its UGens when it has its sample rate set by being connected 
	//      to an AudioOuput. Realized it's not actually a big deal for people to 
	//      set the sample rate of any UGen they create whenever they want. In fact, 
	//      could actually make total sense to want to do this with something playing 
	//      back a chunk of audio loaded from disk. Made this final because it should 
	//      never be overriden. If sub-classes need to know about sample rate changes 
	//      the should override sampleRateChanged()
	public final void setSampleRate(float newSampleRate)
	{
		if (sampleRate != newSampleRate)
		{
			sampleRate = newSampleRate;
			sampleRateChanged();
		}
		if (uGenInputs.size() > 0)
		{
			for(int i=0; i<uGenInputs.size(); i++)
			{		
				if ((uGenInputs.get(i) != null) && (uGenInputs.get(i).isPatched()))
				{
					uGenInputs.get(i).getIncomingUGen().setSampleRate(newSampleRate);
				}
			}			
		}
	}
	
	public void printInputs()
	{
	   for(int i=0; i<uGenInputs.size(); i++)
	   {
		   Minim.debug("uGenInputs " + i + " ");
		   if (uGenInputs.get(i) == null)
		   {
			   Minim.debug("null");   
		   } else {
			   uGenInputs.get(i).printInput();
		   }
	   }
	}
}
