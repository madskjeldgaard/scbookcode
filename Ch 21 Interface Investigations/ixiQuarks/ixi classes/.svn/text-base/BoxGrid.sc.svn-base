// (c) 2006, Thor Magnusson - www.ixi-software.net
// GNU licence - google it.

// TODO: rightMouseUpAction and perhaps rightTrackAction

BoxGrid {

	var <>gridNodes; 
	var tracknode, chosennode, mouseTracker;
	var win, bounds;
	var downAction, upAction, trackAction, keyDownAction, rightDownAction, backgrDrawFunc;
	var background;
	var columns, rows;
	var fillcolor, fillmode;
	var traildrag, bool;
	var font, fontColor;
	
	*new { arg w, bounds, columns, rows; 
		^super.new.initBoxGrid(w, bounds, columns, rows);
	}
	
	initBoxGrid { arg w, argbounds, argcolumns, argrows;
		var p, rect;
		bounds = argbounds ? Rect(20, 20, 400, 200);
		bounds = Rect(bounds.left + 0.5, bounds.top + 0.5, bounds.width, bounds.height);

		win = w ? SCWindow("BoxGrid", 
			Rect(10, 250, bounds.left + bounds.width + 40, bounds.top + bounds.height+30));
		win.front;
		tracknode = 0;
		columns = argcolumns ? 6;
		rows = argrows ? 8;
		background = Color.clear;
		fillcolor = Color.new255(103, 148, 103);
		fillmode = true;
		traildrag = false;
		bool = false;
		font = Font("Arial", 9);
		fontColor = Color.black;
		
		gridNodes = Array.newClear(columns) ! rows;
		
		columns.do({arg c;
			rows.do({arg r;
				rect = Rect((bounds.left+(c*(bounds.width/columns))).round(1)+0.5, 
							(bounds.top+(r*(bounds.height/rows))).round(1)+0.5, 
							(bounds.width/columns).round(1), 
							(bounds.height/rows).round(1)
						);

				gridNodes[r][c] = Box.new(rect, c, r, fillcolor);
			});
		});
				
		mouseTracker = SCUserView(win, Rect(bounds.left+1, bounds.top+1, bounds.width, bounds.height))
			.canFocus_(false)
			.mouseBeginTrackFunc_({|me, x, y, mod|
					chosennode = this.findNode(x, y);
					if(mod == 262401, { // right mouse down
						rightDownAction.value(chosennode.nodeloc);
					}, {
						if(chosennode !=nil, {  
							chosennode.state = not(chosennode.state);
							tracknode = chosennode;
							downAction.value(chosennode.nodeloc);
							this.refresh;	
						});
					});
			})
			.mouseTrackFunc_({|me, x, y, mod|
				chosennode = this.findNode(x, y);
				if(chosennode != nil, {  
					if(tracknode.rect != chosennode.rect, {
						if(traildrag == true, { // on dragging mouse
							if(bool == true, { // boolean switching
								chosennode.state = not(chosennode.state);
							}, {
								chosennode.state = true;
							});
						},{
							chosennode.state = true;
							tracknode.state = false;
						});
						tracknode = chosennode;
						trackAction.value(chosennode.nodeloc);
						this.refresh;
					});
				});
			})
			.mouseEndTrackFunc_({|me, x, y, mod|
				chosennode = this.findNode(x, y);
				if(chosennode !=nil, {  
					tracknode = chosennode;
					upAction.value(chosennode.nodeloc);
					this.refresh;
				});
			})
			
			.keyDownFunc_({ |me, key, modifiers, unicode |				keyDownAction.value(key, modifiers, unicode);
				this.refresh;
			})

			.drawFunc_({
			
			Pen.width = 1;
			background.set; // background color
			Pen.fillRect(bounds+0.5); // background fill

			backgrDrawFunc.value; // background draw function
			Color.black.set;

			// Draw the boxes
			gridNodes.do({arg row;
				row.do({arg node; 
					if(node.state == true, {
						if(fillmode, {
							node.color.set;
							Pen.fillRect(node.fillrect);
							Color.black.set;									Pen.strokeRect(node.fillrect);
						},{
							Color.black.set;									Pen.strokeRect(node.fillrect);
						});
						node.string.drawInRect(Rect(node.fillrect.left+5,
				    					node.fillrect.top+(node.fillrect.height/2)-(font.size/1.5), 
				    					80, 16),   
				    					font, fontColor);

					});
				});
			});

			// Draw the grid
			Color.black.set;
			(columns+1).do({arg i;
				Pen.line(
					Point(bounds.left+(i*(bounds.width/columns)),
							bounds.top).round(1) + 0.5, 
					Point(bounds.left+(i*(bounds.width/columns)),
							bounds.height+bounds.top).round(1) + 0.5
				);
			});
			
			(rows+1).do({arg i;
				Pen.line(
					Point(bounds.left, 
						bounds.top+(i*(bounds.height/rows))).round(1) + 0.5, 
					Point(bounds.width+bounds.left, 
						bounds.top+(i*(bounds.height/rows))).round(1) + 0.5
				);
			});
			Pen.stroke;			
			});
	}
	
	// GRID
	setBackgrColor_ {arg color;
		background = color;
		this.refresh;
	}
		
	setFillMode_ {arg mode;
		fillmode = mode;
		this.refresh;
	}
	
	setFillColor_ {arg color;
		gridNodes.do({arg row;
			row.do({arg node; 
				node.setColor_(color);
			});
		});
		this.refresh;
	}
	
	setTrailDrag_{arg mode, argbool=false; // true or false
		traildrag = mode;
		bool = argbool;
	}

	refresh {
		{
		win.isClosed.not.if({ // if window is not closed, update...
			mouseTracker.refresh;
		});
		}.defer;
	}
		
	// NODES	
	setNodeBorder_ {arg border;
		gridNodes.do({arg row;
			row.do({arg node; 
				node.setBorder_(border);
			});
		});
		this.refresh;
	}
	
	// depricated
	setVisible_ {arg row, col, state;
		gridNodes[col][row].setVisible_(state);
		this.refresh;
	}

	setState_ {arg row, col, state;
		if(state.isInteger, {state = state!=0}); // returns booleans
		gridNodes[col][row].setState_(state);
		this.refresh;
	}
	
	getState {arg row, col;
		var state;
		state = gridNodes[col][row].getState;
		^state.binaryValue;
	}	
	
	setBoxColor_ {arg row, col, color;
		//if(state.isInteger, {state = state != 0}); // returns booleans
		gridNodes[col][row].setColor_(color);
		this.refresh;
	}
	
	getBoxColor {arg row, col;
		^gridNodes[col][row].getColor;	
	}
	
	getNodeStates {
		var array;
		array = Array.newClear(columns) ! rows;
		gridNodes.do({arg rows, r;
			rows.do({arg node, c; 
				array[r][c] = node.state.binaryValue;
			});
		});
		^array;
	}
	
	setNodeStates_ {arg array;
		gridNodes.do({arg rows, r;
			rows.do({arg node, c; 
				node.state = array[r][c]!=0;
			});
		});
		this.refresh;
	}
	
	clearGrid {
		gridNodes.do({arg rows, r;
			rows.do({arg node, c; 
				node.state = false;
			});
		});
		this.refresh;
	}	
	
	// PASSED FUNCTIONS OF MOUSE OR BACKGROUND
	nodeDownAction_ { arg func;
		downAction = func;
	}
	
	nodeUpAction_ { arg func;
		upAction = func;
	}
	
	nodeTrackAction_ { arg func;
		trackAction = func;
	}
	
	keyDownAction_ {arg func;
		mouseTracker.canFocus_(true); // in order to detect keys the view has to be focusable
		keyDownAction = func;
	}
	
	rightDownAction_ {arg func;
		rightDownAction = func;
	}
	
	setBackgrDrawFunc_ { arg func;
		backgrDrawFunc = func;
	}
		
	setFont_ {arg f;
		font = f;
	}
	
	setFontColor_ {arg fc;
		fontColor = fc;
	}
	
	setNodeString_ {arg row, col, string;
		gridNodes[col][row].string = string;
		this.refresh;		
	}
	
	getNodeString {arg row, col;
		^gridNodes[col][row].string;
	}

	remove {
		mouseTracker.remove;
		win.refresh;
	}
	// local function
	findNode {arg x, y;
		gridNodes.do({arg row;
			row.do({arg node; 
				if(node.rect.containsPoint(Point.new(x,y)), {
					^node;
				});
			});
		});
		^nil;
	}
}

Box {
	var <>fillrect, <>state, <>border, <>rect, <>nodeloc, <>color;
	var <>string;
	
	*new { arg rect, column, row, color ; 
		^super.new.initGridNode( rect, column, row, color);
	}
	
	initGridNode {arg argrect, argcolumn, argrow, argcolor;
		rect = argrect;
		nodeloc = [ argcolumn, argrow ];	
		color = argcolor;	
		border = 3;
		fillrect = Rect(rect.left+border, rect.top+border, 
					rect.width-(border*2), rect.height-(border*2));
		state = false;
		string = "";
	}
	
	setBorder_ {arg argborder;
		border = argborder;
		fillrect = Rect(rect.left+border, rect.top+border, 
					rect.width-(border*2), rect.height-(border*2));
	}
	
	setVisible_ {arg argstate;
		state = argstate;
	}
	
	setState_ {arg argstate;
		state = argstate;
	}
	
	getState {
		^state;
	}
	
	setColor_ {arg argcolor;
		color = argcolor;
	}
	
	getColor {
		^color;
	}

}