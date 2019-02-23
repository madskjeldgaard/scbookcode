// (c) 2006, Thor Magnusson - www.ixi-software.net
// GNU licence - google it.

Grid {

	var <>gridNodes; 
	var tracknode, chosennode, mouseTracker;
	var win, bounds;
	var downAction, upAction, trackAction, backgrDrawFunc;
	//var nodeshape;
	var border, background;
	var columns, rows;
	var fillcolor, fillmode;
	var traildrag, bool;

	*new { arg w, bounds, columns, rows, border; 
		^super.new.initGrid(w, bounds, columns, rows, border);
	}
	
	initGrid { arg w, argbounds, argcolumns, argrows, argborder=false;
		var p, rect;
		bounds = argbounds ? Rect(20, 20, 400, 200);
		bounds = Rect(bounds.left + 0.5, bounds.top + 0.5, bounds.width, bounds.height);

		win = w ? SCWindow("Grid", 
			Rect(10, 250, bounds.left + bounds.width + 40, bounds.top + bounds.height+30));
		win.front;
		tracknode = 0;
		border = argborder;
		columns = argcolumns ? 8;
		rows = argrows ? 12;
		//nodeshape = "square";
		background = Color.clear;
		fillcolor = Color.new255(103, 148, 103);
		fillmode = false;
		traildrag = false;
		bool = false;
		
		gridNodes = Array.newClear(columns) ! rows;
		
		columns.do({arg c;
			rows.do({arg r;
				p = Point(
					bounds.left+(bounds.width/(columns+1))+(c*(bounds.width/(columns+1))),
					bounds.top+(bounds.height/(rows+1))+(r*(bounds.height/(rows+1)))				).round(1);	
				rect = Rect(	p.x - ((bounds.width/(columns+1)/2)), 
							p.y - ((bounds.height/(rows+1)/2)), 
							(bounds.width/(columns+1)), 
							(bounds.height/(rows+1)));
				gridNodes[r][c] = GridNode.new(p, rect, c, r, fillcolor);
			});
		});
				
		mouseTracker = SCUserView(win, bounds)
			.canFocus_(false)
			.mouseBeginTrackFunc_({|me, x, y, mod|
					chosennode = this.findNode(x, y);
					if(chosennode !=nil, {  
						chosennode.state = not(chosennode.state);
						chosennode.realstate = not(chosennode.realstate);
						tracknode = chosennode;
						downAction.value(chosennode.nodeloc);
						this.refresh;	
					});
			})
			.mouseTrackFunc_({|me, x, y, mod|
				chosennode = this.findNode(x, y);
				if(chosennode !=nil, {  
					if(tracknode.point != chosennode.point, {
						if(traildrag == true, { // on dragging mouse
							if(bool == true, { // boolean switching
								chosennode.state = not(chosennode.state);
								chosennode.realstate = not(chosennode.realstate);
							}, {
								chosennode.state = true;
								chosennode.realstate = true;
							});
						},{
							chosennode.state = true;
							tracknode.state = false;
							chosennode.realstate = true;
							tracknode.realstate = false;
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
			.drawFunc_({

			Pen.width = 1;
			background.set; // background color
			Pen.fillRect(bounds); // background fil

			backgrDrawFunc.value; // background draw function
			Color.black.set;
			if(border == true, 
				{Pen.strokeRect(
						Rect(bounds.left, bounds.top, bounds.width+1, bounds.height+1))
				});

			columns.do({arg i;
				Pen.line(
					Point(bounds.left+(bounds.width/(columns+1))+(i*(bounds.width/(columns+1))),
							bounds.top).round(1) + 0.5, 
					Point(bounds.left+(bounds.width/(columns+1))+(i*(bounds.width/(columns+1))),
							bounds.height+bounds.top).round(1) + 0.5
				);
			});
			
			rows.do({arg i;
				Pen.line(
					Point(bounds.left, 
						bounds.top+(bounds.height/(rows+1))+(i*(bounds.height/(rows+1)))).round(1) + 0.5, 
					Point(bounds.width+bounds.left, 
						bounds.top+(bounds.height/(rows+1))+(i*(bounds.height/(rows+1)))).round(1) + 0.5
				);
			});
			
			Pen.stroke;
			
			gridNodes.do({arg row;
				row.do({arg node; 
					if(node.state == true, {
						if(node.shape == "circle", {							if(fillmode, { // first fill 
								node.color.set;
								Pen.fillOval(
									Rect(node.point.x - (node.size/2) + 0.5, 
										node.point.y - (node.size/2) + 0.5, 
										node.size, 
										node.size));
								Color.black.set;
							});
							Pen.strokeOval( // then the outline
								Rect(node.point.x - (node.size/2) + 0.5, 
									node.point.y - (node.size/2) + 0.5, 
									node.size, 
									node.size));
						},{	// square
							if(fillmode, {
								node.color.set;
								Pen.fillRect(
									Rect(node.point.x - (node.size/2) + 0.5, 
										node.point.y - (node.size/2) + 0.5, 
										node.size, 
										node.size));
								Color.black.set;
							});
							Pen.strokeRect(
								Rect(node.point.x - (node.size/2) + 0.5, 
									node.point.y - (node.size/2) + 0.5, 
									node.size, 
									node.size));
						});
					});
				});
			});
			Pen.stroke;
			});
	}
	
	// GRID
	setBackgrColor_ {arg color;
		background = color;
		this.refresh;
	}
	
	setBorder_ {arg state;
		border = state;
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
	
	setTrailDrag_{arg mode, argbool=false;
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
	setNodeShape_ {arg row, col, shape;
		if(col == nil, {
			shape = row; // 1st argument is the shape
			gridNodes.do({arg row;
				row.do({arg node; 
					node.shape = shape;
					node.realshape = shape;
				});
			});
		},{
			gridNodes[col][row].shape = shape;
			gridNodes[col][row].realshape = shape;
		});
		this.refresh;
	}
	
	getNodeShape {arg row, col;
		^gridNodes[col][row].shape;
	}
	
	getNodeRealShape {arg row, col;
		^gridNodes[col][row].realshape;
	}
	
	setNodeSize_ {arg size;
		gridNodes.do({arg row;
			row.do({arg node; 
				node.size = size;
			});
		});
		this.refresh;
	}

	// to store various properties of each GridNode
	setPropertyArray {arg array;
		gridNodes.do({arg row;
			row.do({arg node; 
				node.setPropertyArray(array);
			});
		});
	}
	
	// depricated
	setVisible_ {arg row, col, state;
		gridNodes[col][row].setVisible_(state);
		this.refresh;
	}

	setState_ {arg row, col, state;
		if(state.isInteger, {state = state!=0});
		gridNodes[col][row].setState_(state);
		this.refresh;
	}
	
	setRealState_ {arg row, col, state;
		if(state.isInteger, {state = state!=0});
		gridNodes[col][row].realstate = state;
		this.refresh;
	}
	
	getState {arg row, col;
		var state;
		state = gridNodes[col][row].getState;
		^state.binaryValue;
	}	
	
	getRealState {arg row, col;
		var realstate;
		realstate = gridNodes[col][row].getRealState;
		^realstate.binaryValue;
	}	
	
	setNodeColor_ {arg row, col, color;
		gridNodes[col][row].setColor_(color);
		this.refresh;
	}
	
	getNodeColor {arg row, col;
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
				node.realstate = array[r][c]!=0;
			});
		});
		mouseTracker.refresh;
	}
	
	clearGrid {
		gridNodes.do({arg rows, r;
			rows.do({arg node, c; 
				node.state = false;
				node.realstate = false;
			});
		});
		this.refresh;
	}	

	fillGrid {
		gridNodes.do({arg rows, r;
			rows.do({arg node, c; 
				node.state = true;
				node.realstate = true;
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
	
	setBackgrDrawFunc_ { arg func;
		backgrDrawFunc = func;
	}
	
	remove {
		mouseTracker.remove;
		win.refresh;
	}

		
	// local function
	findNode {arg x, y, action;
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

GridNode {
	var <>point, <>state, <>realstate, <>size, <>rect, <>nodeloc, <>color, <>shape, <>realshape;
	var <>propertyArray; // this can be used to store various properties of the GridNode
	
	*new { arg point, rect, column, row, color ; 
		^super.new.initGridNode(point, rect, column, row, color);
	}
	
	initGridNode {arg argpoint, argrect, argcolumn, argrow, argcolor;
		point = argpoint;
		rect = argrect;
		color = argcolor;
		nodeloc = [ argcolumn, argrow ];		
		size = 10;
		state = false;
		realstate = false;
		shape = "square";
		realshape = "square"; // this is a useful property if one is switching on/off
	}
	
	setPropertyArray {arg array;
		propertyArray = array;
	}
	
	getPropertyArray {
		^propertyArray;
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
	
	getRealState {
		^realstate;
	}
		
	setColor_ {arg argcolor;
		color = argcolor;
	}
	
	getColor {
		^color;
	}

}