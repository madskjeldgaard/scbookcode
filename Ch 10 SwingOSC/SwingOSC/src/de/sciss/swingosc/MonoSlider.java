/*
 *  MonoSlider.java
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
 *		28-Oct-06	created from SliderBase
 */

package de.sciss.swingosc;

import java.awt.Color;
import java.awt.Image;
import java.awt.Insets;
import java.awt.MediaTracker;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import javax.swing.event.MouseInputAdapter;

/**
 *	Abstract superclass of range and 2D sliders.
 *	Sliders can be horizontal and/or vertically slideable,
 *	they can have a horizontal or vertical extent.
 *	All values are normalized to the interval [0...1]
 *	where vertical coordinates are flipped (1 = top margin,
 *	0 = bottom margin).
 *
 *	@author		Hanns Holger Rutz
 *	@version	0.59, 25-Feb-08
 */
public abstract class MonoSlider
extends SliderBase
{
	protected float	knobX		= 0.0f;
	protected float	knobY		= 0.0f;
	protected float	knobWidth	= 0.0f;
	protected float	knobHeight	= 0.0f;

	private boolean		hsEnabled;
	private boolean		vsEnabled;
	protected boolean	hrEnabled;
	protected boolean	vrEnabled;
	
	protected static final MultiplyImageFilter fltKnobD =
		new MultiplyImageFilter( 0xFF, 0xFF, 0xFF, 0x7F );
	
	protected Color	colrKnob	= null;
	protected Color	colrKnobD	= null;
	protected final MediumLightImageFilter fltKnob;

	protected MonoSlider()
	{
		super();

		fltKnob			= new MediumLightImageFilter();
		
		final MouseAdapter ma = new MouseAdapter();
		addMouseListener( ma );
		addMouseMotionListener( ma );
}
	
	protected MonoSlider( boolean hsEnabled, boolean vsEnabled,
						  boolean hrEnabled, boolean vrEnabled )
	{
		this();

		this.hsEnabled	= hsEnabled;
		this.vsEnabled	= vsEnabled;
		this.hrEnabled	= hrEnabled;
		this.vrEnabled	= vrEnabled;
	}
	
	/**
	 *	Adjust the horizontal minimum value or position
	 *
	 *	@param	x	the new x or horizontal min value
	 *
	 *	@warning		the value is not checked for valid range;
	 *				be sure that 0 <= x <= 1 - knobWidth;
	 *				be sure to use snap( x ) if a stepSize was
	 *				specified
	 */
	public void setKnobX( float x )
	{
		knobX	= x;
		repaint();
	}

	/**
	 *	Adjust the vertical minimum value or position
	 *
	 *	@param	y	the new y or vertical min value
	 *
	 *	@warning		the value is not checked for valid range;
	 *				be sure that 0 <= y <= 1 - knobHeight;
	 *				be sure to use snap( y ) if a stepSize was
	 *				specified
	 */
	public void setKnobY( float y )
	{
		knobY	= y;
		repaint();
	}
	
	/**
	 *	Adjust the horizontal extent
	 *
	 *	@param	w	the new horizontal knob extent
	 *
	 *	@warning	the value is not checked for valid range;
	 *				be sure that knobX <= w <= 1 - knobX;
	 *				be sure to use snap( w ) if a stepSize was
	 *				specified
	 */
	public void setKnobWidth( float w )
	{
		knobWidth	= w;
		repaint();
	}
	
	/**
	 *	Adjust the vertical extent
	 *
	 *	@param	h	the new vertical knob extent
	 *
	 *	@warning		the value is not checked for valid range;
	 *				be sure that knobY <= h <= 1 - knobY;
	 *				be sure to use snap( h ) if a stepSize was
	 *				specified
	 */
	public void setKnobHeight( float h )
	{
		knobHeight	= h;
		repaint();
	}
	
	/**
	 *	Adjust the colour of the knob or handle.
	 *
	 *	@param	c	the new colour or <code>null</code> to
	 *				have a clear knob. The colour may have
	 *				an alpha component other than 0xFF
	 */
	public void setKnobColor( Color c )
	{
		colrKnob	= c;
		if( c != null ) {
//			if( c.getAlpha() == 0xFF ) {
				fltKnob.setColor( c );
//			} else {
//				final float w = (float) c.getAlpha() / 0xFF;
//				final int add = 0xFF - c.getAlpha();
//				fltKnob.setColor( new Color(	(int) (c.getRed() * w + add),
//											(int) (c.getGreen() * w + add),
//											(int) (c.getBlue() * w + add) ));
//			}
		}
		repaint();
	}
	
	/**
	 *	Queries the horizontal minimum value or position
	 *
	 *	@return	the current x position
	 */
	public float getKnobX()
	{
		return knobX;
	}

	/**
	 *	Queries the vertical minimum value or position
	 *
	 *	@return	the current y position
	 */
	public float getKnobY()
	{
		return knobY;
	}
	
	/**
	 *	Queries the horizontal knob extent
	 *
	 *	@return	the current horizontal width
	 */
	public float getKnobWidth()
	{
		return knobWidth;
	}
	
	/**
	 *	Queries the vertical knob extent
	 *
	 *	@return	the current vertical height
	 */
	public float getKnobHeight()
	{
		return knobHeight;
	}
	
	/**
	 *	Adjusts the horizontal slideability.
	 *	Note: subclasses may have a hardcoded slideability
	 *	and changing it can result in painting artifacts
	 *
	 *	@param	onOff	<code>true</code> to allow the knob to
	 *					be horizontally moved
	 */
	public void setHorizontalSlideEnabled( boolean onOff )
	{
		hsEnabled = onOff;
	}
	
	/**
	 *	Adjusts the vertical slideability
	 *	Note: subclasses may have a hardcoded slideability
	 *	and changing it can result in painting artifacts
	 *
	 *	@param	onOff	<code>true</code> to allow the knob to
	 *					be vertically moved
	 */
	public void setVerticalSlideEnabled( boolean onOff )
	{
		vsEnabled = onOff;
	}
	
	/**
	 *	Queries the horizontal slideability
	 *
	 *	@return	<code>true</code> if the knob can be horizontally moved
	 */
	public boolean getHorizontalSlideEnabled()
	{
		return hsEnabled;
	}
	
	/**
	 *	Queries the vertical slideability
	 *
	 *	@return	<code>true</code> if the knob can be vertically moved
	 */
	public boolean getVerticalSlideEnabled()
	{
		return vsEnabled;
	}
	
	/**
	 *	Adjusts the horizontal resizeability.
	 *	Note: subclasses may have a hardcoded resizeability
	 *	and changing it can result in painting artifacts
	 *
	 *	@param	onOff	<code>true</code> to allow the knob to
	 *					be horizontally resized
	 */
	public void setHorizontalResizeEnabled( boolean onOff )
	{
		hrEnabled = onOff;
	}
	
	/**
	 *	Adjusts the vertical resizeability.
	 *	Note: subclasses may have a hardcoded resizeability
	 *	and changing it can result in painting artifacts
	 *
	 *	@param	onOff	<code>true</code> to allow the knob to
	 *					be vertically resized
	 */
	public void setVerticalResizeEnabled( boolean onOff )
	{
		vrEnabled = onOff;
	}
	
	/**
	 *	Queries the horizontal resizeability
	 *
	 *	@return	<code>true</code> if the knob can be horizontally resized
	 */
	public boolean getHorizontalResizeEnabled()
	{
		return hrEnabled;
	}
	
	/**
	 *	Queries the vertical resizeability
	 *
	 *	@return	<code>true</code> if the knob can be vertically resized
	 */
	public boolean getVerticalResizeEnabled()
	{
		return vrEnabled;
	}
	
	protected void setKnobTo( Point2D p )
	{
		final float x, y;
		
		if( hsEnabled ) {
			x = snap( Math.max( 0f, Math.min( 1.0f, (float) p.getX() )));
		} else {
			x = knobX;
		}
		if( vsEnabled ) {
			y = snap( Math.max( 0f, Math.min( 1.0f, (float) p.getY() )));
		} else {
			y = knobY;
		}
		
		if( (x != knobX) || (y != knobY) ||
			(knobWidth != 0f) || (knobHeight != 0f) ) {

			knobX 		= x;
			knobY 		= y;
			knobWidth	= 0f;
			knobHeight	= 0f;
			repaint();
			fireActionPerformed();
		}
	}
	
	protected void resizeKnobTo( Point2D p, float fixX, float fixY )
	{
		final float resizeX, resizeY;
		final float newKnobX, newKnobY, newKnobW, newKnobH;
		
		if( hsEnabled ) {
			resizeX 	= snap( Math.max( 0f, Math.min( 1.0f, (float) p.getX() )));
		} else {
			resizeX 	= knobX;
			fixX		= knobX;
		}
		if( vsEnabled ) {
			resizeY 	= snap( Math.max( 0f, Math.min( 1.0f, (float) p.getY() )));
		} else {
			resizeY 	= knobY;
			fixY		= knobY;
		}
		
		newKnobX = Math.min( fixX, resizeX );
		newKnobY = Math.min( fixY, resizeY );
		newKnobW = Math.abs( fixX - resizeX );
		newKnobH = Math.abs( fixY - resizeY );
		
		if( (newKnobX != knobX) || (newKnobY != knobY) ||
			(newKnobW != knobWidth) || (newKnobH != knobHeight) ) {

			knobX 		= newKnobX;
			knobY 		= newKnobY;
			knobWidth	= newKnobW;
			knobHeight	= newKnobH;
			repaint();
			fireActionPerformed();
		}
	}
	
	protected void moveKnobTo( float x, float y )
	{
		if( hsEnabled ) {
			x = snap( Math.max( 0f, Math.min( 1.0f - knobWidth, x )));
		} else {
			x = knobX;
		}
		if( vsEnabled ) {
			y = snap( Math.max( 0f, Math.min( 1.0f - knobHeight, y )));
		} else {
			y = knobY;
		}
		
		if( (x != knobX) || (y != knobY) ) {
			knobX 		= x;
			knobY 		= y;
			repaint();
			fireActionPerformed();
		}
	}
		
	protected abstract Insets getValueInsets();
	
	protected Point2D screenToVirtual( Point p )
	{
		final Insets ins = getValueInsets();
		
		return new Point2D.Float( (float) (p.x - ins.left) / (getWidth() - ins.left - ins.right),
								 1.0f - (float) (p.y - ins.top) / (getHeight() - ins.top - ins.bottom) );
	}
	
	protected boolean knobContains( Point2D p )
	{
		return( (!hsEnabled || ((p.getX() >= knobX) && (p.getX() < knobX + knobWidth))) &&
			    (!vsEnabled || ((p.getY() >= knobY) && (p.getY() < knobY + knobHeight)))
		);
	}
	
	protected void waitForImage( Image img )
	{
		final MediaTracker mt = new MediaTracker( this );
		mt.addImage( img, 0 );
		try {
			mt.waitForAll();
		} catch( InterruptedException e1 ) { /* ignored */ }
	}

//	 ---------------- internal classes ----------------

	private class MouseAdapter
	extends MouseInputAdapter
	{
		private boolean 	pressed 	= false;
		private boolean 	moving		= false;
		private float		dragFixX;
		private float		dragFixY;
		
		protected MouseAdapter() { /* empty */ }
		
		public void mousePressed( MouseEvent e )
		{
			if( !isEnabled() ) return;

			requestFocus();

			if( e.isControlDown() ) return;

			pressed = true;
			
			final Point2D vPt = screenToVirtual( e.getPoint() );
				
			if( e.isShiftDown() && (hrEnabled || vrEnabled) ) {
				if( hrEnabled ) {
					if( Math.abs( vPt.getX() - knobX ) <
						Math.abs( knobX + knobWidth - vPt.getX() )) {
						
						dragFixX = knobX + knobWidth;
					} else {
						dragFixX = knobX;
					}
				}
				if( vrEnabled ) {
					if( Math.abs( vPt.getY() - knobY ) <
						Math.abs( knobY + knobHeight - vPt.getY() )) {
						
						dragFixY = knobY + knobHeight;
					} else {
						dragFixY = knobY;
					}
				}
				resizeKnobTo( vPt, dragFixX, dragFixY );
				
			} else if( e.isAltDown() || !(hrEnabled || vrEnabled) ||
				!knobContains( vPt )) {
				
				setKnobTo( vPt );
				dragFixX = knobX;
				dragFixY = knobY;
			} else {
				moving 	= true;
				dragFixX = (float) vPt.getX() - knobX;
				dragFixY = (float) vPt.getY() - knobY;
			}
		}
		
		public void mouseReleased( MouseEvent e )
		{
			pressed 	= false;
			moving		= false;
		}
		
		public void mouseDragged( MouseEvent e )
		{
			if( !pressed || !isEnabled() ) return;
			
			final Point2D vPt = screenToVirtual( e.getPoint() );
			
			if( moving ) {
				moveKnobTo( (float) vPt.getX() - dragFixX, (float) vPt.getY() - dragFixY );
			} else if( hrEnabled || vrEnabled ) {
				resizeKnobTo( vPt, dragFixX, dragFixY );
			} else {
				setKnobTo( vPt );
			}
		}
		
		public void mouseMoved( MouseEvent e )
		{
			// on mac, ctrl+press and moving will
			// not generate mouseDragged messages
			// but mouseMoved instead
			mouseDragged( e );
		}
	}
}