/*
 *	UpdateListener
 *	(SwingOSC classes for SuperCollider)
 *
 *	Copyright (c) 2005-2008 Hanns Holger Rutz. All rights reserved.
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
 */

/**
 *	Useful adapter for descendants/update mechanism
 *
 *	@author	Hanns Holger Rutz
 *	@version	0.58, 12-Jan-08
 */
UpdateListener // interface
{
	var <>verbose = false;

	// adapter implementation
	var funcUpdate, objects, filter;

	// ----------------- constructor -----------------

	*new { arg update, what;
		^super.new.prInit( update, what );
	}
	
	// ----------------- quasi-constructor -----------------

	*newFor { arg object, update, what;
		^this.new( update, what ).addTo( object );
	}
	
	// ----------------- public class methods -----------------

	// interface definition
	*names {
		^[ \update ];
	}

	// ----------------- public instance methods -----------------

	addTo { arg object;
		object.addDependant( this );
		if( objects.isNil, {
			objects = IdentitySet[ object ];
		}, {
			if( objects.includes( object ), {
				MethodError( "Cannot attach to the same object more than once", thisMethod ).throw;
			});
			objects.add( object );
		});
	}

	removeFrom { arg object;
		object.removeDependant( this );
		if( objects.includes( object ).not, {
			MethodError( "Was not attached to this object", thisMethod ).throw;
		});
		objects.remove( object );
	}
	
	removeFromAll {
		objects.do({ arg object;
			object.removeDependant( this );
		});
		objects = nil;
	}
	
	// same as removeFromAll ; makes transition from Updater easier
	remove {
		^this.removeFromAll;
	}
	
	isListening {
		^(objects.size > 0);
	}
		
	isListeningTo { arg object;
		^objects.includes( object );
	}
		
	// ----------------- quasi-interface methods -----------------

	update { arg object, what ... args;
		if( verbose, {
			("UpdateListener.update : object = "++object++"; status = "++what).postln;
		});
		if( filter.isNil, {
			funcUpdate.value( this, object, what, *args );
		}, {
			if( what === filter, {
				funcUpdate.value( this, object, *args );
			});
		});
	}

	// ----------------- private instance methods -----------------

	prInit { arg update, what;
		funcUpdate	= update;
		filter		= what;
	}
}