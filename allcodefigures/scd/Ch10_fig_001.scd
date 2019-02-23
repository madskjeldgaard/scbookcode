Figure 10.1

// boot the sound synthesis server and prepare a SynthDef for buffer playback
(
s.waitForBoot({
    SynthDef( \bufPlay, { arg buf, amp = 1.0, speed = 1.0;
        Out.ar( 0, Pan2.ar( 
PlayBuf.ar( 1, buf, speed * BufRateScale.kr( buf ), loop: 1 ) * amp )
);
    }).add;
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

