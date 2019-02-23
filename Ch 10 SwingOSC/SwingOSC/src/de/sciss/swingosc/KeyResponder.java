/*
 *  KeyResponder.java
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
 *		24-Dec-05	created
 *		25-Mar-06	uses OSCClient
 */
 
package de.sciss.swingosc;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import de.sciss.net.OSCMessage;


/**
 *	A <code>KeyResponder</code> is created for a <code>java.awt.Component</code>.
 *	When instantiating, the responder starts to listen to
 *	<code>KeyEvent</code>s fired from that component, until
 *	<code>remove</code> is called. When an event occurs, the
 *	responder will send a <code>/key</code> OSC message to
 *	the client that has created the responder, with the following arguments:
 *	<pre>
 *		[ "/key", <componentID>, <state>, <keyCode>, <keyChar>, <modifiers> ]
 *	</pre>
 *	with state = (pressed|released|typed), keyCode the VK_somethin,
 *	keyChar the ASCII character as Unicode int, modifiers an int mask
 *	as in java.awt.event.InputEvent
 *
 *	@author		Hanns Holger Rutz
 *	@version	0.53, 02-Jul-07
 */
public class KeyResponder
extends AbstractResponder
implements KeyListener
{
	private static final Class[] listenerClasses	= { KeyListener.class };
	private static final String[] listenerNames	= { "KeyListener" };
	
	public KeyResponder( Object objectID )
	throws IllegalAccessException, NoSuchMethodException, InvocationTargetException
	{
		super( objectID, 5 );
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
		return "/key";
	}

	private void reply( String stateName, KeyEvent e )
	{
		try {
			// [ "/key", <componentID>, <state>, <keyCode>, <keyChar>, <modifiers> ]
			replyArgs[ 1 ] = stateName;
			replyArgs[ 2 ] = new Integer( e.getKeyCode() );
//			replyArgs[ 3 ] = new Integer( Character.getNumericValue( e.getKeyChar() ));
			replyArgs[ 3 ] = new Integer( e.getKeyChar() );
			replyArgs[ 4 ] = new Integer( e.getModifiers() );
			client.reply( new OSCMessage( getOSCCommand(), replyArgs ));
		}
		catch( IOException ex ) {
			SwingOSC.printException( ex, getOSCCommand() );
		}
	}
		
	// -------- KeyListener interface --------

	public void keyPressed( KeyEvent e )
	{
		reply( "pressed", e );
	}

	public void keyReleased( KeyEvent e )
	{
		reply( "released", e );
	}
	
	public void keyTyped( KeyEvent e )
	{
		reply( "typed", e );
	}
}