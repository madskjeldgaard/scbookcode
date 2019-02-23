/*
 *	Insets
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
 */

/**
 *	Helper class like java.awt.Insets, but unmutable.
 *	An Insets object is a representation of the borders of a container.
 *	It specifies the space that a container must leave at each of its edges. 
 *
 *	@author	Hanns Holger Rutz
 *	@version	0.61, 12-Jan-09
 */
Insets {
	var <top, <left, <bottom, <right;
	var allZero;
	
	// ----------------- constructor -----------------

	*new { arg top = 0, left = 0, bottom = 0, right = 0;
		^super.newCopyArgs( top, left, bottom, right ).prInit;
	}
	
	// ----------------- public instance methods -----------------

	addTo { arg rect;
		^if( allZero, rect, { rect.insetAll( left, top, right, bottom )});
	}
	
	subtractFrom { arg rect;
		^if( allZero, rect, { rect.insetAll( left.neg, top.neg, right.neg, bottom.neg )});
	}
	
	leftTop {
		^Point( left, top );
	}

	storeArgs { ^[ top, left, bottom, right ]}
	
	// ----------------- private instance methods -----------------

	prInit {
		allZero = (top == 0) and: (left == 0) and: (right == 0) and: (bottom == 0);
	}
}