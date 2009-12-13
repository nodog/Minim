package ddf.minim.ugens;
import java.util.ArrayList;

import ddf.minim.ugens.UGen.InputType;
import ddf.minim.ugens.UGen.UGenInput;

public class Sum extends UGen{
	
	/**
	 * basic sum of inputs (aimed at audio but could be used for other purposes
	 * 
	 * 
	 * 
	 * @author nb
	 */
	
	/*
	 * nb :
	 * thoughts : first i wanted to let the user patch any number of ins to the Sum, but as
	 * private ArrayList<UGenInput> uGenInputs;
	 * is private, I can't get the length of it in the uGenerate function.
	 * So for now, the number of ins is specified in the constructor. 
	 * 
	 * 
	 * 
	 * 
	 * PROBLEM : problems when fetching last values...
	 * (NullPointerException)
	 */
	
	
	private UGenInput[] sumIns;
	
	
	
	public Sum(int numberOfIns)
	{
		super();
		sumIns = new UGenInput[numberOfIns];
		for(int i=0; i <numberOfIns ; i++)
		{
			sumIns[i] = new UGenInput();
		}
		
	}
	
	
	
	@Override
	protected void uGenerate(float[] channels) 
	{		

		for(int i=0; i<sumIns.length;i++)
		{
			for(int j =0 ; j<channels.length;j++)
			{
			channels[j]=sumIns[i].getLastValues()[j];
			}
		}
	}

}
