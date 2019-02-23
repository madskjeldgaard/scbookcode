/*
 *  ComponentResponder.java
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
 *		24-Mar-08	optionally different objects for focus and bounds
 */
 
package de.sciss.swingosc;

import java.awt.Container;
//import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
//import javax.swing.JComponent;

import javax.swing.SwingUtilities;

import de.sciss.gui.GUIUtil;
import de.sciss.net.OSCMessage;

/**
 *	A <code>ComponentResponder</code> is created for a <code>java.awt.Component</code>.
 *	When instantiating, the responder starts to listen to
 *	<code>ComponentEvent</code>s and <code>FocusEvent</code>s
 *	fired from that component, until
 *	<code>remove</code> is called. When an event occurs, the
 *	responder will send a <code>/component</code> OSC message to
 *	the client that has created the responder, with the following arguments:
 *	<pre>
 *		[ "/component", <componentID>, <state>, [ <x>, <y>, <width>, <height> ]
 *	</pre>
 *	with state = (resized|moved|shown|hidden|gainedFocus|lostFocus)
 *	and for state = resized|moved the new bounds.
 *
 *	@author		Hanns Holger Rutz
 *	@version	0.60, 24-Mar-08
 */
public class ComponentResponder
extends AbstractResponder
implements ComponentListener, FocusListener
{
	private static final Class[] listenerClasses	= { ComponentListener.class, FocusListener.class };
	private static final String[] listenerNames		= { "ComponentListener", "FocusListener" };
	
	private final boolean	absCoords;
	private final Object[]	shortReplyArgs = new Object[ 2 ];
//	private final Insets	in;
	
	private final Object	bObject;

	public ComponentResponder( Object objectID )
	throws IllegalAccessException, NoSuchMethodException, InvocationTargetException
	{
		this( objectID, false );
	}
	
	public ComponentResponder( Object focusID, Object boundsID )
	throws IllegalAccessException, NoSuchMethodException, InvocationTargetException
	{
		this( focusID, boundsID, false );
	}

	public ComponentResponder( Object objectID, boolean absCoords )
	throws IllegalAccessException, NoSuchMethodException, InvocationTargetException
	{
		this( objectID, objectID, absCoords );
	}
	
	// absCoords: if true, coordinates send
	// are transformed from component to component's window's
	// content pane topleft
	public ComponentResponder( Object focusID, Object boundsID, boolean absCoords )
	throws IllegalAccessException, NoSuchMethodException, InvocationTargetException
	{
		super( focusID, 6 );
		
		this.absCoords = absCoords;
		shortReplyArgs[ 0 ]	= replyArgs[ 0 ];
		
		bObject	= focusID.equals( boundsID ) ? object : client.getObject( boundsID );

		add();
//		if( object instanceof JComponent ) {
//			in = (Insets) ((JComponent) object).getClientProperty( "insets" );
//		} else {
//			in = new Insets( 0, 0, 0, 0 );
//		}
	}

	protected Object getObjectForListener( int index )
	{
		return index == 0 ? bObject : object;
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
		return "/component";
	}

	private void reply( String stateName, ComponentEvent e )
	{
		Rectangle b = e.getComponent().getBounds();
		
		try {
			if( absCoords ) {
				final Container cp = SwingUtilities.getRootPane( e.getComponent() ).getContentPane();
				b = GUIUtil.convertRectangle( e.getComponent(), b, cp );
			}
			// [ "/component", <componentID>, <state>, <x>, <y>, <w>, <h> ]
			replyArgs[ 1 ] = stateName;
			replyArgs[ 2 ] = new Integer( b.x ); // + in.left
			replyArgs[ 3 ] = new Integer( b.y ); // + in.top
			replyArgs[ 4 ] = new Integer( b.width ); // - (in.left + in.right)
			replyArgs[ 5 ] = new Integer( b.height ); // - (in.top + in.bottom) 
			client.reply( new OSCMessage( getOSCCommand(), replyArgs ));
		}
		catch( IOException ex ) {
			SwingOSC.printException( ex, getOSCCommand() );
		}
	}
		
	private void replyShort( String stateName )
	{
		try {
			// [ "/component", <componentID>, <state> ]
			shortReplyArgs[ 1 ] = stateName;
			client.reply( new OSCMessage( getOSCCommand(), shortReplyArgs ));
		}
		catch( IOException ex ) {
			SwingOSC.printException( ex, getOSCCommand() );
		}
	}
		
	// -------- ComponentListener interface --------

	public void componentResized( ComponentEvent e )
	{
		reply( "resized", e );
	}

	public void componentMoved( ComponentEvent e )
	{
		reply( "moved", e );
	}
	
	public void componentShown( ComponentEvent e )
	{
		replyShort( "shown" );
	}

	public void componentHidden( ComponentEvent e )
	{
		replyShort( "hidden" );
	}

	// -------- FocusListener interface --------

	public void focusGained( FocusEvent e )
	{
		replyShort( "gainedFocus" );
	}

	public void focusLost( FocusEvent e )
	{
		replyShort( "lostFocus" );
	}
}