package ddf.minim.ugens;



public abstract class Filter extends UGen
{
	
	/*Basic filter UGen implementing a biquadratic transfer function.
	 * Possibility of choosing between HP, LP, BP and notch.
	 * For now, the cutting frequency fc and the quality factor Q are chosen
	 * when the filter is initialized.
	 * I guess later fc and Q will be ins.
	 * For now the filter is supposed to process a mono stream.
	 * */
	 
	//coefficients of the filter

	protected float[] a;
	protected float[] b;//maybe this one could be of dimension 2
	//samples in the filter
	private float[] x;
	private float[] y;
	
	float fc, Q;

	
	public Filter(float centerfreq, float QualityFactor)
	{
			fc=centerfreq;
			Q=QualityFactor;
	}
	
	
	
	
	/*
	 * nb : All coefficients are computed in sampleRateChanged because at constructor time
	 * sampleRate is still undefined (=zero). 
	 * */

	@Override
	protected void sampleRateChanged()
	{
		calcCoeff();
	    initArrays();
	}
	
	
	public void changeFreq(float f)
	{
		fc=f;
		calcCoeff();
	}
	
	
	
	
	//coefficients will be computed in the different filters subclasses
	protected abstract void calcCoeff();
	
	
	@Override
	protected void uGenerate(float[] channels) 
	{

	      System.arraycopy(x, 0, x, 1, x.length - 1);
	      x[0] = channels[0];
	      float s = 0;
	      for (int j = 0; j < a.length; j++)
	      {
	        s += a[j] * x[j];
	      }
	      for (int j = 0; j < b.length; j++)
	      {
	        s += b[j] * y[j];
	      }
	      System.arraycopy(y, 0, y, 1, y.length - 1);
	      y[0] = s;
		

		for(int i = 0; i < channels.length; i++)
		{
			channels[i] = y[0];
		}
	}
	
	
	

	
	  final void initArrays()
	  {
	    int memSize = (a.length >= b.length) ? a.length : b.length;
	    x = new float[memSize];
	    y = new float[memSize];

	  }
	
	
	
}


