/*
 *  ScrollPane.java
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
 *		14-Jan-08	created
 */
package de.sciss.swingosc;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.MouseWheelEvent;

import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.border.Border;
import javax.swing.event.ChangeListener;

/**
 *	@author		Hanns Holger Rutz
 *	@version	0.62, 24-Sep-09
 */
public class ScrollPane
extends JScrollPane
{
	private final Border b;
	
	public ScrollPane( Component view )
	{
		super( view );
		b = getBorder();
		setBorder( null );
		// these come closed to mac feel
		getHorizontalScrollBar().setUnitIncrement( 10 );
		getVerticalScrollBar().setUnitIncrement( 10 );
	}
	
	public void processMouseWheelEvent( MouseWheelEvent e )
	{
		// note: aqua lnf uses shift-modifier to
		// indicate horizontal scrolling
		if( e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL ) {
			final double units = (Math.pow( Math.max( 0, Math.abs( e.getUnitsToScroll() ) - 2 ), 1.3591409142295 ) + 2) * (e.getUnitsToScroll() > 0 ? 1 : -1); 
				
			final JScrollBar bar = e.isShiftDown() ? getHorizontalScrollBar() : getVerticalScrollBar();
			final int amt = (int) (units * bar.getUnitIncrement() * 0.1 + 0.5);
			bar.setValue( bar.getValue() + amt );
			e.consume();
		}
	}
	
	public void setBorder( boolean onOff )
	{
		setBorder( onOff ? b : null );
	}
	
	public void setViewPosition( int x, int y )
	{
		getViewport().setViewPosition( new Point( x, y ));
	}
	
	public int getViewX()
	{
		return getViewport().getViewPosition().x;
	}

	public int getViewY()
	{
		return getViewport().getViewPosition().y;
	}

	// this is just the visible width!
	public int getViewWidth()
	{
		return getViewport().getExtentSize().width;
	}

	// this is just the visible height!
	public int getViewHeight()
	{
		return getViewport().getExtentSize().height;
	}
	
	// this is max( visible width, full view width)
	public int getInnerWidth()
	{
		return getViewport().getViewSize().width;
	}

	// this is max( visible height, full view height)
	public int getInnerHeight()
	{
		return getViewport().getViewSize().height;
	}

	public void addChangeListener( ChangeListener l )
	{
		getViewport().addChangeListener( l );
	}

	public void removeChangeListener( ChangeListener l )
	{
		getViewport().removeChangeListener( l );
	}
}