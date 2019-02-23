/*
 *	JSCViews collection 5
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
 */

/**
 *	NOTE: THIS CLASS IS CURRENTLY UNFINISHED. FOR THE TIME BEING,
 *	ALSO MIGLAYOUT IS NOT PART OF THE SWINGOSC DISTRIBUTION. THIS
 *	NEEDS SOME TIME TO FIGURE OUT HOW TO MAKE IT WORK SMOOTHLY.
 *
 *	@author	Hanns Holger Rutz
 *	@version	0.61, 16-Oct-08
 */
//JSCPanel : JSCContainerView {
//	var layID;
//	
//	// ----------------- quasi-interface methods : crucial-lib support -----------------
//
////	asFlowView { ... }
//
//	// ----------------- private instance methods -----------------
//
//	prChildOrder { arg child; ^child.protCmpLayout ?? "" }
//
//	prInitView {
//		jinsets	= Insets( 3, 3, 3, 3 );  // so focus borders of children are not clipped
//		layID	= "lay" ++ this.id;
//		^this.prSCViewNew([
//			[ '/local', "lay" ++ this.id, '[', '/new', "net.miginfocom.swing.MigLayout", ']',
//				this.id, '[', '/new', "de.sciss.swingosc.Panel", '[', '/ref', layID, ']', ']' ]
//		]);
//	}
//	
//	layout_ { arg constraints;
//		server.sendMsg( '/set', layID, \layoutConstraints, constraints );
//	}
//
//	columnLayout_ { arg constraints;
//		server.sendMsg( '/set', layID, \columnConstraints, constraints );
//	}
//
//	rowLayout_ { arg constraints;
//		server.sendMsg( '/set', layID, \rowConstraints, constraints );
//	}
//
//	prClose { arg preMsg, postMsg;
//		^super.prClose( preMsg ++
//			[[ '/free', layID ]], postMsg );
//	}
//}
