/*
 *	JSCPlugView
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
 *		26-Jan-08  calls javaObject.prRemove upon close
 */

/*
 *	Simple classes for integrating any subclass of
 *	JComponent (using JSCPlugView) or JPanel (using JSCPlugContainerView)
 *	with other JSCView classes.
 *
 *	@version		0.61, 14-Aug-08
 *	@author		Hanns Holger Rutz
 */
JSCPlugView : JSCView {
	var <javaObject;
	
	// ----------------- constructor -----------------

	*new { arg parent, bounds, javaObject;
		var basic;
		
		basic = super.prBasicNew;
		basic.prSetJavaObject( javaObject );
		^basic.init( parent, bounds, javaObject.id );
	}
	
	prInitView { ^this.prSCViewNew }

	// from JavaObject.sc

	// ----------------- private instance methods -----------------

	doesNotUnderstand { arg ... args;
		var result = javaObject.doesNotUnderstand( *args );
		^if( result === javaObject, this, result );
	}
	
	prSetJavaObject { arg o; javaObject = o }

	prClose { arg preMsg, postMsg;
		javaObject.prRemove;	// because super.prClose frees the ref!
		^super.prClose( preMsg, postMsg );
	}
}

JSCPlugContainerView : JSCContainerView {
	var <javaObject;

	// ----------------- constructor -----------------

	*new { arg parent, bounds, javaObject;
		var basic;
		
		basic = super.prBasicNew;
		basic.prSetJavaObject( javaObject );
		^basic.init( parent, bounds, javaObject.id );
	}
	
	// ----------------- private instance methods -----------------

	prInitView { ^this.prSCViewNew }

	doesNotUnderstand { arg ... args;
		javaObject.doesNotUnderstand( *args );
	}
	
	prSetJavaObject { arg o; javaObject = o }

	prClose { arg preMsg, postMsg;
		javaObject.prRemove;	// because super.prClose frees the ref!
		^super.prClose( preMsg, postMsg );
	}
}