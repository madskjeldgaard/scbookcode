Figure 10.2

~amp   = 0.5;  // initial amplitude
~speed = 1.0;  // initial speed (1.0 is normal, 0.5 is half-speed, etc.)
(
~playButton.action = { arg view;
    if( view.value == 1, {
        ~node = Synth( \bufPlay, [ \buf, ~buf, \amp, ~amp, \speed, ~speed ]);
    }, {
        ~node.free; ~node = nil;
    })};
)
~win.setInnerExtent( 370, 72 );  // more suitable dimensions for the window
~win.resizable = false;
JSCStaticText( ~win, Rect( 56, 4, 50, 25 )).align_( \right ).string_( "Amp:" );
(
~ampSlider = JSCSlider( ~win, Rect( 110, 4, 200, 25 ))
    .value_( ~amp ) // initial slider position
    .action_({ arg view;
        ~amp = view.value;
        ~node.set( \amp, ~amp );
    });
)
JSCStaticText( ~win, Rect( 56, 32, 50, 25 )).align_( \right ).string_( "Speed:" );
~speedSpec = ControlSpec( 1/8, 8, \exp );
(
~speedSlider = JSCSlider( ~win, Rect( 110, 32, 200, 25 ))
    .value_( ~speedSpec.unmap( ~speed ))  // initial slider position
    .action_({ arg view;
        ~speed = ~speedSpec.map( view.value );
        ~node.set( \speed, ~speed );
    });
) 

