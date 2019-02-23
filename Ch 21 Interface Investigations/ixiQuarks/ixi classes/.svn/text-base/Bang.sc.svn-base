
// for those missing the PD GUI !!!

Bang {
	
	var win, bounds;
	var mouseTracker, backgrDrawFunc;
	var background, fillmode, fillcolor;
	var state;
	var downAction, upAction;
	
	*new { arg w, bounds; 
		^super.new.initToggle(w, bounds);
	}
	
	initToggle { arg w, argbounds;

		bounds = argbounds ? Rect(20, 20, 400, 200);
		bounds = Rect(bounds.left + 0.5, bounds.top + 0.5, bounds.width, bounds.height);

		win = w ? SCWindow("Bang", 
			Rect(10, 250, bounds.left + bounds.width + 40, bounds.top + bounds.height+30));
		win.front;
		background = Color.clear;
		fillcolor = Color.yellow;
		fillmode = true;
		state = false;
		
		mouseTracker = SCUserView(win, Rect(bounds.left+1, bounds.top+1, bounds.width, bounds.height))
			.canFocus_(false)
			.mouseBeginTrackFunc_({|me, x, y, mod|
					if(mod == 262401, { // right mouse down
//						rightDownAction.value(chosennode.nodeloc);
					}, {
						state = true;
						downAction.value(state);
						me.refresh;
					});
			})
			.mouseEndTrackFunc_({|me, x, y, mod|
				state = false;
				upAction.value(state);
				me.refresh;
			})
			
			.keyDownFunc_({ |me, key, modifiers, unicode |
//				keyDownAction.value(key, modifiers, unicode);
//				this.refresh;
			})

			.drawFunc_({
			
			Color.black.set;
			Pen.width = 1;
			background.set; // background color
			Pen.fillRect(bounds); // background fill

			backgrDrawFunc.value; // background draw function
			
			Color.black.set;
			Pen.strokeRect(bounds); // stroke toggle rect
			
			if(state == true, {
				fillcolor.set; // background color
				Pen.fillOval(Rect(bounds.left+2, bounds.top+2, bounds.width-4, bounds.height-4));
				Color.black.set;
				Pen.strokeOval(Rect(bounds.left+2, bounds.top+2, bounds.width-4, bounds.height-4));
			});
			
			Pen.stroke;			
			});

	}
	
	setBangDownAction_ { arg func;
		downAction = func;
	}
	
	setBangUpAction_ { arg func;
		upAction = func;
	}

	setBackgrDrawFunc_ { arg func;
		backgrDrawFunc = func;
	}
	
	setBackground_ {arg color;
		background = color;
		mouseTracker.refresh;
	}

	setFillColor_ {arg color;
		fillcolor = color;
		mouseTracker.refresh;
	}
	
	setState_ {arg bool;
		state = bool;
		mouseTracker.refresh;
	}
		

}