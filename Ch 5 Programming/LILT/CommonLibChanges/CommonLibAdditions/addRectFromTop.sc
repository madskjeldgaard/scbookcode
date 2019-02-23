/* IZ 0305: Convert rect for y to be from top of screen instead of from bottom of screen */
+ Rect {
	fromTop {
		top = SCWindow.screenBounds.height - (top + height + 44);
	}
	fromRight {
		left = SCWindow.screenBounds.width - left - width;
	}
	resize { | argWidth, argHeight |
	// resize rect for window, keeping its top at the original y
			top = top + height - argHeight;
			height = argHeight ? height;
			width = argWidth ? width;
	}
}

/*
SCWindow.screenBounds;
(
5.do{|i|SCWindow("rft",Rect(10*i+300,i,	
	rrand(200, 400),300).fromTop)
	.front.view.background_(Color.black)};
)

SCWindow("1", Rect(0, 0, 200, 200).fromTop).front;
SCWindow("2", Rect(200, 200, 200, 200).fromTop).front;

*/