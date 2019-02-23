/*
 * 	MovieView.java
 *  SwingOSC
 *
 *  Copyright (c) 2005-2009 Hanns Holger Rutz. All rights reserved.
 *
 *	This software is free software; you can redistribute it and/or
 *	modify it under the terms of the GNU General Public License
 *	as published by the Free Software Foundation; either
 *	version 2, june 1991 of the License, or (at your option) any later version.
 *
 *	This software is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *	General Public License for more details.
 *
 *	You should have received a copy of the GNU General Public
 *	License (gpl.txt) along with this software; if not, write to the Free Software
 *	Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *
 *	For further information, please contact Hanns Holger Rutz at
 *	contact@sciss.de
 *
 *
 *  Changelog:
 *		19-Nov-06	created
 */
package de.sciss.swingosc;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import javax.media.Control;
import javax.media.ControllerEvent;
import javax.media.ControllerListener;
import javax.media.Duration;
import javax.media.GainControl;
import javax.media.MediaLocator;
import javax.media.RealizeCompleteEvent;
import javax.media.Time;
import javax.media.bean.playerbean.MediaPlayer;
import javax.media.control.FramePositioningControl;
import javax.media.control.FrameRateControl;
import javax.swing.JPanel;

/**
 * 	@version	0.63, 30-Jul-09
 *	@author		Hanns Holger Rutz
 */
public class MovieView
extends JPanel
implements ControllerListener
{
	private final MediaPlayer 				mp;
	private FramePositioningControl			fpc			= null;
	private FrameRateControl				frc			= null;
	
	private Component						oldSource	= null;
	private final ComponentEventForwarder	cef;
	
	public MovieView()
	{
		super( new BorderLayout(), false );
		setOpaque( true );
		mp = new MediaPlayer();
		mp.setPopupActive( false );
		mp.setFixedAspectRatio( false ); // like cocoa player
		add( mp );
		cef = new ComponentEventForwarder( this );
		mp.addControllerListener( this );
	}

	public void controllerUpdate( ControllerEvent e )
	{
		if( e instanceof RealizeCompleteEvent ) {
			checkCEF();
		}
	}

	private void checkCEF()
	{
		final Component newSource = mp.getVisualComponent();
		if( newSource != oldSource ) {
			if( oldSource != null ) cef.removeSource( oldSource );
			if( newSource != null ) cef.addSource( newSource );
			oldSource = newSource;
		}
	}
	
	public void dispose()
	{
		if( oldSource != null ) cef.removeSource( oldSource );
		mp.removeControllerListener( this );
		mp.stopAndDeallocate();
		mp.close();
	}
	
	public void setMovie( String path )
	throws IOException
	{
		setMovie( new File( path ).toURL() );
	}
	
	public void setMovie( URL path )
	{
		mp.setMediaLocator( new MediaLocator( path ));
		mp.realize();
		fpc = null;
		revalidate();
//		checkCEF();
	}
	
	public void start()
	{
		mp.start();
	}
	
	public void stop()
	{
		mp.stop();
	}
	
	public void setMuted( boolean muted )
	{
		final GainControl gc = mp.getGainControl();
		if( gc != null) {
			gc.setMute( muted );
		}
	}
	
	public void setVolume( float linear )
	{
		final GainControl gc = mp.getGainControl();
		if( gc != null) {
			gc.setLevel( linear );
		}
	}
	
//	public void setPlaySelectionOnly( boolean onOff )
//	{
//		
//	}
	
	private FramePositioningControl getFramePosCtrl()
	{
		if( fpc != null ) return fpc;
		
		final Control[] controls = mp.getControls();
		for( int i = 0; i < controls.length; i++ ) {
			if( controls[ i ] instanceof FramePositioningControl ) {
				fpc = (FramePositioningControl) controls[ i ];
				return fpc;
			}
		}
		return null;
	}
	
	private FrameRateControl getFrameRateCtrl()
	{
		if( frc != null ) return frc;
		
		final Control[] controls = mp.getControls();
		for( int i = 0; i < controls.length; i++ ) {
			if( controls[ i ] instanceof FrameRateControl ) {
				frc = (FrameRateControl) controls[ i ];
				return frc;
			}
		}
		return null;
	}
	
	public void setFixedAspectRatio( boolean onOff )
	{
		mp.setFixedAspectRatio( onOff );
		revalidate();
//		checkCEF();
	}
	
	public void setToPreferredSize( float ratio )
	{
		// mp.isControlPanelVisible();
		final Dimension d = mp.getPreferredSize();
		setSize( (int) (d.width * ratio + 0.5f), (int) (d.height * ratio + 0.5f) );
		revalidate();
//		checkCEF();
	}
	
	public void setRate( float rate )
	{
		mp.setRate( rate );
	}
	
	public void setLoopMode( boolean onOff )
	{
		mp.setPlaybackLoop( onOff );
	}
	
	public void goToBeginning()
	{
		setCurrentTime( 0.0 );
	}
	
	public void stepForward()
	{
		skip( 1 );
	}
	
	public void stepBack()
	{
		skip( -1 );
	}
	
	public void skip( int numFrames )
	{
		final FramePositioningControl fpc = getFramePosCtrl();
		if( fpc != null) {
			fpc.skip( numFrames );
		}
	}
	
	public void goToEnd()
	{
		final Time 		dur 	= mp.getDuration();
		if( (dur != Duration.DURATION_UNBOUNDED) && (dur != Duration.DURATION_UNKNOWN) ) {
			setCurrentTime( dur.getSeconds() );
		}
	}
	
	public void setCurrentTime( double seconds )
	{
//		final boolean running = mp.getState() == Controller.Started;
//		if( running ) mp.stop();
		mp.setMediaTime( new Time( seconds ));
//		if( running ) mp.start();
	}

	public void setCurrentFrame( int frameIdx )
	{
		final FramePositioningControl fpc = getFramePosCtrl();
		if( fpc != null) {
// fucking scheiss don't work
//			fpc.seek( frameIdx );
// this don't work either. returns null
			final Time t = fpc.mapFrameToTime( frameIdx );
//System.err.println( t );
			if( (t != null) && (t != Time.TIME_UNKNOWN) ) {
				mp.setMediaTime( t );
			} else {
// fobs is in fact so sucky, it will never do what JMF defines.
// for example mapTimeToFrame returns 0 instead of FRAME_UNKNOWN
// so it's comletely useless. bloody bastards.
//				final int currentFrame = fpc.mapTimeToFrame( mp.getMediaTime() );
//				if( currentFrame != FramePositioningControl.FRAME_UNKNOWN ) {
//					System.err.println( "currentFrame = "+currentFrame+" so delta is " + (frameIdx - currentFrame) );
//					skip( frameIdx - currentFrame );
//				} else { // ok, try the cheesy way
					final FrameRateControl frc = getFrameRateCtrl();
					if( frc != null ) {
						float fps;
						fps = frc.getPreferredFrameRate();
						if( fps <= 0f ) {
							fps = frc.getFrameRate() / mp.getRate();
						}
						if( fps > 0f ) {  // sucky fobs returns 0f instead of -1f!!!
							System.err.println( "frameIdx " + frameIdx + "; fps = "+fps+ "; frameIdx / fps = "+(frameIdx / fps));
							mp.setMediaTime( new Time( frameIdx / fps ));
						}
					} else {
						System.err.println( "MovieView.setCurrentFrame : N.A.");
					}
//				}
			}
		}
	}
	
//	public void setEditable( boolean onOff )
//	{
//		
//	}
	
	public void setControlPanelVisible( boolean onOff )
	{
//System.err.println( "setControlPanelVisible( " + onOff + ") ");
//		mp.setControlPanelVisible( !onOff );
		mp.setControlPanelVisible( onOff );	// doesn't really show / hide it, thus ...
		mp.getControlPanelComponent().setVisible( onOff ); // ... hmmm, certainly we were not supposed to this
//		mp.invalidate();
//		mp.validate();
		revalidate();
//		checkCEF();
	}
	
//	public void editCopy()
//	{
//		
//	}
	
//	public void editClear()
//	{
//		
//	}
	
//	public void editCut()
//	{
//		
//	}
	
//	public void editPaste()
//	{
//		
//	}
}