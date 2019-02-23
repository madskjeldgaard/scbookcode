Figure 10.18

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

