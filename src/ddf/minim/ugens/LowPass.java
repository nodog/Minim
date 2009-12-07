package ddf.minim.ugens;

public class LowPass extends Filter
{

		public LowPass(float fc, float Q) 
		{
			super(fc, Q);
		}
	
		protected void calcCoeff()
		{
			    float fracFreq = fc/sampleRate;
				  float x = (float)Math.exp(-2*Math.PI*fracFreq);
				  a = new float[] { 1 - x };
				  b = new float[] { x };
			  /*
		    float freqFrac = fc/sampleRate;
		    float x = (float) Math.exp(-14.445 * freqFrac);
		    a = new float[] { (float) Math.pow(1 - x, 4) };
		    b = new float[] { 4 * x, -6 * x * x, 4 * x * x * x, -x * x * x * x };
		    */
			  //TODO better formula with a proper Q.
			  
		}
}
