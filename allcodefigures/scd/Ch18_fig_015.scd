Figure 18.15
s = Server.local;
s.boot;

(
var score;
var baseNote = 75;
var firstPitch = (baseNote + 0).midicps;
var secondPitch = (baseNote - 4).midicps;
var thirdPitch = (baseNote + 7).midicps;

score = CtkScore.new(
	(a = VSO.new(0.0, 5.0, firstPitch, 0.25, 0.01, 0.21, 2.3, server: s)).score,
	(b = VSO.new(2.2, 4.0, secondPitch, 0.25, 0.01, 0.15, 1.7, server: s)).score,
	(c = VSO.new(3.1, 3.75, thirdPitch, 0.15, 0.3, 0.21, 4, server: s)).score
);
score.play;
)




