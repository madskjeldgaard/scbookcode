Figure 5.20
(
var w_func;
w_func = { arg message = "warning!", bounds = Rect(200, 500, 500, 100);
	var window;
	window = Window("message window", bounds).front;
	TextView(window, window.view.bounds.insetBy(10, 10))
		.string = message;	
};
 // provide text, use default bounds
w_func.(String.new.addAll(Array.new.addAll(" Major news! ").pyramid(7)));
)

