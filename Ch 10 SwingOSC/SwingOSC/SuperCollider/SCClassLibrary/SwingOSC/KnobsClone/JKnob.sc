// blackrain at realizedsound dot net - 05/2006
//	fix key modifiers bug by Stephan Wittwer 08/2006 - thanks!
//	Knob updates only on value changes - 10/2006
//	GUI.cocoa changes by Sciss - 07/2007
//	03-Feb-08 : works both with CocoaGUI and SwingGUI

// NOTE: this is essentially GUI independant and will removed in
// favour if Knob if the latter is merged with this code.
// (just remove the GUI.useID( ... ) wrapper!)
JKnob {
	classvar <>defaultMode;
	var <>color, <value, last, <>step, hit, <>keystep, <>mode, isCentered = false;
	
	var <view, gui, modDrag, modVert;
	var <>mouseOverAction;
	var <>shift_scale = 100.0, <>ctrl_scale = 10.0, <>alt_scale = 0.1;
	
	*new { arg parent, bounds;
		GUI.useID( \swing, { ^super.new.prInit( parent, bounds )});
	}
	
	*viewClass { ^GUI.userView }

	*initClass {
		defaultMode = \round; // early so this can be changed in startup
//		StartUp.add({ var kit;
//			kit = GUI.schemes[ \cocoa ];
//			if( kit.notNil, { kit.knob = Knob });
//			kit = GUI.schemes[ \swing ];
//			if( kit.notNil, { kit.knob = JKnob });
//		});
	}

	*paletteExample { arg parent, bounds;
		^Knob.new( parent, bounds );
	}
	
	remove {
		view.remove;
	}
	
	bounds {
		^view.bounds;
	}
	
	bounds_ { arg value;
		view.bounds_( value );
	}
		
	prInit { arg parent, bounds;
		gui		= GUI.current;
		mode		= defaultMode;
		keystep	= 0.01;
		step		= 0.01;
		value	= 0.0;
		color	= [ Color.blue( 0.7, 0.5 ), Color.green( 0.8, 0.8 ), Color.black.alpha_( 0.3 ), Color.black.alpha_( 0.7 )];
		if( gui.id === \cocoa, {
			modDrag	= 0x00100000;	// cmd key
			modVert	= 0x00040000;	// ctrl key
		}, {
			modDrag	= 0x00040000;	// ctrl key
			modVert	= 0x00020000;	// shift key
		});
		view		= gui.userView.new( parent, bounds )
			.relativeOrigin_( true )
			.drawFunc_({ arg view; this.prDraw( view )})
			.keyDownAction_({ arg ... args; this.prKeyDown( *args )})
			.mouseDownAction_({ arg ... args; this.prMouseDown( *args )})
			.mouseMoveAction_({ arg ... args; this.prMouseMove( *args )})
			.mouseOverAction_({ arg view ... args; mouseOverAction.value( this, *args )})
			.canReceiveDragHandler_({ this.prCanReceiveDrag })
			.receiveDragHandler_({ this.prReceiveDrag })
			.beginDragAction_({ this.prGetDrag });
	}
	
	prDraw { arg view;
		var startAngle, arcAngle, size, widthDiv2, aw, bounds, center, pen;
		
		bounds	= view.bounds.moveTo( 0, 0 );
		center	= bounds.center;
		size		= bounds.width;
		widthDiv2 = size * 0.5;
		
		pen		= gui.pen;
		
		pen.fillColor = color[2];
		pen.addAnnularWedge(
			center, 
			widthDiv2 - (0.08 * size), 
			widthDiv2, 	
			0.25pi, 
			-1.5pi
		);
		pen.fill;

		if (isCentered.not, {
			startAngle = 0.75pi; 
			arcAngle = 1.5pi * value;
		}, {
			startAngle = -0.5pi; 
			arcAngle = 1.5pi * (value - 0.5);
		});

		pen.fillColor = color[1];
		pen.addAnnularWedge(
			center, 
			widthDiv2 - (0.12 * size), 
			widthDiv2, 	
			startAngle, 
			arcAngle
		);
		pen.fill;

		pen.fillColor = color[0];
		aw = widthDiv2 - (0.14 * size);
		pen.addWedge( center, aw, 0, 2pi );
		pen.fill;

		pen.strokeColor = color[3];
		pen.width = (0.08 * size);
		pen.moveTo( center );
		pen.lineTo( Polar( aw, 0.75pi + (1.5pi * value) ).asPoint + center );
		pen.stroke;
	}

	prMouseDown { arg view, x, y, modifiers, buttonNumber, clickCount;
		hit = view.mousePosition;
		this.prMouseMove( view, x, y, modifiers );
	}
	
	prMouseMove { arg view, x, y, modifiers;
		var pt, angle, newHit, inc;

		if (modifiers & modDrag != modDrag, { // we are not dragging out - apple key
		
			newHit	= view.mousePosition;
			x		= newHit.x;
			y		= newHit.y;
			
//			[ hit, newHit ].postln;
		
			case
			{ (mode == \vert) || (modifiers & modVert == modVert) } { // Control
				inc = (hit.y - y) * step;
//				if ( hit.y > y, {
//					inc = step;
//				}, {
//					if ( hit.y < y, {
//						inc = step.neg;
//					});
//				});
				value = (value + inc).clip(0.0, 1.0);
				if (last != value) {
					view.action.value( this, x, y, modifiers );
					last = value;
					view.refresh;
				}
			}
			{ (mode == \horiz) || (modifiers & 0x00080000 == 0x00080000) } { // Option
				inc = (x - hit.x) * step;
//				if ( hit.x > x, {
//					inc = step.neg;
//				}, {
//					if ( hit.x < x, {
//						inc = step;
//					});
//				});
				value = (value + inc).clip(0.0, 1.0);
				if (last != value) {
					view.action.value( this, x, y, modifiers );
					last = value;
					view.refresh;
				}
			}
			{ mode == \round } {
				pt = view.bounds.moveTo( 0, 0 ).center - newHit;
				angle = Point( pt.y, pt.x.neg ).theta;
				if ((angle >= -0.80pi) && (angle <= 0.80pi), {
					value = [-0.75pi, 0.75pi].asSpec.unmap(angle);
					if (last != value) {
						view.action.value( this, x, y, modifiers );
						last = value;
						view.refresh;
					}
				});

			};
			
			hit = newHit;
		});
	}
	
	value_ { arg val;
		value = val.clip( 0.0, 1.0 );
		view.refresh;
	}

	valueAction_ { arg val;
		value = val.clip( 0.0, 1.0 );
		view.action.value( this );
		view.refresh;
	}

	centered_ { arg bool;
		isCentered = bool;
		view.refresh;
	}
	
	centered {
		^isCentered
	}
	
	action { ^view.action }
	action_ { arg func; view.action_( func )}
	
	visible { ^view.visible }
	visible_ { arg bool; view.visible_( bool )}
	
	enabled { ^view.enabled }
	enabled_ { arg bool; view.enabled_( bool )}
	
	canFocus { ^view.canFocus }
	canFocus_ { arg bool; view.canFocus_( bool )}

	refresh { view.refresh }

	getScale { arg modifiers;
		^case
		{ (modifiers & 0x020000) != 0 } { shift_scale }
		{ (modifiers & 0x040000) != 0 } { ctrl_scale }
		{ (modifiers & 0x080000) != 0 } { alt_scale }
		{ 1 };
	}

	increment { arg zoom=1; ^this.valueAction = this.value + (keystep * zoom) }
	decrement { arg zoom=1; ^this.valueAction = this.value - (keystep * zoom) }

	prKeyDown { arg view, char, modifiers, unicode, keycode;
		// standard keydown
		// rand could also use zoom factors
		if (char == $r, { this.valueAction = 1.0.rand; });
		if (char == $n, { this.valueAction = 0.0; });
		if (char == $x, { this.valueAction = 1.0; });
		if (char == $c, { this.valueAction = 0.5; });
		if (char == $], { this.increment( this.getScale( modifiers )); ^this });
		if (char == $[, { this.decrement( this.getScale( modifiers )); ^this });
		if (unicode == 0xF700, { this.increment( this.getScale( modifiers )); ^this });
		if (unicode == 0xF703, { this.increment( this.getScale( modifiers )); ^this });
		if (unicode == 0xF701, { this.decrement( this.getScale( modifiers )); ^this });
		if (unicode == 0xF702, { this.decrement( this.getScale( modifiers )); ^this });

		^nil		// bubble if it's an invalid key
	}

	prReceiveDrag {
		this.valueAction_( gui.view.currentDrag );
	}
	
	prGetDrag { 
		^value;
	}
	
	prCanReceiveDrag {
		^gui.view.currentDrag.isFloat;
	}
	
	canReceiveDragHandler { ^view.canReceiveDragHandler }
	canReceiveDragHandler_ { arg func; view.canReceiveDragHandler_( func )}
	receiveDragHandler { ^view.receiveDragHandler }
	receiveDragHandler_ { arg func; view.receiveDragHandler_( func )}
	beginDragAction { ^view.beginDragAction }
	beginDragAction_ { arg func; view.beginDragAction_( func )}
	resize_{arg mode; view.resize_(mode)}
	resize{^view.resize}
}