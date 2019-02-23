/*
 *  NumberField.java
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
 *		02-Mar-07	created
 */
package de.sciss.swingosc;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.event.DocumentListener;

/**
 *	@author		Hanns Holger Rutz
 *	@version	0.57, 25-Nov-07
 */
public class NumberField
extends de.sciss.gui.NumberField
{
	public NumberField()
	{
		super();
	}
	
	public void setNumber( String s )
	{
		setNumber( Double.valueOf( s ));
	}
	
	// this is here to make DocumentResponder less complex
	// (because now it can connect both Caret and Document listeners to the same object)
	// ; this just forwards the request to the Document.
	public void addDocumentListener( DocumentListener l )
	{
//		collDocListeners.add( l );
		getDocument().addDocumentListener( l );
	}

	public void removeDocumentListener( DocumentListener l )
	{
//		collDocListeners.remove( l );
		getDocument().removeDocumentListener( l );
	}

	/**
	 *	Overwritten to toggle the opacity settings
	 *	when background colour is (semi)transparent
	 */
	public void setBackground( Color c )
	{
		setOpaque( (c != null) && (c.getAlpha() == 0xFF) );
		super.setBackground( c );
	}

	public void paintComponent( Graphics g )
	{
//		final Insets		insets	= getInsets();
		final int			w		= getWidth(); // - (insets.left + insets.right);
		final int			h		= getHeight(); // - (insets.top + insets.bottom);
		final Graphics2D	g2	 	= (Graphics2D) g;
		final Color			colrBg	= getBackground();

		if( (colrBg != null) && (colrBg.getAlpha() > 0) ) {
//			g2.translate( insets.left, insets.top );
			g2.setPaint( colrBg );
			g2.fillRect( 0, 0, w, h );
//			g2.translate( -insets.left, -insets.top );
		}
		super.paintComponent( g );
	}
}