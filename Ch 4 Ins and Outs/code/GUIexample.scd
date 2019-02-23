(
w = Window.new( "Key Example", Rect( 0,0, 150,150 ));
c = UserView( w, Rect( 0, 0, 150, 150 )).background_( Color.white );
c.keyDownAction = { arg view,char,modifiers,unicode,keycode;
	[ char, modifiers, unicode, keycode ].postln;
};
w.front;
)


(
    w = Window( "Mouse Actions", Rect( 200, 400, 350, 180 ), resizable: false );
    a = StaticText( w, Rect( 10, 10, 160, 160 ))
        .string_( "Mouse Pad" )
        .stringColor_( Color.white )
        .align_( \center )
        .background_( Color.blue );
    b = Slider2D( w, Rect( 180, 10, 160, 160 ))
        .canFocus_( false );
    f = { arg view, x, y, modif; var bounds = view.bounds;
		b.setXY( x / bounds.width, 1.0 - (y / bounds.height) );
    };
    a.mouseMoveAction = f;
    a.mouseDownAction = { arg ... args; b.knobColor = Color.red; f.value( *args )};
    a.mouseUpAction = { arg ... args; b.knobColor = Color.clear; f.value( *args )};
    w.front;
)

a.mouseOverAction = f;
w.acceptsMouseOver = true;  // otherwise mouseOverAction won't be called
        
// Mouse events are only fired if the component is enabled.

a.enabled = false;
a.enabled = true;
