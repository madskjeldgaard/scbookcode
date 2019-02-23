/*
 *  SwingClient.java
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
 *		25-Mar-06	created
 *		01-Oct-06	renamed from OSCClient to SwingClient
 */

package de.sciss.swingosc;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import de.sciss.net.OSCPacket;
//import de.sciss.net.OSCPacketCodec;

/**
 *	Representational object for OSC clients communicating with SwingOSC.
 * 
 *	@author		Hanns Holger Rutz
 *	@version	0.55, 31-Jul-07
 */
public class SwingClient
{
	public final Map			locals	= new HashMap();	// objectID to value
	private SocketAddress		replyAddr;
	private final SwingOSC		osc;
//	private OSCPacketCodec		c;
	
	public SwingClient( SwingOSC osc, SocketAddress replyAddr )
	{
		this.osc		= osc;
		this.replyAddr 	= replyAddr;
//		c				= osc.getDefaultCodec();
	}
	
	public void reply( OSCPacket p )
	throws IOException
	{
//		System.out.println( "sending to " + replyAddr );
		osc.send( p, replyAddr );
	}
	
	public Object getObject( Object id )
	{
		final Object result = locals.get( id );
		if( result != null ) return result;
		return osc.getObject( id );
	}
	
	public void setReplyPort( int port )
	{
		setReplyAddress( new InetSocketAddress( ((InetSocketAddress) replyAddr).getAddress(), port ));
	}
	
	public void setReplyAddress( SocketAddress newAddr )
	{
		replyAddr = newAddr;
	}
	
	public SocketAddress getReplyAddress()
	{
		return replyAddr;
	}
	
//	public void setCodec( OSCPacketCodec c )
//	{
//		this.c 	= c;
//	}
//	
//	public OSCPacketCodec getCodec()
//	{
//		return c;
//	}
	
	public void debugDumpLocals()
	{
		System.out.println( "--- debugDumpLocals for '" + replyAddr + "' ---" );
		for( Iterator iter = locals.entrySet().iterator(); iter.hasNext(); ) {
			Map.Entry me = (Map.Entry) iter.next();
			System.out.println( "   key = '" + me.getKey() + "'; value = '" + me.getValue() + "'" );
		}
	}
}