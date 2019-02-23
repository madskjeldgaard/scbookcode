// --------
// -------- ALL IN ONE, LEADING TO THE FINAL EXAMPLE --------
// --------

(
s.waitForBoot({
    SynthDef( \bufPlay, { arg buf, amp = 1.0, speed = 1.0;
        Out.ar( 0, Pan2.ar( PlayBuf.ar( 1, buf, speed * BufRateScale.kr( buf ), loop: 1 ) * amp ));
    }).send( s );
    ~buf = Buffer.read( s, "sounds/a11wlk01.wav" );
});

g = SwingOSC.default;
g.waitForBoot({
    // replace <javaSourceCodePath> here!
    g.addClasses( "file://<javaSourceCodePath>/SoundFileChooser.jar" );
    
    ~amp   = 0.5;  // initial amplitude
    ~speed = 1.0;  // initial speed (1.0 is normal, 0.5 is half-speed, etc.)
    
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
    
    ~win = JSCWindow( "Panel", Rect.new( 128, 60, 760, 340 ));
    ~playButton = JSCButton( ~win, Rect( 4, 4, 40, 56 ))
        .states_([[ "Play", Color.white, Color.green( 0.4 )],
                  [ "Stop", Color.white, Color.red ]])
        .action_({ arg view;
            if( view.value == 1, {
                ~model.node = Synth( \bufPlay, [
                    \buf, ~model.buf, \amp, ~model.amp, \speed, ~model.speed ]);
            }, {
                ~model.node.free; ~model.node = nil;
            })
        });

    JSCStaticText( ~win, Rect( 56, 4, 50, 25 )).align_( \right ).string_( "Amp:" );
    ~ampSlider = JSCSlider( ~win, Rect( 110, 4, 200, 25 ))
        .value_( ~amp ) // initial slider position
        .action_({ arg view;
            ~model.adjust( \amp, view.value, view )});
    JSCStaticText( ~win, Rect( 56, 32, 50, 25 )).align_( \right ).string_( "Speed:" );
    ~speedSpec = ControlSpec( 1/8, 8, \exp );

    ~ampText = JSCStaticText( ~win, Rect( ~ampSlider.bounds.right + 2, 4, 50, 25 ));
    ~ampTextUpd = UpdateListener.newFor( ~model, { arg upd, mod, value;
        ~ampText.string = "% dB".format( value.ampdb.round( 0.1 ))}, \amp );
    ~speedText = JSCStaticText( ~win, Rect( ~ampSlider.bounds.right + 2, 32, 50, 25 ));
    ~speedTextUpd = UpdateListener.newFor( ~model, { arg upd, mod, value;
        ~speedText.string = "%\\%".format( (value * 100).round( 0.1 ))}, \speed );

    ~mySlider            = Event.new;
    ~mySlider.view       = JSCUserView( ~win, Rect( 110, 32, 200, 145 )).focusVisible_( false );
    ~mySlider.pos        = ~speedSpec.unmap( ~model.speed );
    ~mySlider.pressed    = false;
    ~mySlider.project    = false;
    ~mySlider.adjustPos  = { arg slid, pos;
        slid.pos         = pos;
        slid.view.action.value( slid );
        slid.view.refresh };
    ~mySlider.adjustCurve= { arg slid, curve;
        slid.curve       = curve;
        slid.lines       = curve.slide( 2, 1 ).clump( 2 );
        slid.lineLens    = slid.lines.collect({ arg pair; pair[ 0 ].dist( pair[ 1 ])}).normalizeSum; 
        slid.projections = nil;
        slid.view.refresh };

    ~mySlider.adjustCurve( Array.fill( 20, { arg i;
        Polar( (i + 1) / 40, i / 19 * 4pi ).asPoint + (0.5 @ 0.5) }));

    ~mySlider.view.drawFunc = { arg view;
        var b, scale, lnP1, lnP2, linePos, proj, inner, sum = 0;
        b     = view.bounds;
        scale = b.width @ b.height;
        JPen.moveTo( ~mySlider.curve[ 0 ] * scale );
        (1..(~mySlider.curve.size-1)).do({ arg i;
            JPen.lineTo( ~mySlider.curve[ i ] * scale );
        });
        JPen.width = 2;
        JPen.color = Color.black;
        JPen.stroke;
        block { arg break;
            ~mySlider.lineLens.do({ arg len, i;
                if( sum + len >= ~mySlider.pos, {
                    #lnP1, lnP2    = ~mySlider.lines[ i ];
                    linePos        = (~mySlider.pos - sum) / len;
                    proj           = lnP1 + ((lnP2 - lnP1) * linePos);
                    JPen.fillOval( Rect.aboutPoint( proj * scale, 8, 8 ));
                    JPen.fillColor = Color.hsv( 0.7, view.hasFocus.if( 0.7, 0.0 ), 1.0 );
                    inner          = if( ~mySlider.pressed, 5, 7 );
                    JPen.fillOval( Rect.aboutPoint( proj * scale, inner, inner ));
                    break.value;
                });
                sum = sum + len;
            });
        };

        if( ~mySlider.project, {
            ~mySlider.projections.do({ arg pt;
                pt = pt * scale;
                JPen.line( pt + (-5 @ -5), pt + (5 @  5) );
                JPen.line( pt + (-5 @  5), pt + (5 @ -5) );
            });
            JPen.width = 1; JPen.stroke;
        });
    };

    ~ampSliderUpd = UpdateListener.newFor( ~model, { arg upd, mod, value, source;
        if( source !== ~ampSlider, {
            ~ampSlider.value = value;
        })}, \amp );
    ~speedSliderUpd = UpdateListener.newFor( ~model, { arg upd, mod, value, source;
        if( source !== ~mySlider, {
            ~mySlider.adjustPos( ~speedSpec.unmap( value ));
        })}, \speed );

    ~ampSlider.onClose   = { ~ampSliderUpd.remove };
    ~mySlider.onClose    = { ~speedSliderUpd.remove };
    ~ampText.onClose     = { ~ampTextUpd.remove };
    ~speedText.onClose   = { ~speedTextUpd.remove };
    
    ~mouseFunc = { arg view, x, y, modifiers, buttonNumber, clickCount;
        var b, nx, ny, dx, dy, lnP1, lnP2, linePos, lineLenSq,
            proj, dist, sum = 0, minDist = inf, newValue;
    
        b  = view.bounds;
        nx = x / b.width;
        ny = y / b.height;
        ~mySlider.projections = Array( ~mySlider.lines.size );
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
    ~mySlider.view.mouseDownAction = ~mouseFunc;
    ~mySlider.view.mouseMoveAction = ~mouseFunc;
    ~mySlider.view.mouseUpAction   = { arg view; ~mySlider.pressed = false; view.refresh };
    ~mySlider.view.action          = { arg view;
        ~model.adjust( \speed, ~speedSpec.map( ~mySlider.pos ), ~mySlider )};

    ~mySlider.view.keyDownAction = { arg view, char, modifiers, unicode, keycode;
        switch( keycode, 37, {   // decrease slider-value with cursor-left
            ~mySlider.adjustPos( (~mySlider.pos - 0.05).max( 0.0 ))},
        39, {   // increase slider-value with cursor-right
            ~mySlider.adjustPos( (~mySlider.pos + 0.05).min( 1.0 ))})};

    ~action = { arg fileName; var sf, oldBuf;
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

    ~ampSlider.resize     = 2;
    ~mySlider.view.resize = 5;
    ~ampText.resize       = 3;
    ~speedText.resize     = 3;
    ~soundChooser.resize  = 6;
    ~loadButton.resize    = 9;
    ~infoText.resize      = 9;
    
    ~win.front;
})
)