/*
 *  SoundFileViewResponder.java
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
 *		16-Apr-05	created
 */
 
package de.sciss.swingosc;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import de.sciss.io.Span;
import de.sciss.net.OSCMessage;

/**
 *	@author		Hanns Holger Rutz
 *	@version	0.53, 02-Jul-07
 */
public class SoundFileViewResponder
extends AbstractResponder
implements SoundFileView.Listener
{
	private static final Class[] listenerClasses	= { SoundFileView.Listener.class };
	private static final String[] listenerNames		= { "Listener" };
	
	private final Object[] shortReplyArgs = new Object[ 3 ];
	
	public SoundFileViewResponder( Object objectID )
	throws IllegalAccessException, NoSuchMethodException, InvocationTargetException
	{
		super( objectID, 5 );
		add();
		
		shortReplyArgs[ 0 ]	= replyArgs[ 0 ];
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
		return "/soundfile";
	}

	// -------- FocusListener interface --------

	public void cursorChanged( SoundFileView v, long newPosition )
	{
		try {
			// [ "/soundfile", <componentID>, <state>, <x>, <y>, <w>, <h> ]
			shortReplyArgs[ 1 ] = "cursor";
			shortReplyArgs[ 2 ] = new Long( newPosition );
			client.reply( new OSCMessage( getOSCCommand(), shortReplyArgs ));
		}
		catch( IOException ex ) {
			SwingOSC.printException( ex, getOSCCommand() );
		}
	}

	public void selectionChanged( SoundFileView v, int index, Span newSpan )
	{
		try {
			// [ "/soundfile", <componentID>, <state>, <x>, <y>, <w>, <h> ]
			replyArgs[ 1 ] = "selection";
			replyArgs[ 2 ] = new Integer( index );
			replyArgs[ 3 ] = new Long( newSpan.start );
			replyArgs[ 4 ] = new Long( newSpan.stop );
			client.reply( new OSCMessage( getOSCCommand(), replyArgs ));
		}
		catch( IOException ex ) {
			SwingOSC.printException( ex, getOSCCommand() );
		}
	}
}