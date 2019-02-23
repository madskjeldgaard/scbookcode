/*
 *  HyperlinkResponder.java
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
 *		01-Aug-08	created
 */
 
package de.sciss.swingosc;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;

import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import de.sciss.net.OSCMessage;

/**
 *	@author		Hanns Holger Rutz
 *	@version	0.61, 01-Aug-08
 */
public class HyperlinkResponder
extends AbstractResponder
implements HyperlinkListener
{
	private static final Class[] listenerClasses = { HyperlinkListener.class };
	private static final String[] listenerNames	 = { "HyperlinkListener" };

	public HyperlinkResponder( Object objectID  )
	throws IllegalAccessException, NoSuchMethodException, InvocationTargetException
	{
		super( objectID, 4 );
		add();
	}

	protected Class[] getListenerClasses()
	{
		return listenerClasses;
	}
	
	protected String[] getListenerNames()
	{
		return listenerNames;
	}
	
	protected String getOSCCommand()
	{
		return "/hyperlink";
	}
		
	// -------- HyperlinkListener interface --------
	
	public void hyperlinkUpdate( HyperlinkEvent e )
	{
		final String	type	= e.getEventType().toString();
		final URL		url		= e.getURL();
		replyArgs[ 1 ] = type;
		replyArgs[ 2 ] = (url != null) ? url.toString() : "";
		replyArgs[ 3 ] = e.getDescription();
//		reply( type );
		try {
			client.reply( new OSCMessage( getOSCCommand(), replyArgs ));
		}
		catch( IOException ex ) {
			SwingOSC.printException( ex, getOSCCommand() );
		}
	}
}