/*
 *  PropertyDebugger.java
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
 *		11-Nov-05	created
 *		25-Mar-06	uses OSCClient
 */
 
package de.sciss.swingosc;

import java.beans.*;
import java.lang.reflect.*;

/**
 *	@author		Hanns Holger Rutz
 *	@version		0.40, 25-Mar-06
 */
public class PropertyDebugger
implements PropertyChangeListener
{
	private final SwingOSC		osc;
	private final Object		objectID;
	private final Object		object;
	private final SwingClient	client;
	private final Method		addMethod, removeMethod;
	private boolean				isListening	= false;

	private static final Class[]	addRemoveTypes	= new Class[] { PropertyChangeListener.class };
	private final Object[]			addRemoveArgs	= new Object[] { this };

	public PropertyDebugger( Object objectID )
	throws IllegalAccessException, NoSuchMethodException, InvocationTargetException
	{
		osc					= SwingOSC.getInstance();
		this.objectID		= objectID;
		client				= osc.getCurrentClient();
		object				= client.getObject( objectID );
		addMethod			= object.getClass().getMethod( "addPropertyChangeListener", addRemoveTypes );
		removeMethod			= object.getClass().getMethod( "removePropertyChangeListener", addRemoveTypes );
//		replyArgs			= new Object[ (propertyNames.length << 1) + 1 ];
//		replyArgs[ 0 ]		= objectID;
		add();
	}
	
	public void add()
	throws IllegalAccessException, InvocationTargetException
	{
		if( !isListening ) {
			addMethod.invoke( object, addRemoveArgs );
			isListening = true;
		}
	}
	
	public void remove()
	throws IllegalAccessException, InvocationTargetException
	{
		if( isListening ) {
			removeMethod.invoke( object, addRemoveArgs );
			isListening = false;
		}
	}
	
	public void propertyChange( PropertyChangeEvent e )
	{
		System.out.println( "PropertyChange for Component " + objectID +
			": key = " + e.getPropertyName() + "; new value = " + e.getNewValue() );
//	
//		try {
//			osc.getProperties( object, replyArgs, 1, propertyNames, 0, propertyNames.length );
//			osc.trns.send( new OSCMessage( "/action", replyArgs ), addr );
//		}
//		catch( NoSuchMethodException ex ) {
//			SwingOSC.printException( ex, "/action" );
//		}
//		catch( IllegalAccessException ex ) {
//			SwingOSC.printException( ex, "/action" );
//		}
//		catch( InvocationTargetException ex ) {
//			SwingOSC.printException( ex.getTargetException(), "/action" );
//		}
//		catch( IOException ex ) {
//			SwingOSC.printException( ex, "/action" );
//		}
	}
}