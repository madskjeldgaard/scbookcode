/*
 *  MultiplyImageFilter.java
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
 *		13-Dec-05	created
 */
 
package de.sciss.swingosc;

import java.awt.Color;
import java.awt.image.RGBImageFilter;

/**
 *	An RGB filter that multiplies the channels.
 * 
 *	@author		Hanns Holger Rutz
 *	@version	0.3, 13-Dec-05
 */
public class MultiplyImageFilter
extends RGBImageFilter
{
	private int red, green, blue, alpha;
	
	public MultiplyImageFilter()
	{
		super();
		canFilterIndexColorModel = true;
	}

	public MultiplyImageFilter( int red, int green, int blue, int alpha )
	{
		this();
		setColor( red, green, blue, alpha );
	}

	public MultiplyImageFilter( Color c )
	{
		this();
		setColor( c );
	}

	public void setColor( int red, int green, int blue, int alpha )
	{
		this.red		= red;
		this.green	= green;
		this.blue	= blue;
		this.alpha	= alpha;
	}

	public void setColor( Color c )
	{
		this.red		= c.getRed();
		this.green	= c.getGreen();
		this.blue	= c.getBlue();
		this.alpha	= c.getAlpha();
	}

	public int filterRGB( int x, int y, int src )
	{
		return( ((((src & 0xFF) * blue) >> 8)) |
				((((src >> 8) & 0xFF) * green) & 0xFF00) |
				(((((src >> 16) & 0xFF) * red) << 8) & 0xFF0000) |
				(((((src >> 24) & 0xFF) * alpha) << 16) & 0xFF000000)
		);
	}
}
