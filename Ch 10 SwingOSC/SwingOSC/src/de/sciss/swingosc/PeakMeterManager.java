/*
 *  PeakMeterManager.java
 *  SwingOSC
 *
 *  Copyright (c) 2008-2009 Hanns Holger Rutz. All rights reserved.
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
 *  	11-Aug-08	created from EisK
 *  	21-Apr-09	created from Nuages, retrofitted java 1.4
 */
package de.sciss.swingosc;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.Timer;

import de.sciss.app.BasicEvent;
import de.sciss.app.EventManager;
import de.sciss.gui.PeakMeterView;
import de.sciss.jcollider.Bus;
import de.sciss.jcollider.Constants;
import de.sciss.jcollider.Control;
import de.sciss.jcollider.GraphElem;
import de.sciss.jcollider.Group;
import de.sciss.jcollider.OSCResponderNode;
import de.sciss.jcollider.Server;
import de.sciss.jcollider.Synth;
import de.sciss.jcollider.SynthDef;
import de.sciss.jcollider.TrigControl;
import de.sciss.jcollider.UGen;
import de.sciss.jcollider.UGenInfo;
import de.sciss.net.OSCBundle;
import de.sciss.net.OSCMessage;

/**
 *  @author		Hanns Holger Rutz
 *  @version	0.63, 11-Oct-09
 */
public class PeakMeterManager
implements OSCResponderNode.Action, Constants, /* ServerListener, */ ActionListener,
		   EventManager.Processor
{
	private List /* <Client> */			collAllClients		= new ArrayList /* <Client> */();
	private List /* <Client> */			collActiveClients	= new ArrayList /* <Client> */();
	private Map /* <PeakMeterView,Client> */ mapClients		= new HashMap /* <PeakMeterView,Client> */();

	private Server					server				= null;
	
//	private Bus						bus					= null;
//	private Group					grp					= null;
//	
//	private int						numCtrlChans		= 0;
	
	private OSCBundle				meterBangBndl		= null;
	private OSCResponderNode		resp				= null;

	private final Timer				meterTimer;
// EEE
//	private final SuperColliderClient sc;
	
	private int						numTask				= 0;
	
	private final EventManager		elm;
	
	private final Set				defSet				= new HashSet();

	public PeakMeterManager() // ( SuperColliderClient sc ) EEE
	{
// EEE
//		this.sc		= sc;
		meterTimer	= new javax.swing.Timer( 33, this );
		elm			= new EventManager( this );
// EEE
//		sc.addServerListener( this );
	}
	
	public void setRefreshRate( int millis )
	{
		final boolean restart = meterTimer.isRunning();
		meterTimer.setDelay( millis );
		if( restart ) meterTimer.restart();
	}
	
	public void dispose()
	{
// EEE
//		sc.removeServerListener( this );
		disposeServer();
	}

	private void meterBang()
	{
		if( (server != null) && (meterBangBndl != null) ) {
			try {
				server.sendBundle( meterBangBndl );
			}
			catch( IOException e1 ) { /* don't print coz the frequency might be high */ }
		}
	}

	// ------------- ActionListener interface -------------

	public void actionPerformed( ActionEvent e )
	{
		meterBang();
	}

//	// ------------- ServerListener interface -------------
//
//	public void serverAction( ServerEvent e )
//	{
//		switch( e.getID() ) {
//		case ServerEvent.STOPPED:
//			setServer( null );
//			break;
//			
//		case ServerEvent.RUNNING:
//			setServer( e.getServer() );
//			break;
//			
//		default:
//			break;
//		}
//	}

	// ----------------- OSCListener interface -----------------
	
	public void respond( OSCResponderNode r, OSCMessage msg, long time )
	{
		elm.dispatchEvent( new Event( r, msg, time ));
	}

	// ----------------- EventManager.Processor interface -----------------

	public void processEvent( BasicEvent be )
	{
		final Event			e			= (Event) be;
		final OSCMessage	msg			= e.msg;
//		final int			busIndex	= ((Number) msg.getArg( 0 )).intValue();
//		final int			numVals		= ((Number) msg.getArg( 1 )).intValue();
// getWhen doesn't provide a valid value i think
//		final long			time		= e.getWhen(); 
		final long			time		= System.currentTimeMillis(); 
		Client				mc;	
	
//		if( (bus == null) || (busIndex != bus.getIndex()) ) return;

		for( int i = 0, off = 0; i < collActiveClients.size(); i++ ) {
			mc	= (Client) collActiveClients.get( i );
			if( (((Number) msg.getArg( off++ )).intValue() != mc.ctrlBus.getIndex()) ||
				(((Number) msg.getArg( off++ )).intValue() != mc.ctrlBus.getNumChannels()) ) return;
			if( mc.task ) {
				if( mc.monoSum ) {
					mc.peakRMSPairs[ 0 ] = ((Number) msg.getArg( off++ )).floatValue();
					mc.peakRMSPairs[ 1 ] = ((Number) msg.getArg( off++ )).floatValue();
				} else {
					for( int j = 0, k = 0; k < mc.srcChans.length; k++) {
						if( mc.srcChans[ k ] >= 0 ) {
							mc.peakRMSPairs[ j++ ] = ((Number) msg.getArg( off++ )).floatValue();
							mc.peakRMSPairs[ j++ ] = ((Number) msg.getArg( off++ )).floatValue();
						} else {
							mc.peakRMSPairs[ j++ ] = 0f;
							mc.peakRMSPairs[ j++ ] = 0f;
							off += 2;
						}
					}
				}
			} else {
				off += mc.ctrlBus.getNumChannels();
			}
		}
		
		for( int i = 0; i < collActiveClients.size(); i++ ) {
			mc = (Client) collActiveClients.get( i );
			if( mc.task ) mc.view.meterUpdate( mc.peakRMSPairs, 0, time );
		}
	}

	private static void printError( String name, Throwable t )
	{
		System.err.println( name + " : " + t.getClass().getName() + " : " + t.getLocalizedMessage() );
	}

	private void disposeServer()
	{
		Client mc;

		meterTimer.stop();
		
		if( resp != null ) resp.remove();
		
		if( server == null ) return;
		
		for( int i = 0; i < collAllClients.size(); ) {
			mc = (Client) collAllClients.get( i );
			if( mc.server == server ) {
				collAllClients.remove( i );
			} else {
				i++;
			}
		}

		collActiveClients.clear();
		server			= null;
		meterBangBndl	= null;
		
		defSet.clear();
	}
	
	public void setServer( Server s )
	{
		disposeServer();
	
		if( s == null ) return;

		server = s;
				
		for( int i = 0; i < collAllClients.size(); i++ ) {
			final Client mc = (Client) collAllClients.get( i );
			if( mc.server == server ) {
				collActiveClients.add( mc );
			}
		}
				
		resp = new OSCResponderNode( server, "/c_setn", this );
		resortClients();
		
		// XXX playToBundle ...
	}
	
	public void setListenerTask( PeakMeterView view, boolean task )
	{
		setListenerTask( view, task, null );
	}
	
	public void setListenerTask( PeakMeterView view, boolean task, OSCBundle bndl )
	{
		if( !EventQueue.isDispatchThread() ) throw new IllegalMonitorStateException();

		final Client mc = (Client) mapClients.get( view );
		if( mc == null ) return;
		if( mc.task != task ) {
			mc.task	= task;
			if( mc.server == server ) {
				final boolean weCreated = bndl == null;
				if( weCreated ) bndl = new OSCBundle();
				for( int j = 0; j < mc.synths.length; j++ ) {
					if( mc.synths[ j ] != null ) {
						bndl.addPacket( mc.synths[ j ].runMsg( task ));
					}
				}
				if( weCreated && (bndl.getPacketCount() > 0) ) {
					try {
						server.sendBundle( bndl );
					}
					catch( IOException e1 ) {
						printError( "setListenerTask", e1 );
					}
				}
				if( task ) {
					if( ++numTask == 1 ) {
						meterTimer.restart();
					}
				} else {
					if( --numTask == 0 ) {
						meterTimer.stop();
					}
				}
			}
		}
	}

	public void addListener( PeakMeterView view, Bus audioBus, Group g, Bus ctrlBus,
							 boolean task )
	{
		addListener( view, audioBus, g, ctrlBus, task, true, -1 );
	}
	
	public void addListener( PeakMeterView view, Bus audioBus, Group g, Bus ctrlBus,
							 boolean task, int nodeID )
	{
		addListener( view, audioBus, g, ctrlBus, task, true, nodeID );
	}
	
	public void addListener( PeakMeterView view, Bus audioBus, Group g, Bus ctrlBus,
							 boolean task, boolean synthDef, int nodeID )
	{
		final int[] channels = new int[ audioBus.getNumChannels() ];

		for( int i = 0, j = audioBus.getIndex(); i < channels.length; ) {
			channels[ i++ ] = j++;
		}
		addListener( view, audioBus.getServer(), channels, g, ctrlBus, task, synthDef, nodeID );
	}

	public void addListener( PeakMeterView view, Server s, int[] channels, Group g,
							 Bus ctrlBus, boolean task, boolean synthDef, int nodeID )
	{
		final Client mc;

		if( !EventQueue.isDispatchThread() ) throw new IllegalMonitorStateException();
	
		mc = new Client( view, s, channels, g, ctrlBus, task, synthDef, nodeID );
		if( mapClients.put( view, mc ) != null ) throw new IllegalArgumentException( "MeterListener was already registered" );
		collAllClients.add( mc );
		if( mc.server == server ) {
			collActiveClients.add( mc );
			resortClients();
			final OSCBundle bndl = new OSCBundle();
			try {
				playToBundle( bndl, mc );
				if( bndl.getPacketCount() > 0 ) {
					server.sendBundle( bndl );
					if( (resp != null) && !resp.isListening() ) resp.add();
					if( numTask > 0 ) meterTimer.restart();
				}
			}
			catch( IOException e1 ) {
				printError( "addListener", e1 );
			}
		}
	}
	
	private void playToBundle( OSCBundle bndl, Client mc )
	throws IOException
	{
		if( mc.monoSum && (mc.srcChans.length > 0) ) {
			mc.synths[ 0 ] = Synth.basicNew( "swing-peak" + mc.srcChans.length, server,
			                                 mc.nodeID == -1 ? server.nextNodeID() : mc.nodeID );
			bndl.addPacket( mc.synths[ 0 ].newMsg( mc.g, new String[] {
				"i_kOtBs" }, new float[] { mc.ctrlBus.getIndex() }, kAddToTail ));
			final float[][] chans = new float[ 1 ][ mc.srcChans.length ];
			for( int i = 0; i < mc.srcChans.length; i++ ) { // no cast from int[] to float[]...
				chans[ 0 ][ i ] = mc.srcChans[ i ];
//				System.out.println( "chans[ 0 ][ " + i + " ] = " + chans[ 0 ][ i ]); 
			}
			bndl.addPacket( mc.synths[ 0 ].setnMsg( new String[] { "i_aInBs" }, chans ));
			if( !mc.task ) {
				bndl.addPacket( mc.synths[ 0 ].runMsg( false ));
			}
			mc.synths[ 0 ].register();
		} else {
			for( int j = 0, m = mc.ctrlBus.getIndex(); j < mc.synths.length; j++, m += 2 ) {
				if( mc.srcChans[ j ] >= 0 ) {
					mc.synths[ j ] = Synth.basicNew( "swing-peak1", server,
					                                 mc.nodeID == -1 ? server.nextNodeID() : (mc.nodeID + j) );
					bndl.addPacket( mc.synths[ j ].newMsg( mc.g, new String[] {
						"i_aInBs",        "i_kOtBs" }, new float[] {
						mc.srcChans[ j ], m }, kAddToTail ));
					if( !mc.task ) {
						bndl.addPacket( mc.synths[ j ].runMsg( false ));
					}
					mc.synths[ j ].register();
				}
			}
		}
	}
	
	private void stopToBundle( OSCBundle bndl, Client mc )
	{
		for( int i = 0; i < mc.synths.length; i++ ) {
			if( mc.synths[ i ] != null ) {
				bndl.addPacket( mc.synths[ i ].freeMsg() );
				mc.synths[ i ] = null;
			}
		}
	}
	
	private SynthDef createDef( int numChannels )
	throws IOException
	{
		if( UGenInfo.infos == null ) UGenInfo.readBinaryDefinitions();
		
		final Control		i_aInBs	= Control.ir( "i_aInBs", new float[ numChannels ]);
		final GraphElem		i_kOtBs	= Control.ir( "i_kOtBs" );
		final GraphElem		t_trig	= TrigControl.kr( "t_trig" );
		final GraphElem		in		= UGen.ar( "In", i_aInBs );
		GraphElem			rms		= UGen.ar( "Lag", UGen.ar( "squared", in ), UGen.ir( 0.1f ));
//		GraphElem			peak	= UGen.ar( "Peak", in, t_trig );
		GraphElem			trigA	= UGen.ar( "Trig1", t_trig, UGen.ir( "SampleDur" ));
//		GraphElem			peak	= UGen.ar( "Peak", in, trigA );
		GraphElem			peak	= UGen.kr( "Peak", in, trigA );
		final GraphElem		out;
		final SynthDef		def;
		GraphElem			temp;

		if( numChannels > 1 ) {
			temp = peak;
			peak = temp.getOutput( 0 );
			for( int i = 1; i < numChannels; i++ ) {
//				peak = UGen.ar( "max", peak, temp.getOutput( i ));
				peak = UGen.kr( "max", peak, temp.getOutput( i ));
			}
			temp = rms;
			rms  = temp.getOutput( 0 );
			for( int i = 1; i < numChannels; i++ ) {
				rms = UGen.ar( "+", rms, temp.getOutput( i ));
			}
			rms = UGen.ar( "*", rms, UGen.ir( 1.0f / numChannels ));
		}

		// we are reading the values asynchronously through
		// a /c_getn on the meter bus. each request is followed
		// by a /n_set to re-trigger the latch so that we are
		// not missing any peak values.
//		out = UGen.kr( "Out", i_kOtBs, UGen.array( UGen.kr( "Latch", peak, t_trig ), rms ));
		out = UGen.kr( "Out", i_kOtBs, UGen.array( peak, rms ));
		
		def = new SynthDef( "swing-peak" + numChannels, out );
//		def.writeDefFile( new java.io.File( "/Users/rutz/Desktop/meters.scsyndef" ));
		return def;
	}
	
	public void removeListener( PeakMeterView view )
	{
		final Client		mc;
		final OSCBundle		bndl;
		
		if( !EventQueue.isDispatchThread() ) throw new IllegalMonitorStateException();

		mc = (Client) mapClients.remove( view );
		if( mc == null ) return;
		collAllClients.remove( mc );
		if( collActiveClients.remove( mc )) {
			bndl = new OSCBundle();
			stopToBundle( bndl, mc );
			if( bndl.getPacketCount() > 0 ) {
				try {
					if( server.isRunning() ) server.sendBundle( bndl );
					resortClients();
				}
				catch( IOException e1 ) {
					printError( "removeMeterListener", e1 );
				}
			}
		}
	}
	
	private void resortClients()
	{
		numTask	= 0;
		
		if( server == null ) {
			meterTimer.stop();
			if( resp != null ) resp.remove();
			meterBangBndl = null;
			return;
		}
		
		final Integer[] cgetnArgs = new Integer[ collActiveClients.size() << 1 ];
		
		meterBangBndl = new OSCBundle();
		final OSCBundle defBndl = new OSCBundle();
		
		for( int i = 0, k = 0; i < collActiveClients.size(); i++ ) {
			final Client mc = (Client) collActiveClients.get( i );
			if( mc.task ) numTask++;
			meterBangBndl.addPacket( new OSCMessage( "/n_set", new Object[] {
				new Integer( mc.g.getNodeID() ), "t_trig", new Integer( 1 )}));
			cgetnArgs[ k++ ] = new Integer( mc.ctrlBus.getIndex() );
			cgetnArgs[ k++ ] = new Integer( mc.ctrlBus.getNumChannels() );
			
			if( mc.synthDef ) {
				final int numChannels = mc.monoSum ? mc.srcChans.length : 1;
				if( numChannels > 0 ) {
					final Object key = new Integer( numChannels );
					if( !defSet.contains( key )) {
						try {
							defBndl.addPacket( createDef( numChannels ).recvMsg() );
							defSet.add( key );
						}
						catch( IOException e1 ) {
							printError( "resortClients", e1 );
						}
					}
				}
			}
		}
		meterBangBndl.addPacket( new OSCMessage( "/c_getn", cgetnArgs ));
		
		try {
			if( defBndl.getPacketCount() > 0 ) {
				server.sync( defBndl, 4f );
			}
			if( (resp != null) && !resp.isListening() ) resp.add();
			if( numTask == 0 ) {
				meterTimer.stop();
			} else {
				meterTimer.start();
			}
		}
		catch( IOException e1 ) {
			printError( "resortClients", e1 );
		}
	}

	// ------------- internal classes -------------
	
	private static class Event
	extends BasicEvent
	{
		protected OSCMessage msg;
		
		protected Event( Object src, OSCMessage msg, long time )
		{
			super( src, 0, time );
			this.msg = msg;
		}

		public boolean incorporate( BasicEvent oldEvent )
		{
			if( (oldEvent instanceof Event) && (oldEvent.getSource() == getSource()) ) {
				final OSCMessage omsg = ((Event) oldEvent).msg;
				if( omsg.getName().equals( msg.getName() ) &&
					(omsg.getArgCount() == msg.getArgCount()) &&
					omsg.getArg( 0 ).equals( msg.getArg( 0 )) &&	// busIndex
					omsg.getArg( 1 ).equals( msg.getArg( 1 ))) {	// numVals
					
					final Object[] fuseArgs = new Object[ msg.getArgCount() ];
					fuseArgs[ 0 ] = msg.getArg( 0 );
					fuseArgs[ 1 ] = msg.getArg( 1 );
					for( int i = 2; i < fuseArgs.length; i++ ) {
						fuseArgs[ i ] = new Float( Math.max(
						    ((Number)  msg.getArg( i )).floatValue(),
						    ((Number) omsg.getArg( i )).floatValue() ));
					}
					msg = new OSCMessage( msg.getName(), fuseArgs );
					return true;
				}
			}
			return false;
		}
	}
	
	private static class Client
	{
		protected final float[]			peakRMSPairs;
		protected final PeakMeterView	view;
		protected final int[]			srcChans;
		protected final Group			g;
		protected final Bus				ctrlBus;
		protected final boolean			monoSum;
		protected final Synth[]			synths;
		protected final Server			server;
		protected boolean				task;
		protected final boolean			synthDef;
		protected final int				nodeID;
		
		protected Client( PeakMeterView view, Server server, int[] srcChans, Group g,
						  Bus ctrlBus, boolean task, boolean synthDef, int nodeID )
		{
			this.view		= view;
			this.server		= server;
			this.srcChans	= srcChans;
			this.g			= g;
			this.ctrlBus	= ctrlBus;
			this.task		= task;
			this.synthDef	= synthDef;
			this.nodeID		= nodeID;
			
//			System.out.print( "new Client() -> srcChans = " );
//			for( int i = 0; i < srcChans.length; i++ ) {
//				System.out.print(  (i == 0 ? "[ " : ", ") + srcChans[ i ]);
//			}
//			System.out.println( " ]" );

			peakRMSPairs	= new float[ ctrlBus.getNumChannels() ];
			monoSum			= (peakRMSPairs.length == 2) && (srcChans.length > 1);
			if( !monoSum && (peakRMSPairs.length != (srcChans.length << 1)) ) {
				throw new IllegalArgumentException( "Audio/Control Bus Channel Mismatch (" + srcChans.length + " vs. " + peakRMSPairs.length + ")" );
			}
			synths			= new Synth[ peakRMSPairs.length >> 1 ];
		}
		
		public String toString()
		{
			final StringBuffer sb = new StringBuffer();
			sb.append( "[ " );
			for( int i = 0; i < srcChans.length; i++ ) {
				if( i > 0 ) sb.append( ", " );
				sb.append( srcChans[ i ]);
			}
			sb.append( " ]" );
			return( "MeterClient( "+view+", "+server+", " + sb.toString() + ", " + g + ", " + task + " )" );
		}
	}
}
