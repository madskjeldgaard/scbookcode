
 // Figure 3.8 Play cues with a simple GUI

(
SynthDef("playbuf", { arg out = 0, buf, gate = 1;
	Out.ar(out,
		PlayBuf.ar(1, buf, BufRateScale.kr(buf), loop: 1.0) 
		* Linen.kr(gate, doneAction: 2) * 0.6; 
		// with 'doneAction: 2' we release synth when fade is done	
) }).add;
~someSounds = "sounds/*".pathMatch.collect{ |path |  Buffer.read(s, path)}; 
n = 0; // a counter
// here's our GUI code
w = Window.new("Simple CuePlayer", Rect(400, 400, 200, 30)).front;
w.view.decorator = FlowLayout(w.view.bounds);
//this will play each cue in turn
Button.new(w, Rect(0, 0, 80, 20)).states_([["Play Cue", Color.black, Color.clear]]).action_({ 
	if(n < ~someSounds.size, {
		if(n != 0, {~nowPlaying.release;}); 
		~nowPlaying = Synth("playbuf", [buf: ~someSounds[n]]); n=n+1;
	});
}); 
//this sets the counter to the first cue
Button.new(w, Rect(0, 0, 80, 20)).states_([["Stop / Reset", Color.black, Color.clear]]).action_({ n=0; ~nowPlaying.release; }); 
// free the buffers when the window is closed
w.onClose = { ~someSounds.do(_.free); };
)





