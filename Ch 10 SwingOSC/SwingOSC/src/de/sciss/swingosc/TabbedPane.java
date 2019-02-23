/*
 *  TabbedPane.java
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
 *		12-Dec-06	created
 */
 
package de.sciss.swingosc;

import java.awt.AWTEventMulticaster;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *	A simple extention of <code>javax.swing.JTabbedPane</code>
 *	which adds <code>ActionListener</code> functionality
 *	and a <code>setValueAction</code> method for programmatically
 *	dragging the slider.
 *
 *	@author		Hanns Holger Rutz
 *	@version	0.44, 12-Dec-06
 */
public class TabbedPane
extends JTabbedPane
implements ChangeListener
{
	private	ActionListener al = null;

	public TabbedPane()
	{
		super();
		init();
	}

	public TabbedPane( int tabPlacement )
	{
		super( tabPlacement );
		init();
	}

	public TabbedPane( int tabPlacement, int tabLayoutPolicy )
	{
		super( tabPlacement, tabLayoutPolicy );
		init();
	}
			   
	private void init()
	{
		addChangeListener( this );
	}
	
	public void setSelectedIndexNoAction( int n )
	{
		removeChangeListener( this );
		super.setSelectedIndex( n );
		addChangeListener( this );
	}
	
	public synchronized void addActionListener( ActionListener l )
	{
		al = AWTEventMulticaster.add( al, l );
	}
	
	public synchronized void removeActionListener( ActionListener l )
	{
		al = AWTEventMulticaster.remove( al, l );
	}

	public void stateChanged( ChangeEvent e )
	{
		final ActionListener l = al;
		if( l != null ) {
			l.actionPerformed( new ActionEvent( e.getSource(), ActionEvent.ACTION_PERFORMED, null ));
		}
	}
}