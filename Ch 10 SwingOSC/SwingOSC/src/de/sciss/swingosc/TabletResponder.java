/*
 *  TabletResponder.java
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
 *		26-Nov-07	created
 */
 
package de.sciss.swingosc;

import java.awt.Component;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import javax.swing.SwingUtilities;

import de.sciss.net.OSCMessage;

import com.jhlabs.jnitablet.TabletEvent;
import com.jhlabs.jnitablet.TabletProximityEvent;
import com.jhlabs.jnitablet.TabletListener;

/**
 *	@author		Hanns Holger Rutz
 *	@version	0.62, 17-Jul-09
 */
public class TabletResponder
extends AbstractMouseResponder
implements TabletListener
{
	private static final Class[]	listenerClasses	= { TabletListener.class };
	private static final String[]	listenerNames	= { "TabletListener" };
	private final Component			comp;
	private Window					win				= null;
	private final Object[]			proxReplyArgs	= new Object[ 9 ];
	
	public TabletResponder( Object objectID )
	throws IllegalAccessException, NoSuchMethodException, InvocationTargetException
	{
		this( objectID, null );
	}
	
	public TabletResponder( Object objectID, Object frameID  )
	throws IllegalAccessException, NoSuchMethodException, InvocationTargetException
	{
		super( objectID, 15, frameID );
		add();
		
		comp				= (Component) object;
		proxReplyArgs[ 0 ]	= replyArgs[ 0 ];
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
		return "/tablet";
	}
		
	// -------- MouseListener interface --------

	public void tabletEvent( TabletEvent e )
	{
		if( comp.isEnabled() ) {
			try {
				final String state;
				switch( e.getID() ) {
				case MouseEvent.MOUSE_PRESSED:
					state	= "pressed";
					break;
				case MouseEvent.MOUSE_RELEASED:
					state	= "released";
					break;
				case MouseEvent.MOUSE_MOVED:
					if( !acceptsMouseOver ) return;
					state	= "moved";
					break;
				case MouseEvent.MOUSE_DRAGGED:
					state	= "dragged";
					break;
				default:
					throw new IllegalArgumentException( String.valueOf( e.getID() ));
				}
				
				// [ "/tablet", <componentID>, <state>, <deviceID>, <localX>, <localY>, <pressure>,
				//   <tiltX>, <tiltY>, <rota>, <tanPressure>, <absX>, <absY>, <absZ>,
				//   <buttonMask>, <clickCount>
				replyArgs[  1 ] = state;
				replyArgs[  2 ] = new Integer( e.getDeviceID() );
				
				// since TabletView receives a TabletEvent and doesn't
				// want to copy it, it cannot correct the coordinates
				// inplace. Instead the regular cocoa coordinates in the
				// the window are provided. We mangle them here to
				// produce proper java style coordinates relative to
				// the top-left corner of the window.
				if( win == null ) {
					win = SwingUtilities.getWindowAncestor( comp );
					if( win == null ) return;
				}
				final Point	p	= SwingUtilities.convertPoint( win,
				    (int) e.getX(), win.getHeight() - (int) e.getY(), comp );
				replyArgs[  3 ] = new Float( p.x + (e.getX() % 1.0f) );
				replyArgs[  4 ] = new Float( p.y - (e.getY() % 1.0f) );
				replyArgs[  5 ] = new Float( e.getPressure() );
				replyArgs[  6 ] = new Float( e.getTiltX() );
				replyArgs[  7 ] = new Float( e.getTiltY() );
				replyArgs[  8 ] = new Float( e.getRotation() );
				replyArgs[  9 ] = new Float( e.getTangentialPressure() );
				replyArgs[ 10 ] = new Integer( e.getAbsoluteX() );
				replyArgs[ 11 ] = new Integer( e.getAbsoluteY() );
				replyArgs[ 12 ] = new Integer( e.getAbsoluteZ() );
				replyArgs[ 13 ] = new Integer( e.getButtonMask() );
				replyArgs[ 14 ] = new Integer( e.getClickCount() );
				client.reply( new OSCMessage( getOSCCommand(), replyArgs ));
			}
			catch( IOException ex ) {
				SwingOSC.printException( ex, getOSCCommand() );
			}
		}
	}

	public void tabletProximity( TabletProximityEvent e )
	{
		if( comp.isEnabled() ) {
			try {
				// [ "/tablet", <componentID>, "proximity", <deviceID>, <enteringProximity>,
				//   <systemTabletID>, <tabletID>, <pointingDeviceType>, <uniqueID>,
				//	 <pointingDeviceID>
				proxReplyArgs[  1 ] = "proximity";
				proxReplyArgs[  2 ] = new Integer( e.getDeviceID() );
				proxReplyArgs[  3 ] = new Integer( e.isEnteringProximity() ? 1 : 0 );
				proxReplyArgs[  4 ] = new Integer( e.getSystemTabletID() );
				proxReplyArgs[  5 ] = new Integer( e.getTabletID() );
				proxReplyArgs[  6 ] = new Integer( e.getPointingDeviceType() );
				proxReplyArgs[  7 ] = new Integer( e.getUniqueID() );
				proxReplyArgs[  8 ] = new Integer( e.getPointingDeviceID() );
				client.reply( new OSCMessage( getOSCCommand(), proxReplyArgs ));
			}
			catch( IOException ex ) {
				SwingOSC.printException( ex, getOSCCommand() );
			}
		}
	}
}