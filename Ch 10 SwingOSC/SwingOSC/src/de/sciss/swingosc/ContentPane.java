/*
 *  ContentPane.java
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
 *		15-Jan-08	created
 */
package de.sciss.swingosc;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.Icon;
import javax.swing.JPanel;

/**
 *	@author		Hanns Holger Rutz
 *	@version	0.62, 13-Jul-09
 */
public class ContentPane
extends JPanel // JComponent
{
	private Color	colrBg;
	private Icon	icon				= null;

	public ContentPane( boolean resizeActive )
	{
		super( new ColliderLayout( resizeActive ));
		setOpaque( true );
		this.putClientProperty( "origin", Boolean.TRUE ); // detected by Pen
//		System.out.println( "isFocusable? " + this.isFocusable() );
//		setFocusable( true );
	}

	public void setIcon( Icon icon )
	{
		this.icon = icon;
		repaint();
	}
	
	public Icon getIcon()
	{
		return icon;
	}
	
	public void paintComponent( Graphics g )
	{
		super.paintComponent( g );
		
		if( (colrBg != null) && (colrBg.getAlpha() > 0) ) {
			g.setColor( colrBg );
			g.fillRect( 0, 0, getWidth(), getHeight() );
		}
		
		if( icon != null ) {
			icon.paintIcon( this, g, 0, 0 );
		}
	}
	
//	public boolean isOptimizedDrawingEnabled() { return false; }
	
//	public void repaintIcon()
//	{
//		repaint();
//	}

	public void setBackground( Color c )
	{
		colrBg = c;
		repaint();
	}
}
