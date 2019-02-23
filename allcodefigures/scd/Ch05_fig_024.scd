Figure 5.24
Server.default.boot;  // do this first
(						// then the rest of the program
var window, routine;
window = Window("close me to stop").front;
window.onClose = { routine.stop };
routine = { 
	loop {
		(degree: -10 + 30.xrand, dur: 0.05, amp: 0.1.rand).play;
		0.05.rand.wait;
	}
}.fork;
)


