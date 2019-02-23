figure 9.14

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
		SCPen.use{
			//draw outline and background
			SCPen.strokeRect(Rect(offset, offset, width-(offset*2), height-(offset*2)));
			SCPen.fillColor_(Color.white);				
			SCPen.fillRect(Rect(offset, offset, width-(offset*2), height-(offset*2)));

			if(value){
				//draw the toggle cross
				SCPen.line(Point(offset, height-offset), Point(width-offset, offset));
				SCPen.line(Point(offset, offset), Point(width-offset, height-offset));
				SCPen.stroke;

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
