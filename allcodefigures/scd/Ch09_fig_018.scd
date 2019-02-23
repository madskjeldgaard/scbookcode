
figure 9.18

(
var window, button, createSingletonFunc, singletonWindow;

createSingletonFunc = {|view|
	if(singletonWindow.isNil){
		singletonWindow = SCWindow("singleton").front
			.onClose_({singletonWindow = nil});
	}{
		singletonWindow.front;
	}
};

window = SCWindow("open Singleton", Rect(300,300, 200, 40)).front;
button = SCButton(window, Rect(4,4,192, 30))
			.states_([["singleton"]])
			.action_(createSingletonFunc);
)

