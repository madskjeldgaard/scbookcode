/*
 *  MediumLightImageFilter.java
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
 *		15-Dec-05	created
 */
 
package de.sciss.swingosc;

import java.awt.Color;
import java.awt.image.RGBImageFilter;

/**
 *	An RGB filter useful for colouring a textured GUI
 *	surface. This applies combinations of screening and
 *	multiplying to create a sensitive colourization that
 *	preserves both the filter colour and the filtered
 *	image's texture and contrast.
 * 
 *	@author		Hanns Holger Rutz
 *	@version		0.35, 15-Dec-05
 */
public class MediumLightImageFilter
extends RGBImageFilter
{
	private int red, green, blue, alpha;
	private int redM, greenM, blueM, alphaM;
	
	public MediumLightImageFilter()
	{
		super();
		canFilterIndexColorModel = true;
	}

	public MediumLightImageFilter( int red, int green, int blue, int alpha )
	{
		this();
		setColor( red, green, blue, alpha );
	}

	public MediumLightImageFilter( Color c )
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
		
		redM			= 0xFF - red;
		greenM		= 0xFF - green;
		blueM		= 0xFF - blue;
		alphaM		= 0xFF - alpha;
	}

	public void setColor( Color c )
	{
		setColor( c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha() );
	}

	// combination of multiply + screen blends
	// (((1.0 - ((1.0 - src) * (1.0 - flt))) * src) + (src * flt * (1.0 - src)))
	// (((65025 - ((255 - src) * (255 - flt))) * src) + (src * flt * (255 - src)))/65025
	// (((0x10000 - ((0xFF - a) * (0xFF - b))) * a) + (a * b * (0xFF - a))) >> 16
	public int filterRGB( int x, int y, int src )
	{
		final int srcRed, srcGreen, srcBlue;
		final int srcRedM, srcGreenM, srcBlueM;
		final int dstRed, dstGreen, dstBlue;
		final int sumRed, sumRedM, sumGreen, sumGreenM, sumBlue, sumBlueM;
		
		srcRed		= (src >> 16) & 0xFF;
		srcRedM		= 0xFF - srcRed;
		sumRed		= srcRed + red;
		sumRedM		= 0x1FE - sumRed;
		dstRed		= ((0x10000 - redM * srcRedM) * sumRed + (srcRed * red * sumRedM)) >> 17;
		srcGreen		= (src >> 8) & 0xFF;
		srcGreenM	= 0xFF - srcGreen;
		sumGreen		= srcGreen + green;
		sumGreenM	= 0x1FE - sumGreen;
		dstGreen		= ((0x10000 - greenM * srcGreenM) * sumGreen + (srcGreen * green * sumGreenM)) >> 17;
		srcBlue		= src & 0xFF;
		srcBlueM		= 0xFF - srcBlue;
		sumBlue		= srcBlue + blue;
		sumBlueM		= 0x1FE - sumBlue;
		dstBlue		= ((0x10000 - blueM * srcBlueM) * sumBlue + (srcBlue * blue * sumBlueM)) >> 17;
		
//		return(	(src & 0xFF000000) |
//				((dstRed << 16) & 0xFF0000) |
//				(((dstGreen) << 8) & 0xFF00) |
//				dstBlue );

		return(	(src & 0xFF000000) |
				(((dstRed * alpha + srcRed * alphaM) << 8) & 0xFF0000) |
				((dstGreen * alpha + srcGreen * alphaM) & 0xFF00) |
				((dstBlue * alpha + srcBlue * alphaM) >> 8) );
	}
}
