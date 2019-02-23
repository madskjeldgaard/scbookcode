/*
 *  SwingMenuItem.java
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
 *  	27-Jul-08	created
 */
package de.sciss.swingosc;

import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.util.Iterator;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.KeyStroke;

import de.sciss.common.BasicMenuFactory;
import de.sciss.gui.MenuItem;

public class SwingMenuItem
extends MenuItem
{
	private final DispatchAction action;
	
	public SwingMenuItem( String id, String text )
	{
//		this( id, text, null, 0 );
//	}
//	
//	public SwingMenuItem( String id, String text, String accel, int modifiers )
//	{
		super( id, new DispatchAction( text ));
//		super( id, new DispatchAction( text, SwingMenuItem.createKeyStroke( accel, modifiers )));
//		super( id, new DispatchAction( text, KeyStroke.getKeyStroke( KeyEvent.VK_A, InputEvent.META_MASK )));
		action = (DispatchAction) this.getAction();
	}
	
	public void addActionListener( ActionListener l )
	{
		action.addActionListener( l );
	}

	public void removeActionListener( ActionListener l )
	{
		action.removeActionListener( l );
	}
	
	public void setName( String name )
	{
		SwingMenuItem.setName( this, name );
	}
	
	public void setShortCut( String cut )
	{
		SwingMenuItem.setShortCut( this, cut );
	}
	
	protected static void setName( MenuItem mi, String name )
	{
		mi.getAction().putValue( Action.NAME, name );
	}

//	protected static KeyStroke createKeyStroke( String accel, int modifiers )
//	{
//		if( (accel == null) || (accel.length() != 1) ) return null;
//		return createKeyStroke( accel.charAt( 0 ), modifiers );
//	}
//	
	protected static KeyStroke createKeyStroke( String cut )
	{
		final String[]		elem		= cut.split( "\\s" );
		final StringBuffer	sb			= new StringBuffer();
		final boolean		altMeta		= BasicMenuFactory.MENU_SHORTCUT != InputEvent.META_MASK;
		final int			meta1		= BasicMenuFactory.MENU_SHORTCUT;
		final int			meta2		= altMeta ? (InputEvent.CTRL_MASK | InputEvent.ALT_MASK) : InputEvent.CTRL_MASK; 
		int					modifiers	= 0;
		
		for( int i = 0; i < elem.length; i++ ) {
//			System.out.println( "found '" + elem[ i ] + "'" );
			if( elem[ i ].equals( "meta" )) {
				modifiers |= meta1;
			} else if( elem[ i ].equals( "shift" )) {
				modifiers |= InputEvent.SHIFT_MASK;
			} else if( elem[ i ].equals( "meta2" )) {
				modifiers |= meta2;
			} else if( elem[ i ].equals( "alt" )) {
				modifiers |= InputEvent.ALT_MASK;
			} else if( elem[ i ].equals( "ctrl" ) || elem[ i ].equals( "control" )) {
				modifiers |= InputEvent.CTRL_MASK;
			} else if( elem[ i ].equals( "altGraph" )) {
				modifiers |= InputEvent.ALT_GRAPH_MASK;
			} else {
				sb.append( elem[ i ].toUpperCase() );
			}
		}

		if( (modifiers & InputEvent.SHIFT_MASK) != 0 ) {
			sb.insert( 0, "shift " );
		}
		if( (modifiers & InputEvent.CTRL_MASK) != 0 ) {
			sb.insert( 0, "ctrl " );
		}
		if( (modifiers & InputEvent.META_MASK) != 0 ) {
			sb.insert( 0, "meta " );
		}
		if( (modifiers & InputEvent.ALT_MASK) != 0 ) {
			sb.insert( 0, "alt " );
		}
		if( (modifiers & InputEvent.ALT_GRAPH_MASK) != 0 ) {
			sb.insert( 0, "altGraph " );
		}

//		System.out.println( "now is '" + sb.toString() + "'" );

// !!! DOESN'T WORK SINCE THE STROKE IS "TYPED" NOT "PRESSED"
//		return KeyStroke.getKeyStroke( new Character( c ), modifiers );
//		return KeyStroke.getKeyStroke( c, modifiers );
		return KeyStroke.getKeyStroke( sb.toString() );
	}
	
	protected static void setShortCut( MenuItem mi, String cut )
	{
		final KeyStroke accel = createKeyStroke( cut );
		
		final Action action = mi.getAction();
		action.putValue( Action.ACCELERATOR_KEY, accel );

//System.out.println( "keyStroke : " + accel );
		
		// this is a fucking stupid bug in swing:
		// the accelerator key is not taken after the menu item
		// has been created. the hack is to set its action to
		// null and then back to the action again
		for( Iterator iter = mi.getRealized(); iter.hasNext(); ) {
			final Realized r = (Realized) iter.next();
			final AbstractButton b = (AbstractButton) r.c;
			
			b.setAction( null );
			b.setAction( action );
		}
	}
}