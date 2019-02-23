/*
Copyright 2006 Jerry Huxtable

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

	http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

/*
 *	Extended functionality and/or slight adjustments by Hanns Holger Rutz
 *	(C)opyright 2007-2008
 */

package com.jhlabs.jnitablet;

import java.awt.EventQueue;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *	The class which wraps the tablet interface.
 *	To use this, create an instance and then add tabletListeners to it.
 *
 *	@author		Jerry Huxtable
 *	@author		Hanns Holger Rutz
 *	@version	0.12, 21-Feb-09
 */
public class TabletWrapper
{
	static {
		System.loadLibrary( "JNITablet" );
	}

	public static boolean			DEBUG			= false;
	private static final double		VERSION			= 0.12;

	private static TabletWrapper	instance;

	private List					listeners		= new ArrayList();
	private volatile int			numListeners	= 0;

	private TabletWrapper()
	{
		if( instance != null ) throw new IllegalStateException( "Only one instance allowed" );
		instance = this;
		startup();
		if( DEBUG ) System.err.println( "TabletWrapper: started" );
	}
	
	public static final double getVersion()
	{
		return VERSION;
	}

	public synchronized static TabletWrapper getInstance()
	{
		if( instance != null ) return instance;
		return new TabletWrapper();
	}

	public void addTabletListener( TabletListener listener )
	{
		listeners.add( listener );
		numListeners++;
	}

	public void removeTabletListener( TabletListener listener )
	{
		if( listeners.remove( listener )) numListeners--;
	}

	public void finalize()
	{
		shutdown();
	}
	
	private native void startup();
	private native void shutdown();

	private void postProximityEvent(
		final int deviceID,
		final int capabilityMask,
		final boolean enteringProximity,
		final int pointingDeviceID,
		final int pointingDeviceSerialNumber,
		final int pointingDeviceType,
		final int systemTabletID,
		final int tabletID,
		final int uniqueID,
		final int vendorID,
		final int vendorPointingDeviceType
	) {
		if( numListeners == 0 ) return;
	
		final TabletProximityEvent	event				= new TabletProximityEvent( this, 
			deviceID, capabilityMask, enteringProximity,
			pointingDeviceID, pointingDeviceSerialNumber, pointingDeviceType,
			systemTabletID, tabletID, uniqueID, vendorID, vendorPointingDeviceType
		);

		EventQueue.invokeLater( new Runnable() {
			public void run()
			{
				for( Iterator it = listeners.iterator(); it.hasNext(); ) {
					((TabletListener) it.next()).tabletProximity( event );
				}
			}
		});
	}
	
	private void postEvent(
		final int type,
		final int deviceID,
		final float x, final float y,
		final int absoluteX, final int absoluteY, final int absoluteZ,
		final int buttonMask,
//		final int buttonNumber,
		final int clickCount,
		final float pressure, final float rotation,
		final float tiltX, final float tiltY,
		final float tangentialPressure,
		final short vendorDefined1,
		final short vendorDefined2,
		final short vendorDefined3
	) {
//System.out.println( "post " + numListeners );
		if( numListeners == 0 ) return;

		final int			awtType;
		final TabletEvent	event;
		
		switch( type ) {
		case 1: // NSLeftMouseDown
			awtType = MouseEvent.MOUSE_PRESSED;
			break;
		case 2: // NSLeftMouseUp
			awtType = MouseEvent.MOUSE_RELEASED;
			break;
		case 5: // NSMouseMoved
			awtType = MouseEvent.MOUSE_MOVED;
			break;
		case 6: // NSLeftMouseDragged
		case 23: // NSTabletPoint
			awtType = MouseEvent.MOUSE_DRAGGED;
			break;
		default:
			throw new IllegalArgumentException( "type " + type );
		}
		
		event = new TabletEvent( this,
			awtType,
			deviceID,
			x, y,
			absoluteX, absoluteY, absoluteZ,
			buttonMask,
//			buttonNumber,
			clickCount,
			pressure, rotation,
			tiltX, tiltY,
			tangentialPressure,
			vendorDefined1, vendorDefined2, vendorDefined3
		);
		EventQueue.invokeLater( new Runnable() {
			public void run()
			{
				for( Iterator it = listeners.iterator(); it.hasNext(); ) {
					((TabletListener) it.next()).tabletEvent( event );
				}
			}
		});
	}
}