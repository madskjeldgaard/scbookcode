JTToggle : JSCUserView{
	var <> value=false;
	
	init{ arg argParent, argBounds;
		super.init(argParent, argBounds);
		background = Color.white
	}
	
	*viewClass{
		^JSCUserView
	}
	
	draw{
		var width, height, offset=2;
		width =  this.bounds.width;
		height = this.bounds.height;
		GUI.pen.use{
			//draw outline and background
			GUI.pen.strokeRect(Rect(offset, offset, width-(offset*2), height-(offset*2)));
			GUI.pen.fillColor_(Color.white);				GUI.pen.fillRect(Rect(offset, offset, width-(offset*2), height-(offset*2)));

			if(value){
				//draw the toggle cross
				GUI.pen.line(Point(offset, height-offset), Point(width-offset, offset));
				GUI.pen.line(Point(offset, offset), Point(width-offset, height-offset));
				GUI.pen.stroke;

			};
		}
	}
	
	//override 
	mouseDown{arg x, y, modifiers, buttonNumber, clickCount;
		value = value.not;
		this.refresh;
		mouseDownAction.value(this, x, y, modifiers, buttonNumber, clickCount);	
	}
}