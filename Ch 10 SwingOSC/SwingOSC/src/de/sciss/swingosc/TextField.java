/*
 *  TextField.java
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

import javax.swing.JTextField;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;

/**
 *	@author		Hanns Holger Rutz
 *	@version	0.56, 09-Oct-07
 */
public class TextField
extends JTextField
{
	public TextField()
	{
		super();
	}
	
	public TextField( String text )
	{
		super( text );
	}

	public TextField( int columns )
	{
		super( columns );
	}

	public TextField( String text, int columns )
	{
		super( text, columns );
	}

	public TextField( Document doc, String text, int columns )
	{
		super( doc, text, columns );
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
		final Color colrBg	= getBackground();

		if( (colrBg != null) && (colrBg.getAlpha() > 0) ) {
			g.setColor( colrBg );
			g.fillRect( 0, 0, getWidth(), getHeight() );
		}
		super.paintComponent( g );
	}
}