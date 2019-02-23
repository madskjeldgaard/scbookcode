/*
 *	Collapse
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
 *	- 31-Mar-06	former defer method is renamed to listDefer, former defer2 method becomes defer !
 *	- 16-Jun-06	added cancel + reschedule methods
 *	- 30-Jan-07	removed TypeSafe calls ; added instantaneous ; uses thisThread.seconds
 *	- 26-Jul-08	added rescheduleWith
 */

/**
 *	The Collapse class is useful for deferring actions to a certain clock
 *	or scheduling them while reducing system load to a minimum. The Collapse
 *	constructor takes a function to be deferred, a delta time span and a clock
 *	to defer to. An action is deferred by calling the defer method with
 *	arbitrary arguments. The function's value method is called with these
 *	arguments after the scheduled delay. When defer is called before the
 *	function was executed, the function is deferred again by the schedule delay
 *	and the pending call is cancelled. The new arguments overwrite the previous
 *	(pending) arguments.
 *
 *	An application example: responding to a MIDI
 *	controller while allowing the user to adjust the MIDI dial within a 100ms
 *	window for example. The function is deferred until no more controller updates
 *	occur for the given delta time of 100ms. Continuous rotations will update the
 *	arguments (the controller value) while postponing the function until the
 *	user releases the dial.
 *
 *	@version	0.58, 31-Dec-07
 *	@author	Hanns Holger Rutz
 */
Collapse : Object
{
	/**
	 *	the arguments passed to defer
	 */
	var <args;
	
	/**
	 *	the deferred function
	 */
	var <func;
	
	/**
	 *	the scheduling delta time
	 */
	var <delta;
	
	/**
	 *	the clock to execute the function within
	 */
	var <clock;
	
	/**
	 *	true if the function was deferred.
	 *	after the function is execute, the started
	 *	value is reset to false
	 */
	var <started	= false;
	
	/**
	 *	true if the cancel was called.
	 *	reset to false when reschedule is called
	 */
	var <cancelled = false;
	
	var execTime, collapseFunc;

	/**
	 *	Creates a new Collapse.
	 *
	 *	@param	func		the function to execute when deferring; nil is allowed
	 *	@param	delta	the amount of time to defer in seconds, defaults to 0.0
	 *	@param	clock	the clock to execute the function within, defaults to AppClock
	 */
	*new { arg func, delta = 0.0, clock;
		^super.new.prInit( func, delta, clock );
	}
	
	prInit { arg argFunc, argDelta, argClock;
		func			= argFunc;
		delta 		= argDelta;
		clock		= argClock ? AppClock;
		
//		TypeSafe.checkArgClasses( thisMethod,
//			[ func, delta, clock ], [ Function, Number, Meta_Clock ], [ true, false, false ]);
		
		collapseFunc	= {
			var now;
			if( cancelled.not, {
				now = thisThread.seconds; // Main.elapsedTime;
				if( now < execTime, {	// too early, reschedule
//					clock.sched( execTime - now + 0.01, collapseFunc );
					execTime - now; // + 0.001; why was this extra delay originally needed? XXX
				}, {					// ok, execute function
					try { func.valueArray( args )} { arg err; err.reportError };
					started = false;
					nil;
				});
			}, {
				started = false;
				nil;
			});
		};
	}
	
	cancel {
		cancelled = true;
	}
	
	/**
	 *	(Re)schedules the function for execution
	 *	with the given list of arguments.
	 *
	 *	@param	args		zero or more arguments which are passed to the function upon execution
	 */
	defer { arg ... args;
		if( cancelled.not, {
			this.prSetArgs( args );
			this.reschedule;
		});
	}
	
	/**
	 *	Resets the scheduling delay to the original delta.
	 *	If the collapse was not yet scheduled, this method will do it.
	 *	The cancel status is cleared.
	 */
	reschedule {
		var newExecTime;
		newExecTime = thisThread.seconds + delta; // Main.elapsedTime + delta;
		if( started.not or: { newExecTime >= execTime }, {
			execTime = newExecTime;
			if( started.not, {
				started		= true;
				cancelled		= false;
				clock.sched( delta, collapseFunc );
			});
		}, {
			execTime = newExecTime;
			"Cannot reduce execution time of already scheduled Collapse!".warn;
		});
	}

	/**
	 *	Resets the scheduling delay to a new given delta.
	 *	If the collapse was not yet scheduled, this method will do it.
	 *	The cancel status is cleared.
	 */
	rescheduleWith { arg newDelta;
		delta = newDelta;
		this.reschedule;
	}

	/**
	 *	Similiarly to defer, this sets the function
	 *	args and schedules the collapse if it hadn't been
	 *	started. Unlike defer, the scheduling delay is
	 *	not reset.
	 *
	 *	@param	args		zero or more arguments which are passed to the function upon execution
	 */
	instantaneous { arg ... args;
		if( started.not, {
			this.defer( *args );
		}, {
			this.prSetArgs( args );
		});
	}
	
	/**
	 *	(Re)schedules the function for execution
	 *	with the arguments provided as an array.	
	 *
	 *	@param	args		an array of zero or more arguments which are passed to the function upon execution
	 */
	listDefer { arg args;
		if( cancelled.not, {
			this.prSetArgs( args );
			this.reschedule;
		});
	}
	
	/**
	 *	Similiarly to defer, this sets the function
	 *	args and schedules the collapse if it hadn't been
	 *	started. Unlike defer, the scheduling delay is
	 *	not reset.
	 *
	 *	@param	args		an array of zero or more arguments which are passed to the function upon execution
	 */
	listInstantaneous { arg args;
		if( started.not, {
			this.listDefer( args );
		}, {
			this.prSetArgs( args );
		});
	}

	// ------------- private -------------

	prSetArgs { arg argArgs;
		args = argArgs;
	}
}
