/*
 * 	ComponentEventForwarder.java
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
 *		29-Jul-09	created
 */
package de.sciss.swingosc;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.SwingUtilities;

/**
 * 	@version	0.63, 30-Jul-09
 *	@author		Hanns Holger Rutz
 */
public class ComponentEventForwarder
implements MouseListener, MouseMotionListener, KeyListener, FocusListener
{
	private final Component target;
//	private final List sources = new ArrayList();
	
	public ComponentEventForwarder( Component target )
	{
		this.target	= target;
	}
	
	public void addSource( Component source )
	{
		source.addMouseListener( this );
		source.addMouseMotionListener( this );
		source.addKeyListener( this );
		source.addFocusListener( this );
	}
	
	public void removeSource( Component source )
	{
		source.removeMouseListener( this );
		source.removeMouseMotionListener( this );
		source.removeKeyListener( this );
		source.removeFocusListener( this );
	}
	
//	public void remove()
//	{
//	}
	
	private void redispatch( AWTEvent e )
	{
		e.setSource( target );
		target.dispatchEvent( e );
	}

	private void redispatchMouse( MouseEvent e )
	{
		redispatch( SwingUtilities.convertMouseEvent( e.getComponent(), e, target ));
	}

	public void mouseClicked( MouseEvent e )
	{
		redispatchMouse( e );
	}

	public void mouseEntered( MouseEvent e )
	{
		redispatchMouse( e );
	}

	public void mouseExited( MouseEvent e )
	{
		redispatchMouse( e );
	}

	public void mousePressed( MouseEvent e )
	{
		redispatchMouse( e );
	}

	public void mouseReleased( MouseEvent e )
	{
		redispatchMouse( e );
	}

	public void mouseDragged( MouseEvent e )
	{
		redispatchMouse( e );
	}

	public void mouseMoved( MouseEvent e )
	{
		redispatchMouse( e );
	}
	
	public void keyPressed( KeyEvent e )
	{
		redispatch( e );
	}

	public void keyReleased( KeyEvent e )
	{
		redispatch( e );
	}

	public void keyTyped( KeyEvent e )
	{
		redispatch( e );
	}

	public void focusGained( FocusEvent e )
	{
		redispatch( e );
	}

	public void focusLost( FocusEvent e )
	{
		redispatch( e );
	}
}