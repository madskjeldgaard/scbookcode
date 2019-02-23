figure 9.6

(
var window;
window = SCWindow("AppClock").front;
Task({
	100.do{|i|
		window.view.background_(Color.grey(i/100));
		0.04.wait;
	}
}).play(AppClock);
)

