package de.sciss.swingosc;

import java.awt.image.BufferedImage;

public class SwingImage
extends BufferedImage
{
	public SwingImage( int w, int h, int imageType )
	{
		super( w, h, imageType );
	}
	
	public void setRGB( int startX, int startY, int w, int h, Object[] rgbArray, int offset, int scanSize )
	{
		final int len = rgbArray.length;
		final int[] intArray = new int[ len ];
		for( int i = 0; i < len; i++ ) {
			intArray[ i ] = ((Integer) rgbArray[ i ]).intValue();
		}
		setRGB( startX, startY, w, h, intArray, offset, scanSize );
	}
}
