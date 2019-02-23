// the traveling speed wind and storm sound for Navegar - sonification.

(
q.numSpeakers = q.numSpeakers ? 8; 
~wind.clear.ar(q.numSpeakers);

~wind = { |wfreq = 100, q = 1, dRate = 3, drift = 2, amp=0.1, x=0, y= -1, focus=10, step=0.333| 
	var n = 8, snd, pos, panamps; 
	x = Ramp.kr(x, step); y = Ramp.kr(y, step);
	
	pos = atan2(y, x) * (-1/pi) + 0.5; // look west;
	
	wfreq = wfreq.lag(0.3); 
	dRate = dRate.lag(0.3);
	q = q.lag(0.3);
	amp = amp.lag(0.3);
	focus = focus.clip(0.1, 10).lag(0.3);
	panamps = PanAz.kr(8, 1, pos, amp, (20 / focus).min(8)) ** focus; 
	panamps = panamps / (panamps.sum.max(1e-15));
	
	snd = LeakDC.ar(
		RLPF.ar({ BrownNoise.ar + WhiteNoise.ar(0.25) } ! n, 
			wfreq * LFDNoise3.kr(dRate ! n, drift).midiratio, 
			q.reciprocal * LFDNoise3.kr(dRate ! n, 0.2, 1)
		), 
		0.925
	) * panamps * amp; 
//	Splay.ar(snd, levelComp: false);
};

q.setWind = { |q, newCoord, event|
	var speed, angle, dirPoint;
	q.lastCoord = q.lastCoord ? newCoord; 
	q.lastAngle = q.lastAngle ? -0.75pi;	// southwest; 
	[q.lastCoord, q.lastAngle ];
	speed = q.sphDist(q.lastCoord[0], q.lastCoord[1], newCoord[0], newCoord[1]) 
		* 0.33 * q.earthRadius;
		
	angle = if (speed > 0, { 
		q.sphAngle(q.lastCoord[0], q.lastCoord[1], newCoord[0], newCoord[1]);
	}, { q.lastAngle });
	dirPoint = Polar(1, angle).asPoint; 
	q.lastCoord = newCoord; 
	q.lastAngle = angle;
 	[ \setWind, \speed, speed.round(0.001), \dirPoint, dirPoint.round(0.001), \event, event].postln;

	event = event ? q.lastEvent ? \normal;
	q.lastEvent = event;
		event.switch(
			\storm, { 
				~wind.set(
					\x, dirPoint.x, \y, dirPoint.y,
					\wfreq, rrand(200, 350), 
					\dRate, rrand(12, 15), \drift, rrand(7, 10), \q, 2.5, 
					\focus, rrand(0.2, 0.4)
				) }, 
			\calm, { 
				~wind.set(
					\x, dirPoint.x, \y, dirPoint.y,
					\wfreq, rrand(45, 55), 
					\dRate, 1, \drift, 2, 
					\q, 1, 
					\focus, rrand(0.2, 0.5)
				) }, 
			\normal, {	
				// \default.postln;
				~wind.set(
					\x, dirPoint.x, \y, dirPoint.y,
					\wfreq, speed * 1 + 50, 
					\dRate, 3, \drift, rrand(2.5, 4), 
					\q, speed * 0.02 + 1, 
					\focus, (speed * 0.3).sqrt
				) }
		);
};
)

/*	// tests
~wind.playN;
~wind.scope;
q.setWind([-6, 37], \storm);
q.setWind([-6, 36], \calm);
q.setWind([-6, 36], \normal);
q.setWind([-6.5, 37], \normal);
q.setWind([-6, 37], \normal);

NodeProxyEditor(~wind);
*/