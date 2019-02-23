/*
 *  AbstractResponder.java
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
 *		25-Mar-06	uses OSCClient
 */
 
package de.sciss.swingosc;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import de.sciss.net.OSCMessage;

/**
 *	Superclass of different event responders.
 *	This handles the listener registration / unregistration
 *	using java reflection.
 *
 *	@author		Hanns Holger Rutz
 *	@version	0.40, 25-Mar-06
 *
 *	@todo		could be much more efficent if the getter methods
 *				would only be looked up once!
 */
public abstract class AbstractResponder
{
	protected final SwingOSC		osc;
//	private final Object			objectID;
	protected final Object			object;
	private Object[]				propertyNames;
	protected final SwingClient		client;
	protected final Object[]		replyArgs;
	private final Method[]			addMethods, removeMethods;
	private boolean					isListening	= false;
	private boolean					methodsInit = false;
	
	private final Object[]			addRemoveArgs = new Object[] { this };
	
	protected AbstractResponder( Object objectID )
	throws IllegalAccessException, NoSuchMethodException, InvocationTargetException
	{
		this( objectID, new Object[0] );
	}

	protected AbstractResponder( Object objectID, String propertyName )
	throws IllegalAccessException, NoSuchMethodException, InvocationTargetException
	{
		this( objectID, new Object[] { propertyName });
	}

	protected AbstractResponder( Object objectID, Object[] propertyNames )
	throws IllegalAccessException, NoSuchMethodException, InvocationTargetException
	{
		this( objectID, (propertyNames.length << 1) + 2 );
		this.propertyNames	= propertyNames;
//		this.extraArgs		= extraArgs;
		add();
	}
	
	protected AbstractResponder( Object objectID, int numReplyArgs )
//	throws NoSuchMethodException
	{
		final String[] listenerNames = getListenerNames();
	
		osc					= SwingOSC.getInstance();
		client				= osc.getCurrentClient();
		object				= client.getObject( objectID );
	
		addMethods			= new Method[ listenerNames.length ];
		removeMethods		= new Method[ listenerNames.length ];
//		for( int i = 0; i < listenerClasses.length; i++ ) {
//			types				= new Class[] { listenerClasses[ i ]};
//			addMethods[ i ]		= object.getClass().getMethod( "add" + listenerNames[ i ], types );
//			removeMethods[ i ]	= object.getClass().getMethod( "remove" + listenerNames[ i ], types );
//		}
		
		replyArgs			= new Object[ numReplyArgs ];
		replyArgs[ 0 ]		= objectID;
	}
	
	protected abstract Class[] getListenerClasses();
	protected abstract String[] getListenerNames();
	protected abstract String getOSCCommand();
	
	protected Object getObjectForListener( int index )
	{
		return object;
	}

	public void add()
	throws IllegalAccessException, InvocationTargetException, NoSuchMethodException
	{
		if( !isListening ) {
			if( !methodsInit ) {
				final Class[] listenerClasses	= getListenerClasses();
				final String[] listenerNames	= getListenerNames();
				Class[] types;
				Object o;
				for( int i = 0; i < listenerClasses.length; i++ ) {
					types = new Class[] { listenerClasses[ i ]};
					o = getObjectForListener( i );
					addMethods[ i ]		= o.getClass().getMethod( "add" + listenerNames[ i ], types );
					removeMethods[ i ]	= o.getClass().getMethod( "remove" + listenerNames[ i ], types );
				}
				methodsInit = true;
			}
			for( int i = 0; i < addMethods.length; i++ ) {
				addMethods[ i ].invoke( getObjectForListener( i ), addRemoveArgs );
			}
			isListening = true;
		}
	}
	
	public void remove()
	throws IllegalAccessException, InvocationTargetException
	{
		if( isListening ) {
			for( int i = 0; i < removeMethods.length; i++ ) {
				removeMethods[ i ].invoke( getObjectForListener( i ), addRemoveArgs );
			}
			isListening = false;
		}
	}
	
	protected void reply( String stateName )
	{
		try {
			replyArgs[ 1 ] = stateName;
			osc.getProperties( object, replyArgs, 2, propertyNames, 0, propertyNames.length );
			client.reply( new OSCMessage( getOSCCommand(), replyArgs ));
		}
		catch( NoSuchMethodException ex ) {
			SwingOSC.printException( ex, getOSCCommand() );
		}
		catch( IllegalAccessException ex ) {
			SwingOSC.printException( ex, getOSCCommand() );
		}
		catch( InvocationTargetException ex ) {
			SwingOSC.printException( ex.getTargetException(), getOSCCommand() );
		}
		catch( IOException ex ) {
			SwingOSC.printException( ex, getOSCCommand() );
		}
	}
}