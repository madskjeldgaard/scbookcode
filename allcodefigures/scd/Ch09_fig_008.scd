
figure 9.8

(
var window, button, composite, slider, numberbox;
window = SCWindow("myWindow", Rect(100,100, 288, 80));
button = SCButton(window, Rect(4,4, 100, 20))
			.states_([["hide"], ["show"]])
			.action_({|v| 
				if(v.value==0){
					composite.visible_(true)	
				}{
					composite.visible_(false)
				}
			});

composite = SCCompositeView(window, Rect(0, 40, 268, 28))
			.background_(Color.blue);
numberbox = SCNumberBox(composite, Rect(4, 4, 40,20));
slider = SCSlider(composite, Rect(48,4, 200, 20));
	
window.front;
)

