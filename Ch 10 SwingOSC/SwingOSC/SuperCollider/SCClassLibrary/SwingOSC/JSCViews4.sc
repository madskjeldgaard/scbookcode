/*
 *	JSCViews collection 4
 *	(SwingOSC classes for SuperCollider)
 *
 *	Copyright (c) 2005-2010 Hanns Holger Rutz. All rights reserved.
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
 *	Changelog:
 *		02-Oct-09		added JPeakMeterSettings
 */

/**
 *	@version		0.64, 14-Mar-10
 *	@author		Hanns Holger Rutz
 */
JPeakMeterSettings {
	classvar all;			// IdentityDictionary mapping SwingOSC to Server to JPeakMeterSettings

	var <swing, <scsynth, <refreshRate = 30.0;
	
	*new { arg swing, scsynth;
		^super.newCopyArgs( swing, scsynth );
	}

	*newFrom { arg swing, scsynth;
		var perSwing, settings;
		
		if( all.isNil, {
			all = IdentityDictionary.new;
		});
		perSwing	= all[ swing ];
		if( perSwing.isNil, {
			perSwing = IdentityDictionary.new;
			all[ swing ] = perSwing;
		});
		settings = perSwing[ scsynth ];
		if( settings.isNil, {
			settings	= this.new( swing, scsynth );
			perSwing[ scsynth ] = settings;
		});
		^settings;
	}
	
	*get { arg swing, scsynth;
		var perSwing;
		if( all.isNil, { ^nil });
		perSwing = all[ swing ];
		^if( perSwing.notNil, { perSwing[ scsynth ]});
	}
	
	*setRefreshRate { arg rate, swing, scsynth;
		this.newFrom( swing, scsynth ).refreshRate = rate;
	}
	
	refreshRate_ { arg value;
		var manager;
		if( value != refreshRate, {
			refreshRate	= value;
			manager		= JPeakMeterManager.get( swing, scsynth );
			if( manager.notNil, { manager.refreshRate = refreshRate });
		});
	}
}

JPeakMeterManager {
	classvar all;			// IdentityDictionary mapping JSCSynth to JPeakMeterManager

	var <id;				// server side manager id
	
	var jscsynth, views;
	var fBooted, fPeriod, fQuit;

	var inited = false, created = false;
	
	var verbose = false; // for debugging purposes
	
	// ----------------- quasi-constructor -----------------

	*newFrom { arg swing, scsynth;
		var res, jscsynth;
		
		jscsynth = JSCSynth.newFrom( swing, scsynth );
		if( all.isNil, {
			all	= IdentityDictionary.new;
		});
		res = all[ jscsynth ];
		if( res.isNil, {
			res = this.new( jscsynth );
			all[ jscsynth ] = res;
		});
		^res;
	}
	
	*get { arg swing, scsynth; ^if( all.notNil, { all[ JSCSynth.get( swing, scsynth )]})}
	
	// ----------------- constructor -----------------

	*new { arg jscsynth;
		^super.new.prInit( jscsynth );
	}

	// ----------------- public instance methods -----------------
	refreshRate_ { arg value;
		if( created, {
			jscsynth.swing.sendMsg( '/set', this.id, \refreshRate, (1000 / value).round );
		});
	}
	
	// ----------------- private instance methods -----------------

	prInit { arg argJSCSynth;
		fBooted		= { this.prBooted };
		fPeriod		= { this.prPeriod };
		fQuit		= { this.prQuit };
		ServerTree.add( fBooted );
		CmdPeriod.add( fPeriod );
		ServerQuit.add( fQuit );
		views		= IdentitySet.new;
		jscsynth		= argJSCSynth;
		id			= jscsynth.swing.nextNodeID;
//		jscsynth.swing.listSendMsg([ '/method',
//			'[', '/local', id, '[', '/new', "de.sciss.swingosc.PeakMeterManager", ']', ']',
//			\setServer ] ++ jscsynth.asSwingArg );

		if( jscsynth.scsynth.serverRunning, fBooted );
	}
	
	protRegister { arg view;
		views.add( view );
//		[ "serverRunning", jscsynth.scsynth.serverRunning ].postln;
		if( jscsynth.scsynth.serverRunning && inited, { this.prAddView( view )});
	}

	protUnregister { arg view;
		views.remove( view );
		if( inited, { this.prRemoveView( view )});
		if( views.size == 0, {
			this.prDispose;
		});
	}
	
	prDispose {
		if( verbose, { "DISPOSE".postln });
		ServerTree.remove( fBooted );
		CmdPeriod.remove( fPeriod );
		ServerQuit.remove( fQuit );
		all.removeAt( jscsynth );
		this.prQuit;
		jscsynth = nil;
	}
	
	protSetActive { arg view, active;
		if( jscsynth.scsynth.serverRunning.not, { ^this });
		jscsynth.swing.listSendMsg([ '/method', this.id, \setListenerTask ] ++ view.asSwingArg ++ [ active ]);
	}
	
	prBooted {
		var settings;
		if( verbose, { "BOOTED".postln });
		if( inited.not, {
			inited = true;
			if( created.not, {
				settings = JPeakMeterSettings.get( jscsynth.swing, jscsynth.scsynth );
				jscsynth.swing.listSendMsg([ '/set',
					'[', '/local', id, '[', '/new', "de.sciss.swingosc.PeakMeterManager", ']', ']' ]
					++ if( settings.notNil, {[ \refreshRate, (1000 / settings.refreshRate).round ]}) ++
					[ \server ] ++ jscsynth.asSwingArg );
				created = true;
			});
		});
		views.do({ arg view; this.prAddView( view )});
	}

	prQuit {
		if( verbose, { "QUIT".postln });
		inited = false;
		views.do({ arg view; view.protPeriod });
		if( created, {
			jscsynth.swing.sendBundle( nil, [ '/method', this.id, \dispose ], [ '/free', this.id ]);
			created = false;
		});
	}
	
	prPeriod {
		if( verbose, { ("PERIOD " ++ inited).postln });
		if( inited.not, { ^this });
		inited = false;
		views.do({ arg view;
			view.protPeriod;
			this.prRemoveView( view );
// prBooted is called again, taking care of the re-registering!
//			if( view.protValid, {
//				this.prAddView( view );
//			});
		});
	}

	prRemoveView { arg view;
		if( verbose, { ("REMOVE " ++ view.id).postln });
		jscsynth.swing.listSendMsg([ '/method', this.id, \removeListener ] ++ view.asSwingArg );
	}

	prAddView { arg view;
		var ctrlBus, group;
		if( view.protValid.not, {
			if( verbose, { ("ADDVIEW " ++ view.id ++ " --> IGNORED").postln });
			^this
		});
		if( verbose, { ("ADDVIEW " ++ view.id).postln });
		ctrlBus	= view.protGetCtrlBus;
		group	= view.protGetGroup;
		jscsynth.swing.listSendMsg([ '/method', this.id, \addListener ] ++ view.asSwingArg ++
			[ '[', '/new', "de.sciss.jcollider.Bus" ] ++ jscsynth.asSwingArg ++
			[ view.bus.rate, view.bus.index, view.bus.numChannels, ']' ] ++
			[ '[', '/method', "de.sciss.jcollider.Group", \basicNew ] ++ jscsynth.asSwingArg ++ [ group.nodeID, ']' ] ++
			[ '[', '/new', "de.sciss.jcollider.Bus" ] ++ jscsynth.asSwingArg ++
			[ ctrlBus.rate, ctrlBus.index, ctrlBus.numChannels, ']', view.active, view.protGetNodeID ]);
	}
}

JSCPeakMeter : JSCControlView {
	var <bus, <group, manager;
	var <active = true;
	var <border = false, <caption = false, <captionVisible = true, <captionPosition = \left;
	var <rmsPainted = true, <holdPainted = true;
//	var acResp;	// OSCpathResponder for action listening
	var weCreatedGroup = false;
	var ctrlBus, nodeID;
	var <numChannels = 0;
	var valid = true; // false with user-provided group after cmdperiod
	var <orientation = \v;
	
	*setRefreshRate { arg rate, swing, scsynth;
		JPeakMeterSettings.setRefreshRate( rate, swing ?? { SwingOSC.default }, scsynth ?? { Server.default });
	}

	// ----------------- public instance methods -----------------

	active_ { arg bool;
		if( bool != active, {
			active = bool;
			if( manager.notNil, {
				manager.protSetActive( this, active );
			});
		});
	}
	
	border_ { arg bool;
		if( bool != border, {
			border = bool;
			this.setProperty( \border, border );
		});
	}
	
	caption_ { arg bool;
		if( bool != caption, {
			caption = bool;
			this.setProperty( \caption, caption );
		});
	}

	captionVisible_ { arg bool;
		if( bool != captionVisible, {
			captionVisible = bool;
			this.setProperty( \captionVisible, captionVisible );
		});
	}

	captionPosition_ { arg value;
		if( value != captionPosition, {
			captionPosition = value;
			this.setProperty( \captionPosition, captionPosition );
		});
	}
	
	rmsPainted_ { arg bool;
		if( bool != rmsPainted, {
			rmsPainted = bool;
			this.setProperty( \rmsPainted, rmsPainted );
		});
	}

	holdPainted_ { arg bool;
		if( bool != holdPainted, {
			holdPainted = bool;
			this.setProperty( \holdPainted, holdPainted );
		});
	}
	
	orientation_ { arg orient;
		if( orient != orientation, {
			orientation = orient;
			this.setProperty( \orientation, orientation );
		});
	}

	font { ^this.getProperty( \font )}
	font_ { arg argFont;
		this.setProperty( \font, argFont );
	}

	group_ { arg g;
		if( (g != group) or: { valid.not }, {
			this.prUnregister;
			if( g.notNil and: { bus.notNil and: { g.server != bus.server }}, {
				Error( "Bus and Group cannot be on different servers" ).throw;
			});
			group	= g;
			valid	= true;
			this.prRegister;
		});
	}
	
	bus_ { arg b;
		if( b != bus, {
			// if( (bus.server != b.server) or: { b.numChannels != bus.numChannels }, { ... });
			this.prUnregister;
			if( b.notNil, {
				if( group.notNil and: { group.server != b.server }, {
					Error( "Bus and Group cannot be on different servers" ).throw;
				});
				numChannels	= b.numChannels;
				nodeID		= nil;
//				ctrlBus		= Bus.control( b.server, numChannels << 1 );
				server.sendMsg( '/set', this.id, \numChannels, numChannels );
				bus			= b;
				this.prRegister;
			}, {
//				numChannels 	= 0;
				nodeID		= -1;
				bus			= nil;
			});
		});
	}
	
	numChannels_ { arg ch;
		if( ch != numChannels, {
			if( bus.notNil, {
				Error( "Cannot change numChannels when bus is set" ).throw;
			});
			numChannels = ch;
			server.sendMsg( '/set', this.id, \numChannels, numChannels );
		});
	}

	// ----------------- public class methods -----------------
	
	*meterServer { arg server;
		var win, inBus, outBus, fntSmall, viewWidth, inMeterWidth, outMeterWidth, inMeter, outMeter,
		    inGroup, outGroup, chanWidth = 13, meterHeight = 220, fLab, fBooted, numIn, numOut /*, fPeriod */ ;

		numIn		= server.options.numInputBusChannels;
		numOut		= server.options.numOutputBusChannels;
		inMeterWidth	= numIn * chanWidth + 29;
		outMeterWidth	= numOut * chanWidth + 29;
		viewWidth		= inMeterWidth + outMeterWidth + 11;

	    win		= JSCWindow( server.name ++ " levels (dBFS)", Rect( 5, 305, viewWidth, meterHeight + 26 ), false );
	    inMeter	= JSCPeakMeter( win, Rect( 4, 4, inMeterWidth, meterHeight ))
	    	.border_( true ).caption_( true );
//	    	.numChannels_( inGroup.numChannels );
//	    	.group_( inGroup )
//	    	.bus_( inBus );
	    outMeter	= JSCPeakMeter( win, Rect( inMeterWidth + 8, 4, outMeterWidth, meterHeight ))
	    	.border_( true ).caption_( true );
//	    	.numChannels_( outGroup.numChannels );
//	    	.group_( outGroup )
//	    	.bus_( outBus );
	    	
	    	fntSmall = JFont( "Helvetica", 8 );
	    	
	    	fLab = { arg name, numChannels, xOff; var comp;
	  		comp = JSCCompositeView( win, Rect( xOff, meterHeight + 4, numChannels * chanWidth + 28, 18 ))
	  			.background_( Color.black );
	  		JSCStaticText( comp, Rect( 0, 0, 22, 18 ))
	  			.align_( \right ).font_( fntSmall ).stringColor_( Color.white ).string_( name );
	  		numChannels.do({ arg ch;
		  		JSCStaticText( comp, Rect( 21 + (ch * chanWidth), 0, 20, 18 ))
		  			.align_( \center ).font_( fntSmall ).stringColor_( Color.white ).string_( ch.asString );
	  		});
	    	};
	    	
	    	fLab.value( "in", numIn, 4 );
	    	fLab.value( "out", numOut, 8 + inMeterWidth );
	    	
	    	fBooted = {
//	    		"-----------Yo".postln;
			inGroup			= Group.head( RootNode( server ));
			outGroup			= Group.tail( RootNode( server ));
//[ inGroup, outGroup ].postln;
			outBus			= Bus( \audio, 0, server.options.numOutputBusChannels, server );
			inBus			= Bus( \audio, outBus.numChannels, server.options.numInputBusChannels, server );
			inMeter.group		= inGroup;
			inMeter.bus		= inBus;
			outMeter.group	= outGroup;
			outMeter.bus		= outBus;
	    	};
	    	
//	    	fPeriod = {
//	    		inMeter.bus		= nil;
//	    		inMeter.group		= nil;
//	    		outMeter.bus		= nil;
//	    		outMeter.group	= nil;
//	    	};
	    	
		win.front;

		win.onClose_({
			ServerTree.remove( fBooted );
//			CmdPeriod.remove( fPeriod );
//			ServerQuit.remove( fPeriod );
			inGroup.free; inGroup = nil;
			outGroup.free; outGroup = nil;
		});

		ServerTree.add( fBooted );
//		CmdPeriod.add( fPeriod );
//		ServerQuit.add( fPeriod );
		if( server.serverRunning, fBooted ); // otherwise starts when booted
	}
	
	// ----------------- private instance methods -----------------

	protGetCtrlBus {
		if( ctrlBus.isNil, {
			ctrlBus = Bus.control( bus.server, numChannels << 1 );
		});
		^ctrlBus;
	}
	
	protGetNodeID {
		if( nodeID.isNil, {
			nodeID = Array.fill( numChannels, { bus.server.nextNodeID }).first;
		});
		^nodeID;
	}
	
	protGetGroup {
		if( group.isNil, {
			group			= Group.tail( RootNode( bus.server ));
			weCreatedGroup	= true;
		});
		^group;
	}
	
	protPeriod {
		nodeID = nil;
		if( group.notNil, {
			group = nil;
			if( weCreatedGroup, {
				weCreatedGroup	= false;
			}, {
				valid			= false;
			});
		});
	}
	
	protValid { ^valid }

	prClose { arg preMsg, postMsg;
		this.prUnregister;
		^super.prClose( preMsg, postMsg );
	}

	prInitView {
		^this.prSCViewNew([
			[ '/local', this.id, '[', '/new', "de.sciss.gui.PeakMeterPanel", ']' ]
		]);
	}
	
	prUnregister {
		if( manager.notNil, {
			manager.protUnregister( this );
			manager = nil;
		});
		if( weCreatedGroup, {
			group.free;
			group = nil;
			weCreatedGroup = false;
		});
		ctrlBus.free;
		ctrlBus = nil;
	}
	
	prRegister {
		if( bus.notNil and: { bus.numChannels > 0 }, {
			manager = JPeakMeterManager.newFrom( this.server, bus.server );
			manager.protRegister( this );
		});
	}
		
	prSendProperty { arg key, value;
		key	= key.asSymbol;

		// fix keys
		case { key === \captionPosition }
		{
			switch( value,
			\left,   { value = 2 },
			\right,  { value = 4 },
			\center, { value = 0 }
			);
		}
		{ key === \rmsPainted }
		{
			key = \rMSPainted;
		}
		{ key === \orientation }
		{
			switch( value,
			\v,  { value = 1 },
			\h,  { value = 0 }
			);
[ key, value ].postln;
		};
		^super.prSendProperty( key, value );
	}
}
