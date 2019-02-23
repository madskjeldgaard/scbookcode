/*
 *  Panel.java
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
 *		12-Nov-05	created
 */

package de.sciss.swingosc;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.geom.*;
import javax.swing.BorderFactory;
import javax.swing.JPanel;

/**
 *	An attempt to get gradient backgrounds working
 *	not yet finished.
 *
 *  @author		Hanns Holger Rutz
 *  @version	0.45, 22-Jan-07
 */
public class Panel
extends JPanel
{
	private Color			colrBg;
	private GradientPaint	pntCustomBgProto	= null;
	private GradientPaint	pntCustomBg			= null;
	private int				recentWidth			= -1;
	private int				recentHeight		= -1;
	
	public Panel( LayoutManager layout, boolean isDoubleBuffered )
	{
		super( layout, isDoubleBuffered );
		init();
	}
			  
	public Panel( LayoutManager layout )
	{
		super( layout );
		init();
	}

	public Panel( boolean isDoubleBuffered )
	{
		super( isDoubleBuffered );
		init();
	}

	public Panel()
	{
		super();
		init();
	}
	
	private void init()
	{
		setBorder( BorderFactory.createEmptyBorder( 3, 3, 3, 3 ));
		putClientProperty( "insets", getInsets() );
		setOpaque( false );
		setBackground( new Color( 0, 0, 0, 0 ));
	}

	public void setBackground( Color c )
	{
		pntCustomBgProto	= null;
		pntCustomBg			= null;
		colrBg				= c;
//		setOpaque( (c != null) && (c.getAlpha() == 0xFF) );
//		super.setBackground( c );
		repaint();
	}

	public void setBackground( GradientPaint pnt )
	{
		pntCustomBgProto	= pnt;
		colrBg				= null;
		recentWidth			= -1;	// triggers recalc
//		setOpaque( (pnt.getColor1().getAlpha() == 0xFF) &&
//				   (pnt.getColor2().getAlpha() == 0xFF) );
		repaint();
	}
	
//	public boolean isOptimizedDrawingEnabled() { return false; }
	
	private void recalcGradientPaint()
	{
		final Point2D	pt1	= pntCustomBgProto.getPoint1();
		final Point2D	pt2	= pntCustomBgProto.getPoint2();
		pntCustomBg			= new GradientPaint(
			new Point( (int) (pt1.getX() * recentWidth), (int) (pt1.getY() * recentHeight) ), pntCustomBgProto.getColor1(),
			new Point( (int) (pt2.getX() * recentWidth), (int) (pt2.getY() * recentHeight) ), pntCustomBgProto.getColor2() );
	}

	public void paintComponent( Graphics g )
	{
		final Insets		insets	= getInsets();
		final int			w		= getWidth() - (insets.left + insets.right);
		final int			h		= getHeight() - (insets.top + insets.bottom);
		final Graphics2D	g2	 	= (Graphics2D) g;

		if( pntCustomBgProto != null ) {

			if( (recentWidth != w) || (recentHeight != h) ) {
				recentWidth		= w;
				recentHeight	= h;
				recalcGradientPaint();
			}
			
			g2.translate( insets.left, insets.top );
			g2.setPaint( pntCustomBg );
			g2.fillRect( 0, 0, w, h );
			g2.translate( -insets.left, -insets.top );
		} else if( (colrBg != null) && (colrBg.getAlpha() > 0) ) {
			g2.translate( insets.left, insets.top );
			g2.setPaint( colrBg );
			g2.fillRect( 0, 0, w, h );
			g2.translate( -insets.left, -insets.top );
		} else super.paintComponent( g );
	}
}