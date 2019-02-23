TToggle : SCUserView{
	var <> value=false;
	
	init{ arg argParent, argBounds;
		super.init(argParent, argBounds);
		background = Color.white
	}
	
	*viewClass{
		^SCUserView
	}
	
	draw{
		var width, height, offset=2;
		width =  this.bounds.width;
		height = this.bounds.height;
		Pen.use{
			//draw outline and background
			Pen.strokeRect(Rect(offset, offset, width-(offset*2), height-(offset*2)));
			Pen.fillColor_(Color.white);				Pen.fillRect(Rect(offset, offset, width-(offset*2), height-(offset*2)));

			if(value){
				//draw the toggle cross
				Pen.line(Point(offset, height-offset), Point(width-offset, offset));
				Pen.line(Point(offset, offset), Point(width-offset, height-offset));
				Pen.stroke;

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