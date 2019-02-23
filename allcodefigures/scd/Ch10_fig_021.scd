Figure 10.21

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

