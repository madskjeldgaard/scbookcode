Tdef(\navegar).stop.play;
Tdef(\navegar).pause;
Tdef(\navegar).resume;


Tdef(\navegar).set(\steptime, 0.3, \nCount, 15, \timelineOff, 0, \postN, 10); 

~wind.resume.play;

~wind.set(\amp, 0.08);

NodeProxyEditor(~wind);

s.recChannels_(q.nChans).prepareForRecord("Navegar_8ch.aiff");
s.record;
Tdef(\navegar).stop.play;
Tdef(\navegar).stop;

p.envir.keysValuesDo { |k, px| if (px.paused.not) { k.postln } }; 

~allChans.stop;
~allChans.play(fadeTime: 2);
~allChans.vol_(0.2);


n = NodeProxyEditor(p[\Paraguay]);

n.proxy_(p[\Uruguay], \Uruguay);

p[\East_Timor].pause;
p[\Mauritius].pause;
p[\Madagascar].resume;
p[\Saint_Helena].resume;
p[\Mozambique].resume;

~all8.filter(5, { |in| Splay.ar(in.keep(8)) ++ Silent.ar(6) });

~all8.playN(
	outs: [ 0, [ 0, 1 ], [ 0, 1 ], 1 ],
	amps: [ 1, [ 0.8, 0.5 ], [ 0.5, 0.8 ], 1 ],
	vol: 1
);

~all8.playNDialog;
s.scope(8)

Document.open(q.dir ++ "/Magellan_Reise.rtf")