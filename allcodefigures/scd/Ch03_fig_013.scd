
// Figure 3.13 Making a stuttering gesture using a geometric pattern

(/* a routine for creating a ritardando stutter with panning, you must have
run the code in fig 3.9 so that this routine may find some sounds already loaded into buffers, you can change the index of ~bufferedCues to test the routine on different sounds */


~stut = Routine( { var dur, pos;
~stutPatt = Pseq([Pgeom(0.01, 1.1707, 18), Pn(0.1, 1),Pgeom(0.1, 0.94, 200) ]);
~str= ~stutPatt.asStream;
100.do{
    dur =  ~str.next;
    dur.postln; 	//so we can check values on the post window
    ~sample = Synth("samplePlayer",[\out, 0, \buf,  ~bufferedCues[0], \at, 0.1, \rel, 0.05,\pSpeed, 0.5]);
    dur.wait;
}
});
)

//now play it
~stut.play;
// reset before you play again!
~stut.reset; 
 

