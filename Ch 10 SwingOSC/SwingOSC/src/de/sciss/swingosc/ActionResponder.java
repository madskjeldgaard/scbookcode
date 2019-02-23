/*
 *  ActionResponder.java
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
 *		12-Nov-05	created
 *		02-Jul-07	adds modifiers reply arg
 */
 
package de.sciss.swingosc;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;

/**
 *	An <code>ActionResponder</code> is created for one component.
 *	When instantiating, the responder starts to listen to
 *	<code>ActionEvent</code>s fired from that component, until
 *	<code>remove</code> is called. When an event occurs, the
 *	responder will send a <code>/action</code> OSC message to
 *	the client that has created the responder, with the first
 *	argument being the component's ID, followed by the state
 *	change <code>performed</code>, followed by key-value
 *	pairs of properties. The property key names are specified
 *	in the constructor.
 *
 *	@author		Hanns Holger Rutz
 *	@version	0.57, 03-Jan-08
*/
public class ActionResponder
extends AbstractResponder
implements ActionListener
{
	private static final Class[] listenerClasses = { ActionListener.class };
	private static final String[] listenerNames	 = { "ActionListener" };

	public ActionResponder( Object objectID )
	throws IllegalAccessException, NoSuchMethodException, InvocationTargetException
	{
		super( objectID );
	}

	public ActionResponder( Object objectID, String propertyName )
	throws IllegalAccessException, NoSuchMethodException, InvocationTargetException
	{
		super( objectID, propertyName );
	}

	public ActionResponder( Object objectID, Object[] propertyNames )
	throws IllegalAccessException, NoSuchMethodException, InvocationTargetException
	{
		super( objectID, propertyNames );
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
		return "/action";
	}

	// -------- ActionListener interface --------

	public void actionPerformed( ActionEvent e )
	{
//		replyArgs[ 2 ] = new Integer( e.getModifiers() );
		reply( "performed" );
	}
}