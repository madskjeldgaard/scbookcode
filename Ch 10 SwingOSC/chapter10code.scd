// Chapter 10 - SwingOSC

// --------
// -------- SECTION 2 - INSTALLATION --------
// --------

//////// configuring startup file
// execute the next line only if you need to explicitly specify a java version
SwingOSC.java = "<javaPath>/java";	// replace <javaPath> here!
SwingOSC.program = "<installPath>/SwingOSC.jar";   // replace <installPath> here!
g = SwingOSC.default;

// --------
// -------- SECTION 3 - A SIMPLE GUI EXAMPLE --------
// --------

//////// booting SwingOSC
g.boot;
// wait until the "SwingOSC : server connected." message appears!

//////// creating and opening a window
~win = JSCWindow.new;
~win.front;

//////// adding a two-state button
(
~playButton = JSCButton( ~win, Rect( 4, 4, 40, 56 ));
~playButton.states = [[ "Play", Color.white, Color.green( 0.4 )],
                      [ "Stop", Color.white, Color.red ]];
~playButton.action = { arg view; "New value is %\n".postf( view.value )};
)

//////// Figure 10.1
// boot the sound synthesis server and prepare a SynthDef for buffer playback
(
s.waitForBoot({
    SynthDef( \bufPlay, { arg buf, amp = 1.0, speed = 1.0;
        Out.ar( 0, Pan2.ar( PlayBuf.ar( 1, buf, speed * BufRateScale.kr( buf ), loop: 1 ) * amp ));
    }).send( s );
    ~buf = Buffer.read( s, "sounds/a11wlk01.wav" );
});
)

// now replace the action function
(
~playButton.action = { arg view;
    if( view.value == 1, {
        ~node = Synth( \bufPlay, [ \buf, ~buf ]);
    }, {
        ~node.free; ~node = nil;
    })
};
)

//////// Figure 10.2
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
~win.setInnerExtent( 360, 72 );  // more suitable dimensions for the window
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

//////// alternative verbose version
// this is an alternative variant that should not be executed:
~label = JSCStaticText( ~win, Rect( 56, 4, 50, 25 ));
~label.align = \right;
~label.string = "Amp:";

//////// remove the variant if you accidentally executed its code
~label.remove;

// --------
// -------- SECTION 4 - USING THE MODEL-VIEW-CONTROLLER PATTERN --------
// --------

//////// Figure 10.4
~node.free; ~node = nil;

(
~model        = Event.new;
~model.amp    = ~amp;
~model.speed  = ~speed;
~model.node   = ~node;
~model.buf    = ~buf;
~model.adjust = { arg mod, key, value, source;
    mod.put( key, value );
    mod.changed( key, value, source )};

UpdateListener.newFor( ~model, { arg upd, mod, value;
    mod.node.set( \amp, value )}, \amp );
UpdateListener.newFor( ~model, { arg upd, mod, value;
    mod.node.set( \speed, value )}, \speed );

~playButton.action = { arg view;
    if( view.value == 1, {
        ~model.node = Synth( \bufPlay, [
            \buf, ~model.buf, \amp, ~model.amp, \speed, ~model.speed ]);
    }, {
        ~model.node.free; ~model.node = nil;
    })};
~ampSlider.action   = { arg view;
    ~model.adjust( \amp, view.value, view )};
~speedSlider.action = { arg view;
    ~model.adjust( \speed, ~speedSpec.map( view.value ), view )};
)

//////// Figure 10.5
(
~ampText = JSCStaticText( ~win, Rect( ~ampSlider.bounds.right + 2, 4, 50, 25 ));
~ampTextUpd = UpdateListener.newFor( ~model, { arg upd, mod, value;
    ~ampText.string = "% dB".format( value.ampdb.round( 0.1 ))}, \amp );
~speedText = JSCStaticText( ~win, Rect( ~speedSlider.bounds.right + 2, 32, 50, 25 ));
~speedTextUpd = UpdateListener.newFor( ~model, { arg upd, mod, value;
    ~speedText.string = "%\\%".format( (value * 100).round( 0.1 ))}, \speed );
)
// note: the sliders must be initially dragged to cause view updates!

//////// algorithmic controller
(
~ctrlFunc = { 60.do({
	~model.adjust( \speed, exprand( 1/8, 8 ), thisFunction );
	0.1.wait })};
)
~ctrlFunc.fork;	// run as a Routine

//////// Figure 10.7
(
~ampSliderUpd = UpdateListener.newFor( ~model, { arg upd, mod, value, source;
    if( source !== ~ampSlider, {
        ~ampSlider.value = value;
    })}, \amp );
~speedSliderUpd = UpdateListener.newFor( ~model, { arg upd, mod, value, source;
    if( source !== ~speedSlider, {
        ~speedSlider.value = ~speedSpec.unmap( value );
    })}, \speed );
)

//////// remove observers when views close
~ampSlider.onClose   = { ~ampSliderUpd.remove };
~speedSlider.onClose = { ~speedSliderUpd.remove };
~ampText.onClose     = { ~ampTextUpd.remove };
~speedText.onClose   = { ~speedTextUpd.remove };

//////// MVC is complete
~ctrlFunc.fork;

// --------
// -------- SECTION 5 - UNDER THE HOOD - THE INNER WORKINGS OF SWINGSOSC --------
// --------

// --------
// -------- SECTION 6 - RESPONDERS AND ASYNCHRONICITY --------
// --------

//////// seeing which messages are sent by the responders back to the client
g.dumpOSC( 0, 1 );
// if the "/info" messages are bothering, stop alive-thread
g.stopAliveThread;
// turning it off again
g.dumpOSC( 0, 0 );

//////// wait requires code to be run inside a Routine
2.wait; "hello".postln;           // not allowed! ("yield was called outside of a Routine.")
fork { 2.wait; "hello".postln };  // ok!

// --------
// -------- SECTION 7 - EXTENDING THE GUI REPERTOIRE USING JSCUSERVIEW --------
// --------

//////// removing the standard speed JSCSlider
~speedSlider.remove;

//////// Figure 10.13
(
~mySlider            = Event.new;
~mySlider.view       = JSCUserView( ~win, Rect( 110, 32, 200, 145 )).focusVisible_( false );
~mySlider.pos        = ~speedSpec.unmap( ~model.speed );
// these will be used in the final draw func:
~mySlider.pressed    = false;
~mySlider.project    = false;
// this will update the slider position and refresh the view:
~mySlider.adjustPos  = { arg slid, pos;
    slid.pos         = pos;
    slid.view.action.value( slid );
    slid.view.refresh };
// these will set the slider track curve and refresh the view:
~mySlider.adjustCurve= { arg slid, curve;
    slid.curve       = curve;
    slid.lines       = curve.slide( 2, 1 ).clump( 2 );
    slid.lineLens    = slid.lines.collect({ arg pair; pair[ 0 ].dist( pair[ 1 ])}).normalizeSum; 
    slid.projections = nil;
    slid.view.refresh };
// a primary draw function just to indicate the view's bounds:
~mySlider.view.drawFunc = { arg view; var b = view.bounds;
    JPen.line( 0 @ 0, b.width @ b.height );
    JPen.line( 0 @ b.height, b.width @Ê0 );
    JPen.stroke };
// make the window a little bigger:
~win.setInnerExtent( 370, 180 );
)

//////// Figure 10.14
(
~mySlider.adjustCurve([ 0 @ 0, 0.25 @ 1, 0.5 @ 0, 1.0 @ 0.5 ]);
~mySlider.view.drawFunc = { arg view;
    var b, scale, lnP1, lnP2, linePos, proj, inner, sum = 0;
    b     = view.bounds;
    // scaling factor for coordinates normalized to 0...1
    scale = b.width @ b.height;
    // set the initial curve coordinate
    JPen.moveTo( ~mySlider.curve[ 0 ] * scale );
    (1..(~mySlider.curve.size-1)).do({ arg i;
        // create the curve by adding successive line segments
        JPen.lineTo( ~mySlider.curve[ i ] * scale );
    });
    // stroke the curve with a 2-pixel wide black pen
    JPen.width = 2;
    JPen.color = Color.black;
    JPen.stroke;
    // draw the knob: iterate over the line segments until
    // the one is found inside which the knob (read from ~mySlider.pos)
    // is located.
    block { arg break;
        ~mySlider.lineLens.do({ arg len, i;
            // if we have found the line segment...
            if( sum + len >= ~mySlider.pos, {
                // calucate the point inside this segment
                // (proj) and draw a filled circle around it
                #lnP1, lnP2    = ~mySlider.lines[ i ];
                linePos        = (~mySlider.pos - sum) / len;
                proj           = lnP1 + ((lnP2 - lnP1) * linePos);
                JPen.fillOval( Rect.aboutPoint( proj * scale, 8, 8 ));
                // if this view is focused, use blue color, otherwise grey
                JPen.fillColor = Color.hsv( 0.7, view.hasFocus.if( 0.7, 0.0 ), 1.0 );
                // if the mouse is pressed, the outline should be thicker
                inner          = if( ~mySlider.pressed, 5, 7 );
                JPen.fillOval( Rect.aboutPoint( proj * scale, inner, inner ));
                break.value;
            });
            sum = sum + len;
        });
    };
    
    // a debugging utility to visualize the point projections
    // of the mouse dragging
    if( ~mySlider.project, {
        ~mySlider.projections.do({ arg pt;
            pt = pt * scale;
            JPen.line( pt + (-5 @ -5), pt + (5 @  5) );
            JPen.line( pt + (-5 @  5), pt + (5 @ -5) );
        });
        JPen.width = 1; JPen.stroke;
    });
};
)

//////// Figure 10.15
(
var mouseFunc = { arg view, x, y, modifiers, buttonNumber, clickCount;
    var b, nx, ny, dx, dy, lnP1, lnP2, linePos, lineLenSq,
        proj, dist, sum = 0, minDist = inf, newValue;
    
    b  = view.bounds;
    nx = x / b.width;
    ny = y / b.height;
    ~mySlider.projections = Array( ~mySlider.lines.size );
    // look up the line segment which is closest to the mouse
    ~mySlider.lines.do({ arg pair, i;
        #lnP1, lnP2 = pair;
        dx          = lnP2.x - lnP1.x;
        dy          = lnP2.y - lnP1.y;
        lineLenSq   = (dx*dx) + (dy*dy);
        dist        = (((nx - lnP1.x) * dx) + ((ny - lnP1.y) * dy)) / lineLenSq;
        proj        = (lnP1.x + (dist * dx)) @ (lnP1.y + (dist * dy));
        if( lnP1.x != lnP2.x, {
            linePos = (proj.x - lnP1.x) / dx;
        }, {
            linePos = (proj.y - lnP1.y) / dy;
        });
        if( linePos < 0, {
            proj = lnP1;
        }, { if( linePos > 1, {
            proj = lnP2;
        })});
        ~mySlider.projections.add( proj );
        dist = proj.dist( nx @ ny );
        if( dist < minDist, {
            newValue = sum + (linePos.clip( 0, 1 ) * ~mySlider.lineLens[i]);
            minDist  = dist;
        });
        sum = sum + ~mySlider.lineLens[ i ];
    });
    ~mySlider.pressed = true;
    ~mySlider.adjustPos( newValue );
};
~mySlider.view.mouseDownAction = mouseFunc;
~mySlider.view.mouseMoveAction = mouseFunc;
~mySlider.view.mouseUpAction   = { arg view; ~mySlider.pressed = false; view.refresh };
~mySlider.view.action          = { arg view;
    ~model.adjust( \speed, ~speedSpec.map( ~mySlider.pos ), ~mySlider )};
)

//////// making the point projections visible
~mySlider.project = true; ~mySlider.view.refresh;

//////// using a more elaborate curve
(
~mySlider.adjustCurve( Array.fill( 20, { arg i;
    Polar( (i + 1) / 40, i / 19 * 4pi ).asPoint + (0.5 @ 0.5) }));
)

//////// adding keyboard control
(
~mySlider.view.keyDownAction = { arg view, char, modifiers, unicode, keycode;
    switch( keycode, 37, {   // decrease slider-value with cursor-left
        ~mySlider.adjustPos( (~mySlider.pos - 0.05).max( 0.0 ))},
    39, {   // increase slider-value with cursor-right
        ~mySlider.adjustPos( (~mySlider.pos + 0.05).min( 1.0 ))})};
)

// --------
// -------- SECTION 8 - GENERIC JAVA SCRIPTING --------
// --------

//////// making incoming and outgoing OSC-messages visible
g.dumpOSC( 1, 1 );

//////// instantiating a JFrame and operating on it
~frame = JavaObject( "javax.swing.JFrame", nil, "Test" );
~frame.setSize( 400, 400 );
~frame.setVisible( true );

//////// adding a file chooser to the frame
~cp          = ~frame.getContentPane__;
~fileChooser = JavaObject( "javax.swing.JFileChooser" );
~cp.add( ~fileChooser );
~cp.revalidate;

//////// receiving a primitive value from the server
fork { ~title = ~frame.getTitle_; ~title.postln };

//////// reading the currently selected file's path
// note that this works only if a file is selected,
// otherwise an error is printed. we leave out the
// error handling here for brevity!
(
fork { ~file = ~fileChooser.getSelectedFile__;
       ~fileName = ~file.getAbsolutePath_; ~file.destroy; ~fileName.postln };
)

//////// Figure 10.17
(
~action = { arg fileName; "Selected file is '%'\n".postf( fileName )};
~sResp  = JavaObject( "de.sciss.swingosc.ActionResponder", nil, ~fileChooser.id );
~cResp  = OSCpathResponder( g.addr, [ '/action', ~fileChooser.id ], {
    fork { var file, fileName;
        file     = ~fileChooser.getSelectedFile__;
        fileName = file.getAbsolutePath_;
        file.destroy;
        ~action.value( fileName.asString );
    };
}).add;
)

//////// turn off OSC-dumping
g.dumpOSC( 0, 0 );

// --------
// -------- SECTION 9 - EMBEDDING ARBITRARY SWING VIEWS IN A JSCWINDOW --------
// --------

//////// Figure 10.18
(
~cp.remove( ~fileChooser );
~frame.dispose;
~cp.destroy; ~frame.destroy;
~win.setInnerExtent( 760, 340 );   // again a bit bigger
~plug = JSCPlugView( ~win, Rect( 370, 2, 386, 336 ), ~fileChooser );
~action = { arg fileName; var sf, oldBuf;
    // try to open as a sound file. returns nil if it could not be opened
    if( (sf = SoundFile.openRead( fileName )).notNil, {
        sf.close;  // the header info was read, we can close the file
        if( sf.numChannels == 1, {  // allow mono files only
            oldBuf = ~model.buf;
            if( ~model.node.notNil, {  // free old buffer when synth is freed
                UpdateListener.newFor( ~model.node, { arg upd;
                    upd.remove; oldBuf.free }, \n_end );
                ~model.node.register;
            }, {
                oldBuf.free;
            });
            ~model.buf = Buffer.read( s, fileName );  // replace buffer
        }, {
            "Sound file must be mono".error;
        });
    });
};
)

// --------
// -------- SECTION 10 - USING CUSTOM JAVA CLASSES --------
// --------

// SoundFileChooser.java and JSCSoundFileChooser.sc are contained in separate files!

//////// add the SoundFileChooser.jar to the classpath
g.addClasses( "file://<javaSourceCodePath>/SoundFileChooser.jar" );   // replace <javaSourceCodePath> here!

//////// Figure 10.21
(
~plug.remove;	// this implicitly calls ~fileChooser.destroy!
~soundChooser        = JSCSoundFileChooser( ~win, Rect( 370, 2, 386, 306 ));
~soundChooser.path   = "sounds/a11wlk01.wav".absolutePath;
~loadButton          = JSCButton( ~win, Rect( 370, 312, 60, 24 )).states_([[ "Load" ]]).enabled_( false );
~infoText            = JSCStaticText( ~win, Rect( 438, 312, 318, 24 ));
~soundChooser.action = { arg view; var enabled = false, info = "", sf;
    if( view.path.notNil and: { (sf = SoundFile.openRead( view.path )).notNil }, {
        sf.close;
        enabled         = sf.numChannels == 1;
        info            = "% audio, %-chan. % % kHz, %".format( sf.headerFormat, sf.numChannels, sf.sampleFormat, sf.sampleRate/1000, (sf.numFrames/sf.sampleRate).asTimeString );
    });
    ~loadButton.enabled = enabled;
    ~infoText.string    = info;
};
~loadButton.action   = { ~action.value( ~soundChooser.path )};
)

//////// Figure 10.22
(
~ampSlider.resize     = 2;
~mySlider.view.resize = 5;
~ampText.resize       = 3;
~speedText.resize     = 3;
~soundChooser.resize  = 6;
~loadButton.resize    = 9;
~infoText.resize      = 9;
~win.resizable        = true;
)
