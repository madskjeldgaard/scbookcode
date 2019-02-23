/*
 *  UserView.java
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

//import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;

import de.sciss.gui.AquaFocusBorder;

/**
 *	Extends <code>Label</code> with
 *	added support for focus border
 *
 *	@author		Hanns Holger Rutz
 *	@version	0.62, 17-Jul-09
 */
public class UserView
extends JComponent
implements FocusListener
{
	private boolean			focusBorderVisible	= true;
	private AquaFocusBorder	border;
	private Pen				pen;
	private boolean			clear				= true;
	private boolean			shouldPaintBg		= true;
	
//	private Color bgColr = null;
	
	public UserView( Pen image )
	{
		super();
		init();
		setPen( image );
	}

	public UserView()
	{
		super();
		init();
	}
	
	public void setPen( Pen pen )
	{
		this.pen = pen;
	}
	
	private void init()
	{
		border = new AquaFocusBorder();
		setBorder( border );
		putClientProperty( "insets", getInsets() );
		setFocusable( true );
		addFocusListener( this );
		addMouseListener( new MouseAdapter() {
			public void mousePressed( MouseEvent e )
			{
				if( isFocusable() && isEnabled() ) requestFocus();
			}
		});
		setBackground( new Color( 0, 0, 0, 0 ));
		setOpaque( !clear );
	}
	
	public void setClearOnRefresh( boolean clear )
	{
		this.clear = clear;
		if( clear ) shouldPaintBg = true;
		setOpaque( !clear );
	}
	
	public void clearDrawing()
	{
		shouldPaintBg = true;
		setOpaque( false );
	}
	
//	public void repaintIcon()
//	{
//		if( clear ) {
//			repaint();
//		} else {
//			paintImmediately( 0, 0, getWidth(), getHeight() );
//		}
//	}

	public void setBackground( Color c )
	{
		shouldPaintBg = true;
//		setOpaque( (c != null) && (c.getAlpha() == 0xFF) );
		super.setBackground( c );
//bgColr = c;
//		repaint();
	}

	public void setFocusVisible( boolean b )
	{
		if( b != focusBorderVisible ) {
			focusBorderVisible = b;
			border.setVisible( b );
		}
	}

//private final java.util.Random rnd = new java.util.Random();

	public void paintComponent( Graphics g )
	{
		final Color		bg 		= getBackground();
		final Insets	insets	= getInsets();
		
		if( shouldPaintBg ) {
			super.paintComponent( g );

			if( (bg != null) && (bg.getAlpha() > 0) ) {
				g.setColor( bg );
				g.fillRect( insets.left, insets.top,
					getWidth() - (insets.left + insets.right),
					getHeight() - (insets.top + insets.bottom ));
			}
			if( !clear ) {
				shouldPaintBg = false;
				setOpaque( true );
			}
		}
		if( pen != null ) {
			if( pen.getAbsCoords() ) {
				pen.paintIcon( this, g, 0, 0 );
			} else {
				pen.paintIcon( this, g, insets.left, insets.top );
			}
		}
	}

//	 ---------------- FocusListener interface ----------------

	public void focusGained( FocusEvent e )
	{
		if( focusBorderVisible ) repaint();
	}

	public void focusLost( FocusEvent e )
	{
		if( focusBorderVisible ) repaint();
	}
}