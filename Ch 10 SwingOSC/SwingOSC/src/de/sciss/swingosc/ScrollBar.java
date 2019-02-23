/*
 *  ScrollBar.java
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
 *		16-Jul-07	created
 */
 
package de.sciss.swingosc;

import java.awt.AWTEventMulticaster;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
//import java.awt.event.KeyEvent;
//import javax.swing.InputMap;
import javax.swing.JScrollBar;
//import javax.swing.KeyStroke;
//import javax.swing.UIManager;

/**
 *	A simple extention of <code>javax.swing.JScrollBar</code>
 *	which adds <code>ActionListener</code> functionality
 *	and a <code>setValueAction</code> method for programmatically
 *	dragging the slider.
 *
 *	@author		Hanns Holger Rutz
 *	@version	0.54, 17-Jul-07
 */
public class ScrollBar
extends JScrollBar
implements AdjustmentListener
{
	private	ActionListener al = null;
	
//	private boolean valueWasAdjusting = false;
	
	public ScrollBar()
	{
		super();
		init();
	}

	public ScrollBar( int orientation )
	{
		super( orientation );
		init();
	}

	public ScrollBar( int orientation, int value, int extent, int min, int max )
	{
		super( orientation, value, extent, min, max );
		init();
	}

	private void init()
	{
		addAdjustmentListener( this );
//		final InputMap imap = getInputMap();
//		imap.put( KeyStroke.getKeyStroke( KeyEvent.VK_LEFT, 0 ), "none" );
//		imap.put( KeyStroke.getKeyStroke( KeyEvent.VK_UP, 0 ), "none" );
//		imap.put( KeyStroke.getKeyStroke( KeyEvent.VK_RIGHT, 0 ), "none" );
//		imap.put( KeyStroke.getKeyStroke( KeyEvent.VK_DOWN, 0 ), "none" );
	}
	
//	public boolean getValueWasAdjusting()
//	{
//		return valueWasAdjusting;
//	}
		
	public void setValueNoAction( int n )
	{
		removeAdjustmentListener( this );
		try {
			super.setValue( n );
		} finally {
			addAdjustmentListener( this );
		}
	}
	
	public void setValuesNoAction( int newValue, int newExtent, int newMin, int newMax )
	{
		removeAdjustmentListener( this );
		try {
			super.setValues( newValue, newExtent, newMin, newMax );
		} finally {
			addAdjustmentListener( this );
		}
	}
	
//	public void setValueAction( int n )
//	{
//		setValue( n );
//		fireStateChanged();
//	}
	
	public synchronized void addActionListener( ActionListener l )
	{
		al = AWTEventMulticaster.add( al, l );
	}
	
	public synchronized void removeActionListener( ActionListener l )
	{
		al = AWTEventMulticaster.remove( al, l );
	}

	public void adjustmentValueChanged( AdjustmentEvent e )
	{
//		valueWasAdjusting = e.getValueIsAdjusting();
		
		final ActionListener l = al;
		if( l != null ) {
			l.actionPerformed( new ActionEvent( e.getSource(), ActionEvent.ACTION_PERFORMED, null ));
		}
	}
}