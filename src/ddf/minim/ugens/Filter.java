package ddf.minim.ugens;

public class Filter extends UGen
{
	
	/*Basic filter UGen implementing a biquadratic transfer function.
	 * Possibility of choosing between HP, LP, BP and notch.
	 * For now, the cutting frequency fc and the quality factor Q are chosen
	 * when the filter is initialized.
	 * I guess later fc and Q will be ins.
	 * For now the filter is supposed to process a mono stream.
	 * */
	 
	//coefficients of the filter

	private float[] b = {1,0f,0f};
	private float[] a = {1,0f,0f};//maybe this one could be of dimension 2
	//samples in the filter
	private float[] x = {0f,0f,0f};
	private float[] y = {0f,0f,0f};
	
	
	public enum its { LP, HP };
	
	
	public Filter(its c, float fc, float Q)
	{
		//TODO translate fc and q into b and a
		
		float X=(float)(Math.exp(-Math.PI*2*fc/44100));
		
		switch(c)
		{
			case LP : 
				b[0]=1-X;
				a[1]=X;
				break;
			case HP :
				b[0]=(1+X)/2;
				b[1]=-(1+X)/2;
				a[1]=X;
				break;
			default :
				break;
		}
		System.out.println("coeff " + b[0] + ", " + b[1] + ", " + b[2]);
	}
	
	
	@Override
	protected void uGenerate(float[] channels) 
	{
		/*The following lines implement :
		 * y(n)=x(n)+b(1)*x(n-1)+b(2)*x(n-2)-a(1)*y(n-1)-a(2)*y(n-2)
		*/
		x[2]=x[1];
		x[1]=x[0];
		x[0]=channels[0];
		y[2]=y[1];
		y[1]=y[0];
		y[0]=b[0]*x[0]+b[1]*x[1]+b[2]*x[2]-a[1]*y[1]-a[2]*y[2];
		
		for(int i = 0; i < channels.length; i++)
		{
			channels[i] = y[0];
		}
	}
}
