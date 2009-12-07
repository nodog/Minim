package ddf.minim.ugens;

public class LowpassRes2 extends UGen {
	
	private float K;
	private float cutoff;
	private float Q;
	private float B0,B1,B2,B3,B4,B5,A1A0,A2A0,A4A3,A5A3;
	private float State0,State1,State2,State3,Stage1;
	
	
	
	
	public LowpassRes2(float fc, float qu)
	{
		cutoff=fc;
		Q=qu;
	}
	
	
	public void calc()
	{
	
	
	 K = (float)Math.tan(Math.PI * cutoff /sampleRate); 
 
	
	float a = 0.76536686473f * Q * K; 
	float b = 1.84775906502f * Q * K; 

	K = K*K; //(to optimize it a little bit) 

	//Calculate the first biquad: 

	float A0 = (K+a+1); 
	float A1 = 2*(1-K); 
	float A2 =(a-K-1); 
	B0 = K; 
	B1 = 2*B0; 
	B2 = B0; 

	//Calculate the second biquad: 

	float A3 = (K+b+1); 
	float A4 = 2*(1-K); 
	float A5 = (b-K-1); 
	B3 = K; 
	B4 = 2*B3; 
	B5 = B3; 

	
	
	
	A1A0 = A1/A0;
	A2A0 = A2/A0;
	A4A3 = A4/A3;
	A5A3 = A5/A3;
	
	
	
	}
	
	@Override
	protected void sampleRateChanged()
	{
	    calc();
	}
	

	public void changeFreqAndRes(float f, float qu)
	{
		cutoff=f;
		Q=qu;
		calc();
	}
	
	
	
	
	
	
	
	
	@Override
	protected void uGenerate(float[] channels) 
	{
		float Input = channels[0];
		float Output;

		//Then calculate the output as follows: 

		Stage1 = B0*Input + State0; 
		State0 = B1*Input + A1A0*Stage1 + State1; 
		State1 = B2*Input + A2A0*Stage1; 

		Output = B3*Stage1 + State2; 
		State2 = B4*Stage1 + A4A3*Output + State2; 
		State3 = B5*Stage1 + A5A3*Output;
	      

		for(int i = 0; i < channels.length; i++)
		{
			channels[i] = Output;
		}
	}
	

}
