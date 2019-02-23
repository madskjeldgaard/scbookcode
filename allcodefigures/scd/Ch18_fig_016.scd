
Figure 18.16
s = Server.local;
s.boot;

(
var score;
var baseNote = 75;
var firstPitch = (baseNote + 0).midicps;
var secondPitch = (baseNote - 4).midicps;
var thirdPitch = (baseNote + 7).midicps;

d = [2.4, 1.7];
a = NRT_TimeFrame.new(0.0, 11);
b = NRT_TimeFrame.new(a.starttime + d.at(0), a.endtime - (a.starttime + d.at(0)));
c = NRT_TimeFrame.new(b.starttime + d.at(1), b.endtime - (b.starttime + d.at(1)));

score = CtkScore.new(
	(a = VSO.new(a.starttime, a.duration, firstPitch, 0.25, 0.01, 0.21, 2.3, server: s)).score,
	(b = VSO.new(b.starttime, b.duration, secondPitch, 0.25, 0.01, 0.15, 1.7, server: s)).score,
	(c = VSO.new(c.starttime, c.duration, thirdPitch, 0.15, 0.3, 0.21, 4, server: s)).score
);
score.play;
//N.B. You may notice three "Node not found" warnings.
// This is expected behavior for this version of Ctk
)


