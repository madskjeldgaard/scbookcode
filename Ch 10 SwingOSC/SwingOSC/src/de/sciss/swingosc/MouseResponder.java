/*
 *  MouseResponder.java
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
 *		03-Feb-07	added acceptsMouseOver ; only fires when component is enabled
 */
 
package de.sciss.swingosc;

import java.awt.Container;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import javax.swing.SwingUtilities;

import de.sciss.gui.GUIUtil;
import de.sciss.net.OSCMessage;


/**
 *	A <code>MouseResponder</code> is created for a <code>java.awt.Component</code>.
 *	When instantiating, the responder starts to listen to
 *	<code>MouseEvent</code>s fired from that component, until
 *	<code>remove</code> is called. When an event occurs, the
 *	responder will send a <code>/mouse</code> OSC message to
 *	the client that has created the responder, with the following arguments:
 *	<pre>
 *		[ "/mouse", <componentID>, <state>, <x>, <y>, <modifiers>, <button>, <clickCount> ]
 *	</pre>
 *	with state = (pressed|released|moved|dragged|entered|exited), x and y the
 *	current mouse coordinates (relative to component topleft),
 *	modifiers an int mask as in java.awt.event.InputEvent, button the
 *	identifier of the mouse button, and click count the number of
 *	successive clicks.
 *
 *	@author		Hanns Holger Rutz
 *	@version	0.59, 25-Feb-08
 */
public class MouseResponder
extends AbstractMouseResponder
implements MouseListener, MouseMotionListener
{
	private static final Class[] listenerClasses	= { MouseListener.class, MouseMotionListener.class };
	private static final String[] listenerNames	= { "MouseListener", "MouseMotionListener" };
	
	private final boolean	absCoords;
	
	public MouseResponder( Object objectID )
	throws IllegalAccessException, NoSuchMethodException, InvocationTargetException
	{
		this( objectID, false );
	}
	
	public MouseResponder( Object objectID, boolean absCoords )
	throws IllegalAccessException, NoSuchMethodException, InvocationTargetException
	{
		this( objectID, absCoords, null );
	}
	
	// absCoords: if true, coordinates send
	// are transformed from component to component's window's
	// content pane topleft
	public MouseResponder( Object objectID, boolean absCoords, Object frameID )
	throws IllegalAccessException, NoSuchMethodException, InvocationTargetException
	{
		super( objectID, 7, frameID );
		add();
		
		this.absCoords	= absCoords;
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
		return "/mouse";
	}

	private void reply( String stateName, MouseEvent e )
	{
		try {
			final Point p;
			
			// note: extract these first because due to a bug
			// in SwingUtilities.convertMouseEvent, some
			// modifiers may be swallowed
			replyArgs[ 4 ] = new Integer( e.getModifiers() );
			replyArgs[ 5 ] = new Integer( e.getButton() );
			replyArgs[ 6 ] = new Integer( e.getClickCount() );
			if( absCoords ) {
				final Container cp = SwingUtilities.getRootPane( e.getComponent() ).getContentPane();
//				e = SwingUtilities.convertMouseEvent( e.getComponent(), e, cp );
				p = GUIUtil.convertPoint( e.getComponent(), new Point( e.getX(), e.getY() ), cp );
			} else {
				p = new Point( e.getX(), e.getY() );
			}
			// [ "/key", <componentID>, <state>, <keyCode>, <keyChar>, <modifiers> ]
			replyArgs[ 1 ] = stateName;
			replyArgs[ 2 ] = new Integer( p.x );
			replyArgs[ 3 ] = new Integer( p.y );
			client.reply( new OSCMessage( getOSCCommand(), replyArgs ));
		}
		catch( IOException ex ) {
			SwingOSC.printException( ex, getOSCCommand() );
		}
	}
		
	// -------- MouseListener interface --------

	public void mouseClicked( MouseEvent e ) { /* ignored */ }
	
	public void mousePressed( MouseEvent e )
	{
		if( e.getComponent().isEnabled() ) reply( "pressed", e );
	}

	public void mouseReleased( MouseEvent e )
	{
		if( e.getComponent().isEnabled() ) reply( "released", e );
	}
	
	public void mouseEntered( MouseEvent e )
	{
		if( acceptsMouseOver && e.getComponent().isEnabled() ) reply( "entered", e );
	}

	public void mouseExited( MouseEvent e )
	{
		if( acceptsMouseOver && e.getComponent().isEnabled() ) reply( "exited", e );
	}

	// -------- MouseMotionListener interface --------

	public void mouseDragged( MouseEvent e )
	{
		if( e.getComponent().isEnabled() ) reply( "dragged", e );
	}

	public void mouseMoved( MouseEvent e )
	{
		if( acceptsMouseOver && e.getComponent().isEnabled() ) reply( "moved", e );
	}
}