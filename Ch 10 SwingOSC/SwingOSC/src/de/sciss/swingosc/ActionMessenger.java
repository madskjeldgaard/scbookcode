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
 *		07-Feb-07	created
 */
 
package de.sciss.swingosc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.awt.event.ActionEvent;

/**
 *	A subclass of ActionResponder for the more complicated
 *	gadgets like MultiSlider etc. Addtionally to querying properties
 *	and sending single values back, it invokes a specified
 *	method on the object it's listening too, assuming that
 *	this method takes care of sending back the values to the
 *	client. This is for example the case with
 *	MultiSlider's sendValuesAndClear.
 * 
 *	@author		Hanns Holger Rutz
 *	@version	0.50, 07-Feb-07
 */
public class ActionMessenger
extends ActionResponder
{
	private final Method	method;
	private final Object[]	methodCArgs;
	
	private static final String kErrorText = "ActionMessenger.actionPerformed";
	
	public ActionMessenger( Object objectID, String methodName, Object[] methodArgs )
	throws NoSuchMethodException, IllegalAccessException, InvocationTargetException
	{
		super( objectID );
		methodCArgs	= new Object[ methodArgs.length ];
		method		= osc.findBestMethod( object, methodName, methodArgs, methodCArgs);
	}

	public ActionMessenger( Object objectID, Object[] propertyNames, String methodName, Object[] methodArgs )
	throws NoSuchMethodException, IllegalAccessException, InvocationTargetException
	{
		super( objectID, propertyNames );
		methodCArgs	= new Object[ methodArgs.length ];
		method		= osc.findBestMethod( object, methodName, methodArgs, methodCArgs);
	}
	
	// -------- ActionListener interface --------

	// extends behaviour of ActionResponder
	public void actionPerformed( ActionEvent e )
	{
		super.actionPerformed( e );
		
		try {
			method.invoke( object, methodCArgs );
		}
		catch( LinkageError e1 ) {
			SwingOSC.printException( e1, kErrorText );
		}
		catch( SecurityException e1 ) {
			SwingOSC.printException( e1, kErrorText );
		}
		catch( IllegalAccessException e1 ) {
			SwingOSC.printException( e1, kErrorText );
		}
		catch( IllegalArgumentException e1 ) {
			SwingOSC.printException( e1, kErrorText );
		}
		catch( InvocationTargetException e1 ) {
			SwingOSC.printException( e1, kErrorText );
		}
		catch( ClassCastException e1 ) {
			SwingOSC.printException( e1, kErrorText );
		}
	}
}
