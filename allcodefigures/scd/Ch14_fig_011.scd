Figure 14.11


a = VBAPSpeakerArray.new(3, [[-22.5, 14.97], [22.5, 14.97], [-67.5, 14.97], [67.5, 14.97], [-112.5, 14.97], [112.5, 14.97], [-157.5, 14.97], [157.5, 14.97], [-45, 0], [45, 0], [-90, 0], [90, 0], [-135, 0], [135, 0], [0, 0], [180, 0]]); // zig zag partial dome

b = a.loadToBuffer; // send speaker config to the server

(
// pan around the circle up and down
x = { |azi = 0, ele = 0, spr = 10|
var source;
source = PinkNoise.ar(0.2);
VBAP.ar(16, source, b, LFSaw.kr(0.5, 0).range(-180, 180) * -1, SinOsc.kr(3, 0).range(0, 14.97), spr);
}.play;
)


