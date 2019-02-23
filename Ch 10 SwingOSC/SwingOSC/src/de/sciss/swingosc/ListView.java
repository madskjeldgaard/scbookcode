/*
 *  ListView.java
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
 *		26-Dec-06	created
 *		29-Jan-08	added setValue
 */
package de.sciss.swingosc;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import javax.swing.JList;
import javax.swing.ListModel;
import javax.swing.event.ListSelectionListener;

/**
 *	Extends <code>JList</code> by a buffering
 *	mechanism for data updates. A large body of
 *	data can be updated by calling <code>addData</code>
 *	repeatedly in a block of <code>beginDataUpdate</code>
 *	and <code>endDataUpdate</code> statements.
 *
 *	@author		Hanns Holger Rutz
 *	@version	0.59, 29-Jan-08
 */
public class ListView
extends JList
{
	private final List updateBlocks = new ArrayList();
	private int numUpdateItems = 0;
	
	public ListView()
	{
		super();
	}

	public ListView( ListModel dataModel )
	{
		super( dataModel );
	}
	
	public ListView( Object[] listData )
	{
		super( listData );
	}
	
	public ListView( Vector listData )
	{
		super( listData );
	}
	
	// sets index without action firing
	public void setValue( int idx )
	{
		final ListSelectionListener[] l = this.getListSelectionListeners();
		for( int i = 0; i < l.length; i++ ) {
			removeListSelectionListener( l[ i ]);
		}
		try {
			this.setSelectedIndex( idx );
		}
		finally {
			for( int i = 0; i < l.length; i++ ) {
				addListSelectionListener( l[ i ]);
			}
		}
	}
	
	public void beginDataUpdate()
	{
		numUpdateItems = 0;
		updateBlocks.clear();
	}
	
	public void addData( Object[] update )
	{
		numUpdateItems += update.length;
		updateBlocks.add( update );
	}
	
	public void setListData( Object[] data, int selectedIndex )
	{
		final ListSelectionListener[] al = getListSelectionListeners();
		for( int i = 0; i < al.length; i++ ) removeListSelectionListener( al[ i ]);
		try {
			setListData( data );
			setSelectedIndex( selectedIndex );
		}
		finally {
			for( int i = 0; i < al.length; i++ ) addListSelectionListener( al[ i ]);
		}
	}

	public void endDataUpdate()
	{
		final Object[] data = new Object[ numUpdateItems ];
		Object[] block;
		for( int i = 0, off = 0; i < updateBlocks.size(); i++ ) {
			block = (Object[]) updateBlocks.get( i );
			System.arraycopy( block, 0, data, off, block.length );
			off += block.length;
		}
		numUpdateItems = 0;
		updateBlocks.clear();
		setListData( data );
	}
	
	public void endDataUpdate( int selectedIndex )
	{
		final ListSelectionListener[] al = getListSelectionListeners();
		for( int i = 0; i < al.length; i++ ) removeListSelectionListener( al[ i ]);
		try {
			endDataUpdate();
			setSelectedIndex( selectedIndex );
		}
		finally {
			for( int i = 0; i < al.length; i++ ) addListSelectionListener( al[ i ]);
		}
	}

	/**
	 *	Overwritten to toggle the opacity settings
	 *	when background colour is (semi)transparent
	 */
	public void setBackground( Color c )
	{
		setOpaque( (c != null) && (c.getAlpha() == 0xFF) );
		super.setBackground( c );
	}
}
