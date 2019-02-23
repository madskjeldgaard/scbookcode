
figure 9.13


(
var window, userView, value=false;
window = SCWindow("toggle view", Rect(200,200, 166, 66)).front;
userView = SCUserView(window, Rect(10,10, 40,40))
				.drawFunc_({|v|
					var width, height, offset=2;
					width =  v.bounds.width;
					height = v.bounds.height;
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
				})
				//switch states on mouse-down
				.mouseDownAction_({|view|
					value = value.not;
					view.refresh;
				});
~win = window;
)



