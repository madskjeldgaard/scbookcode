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

// A sample program showing how to obtain tablet event information in Java on Max OS X

import java.awt.Color;
import java.awt.Container;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.jhlabs.jnitablet.TabletEvent;
import com.jhlabs.jnitablet.TabletProximityEvent;
import com.jhlabs.jnitablet.TabletListener;
import com.jhlabs.jnitablet.TabletWrapper;

/**
 *	@author		Jerry Huxtable
 *	@author		Hanns Holger Rutz
 *	@version	0.11, 24-Feb-08
 */
public class TabletTest
{
	// A main program for testing
	public static void main( String args[] )
	{
		EventQueue.invokeLater( new Runnable() {
			public void run()
			{
				new TabletTest();
			}
		});
	}
	
	public TabletTest()
	{
		final TabletWrapper tabletWrapper = TabletWrapper.getInstance();

		final TabletListener listener = new TabletListener() {
			public void tabletEvent( TabletEvent e ) {
				System.out.println( "TabletEvent" );
				System.out.println( "  id                         " + e.getID() );
				System.out.println( "  deviceID                   " + e.getDeviceID() );
				System.out.println( "  x                          " + e.getX() );
				System.out.println( "  y                          " + e.getY() );
				System.out.println( "  absoluteY                  " + e.getAbsoluteY() );
				System.out.println( "  absoluteX                  " + e.getAbsoluteX() );
				System.out.println( "  absoluteZ                  " + e.getAbsoluteZ() );
				System.out.println( "  buttonMask                 " + e.getButtonMask() );
				System.out.println( "  clickCount                 " + e.getClickCount() );
				System.out.println( "  pressure                   " + e.getPressure() );
				System.out.println( "  rotation                   " + e.getRotation() );
				System.out.println( "  tiltX                      " + e.getTiltX() );
				System.out.println( "  tiltY                      " + e.getTiltY() );
				System.out.println( "  tangentialPressure         " + e.getTangentialPressure() );
				System.out.println( "  vendorDefined1             " + e.getVendorDefined1() );
				System.out.println( "  vendorDefined2             " + e.getVendorDefined2() );
				System.out.println( "  vendorDefined3             " + e.getVendorDefined3() );
				System.out.println();
			}
			
			public void tabletProximity( TabletProximityEvent e ) {
				System.out.println( "TabletProximityEvent" );
				System.out.println( "  capabilityMask             " + e.getCapabilityMask() );
				System.out.println( "  deviceID                   " + e.getDeviceID() );
				System.out.println( "  enteringProximity          " + e.isEnteringProximity() );
				System.out.println( "  pointingDeviceID           " + e.getPointingDeviceID() );
				System.out.println( "  pointingDeviceSerialNumber " + e.getPointingDeviceSerialNumber() );
				System.out.println( "  pointingDeviceType         " + e.getPointingDeviceType() );
				System.out.println( "  systemTabletID             " + e.getSystemTabletID() );
				System.out.println( "  tabletID                   " + e.getTabletID() );
				System.out.println( "  uniqueID                   " + e.getUniqueID() );
				System.out.println( "  vendorID                   " + e.getVendorID() );
				System.out.println( "  vendorPointingDeviceType   " + e.getVendorPointingDeviceType() );
				System.out.println();
			}
		};
		
		JFrame f = new JFrame();
		JPanel p = new JPanel();
		final Container cp = f.getContentPane();
		p.setBackground( Color.red );
		cp.setLayout( new GridLayout( 3, 3 ));
		for( int i = 0; i < 4; i++ ) cp.add( new JLabel() );
		cp.add( p );
		for( int i = 0; i < 4; i++ ) cp.add( new JLabel() );

		p.addMouseListener( new MouseAdapter() {
			public void mouseEntered( MouseEvent e ) {
				tabletWrapper.addTabletListener( listener );
			}

			public void mouseExited( MouseEvent e ) {
				tabletWrapper.removeTabletListener( listener );
			}
		});

		f.setSize( 300, 300 );
		f.setVisible( true );
	}
}