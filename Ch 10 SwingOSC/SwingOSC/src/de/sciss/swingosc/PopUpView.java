/*
 *  PopUpView.java
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
 *		29-Jan-08	wrapping data in UniqueObject so no multiple items are checkmarked
 *					; added setValue
 */
package de.sciss.swingosc;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import javax.swing.JComboBox;
import javax.swing.ComboBoxModel;

/**
 *	Extends <code>JComboBox</code> by a buffering
 *	mechanism for data updates. A large body of
 *	data can be updated by calling <code>addData</code>
 *	repeatedly in a block of <code>beginDataUpdate</code>
 *	and <code>endDataUpdate</code> statements.
 *
 *	@author		Hanns Holger Rutz
 *	@version	0.64, 28-Dec-09
 */
public class PopUpView
extends JComboBox
{
	private static final String SIZE_REGULAR = "regular";
	private static final String SIZE_SMALL	 = "small";
	private static final String SIZE_MINI	 = "mini";
	
	private final List updateBlocks = new ArrayList();
//	private int numUpdateItems = 0;
	private boolean isAqua = getUI().getClass().getName().startsWith( "com.apple" );
	private String currentSize = SIZE_REGULAR; // re aqua lnf
	
	public PopUpView()
	{
		super();
		init();
	}

	public PopUpView( ComboBoxModel dataModel )
	{
		super( dataModel );
		init();
	}
	
	public PopUpView( Object[] listData )
	{
		super( listData );
		init();
	}
	
	public PopUpView( Vector listData )
	{
		super( listData );
		init();
	}

	protected void updateAquaSize()
	{
		final String newSize;
		final int h = getHeight() - (isFocusable() ? 4 : 0);
		if( h >= 22 ) {
			newSize = SIZE_REGULAR;
		} else if( h >= 19 ) {
			newSize = SIZE_SMALL;
		} else { // >= 15
			newSize = SIZE_MINI;
		}
		if( newSize != currentSize ) {
			putClientProperty( "JComponent.sizeVariant", newSize );
			repaint();
			currentSize = newSize;
		}
	}

	// sets index without action firing
	public void setValue( int idx )
	{
		final ActionListener[] l = this.getActionListeners();
		for( int i = 0; i < l.length; i++ ) {
			removeActionListener( l[ i ]);
		}
		try {
			this.setSelectedIndex( idx );
		}
		finally {
			for( int i = 0; i < l.length; i++ ) {
				addActionListener( l[ i ]);
			}
		}
	}
	
	private void init()
	{
// stupid apple vm has drawing glitches if we do this
//		setBackground( new Color( 0, 0, 0, 0 ));
		if( isAqua ) {
			addComponentListener( new ComponentAdapter() {
				public void componentResized( ComponentEvent e )
				{
					updateAquaSize();
				}
			});
			addPropertyChangeListener( "focusable", new PropertyChangeListener() {
				public void propertyChange( PropertyChangeEvent e )
				{
					updateAquaSize();
				}
			});
		}
	}
	
//	public void setBackground( Color c )
//	{
//		setOpaque( (c != null) && (c.getAlpha() == 0xFF) );
//		super.setBackground( c );
//	}
	
	public void paintComponent( Graphics g )
	{
		final Color bg = getBackground();
		if( (bg != null) && (bg.getAlpha() > 0) ) {
			g.setColor( bg );
			g.fillRect( 0, 0, getWidth(), getHeight() );
		}
		super.paintComponent( g );
	}

	public void beginDataUpdate()
	{
//		numUpdateItems = 0;
		updateBlocks.clear();
	}
	
	public void addData( Object[] update )
	{
//		numUpdateItems += update.length;
		updateBlocks.add( update );
	}
	
	public void endDataUpdate()
	{
		Object[] block;
		removeAllItems();
		for( int i = 0; i < updateBlocks.size(); i++ ) {
			block = (Object[]) updateBlocks.get( i );
			for( int j = 0; j < block.length; j++ ) {
				addItem( new UniqueObject( block[ j ]));
			}
		}
//		numUpdateItems = 0;
		updateBlocks.clear();
	}
	
	public void setListData( Object[] data )
	{
		removeAllItems();
		for( int j = 0; j < data.length; j++ ) {
			addItem( new UniqueObject( data[ j ]));
		}
	}

	public void setListData( Object[] data, int selectedIndex )
	{
		final ActionListener[] al = getActionListeners();
		for( int i = 0; i < al.length; i++ ) removeActionListener( al[ i ]);
		try {
			setListData( data );
			setSelectedIndex( selectedIndex );
		}
		finally {
			for( int i = 0; i < al.length; i++ ) addActionListener( al[ i ]);
		}
	}

	public void endDataUpdate( int selectedIndex )
	{
		final ActionListener[] al = getActionListeners();
		for( int i = 0; i < al.length; i++ ) removeActionListener( al[ i ]);
		try {
			endDataUpdate();
			setSelectedIndex( selectedIndex );
		}
		finally {
			for( int i = 0; i < al.length; i++ ) addActionListener( al[ i ]);
		}
	}
	
	// equals returns true only when a === b
	private static class UniqueObject
	{
		private final Object value;
		
		protected UniqueObject( Object value )
		{
			this.value = value;
		}
		
		public String toString() { return value.toString(); }
	}
}