/*
 *  DragView.java
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
 *		31-Jan-07	created
 */
 
package de.sciss.swingosc;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URL;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.border.Border;

import de.sciss.gui.AquaFocusBorder;

/**
 *	Extends <code>javax.swing.JLabel</code> with
 *	added support for opaque coloured background
 *	and drag-view like borders.
 *
 *	@author		Hanns Holger Rutz
 *	@version	0.59, 25-Feb-08
 */
public class DragView
extends JLabel
implements FocusListener, MouseListener
{
	public static final int TARGET		= 0;
	public static final int SOURCE		= 1;
	public static final int BOTH		= 2;

	private static Image dragImg		= null;
	
	private final int type;
	private Color bg;

	public DragView( int type, String text, Icon icon, int horizontalAlignment )
	{
		super( text, icon, horizontalAlignment );
		this.type = type;
		init();
	}
			  
	public DragView( int type, String text, int horizontalAlignment )
	{
		super( text, horizontalAlignment );
		this.type = type;
		init();
	}
			  
	public DragView( int type, String text )
	{
		super( text );
		this.type = type;
		init();
	}

	public DragView( int type, Icon image, int horizontalAlignment )
	{
		super( image, horizontalAlignment );
		this.type = type;
		init();
	}

	public DragView( int type, Icon image )
	{
		super( image );
		this.type = type;
		init();
	}

	public DragView( int type )
	{
		super();
		this.type = type;
		init();
	}
	
	private void init()
	{
		final Border focusBorder;
		
		if( dragImg == null ) {
			try {
				final URL url = getClass().getResource( "dragviews.png" );
				dragImg = Toolkit.getDefaultToolkit().getImage( url );
				final MediaTracker mt = new MediaTracker( this );
				mt.addImage( dragImg, 0 );
				mt.waitForAll();
			}
			catch( InterruptedException e1 ) { /* ignored */ }
			catch( NullPointerException e1 ) { System.out.println( e1 );}
		}
//		setBackground( new Color( 0, 0, 0, 0 ));
		super.setBackground( new Color( 0, 0, 0, 0 ));
		setFocusable( false );
		focusBorder = new AquaFocusBorder();
		putClientProperty( "insets", focusBorder.getBorderInsets( this ));
		setBorder( BorderFactory.createCompoundBorder( focusBorder,
			BorderFactory.createEmptyBorder( 3, 6, 3, 6 )));
		addFocusListener( this );
		addMouseListener( this );
	}
	
	public void setBackground( Color c )
	{
//		setOpaque( (c != null) && (c.getAlpha() == 0xFF) );
//		super.setBackground( c );
		bg = c;
		repaint();
	}
	
	public void paintComponent( Graphics g )
	{
		final Graphics2D	g2			= (Graphics2D) g;
//		final Color			bg			= getBackground();
		final int			w			= getWidth() - 6;
		final int			dx1			= Math.min( 9, w >> 1 );
		final int			dx2			= w - Math.min( 16, w - dx1 );
		final int			h			= getHeight() - 6;
		final int			dy1			= Math.min( 10, h >> 1 );
		final int			dy2			= h - Math.min( 10, h - dy1 );
		final int			sx1			= type * 33;
		final int			sx2			= sx1 + dx1;
		final int			sx3			= sx1 + 9;
		final int			sx4			= sx3 + Math.min( 8, dx2 - dx1 );
		final int			sx6			= sx1 + 33;
		final int			sx5			= sx6 - (w - dx2);
		final int			sy1			= 10 + Math.min( 8, dy2 - dy1 );
		final int			sy2			= 28 - (h - dy2);
		final Map			origHints	= g2.getRenderingHints();
		
		g2.translate( 3, 3 );
		if( (bg != null) && (bg.getAlpha() > 0) ) {
			g2.setColor( bg );
//			g2.fillRect( 0, 0, getWidth(), getHeight() );
			g2.fillRoundRect( 0, 0, w - 1, h - 1, 8, 8 ); // why -1 ?
		}
		g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF );
		g2.setRenderingHint( RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED );
		g2.drawImage( dragImg, 0, 0, dx1, dy1, sx1, 0, sx2, dy1, this );
		g2.drawImage( dragImg, dx1, 0, dx2, dy1, sx3, 0, sx4, dy1, this );
		g2.drawImage( dragImg, dx2, 0, w, dy1, sx5, 0, sx6, dy1, this );
		g2.drawImage( dragImg, 0, dy1, dx1, dy2, sx1, 10, sx2, sy1, this );
		g2.drawImage( dragImg, dx2, dy1, w, dy2, sx5, 10, sx6, sy1, this );
		g2.drawImage( dragImg, 0, dy2, dx1, h, sx1, 28 - (h - dy2), sx2, 28, this );
		g2.drawImage( dragImg, dx1, dy2, dx2, h, sx3, sy2, sx4, 28, this );
		g2.drawImage( dragImg, dx2, dy2, w, h, sx5, sy2, sx6, 28, this );
		
		g2.translate( -3, -3 );
		g2.setRenderingHints( origHints );
		super.paintComponent( g2 );
	}

//	 ---------------- FocusListener interface ----------------

	public void focusGained( FocusEvent e )
	{
		repaint();
	}

	public void focusLost( FocusEvent e )
	{
		repaint();
	}

	//	 ---------------- MouseListener interface ----------------
	
	public void mousePressed( MouseEvent e )
	{
		if( isFocusable() && isEnabled() ) requestFocus();
	}
	
	public void mouseReleased( MouseEvent e ) { /* ignored */ }
	public void mouseClicked( MouseEvent e ) { /* ignored */ }
	public void mouseEntered( MouseEvent e ) { /* ignored */ }
	public void mouseExited( MouseEvent e ) { /* ignored */ }
}