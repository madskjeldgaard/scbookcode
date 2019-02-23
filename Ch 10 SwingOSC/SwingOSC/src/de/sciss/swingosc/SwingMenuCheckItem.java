/*
 *  SwingMenuCheckItem.java
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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractButton;

import de.sciss.gui.MenuCheckItem;

/**
 *	@author		Hanns Holger Rutz
 *	@version	0.10, 01-Aug-08
 */
public class SwingMenuCheckItem
extends MenuCheckItem
{
	private final Action action;
	
	public SwingMenuCheckItem( String id, String text )
	{
		super( id, new Action( text ));
		action = (Action) this.getAction();
		action.setCheckItem( this );
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
	
	private static class Action
	extends DispatchAction
	{
		private MenuCheckItem mci;
		
		protected Action( String name )
		{
			super( name );
		}
		
		protected void setCheckItem( MenuCheckItem mci )
		{
			this.mci = mci;
		}
		
		public void actionPerformed( ActionEvent e )
		{
			boolean state = ((AbstractButton) e.getSource()).isSelected();

			if( mci != null ) mci.setSelected( state );
			
			super.actionPerformed( e );
		}
	}
}