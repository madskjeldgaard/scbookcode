/*
 *  Label.java
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
 *		13-Nov-05	created
 */
 
package de.sciss.swingosc;

import java.awt.Color;
import java.awt.Graphics;
import javax.swing.Icon;
import javax.swing.JLabel;

/**
 *	Extends <code>javax.swing.JLabel</code> with
 *	added support for opaque coloured background.
 *
 *	@author		Hanns Holger Rutz
 *	@version	0.3, 13-Nov-05
 */
public class Label
extends JLabel
{
	public Label( String text, Icon icon, int horizontalAlignment )
	{
		super( text, icon, horizontalAlignment );
		init();
	}
			  
	public Label( String text, int horizontalAlignment )
	{
		super( text, horizontalAlignment );
		init();
	}
			  
	public Label( String text )
	{
		super( text );
		init();
	}

	public Label( Icon image, int horizontalAlignment )
	{
		super( image, horizontalAlignment );
		init();
	}

	public Label( Icon image )
	{
		super( image );
		init();
	}

	public Label()
	{
		super();
		init();
	}
	
	private void init()
	{
		setBackground( new Color( 0, 0, 0, 0 ));
	}
	
	public void setBackground( Color c )
	{
		setOpaque( (c != null) && (c.getAlpha() == 0xFF) );
		super.setBackground( c );
	}
	
	public void paintComponent( Graphics g )
	{
		final Color bg = getBackground();
		if( (bg != null) && (bg.getAlpha() > 0) ) {
			g.setColor( bg );
			g.fillRect( 0, 0, getWidth(), getHeight() );
		}
		super.paintComponent( g );
	}
}