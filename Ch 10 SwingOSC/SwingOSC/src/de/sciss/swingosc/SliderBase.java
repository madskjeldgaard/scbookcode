/*
 *  SliderBase.java
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
 *		13-Dec-05	created
 */

package de.sciss.swingosc;

import java.awt.AWTEventMulticaster;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.geom.AffineTransform;
import javax.swing.JComponent;

import de.sciss.gui.AquaFocusBorder;

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
public abstract class SliderBase
extends JComponent
implements FocusListener
{
	private float	stepSize	= 0.0f;
		
	protected static final Color	colrBdT		= new Color( 0, 0, 0, 0x55 );
	protected static final Color	colrBdTSh1	= new Color( 0, 0, 0, 0x23 );
	protected static final Color	colrBdTSh2	= new Color( 0, 0, 0, 0x0F );
	protected static final Color	colrBdLR	= new Color( 0, 0, 0, 0x2E );
	protected static final Color	colrBdLRSh	= new Color( 0, 0, 0, 0x10 );
	protected static final Color	colrBdB		= new Color( 0, 0, 0, 0x19 );
	protected static final Color	colrBg		= new Color( 0, 0, 0, 0x0A );
	
	private	ActionListener al = null;
	
	private Color bg;
	private boolean backgroundPainted	= true;

	protected SliderBase()
	{
		super();
		setFocusable( true );
		setBorder( new AquaFocusBorder() );
		putClientProperty( "insets", getInsets() );
		addFocusListener( this );
		setOpaque( false );
		super.setBackground( new Color( 0, 0, 0, 0 ));
	}

	public void setBackground( Color c )
	{
		bg = c;
		repaint();
	}
	
	public void setBackgroundPainted( boolean b )
	{
		backgroundPainted = b;
		repaint();
	}

	/**
	 *  Frees any resources associated with the
	 *	slider. Call this method when the slider
	 *	is not displayed any more and will not
	 *	be used again.
	 */
	public void dispose()
	{
		 /* empty */ 
	}
	
	/**
	 *	Adjust the grid to which all coordinates
	 *	are snapped.
	 *
	 *	@param	stepSize	the new step size
	 *						which must be in the range
	 *						[0...1], where 0 means no snapping
	 *
	 *	@warning	the current coordinates are not affected by
	 *			calling this method. make sure to adjust them
	 *			manually if required
	 */
	public void setStepSize( float stepSize )
	{
		this.stepSize = stepSize;
	}
	
	/**
	 *	Applies the grid raster to a coordinate
	 *
	 *	@param	n	a coordinate in the range [0...1]
	 *	@return	the coordiante snapped to the nearest grid
	 *			position or the originally coordinate if snapping
	 *			is not used
	 */
	public float snap( float n )
	{
		if( stepSize <= 0f ) {
			return( n );
		} else {
			return( Math.round( n / stepSize ) * stepSize );
		}
	}

	/**
	 *	Registers a new <code>ActionListener</code> with
	 *	the component. <code>ActionEvent</code>s are fired
	 *	when the user adjusts the knob.
	 *
	 *	@param	l	the listener to register
	 *	@synchronization	this method is thread safe
	 */
	public synchronized void addActionListener( ActionListener l )
	{
		al = AWTEventMulticaster.add( al, l );
	}
	
	/**
	 *	Unregisters a <code>ActionListener</code> from
	 *	the component.
	 *
	 *	@param	l	the listener to remove from being notified
	 *	@synchronization	this method is thread safe
	 */
	public synchronized void removeActionListener( ActionListener l )
	{
		al = AWTEventMulticaster.remove( al, l );
	}

	protected void fireActionPerformed()
	{
		final ActionListener l = al;
		if( l != null ) {
			l.actionPerformed( new ActionEvent( this, ActionEvent.ACTION_PERFORMED, null ));
		}
	}
	
	public void paintComponent( Graphics g )
	{
//		super.paintComponent( g );
		
		final Graphics2D		g2		= (Graphics2D) g;
		final Insets			ins 	= getInsets();
		final int				cw		= getWidth() - ins.left - ins.right;
		final int				ch		= getHeight() - ins.top - ins.bottom;
		final AffineTransform atOrig	= g2.getTransform();

		g2.translate( ins.left, ins.top );
		
		if( backgroundPainted ) {
			if( (bg != null) && (bg.getAlpha() > 0) ) {
				g2.setColor( bg );
				g2.fillRect( 0, 0, cw, ch );
			}
	
	//		g2.setColor( getBackground() );
	//		g2.fillRect( 1, 2, cw - 2, ch - 4 );
			g2.setColor( colrBg );
			g2.fillRect( 1, 2, cw - 2, ch - 4 );
			g2.setColor( colrBdT );
	//		g2.drawLine( 0, 0, cw, 0 );
			g2.drawLine( 0, 0, cw - 1, 0 );
			g2.setColor( colrBdTSh1 );
			g2.drawLine( 1, 1, cw - 2, 1 );
			g2.setColor( colrBdTSh2 );
			g2.drawLine( 1, 2, cw - 2, 2 );
			g2.setColor( colrBdB );
			g2.drawLine( 1, ch - 1, cw - 1, ch - 1 );
			g2.setColor( colrBdLR );
			g2.drawLine( 0, 1, 0, ch - 1 );
			g2.drawLine( cw - 1, 1, cw - 1, ch - 1 );
			g2.setColor( colrBdLRSh );
			g2.drawLine( 1, 2, 0, ch - 2 );
			g2.drawLine( cw - 2, 2, cw - 2, ch - 2 );
		}
		paintKnob( g2, cw, ch );
		
		g2.setTransform( atOrig );
	}

	protected abstract void paintKnob( Graphics2D g2, int w, int h );

//	 ---------------- FocusListener interface ----------------

	public void focusGained( FocusEvent e )
	{
		repaint();
	}

	public void focusLost( FocusEvent e )
	{
		repaint();
	}
}