// Figure 3.10 Recording the results of making sounds with supercollider

s.boot; // make sure the server is running
(  	// first evaluate this section
b = Buffer.read(s, "sounds/a11wlk01.wav"); // a source
s.prepareForRecord; // prepare the server to record (you must do this first)
)
(	// simultaneously start the processing and recording
s.bind({
	// here's our funky effect
	x = { var columbia, amp; 
		columbia = PlayBuf.ar(1, b, loop: 1);
		amp = Amplitude.ar(columbia, 0.5, 0.5, 4000, 250); // 'sticky' amp follower
		Out.ar(0, Resonz.ar(columbia, amp, 0.02, 3)) // filter; freq follows amp
		}.play;
s.record;
});
)
s.pauseRecording; // pause
s.record // start again
s.stopRecording; // stop recording and close the resulting sound file


