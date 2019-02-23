/*
 *  OSCSharedBufSetNMsg.java
 *  SwingOSC
 *
 *  Copyright (c) 2004-2008 Hanns Holger Rutz. All rights reserved.
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
 *		17-Dec-05	copied from Meloncillo
 *		22-Feb-06	copied from Inertia
 */

// package de.sciss.inertia.debug;
package de.sciss.swingosc;

import java.io.*;
import java.nio.*;

import de.sciss.net.*;

/**
 *  Specialized OSC message class
 *  for float array buffer transfers.
 *  At the moment only message reception (decoding)
 *  is handled.
 *  An instance of this class uses one presupplied
 *  float[] array to store incoming data. The message
 *  is supposed to be compatible with scsynth's /b_setn
 *  message, and data is copied according to the buffer
 *  offset of the message, such as to keep a "mirror"
 *  of the buffer contents on the SuperCollider side.
 *  buffer contents which would exceed the java buffer
 *  will be rejected.
 *
 *  @author		Hanns Holger Rutz
 *  @version	0.53, 29-Apr-07
 */
public class OSCSharedBufSetNMsg
extends OSCMessage // SpecificOSCMessage
{
    private final float[] floatArray;
    private int bufNum, startIdx, numSamples; //, arrayOffset;
//    private static final byte[] HEADER = "/b_setn\u0000,iii".getBytes();
    private static final String NAME = "/b_setn";
    
    //  [ "/b_setn", bufNum, startIdx, numFrames, float1, float2, float3 ... ] 
	public OSCSharedBufSetNMsg( float[] floatArray )
	{
		super( NAME, NO_ARGS );
        
        this.floatArray = floatArray;
	}
    
    public int getBufferIndex()
    {
        return bufNum;
    }

    public int getStartOffset()
    {
        return startIdx;
    }

    public int getNumSamples()
    {
        return numSamples;
    }

    public float[] getFloatArray()
    {
        return floatArray;
    }

	public int getSpecificSize()
	throws IOException
	{
		return( 8 + ((4 + numSamples + 4) & ~3) + ((3 * numSamples) << 2) );
	}
    
	public OSCMessage decodeSpecific( ByteBuffer b )
	throws BufferUnderflowException, IOException
	{
		int        	i, pos1;
		FloatBuffer	fb;
        
//System.err.println( "/b_setn decoding" );

        // ',iii'
		if( b.getInt() != 0x2C696969 ) throw new OSCException( OSCException.FORMAT, null );

        pos1	= b.position();    // 'ffff'
        i	= b.getInt();
        while( i == 0x66666666 ) i = b.getInt();
        numSamples = b.position() - 4 - pos1;
        switch( i ) {
        case 0x66666600:
        		numSamples += 3;
            break;
        case 0x66660000:
        		numSamples += 2;
            break;
        case 0x66000000:
        	numSamples += 1;
            break;
        case 0x00000000:
            break;
        default:
            if( (i & 0xFF) != 0 ) {
//              OSCPacketCodec.skipToValues( b );
        		while( b.get() != 0x00 ) ;
        		b.position( (b.position() + 3) & ~3 );
            }
            break;
        }

		bufNum      = b.getInt();
        startIdx    = b.getInt();
        
        if( numSamples != b.getInt() ) throw new OSCException( OSCException.FORMAT, null );
		if( startIdx + numSamples > floatArray.length ) throw new OSCException( OSCException.BUFFER, "/b_setn : buffer to big" );

        fb              = b.asFloatBuffer();
//        msg.arrayOffset = 0;
//System.out.println( "decoding float array : "+fb.position()+" / "+fb.limit()+" / "+msg.numFrames );
        fb.get( floatArray, startIdx, numSamples );
        b.position( b.position() + (numSamples << 2) );
        
        return this;
	}
}