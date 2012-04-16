/**
  * This sketch demonstrates how to use one of the low pass filters that comes with Minim. <br />
  * Move the mouse right to increase the cutoff frequency, move it to the left to descrease it.
  */

import ddf.minim.*;
import ddf.minim.effects.*;

Minim minim;
AudioPlayer groove;
LowPassSP lpf;

void setup()
{
  size(512, 200, P3D);
  minim = new Minim(this);
  groove = minim.loadFile("groove.mp3");
  groove.loop();
  // make a low pass filter with a cutoff frequency of 100 Hz
  // the second argument is the sample rate of the audio that will be filtered
  // it is required to correctly compute values used by the filter
  lpf = new LowPassSP(100, groove.sampleRate());
  groove.addEffect(lpf);
}

void draw()
{
  background(0);
  stroke(255);
  // we multiply the values returned by get by 50 so we can see the waveform
  for ( int i = 0; i < groove.bufferSize() - 1; i++ )
  {
    float x1 = map(i, 0, groove.bufferSize(), 0, width);
    float x2 = map(i+1, 0, groove.bufferSize(), 0, width);
    line(x1, height/4 - groove.left.get(i)*50, x2, height/4 - groove.left.get(i+1)*50);
    line(x1, 3*height/4 - groove.right.get(i)*50, x2, 3*height/4 - groove.right.get(i+1)*50);
  }
}

void mouseMoved()
{
  // map the mouse position to the range [20, 1000], an arbitrary range of cutoff frequencies
  float cutoff = map(mouseX, 0, width, 20, 1000);
  lpf.setFreq(cutoff);
}

