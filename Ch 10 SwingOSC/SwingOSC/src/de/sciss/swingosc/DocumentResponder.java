/*
 *  DocumentResponder.java
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
 *		08-Feb-07	created
 */
 
package de.sciss.swingosc;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
//import javax.swing.text.Document;

import de.sciss.net.OSCMessage;


/**
 *	@author		Hanns Holger Rutz
 *	@version	0.59, 25-Feb-08
 */
public class DocumentResponder
extends AbstractResponder
implements DocumentListener, CaretListener
{
	private static final Class[] listenerClasses	= { DocumentListener.class, CaretListener.class };
	private static final String[] listenerNames		= { "DocumentListener", "CaretListener" };

	private final Object[] shortReplyArgs = new Object[ 4 ];

	public DocumentResponder( Object objectID )
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
		return "/doc";
	}

	private void reply( String stateName, DocumentEvent e )
	{
		int len = e.getLength();
		int off	= e.getOffset();
		int chunkLen;
		
		try {
			// message size = 40 bytes plus text, so we've go 8192 - 40 = 8152 text chars
			// [ "/doc", <componentID>, <state>, <offset>, <length>, <text> ]
			replyArgs[ 1 ] = stateName;
			do {
				replyArgs[ 2 ] = new Integer( off );
				chunkLen = Math.min( 8152, len );
				replyArgs[ 3 ] = new Integer( chunkLen );
				replyArgs[ 4 ] = e.getDocument().getText( off, chunkLen );
				client.reply( new OSCMessage( getOSCCommand(), replyArgs ));
				off += chunkLen;
				len -= chunkLen;
			}
			while( len > 0 );
		}
		catch( IOException ex ) {
			SwingOSC.printException( ex, getOSCCommand() );
		}
		catch( BadLocationException ex ) {
			SwingOSC.printException( ex, getOSCCommand() );
		}
	}

	private void replyShort( String stateName, DocumentEvent e )
	{
		try {
			// [ "/doc", <componentID>, <state>, <offset>, <length> ]
			shortReplyArgs[ 1 ] = stateName;
			shortReplyArgs[ 2 ] = new Integer( e.getOffset() );
			shortReplyArgs[ 3 ] = new Integer( e.getLength() );
			client.reply( new OSCMessage( getOSCCommand(), shortReplyArgs ));
		}
		catch( IOException ex ) {
			SwingOSC.printException( ex, getOSCCommand() );
		}
	}

	private void replyShort( String stateName, CaretEvent e )
	{
		try {
			// [ "/doc", <componentID>, <state>, <dot>, <mark> ]
			shortReplyArgs[ 1 ] = stateName;
			shortReplyArgs[ 2 ] = new Integer( e.getDot() );
			shortReplyArgs[ 3 ] = new Integer( e.getMark() );
			client.reply( new OSCMessage( getOSCCommand(), shortReplyArgs ));
		}
		catch( IOException ex ) {
			SwingOSC.printException( ex, getOSCCommand() );
		}
	}

	// -------- DocumentListener interface --------

	public void insertUpdate( DocumentEvent e )
	{
		reply( "insert", e );
	}

	public void removeUpdate( DocumentEvent e )
	{
		replyShort( "remove", e );
	}
	
	public void changedUpdate( DocumentEvent e )
	{
		// not transmitted right now
	}

	// -------- CaretListener interface --------

	public void caretUpdate( CaretEvent e )
	{
		replyShort( "caret", e );
	}
}