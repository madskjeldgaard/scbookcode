/*
 *  TabletView.java
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

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.sciss.app.DynamicAncestorAdapter;
import de.sciss.app.DynamicListening;

import com.jhlabs.jnitablet.TabletEvent;
import com.jhlabs.jnitablet.TabletProximityEvent;
import com.jhlabs.jnitablet.TabletListener;
import com.jhlabs.jnitablet.TabletWrapper;

/**
 *	@author		Hanns Holger Rutz
 *	@version	0.59, 25-Feb-08
 */
public class TabletView
extends UserView
implements DynamicListening, TabletListener
{
	private boolean 				added			= false;
	protected boolean 				inside			= false;
	protected boolean 				pressed			= false;
	private final List				listeners		= new ArrayList();
	protected TabletProximityEvent	lastEnterEvent;
	protected boolean				dispatchExit	= false;
	
	public TabletView()
	{
		super();
		
		new DynamicAncestorAdapter( this ).addTo( this );
		addMouseListener( new MouseAdapter() {
			public void mousePressed( MouseEvent e )
			{
//System.out.println( "mousePressed " + isEnabled() );
				if( isEnabled() ) {
					pressed = true;
				}
			}
			
			public void mouseReleased( MouseEvent e )
			{
				pressed = false;
//				if( !inside ) remove();
			}
			
			public void mouseEntered( MouseEvent e ) {
//System.out.println( "mouseEntered " + isEnabled() );
				if( isEnabled() ) {
					inside	= true;
					if( lastEnterEvent != null ) {
						dispatch( lastEnterEvent );
						lastEnterEvent	= null;
						dispatchExit	= true;
					}
//					add();
				}
			}

			public void mouseExited( MouseEvent e ) {
				inside = false;
//				if( !pressed ) remove();
			}
		});
		
		// XXX don't ask me why this...
//		add(); remove();
		
//		final TabletWrapper tabletWrapper = TabletWrapper.getInstance();
//		tabletWrapper.addTabletListener( this );
//		tabletWrapper.removeTabletListener( this );
	}
	
	private void remove()
	{
		if( added ) {
//			System.out.println( "removing" );
			TabletWrapper.getInstance().removeTabletListener( this );
			added = false;
		}
	}
	
	private void add()
	{
		if( !added ) {
//			System.out.println( "adding" );
			TabletWrapper.getInstance().addTabletListener( this );
			added = true;
		}
	}
	
	public void addTabletListener( TabletListener l )
	{
		listeners.add( l );
	}
	
	public void removeTabletListener( TabletListener l )
	{
		listeners.remove( l );
	}

//	 ---------------- DynamicListening interface ----------------

	public void startListening()
	{
		add();
	}

	public void stopListening()
	{
		remove();
	}
	
//	 ---------------- TabletListener interface ----------------

	public void tabletEvent( TabletEvent e )
	{
		switch( e.getID() ) {
		case MouseEvent.MOUSE_DRAGGED:
			// ignore messages that originate from drags that started outside the view
			if( !pressed ) return;
			break;
		case MouseEvent.MOUSE_MOVED:
			// ignore messages that originate from moves that left the view
			if( !inside ) return;
			break;
		case MouseEvent.MOUSE_PRESSED:
			// ignore messages that originate from clicking outside the view
			if( !inside ) return;
			break;
		case MouseEvent.MOUSE_RELEASED:
			// ignore messages that originate from clicking outside the view
			if( !pressed ) return;
			break;
		default:
			break;
		}

		for( Iterator iter = listeners.iterator(); iter.hasNext(); ) {
			((TabletListener) iter.next()).tabletEvent( e );
		}
//		System.out.println( "TabletEvent" );
//		System.out.println( "  id                         " + e.getID() );
//		System.out.println( "  x                          " + e.getX() );
//		System.out.println( "  y                          " + e.getY() );
//		System.out.println( "  absoluteY                  " + e.getAbsoluteY() );
//		System.out.println( "  absoluteX                  " + e.getAbsoluteX() );
//		System.out.println( "  absoluteZ                  " + e.getAbsoluteZ() );
//		System.out.println( "  buttonMask                 " + e.getButtonMask() );
//		System.out.println( "  pressure                   " + e.getPressure() );
//		System.out.println( "  rotation                   " + e.getRotation() );
//		System.out.println( "  tiltX                      " + e.getTiltX() );
//		System.out.println( "  tiltY                      " + e.getTiltY() );
//		System.out.println( "  tangentialPressure         " + e.getTangentialPressure() );
//		System.out.println( "  vendorDefined1             " + e.getVendorDefined1() );
//		System.out.println( "  vendorDefined2             " + e.getVendorDefined2() );
//		System.out.println( "  vendorDefined3             " + e.getVendorDefined3() );
//		System.out.println();
	}

	protected void dispatch( TabletProximityEvent e )
	{
		for( Iterator iter = listeners.iterator(); iter.hasNext(); ) {
			((TabletListener) iter.next()).tabletProximity( e );
		}
	}
	
	public void tabletProximity( TabletProximityEvent e )
	{
		if( e.isEnteringProximity() ) {
			if( inside ) {
				lastEnterEvent	= null;
				dispatch( e );
				dispatchExit	= true;
			} else {
				lastEnterEvent	= e;
			}
		} else {
			if( dispatchExit ) {
				dispatchExit	= false;
				dispatch( e );
			}
		}

//		System.out.println( "TabletProximityEvent" );
//		System.out.println( "  capabilityMask             " + e.getCapabilityMask() );
//		System.out.println( "  deviceID                   " + e.getDeviceID() );
//		System.out.println( "  enteringProximity          " + e.isEnteringProximity() );
//		System.out.println( "  pointingDeviceID           " + e.getPointingDeviceID() );
//		System.out.println( "  pointingDeviceSerialNumber " + e.getPointingDeviceSerialNumber() );
//		System.out.println( "  pointingDeviceType         " + e.getPointingDeviceType() );
//		System.out.println( "  systemTabletID             " + e.getSystemTabletID() );
//		System.out.println( "  tabletID                   " + e.getTabletID() );
//		System.out.println( "  uniqueID                   " + e.getUniqueID() );
//		System.out.println( "  vendorID                   " + e.getVendorID() );
//		System.out.println( "  vendorPointingDeviceType   " + e.getVendorPointingDeviceType() );
//		System.out.println();
	}
}