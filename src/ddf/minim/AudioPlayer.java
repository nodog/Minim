/*
 *  Copyright (c) 2007 - 2008 by Damien Di Fede <ddf@compartmental.net>
 *
 *   This program is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU Library General Public License as published
 *   by the Free Software Foundation; either version 2 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Library General Public License for more details.
 *
 *   You should have received a copy of the GNU Library General Public
 *   License along with this program; if not, write to the Free Software
 *   Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

package ddf.minim;

import ddf.minim.spi.AudioOut;
import ddf.minim.spi.AudioRecordingStream;

/**
 * An AudioPlayer provides a self-contained way of playing a 
 * sound file by streaming it from disk (or the internet). It
 * provides methods playing and looping the file, as well 
 * as methods for setting the position in the file and 
 * looping a section of the file. You can obtain an 
 * AudioPlayer by using the loadFile method of the Minim 
 * class.
 * 
 * @example Basics/PlayAFile
 * 
 * @related Minim
 * 
 * @author Damien Di Fede
 */

public class AudioPlayer extends AudioSource implements Playable
{
	// the rec that this plays
	private AudioRecordingStream	recording;
	private AudioOut		output;

	/**
	 * Constructs an <code>AudioPlayer</code> that plays <code>recording</code>.
	 * It is expected that <code>recording</code> will have a
	 * <code>DataLine</code> to control. If it doesn't, any calls to
	 * <code>Controller</code>'s methods will result in a
	 * <code>NullPointerException</code>.
	 * 
	 * @param recording
	 *           the <code>AudioRecording</code> to play
	 *           
	 * @invisible
	 */
	public AudioPlayer(AudioRecordingStream recording, AudioOut out)
	{
		super(out);
		this.recording = recording;
		output = out;
		// output.setAudioSignal( new StreamSignal(recording, output.bufferSize()) );
		output.setAudioStream(recording);
	}

   /**
    * Starts playback from the current position. 
    * If this was previously set to loop, looping will be disabled.
    * 
    */
	public void play()
	{
		recording.play();
	}

   /**
    * Starts playback some number of milliseconds into the file. 
    * If this was previously set to loop, looping will be disabled.
    * 
    * @param millis 
    * 			int: how many milliseconds from the beginning of the file to begin playback from 
    */
	public void play(int millis)
	{
		cue(millis);
		play();
	}

	/**
	 * Pauses playback.
	 */
	public void pause()
	{
		recording.pause();
	}

   /**
    * Rewinds to the beginning. This <i>does not</i> stop playback. 
    */
	public void rewind()
	{
		cue(0);
	}

   /**
    * Set the <code>AudioPlayer</code> to loop. 
    * If it is already playing, the position
    * <i>will not</i> be reset to the beginning. 
    * If it is not playing, it will start playing. 
    * To loop indefinitely, use <code>loop()</code>.
    * 
    * @param num
    *          int: the number of times to loop
    */
	public void loop(int num)
	{
		recording.loop(num);
	}
	
   /**
    * Sets the <code>AudioPlayer</code> to loop. 
    * If it is already playing, the position
    * <i>will not</i> be reset to the beginning. 
    * If it is not playing, it will start playing. 
    */
	public void loop()
	{
		recording.loop(Minim.LOOP_CONTINUOUSLY);
	}

   /**
    * Returns the number of loops left to do. 
    * 
    * @return int: the number of loops left
    */
	public int loopCount()
	{
		return recording.getLoopCount();
	}

   /**
    * Returns the length of the sound in milliseconds. If for any reason the 
    * length could not be determined, this will return -1. However, an unknown 
    * length should not impact playback.
    * 
    * @return int: the length of the sound in milliseconds
    */
	public int length()
	{
		return recording.getMillisecondLength();
	}

   /**
    * Returns the current position of the "playhead" in milliseconds
    * (ie how much of the sound has already been played)
    * 
    * @return int: the current position of the "playhead" in milliseconds
    */
	public int position()
	{
		return recording.getMillisecondPosition();
	}

   /**
    * Sets the position to <code>millis</code> milliseconds from
    * the beginning. This will not change the playstate. If an error
    * occurs while trying to cue, the position will not change. 
    * If you try to cue to a negative position or to a position 
    * that is greater than <code>length()</code>, the amount will be clamped 
    * to zero or <code>length()</code>.
    * 
    * @param millis 
    * 			int: the millisecond position to place the "playhead"
    */
	public void cue(int millis)
	{
		if (millis < 0)
    {
			millis = 0;
    }
    else if (millis > length())
    {
			millis = length();
    }
		recording.setMillisecondPosition(millis);
	}

	  /**
	   * Skips <code>millis</code> milliseconds from the current position. 
	   * <code>millis</code> can be negative, which will make this skip backwards. 
	   * If the skip amount would result in a negative position or a position that is greater than 
	   * <code>length()</code>, the new position will be clamped to zero or 
	   * <code>length()</code>.
	   * 
	   * @param millis 
	   * 			int: how many milliseconds to skip, sign indicates direction
	   */
	public void skip(int millis)
	{
		int pos = position() + millis;
		if (pos < 0)
    {
			pos = 0;
    }
		else if (pos > length())
    {
			pos = length();
    }
    Minim.debug("AudioPlayer.skip: skipping " + millis + " milliseconds, new position is " + pos);
		recording.setMillisecondPosition(pos);
	}

   /**
    * Returns true if the <code>AudioPlayer</code> is currently playing 
    * and has more than one loop left to play.
    * 
    * @return true if this is looping, false if not
    */
	public boolean isLooping()
	{
		return recording.getLoopCount() != 0;
	}

   /**
    * Indicates if the <code>AudioPlayer</code> is currently playing.
    * 
    * @return true if this is currently playing, false if not
    */
	public boolean isPlaying()
	{
		return recording.isPlaying();
	}

	/**
	 * Returns the meta data for the recording being played by this player.
	 * 
	 * @return the meta data for this player's recording
	 */
	public AudioMetaData getMetaData()
	{
		return recording.getMetaData();
	}

   /**
    * Sets the loop points used when looping.
    * 
    * @param start 
    * 		int: the start of the loop in milliseconds
    * @param stop 
    * 		int: the end of the loop in milliseconds
    */
	public void setLoopPoints(int start, int stop)
	{
		recording.setLoopPoints(start, stop);

	}
	
	/**
	 * Release the resources associated with playing this file.
	 */
	public void close()
	{
		recording.close();
		super.close();
	}
}
