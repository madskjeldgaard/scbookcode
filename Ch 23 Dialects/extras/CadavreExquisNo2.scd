// cadavre exquis no. 2, from sc-users list, October 2005.
// Archives at http://lists.create.ucsb.edu/mailman/listinfo/sc-users


// On 22 Oct 2005, at 22:50, Julian Rohrhuber wrote:

// cadavre exquis no. 2

(
z = { var x = 1.0.rand; { |i, j| (x * i) + (x * j) % 5 } };

y = { |q=0.1|
	Formlet.ar(
		Dust.ar(LFPulse.kr(20 * q, [0.2, 0.2]) * 1000 + 10), 
		q * 400 + 800, 
		0.08, 
		0.1
	)
};

fork {
	var d = y.play;
	var q;
	1.wait;
	3.do {
		3.do {|i|
			q = z.value;
			3.do {|j|
				d.set(\q, q.(i, j));
				2.wait
			}
		}
	};
	d.free;
}
)


//////////////

// On 22 Oct 2005, Tom Hall wrote:

// "curried" cadavre exquis no. 2
// y has got places to go?

(
z = { var x = 1.0.rand; { |i, j| (x * i) + (x * j) % 5 } };

y = { |q=0.1|
	var  distance, pitchRatio, amp;    // see hypot help
	distance = hypot(9, LFSaw.kr(1/6, 0, 100));
	pitchRatio = (344 - Slope.kr(distance)) / 344;
	amp = 300 / distance.squared;
	Formlet.ar(Dust.ar(LFPulse.kr(20 * q, [0.2, 0.2]) * 1000 + 10),
			(q * pitchRatio * 1000) + 750, 0.08, 0.1, amp)
};

fork {
	var d = y.play;
	var q;
	1.wait;
	3.do {
		3.do {|i|
			q = z.value;
			3.do {|j|
				d.set(\q, q.(i, j));
				2.wait
			}
		}
	};
	d.free;
}
)


////////////

// On 25 Oct 2005, kernal wrote:

// cadavre exquis no. 2
// reminds me of YOU ;-)

(
z = { var x = 1.0.rand; { |i, j| (x * i) + (x * j) % 9 } };

y = { |q=0.1|
	
	Limiter.ar(
		Formlet.ar(
			CombC.ar(SinOsc.ar(232+(q*183) + {LFNoise2.ar(q*2332+0.45,9)}.dup,0,1),
				1,q,q*202), q * 808 + 50, 0.02, 0.1,0.5),
		0.9,0.01
	)
};

fork {
	var d = y.play;
	var q;
	1.wait;
	3.do {
		3.do {|i|
			q = z.value;
			3.do {|j|
				d.set(\q, q.(i, j));
				2.wait
			}
		}
	};
	d.free;
}
)


/////////////

// On 28 Oct 2005, Tim Walters wrote:

// cadavre exquis no. 2
// another round

(
z = { var x = 1.0.rand; { |i, j| (x * i) + (x * j) % 9 } };

y = { |q=0.1|

    var a;
    a = SinOsc.ar(232+(q*183)+{LFNoise2.ar(q*q+0.45,9)}.dup,0,1);
    LPF.ar(
        CombC.ar(a,1,Lag.kr(q, q.squared),q*808) * 0.25
    , Lag.kr(q, q.squared) * 808 + 50, (q + 1).reciprocal.squared, 0.1, a)
};

fork {
    var d = y.play;
    var q;
    (1.0.rand + 0.1).wait;
    3.do {
        3.do {|i|
            q = z.value;
            3.do {|j|
                d.set(\q, q.(i, j));
                (2.0.rand + 0.2).wait
            }
        }
    };
    d.free;
}
)


/////////////

// On 28 Oct 2005, Julian Rohrhuber wrote:

// cadavre exquis no. 2
// some more curry

(
z = { var x = 1.0.rand;
	{ |i|
		{ |j| (x * i) + (x * j) % 5 }
	}
};

y = { |q=0.1|

    var a, f;
    var  distance, pitchRatio, amp;    // see hypot help
	distance = hypot(9, LFSaw.kr(1/6, 0.5, 100));
	pitchRatio = (344 - Slope.kr(distance)) / 344;
	amp = 300 / distance.squared;
	f = q * pitchRatio * 1000 + 200;
 	a = SinOsc.ar(f + { LFNoise2.ar(q * q + 0.45, 9) }.dup) * amp * 0.1;
    RHPF.ar(
       	CombC.ar(a, 1, Lag.kr(q, q.squared), q * 808) * 0.25,
   		Lag.kr(q, q.squared) * 808 + 50,
   		(q + 1).reciprocal.squared,
   		0.1,
   		a
   	)
};

fork {
	var d = y.play;
	var q, b;
	(1.0.rand + 0.1).wait;
		3.do {
		b = z.value;
		3.do {|i|
			q = b.(i);
			3.do {|j|
				d.set(\q, q.(j));
				2.wait
			}
		}
	};
	d.free;
}
)


////////////

// On 29 Oct 2005, Jonathan Segal wrote:

// cadavre exquis no. 2
// chunks in the sauce

(
z = { var x = 1.0.rand;
    { |i|
        { |j| (x * i) + (x * j) % 5 }
    }
};

y = { |q=0.1|

    var a, f;
    var  distance, pitchRatio, amp, pulser, ratio;    // see hypot help
        pulser = LFNoise0.kr(q.reciprocal).range (0.125, 8);
    distance = hypot(9, LFCub.kr(pulser, 0.5, 100));
    pitchRatio = (distance.squared - Slope.kr(distance)) / distance.squared;
    amp = 300 / distance.squared;
    f = q * Slope.kr(pitchRatio, 0.002).squared  + 150;
     a = (SinOsc.ar(f + { LFNoise0.ar(q.reciprocal*2, 27) }.dup) * amp).distort;
     ratio=LFNoise2.kr(q).range(0,1);
     CombC.ar(a*ratio, 1, Lag.kr(q/500, q).max(0.0001),q, a*ratio.reciprocal).softclip

};

fork {
    var d = y.play;
    var q, b;
    (1.0.rand + 0.1).wait;
        4.do {
        b = z.value;
        5.do {|i|
            q = b.(i);
            3.do {|j|
                d.set(\q, q.(j));
                0.5.wait
            }
        }
    };
    d.free;
}
)


////////////

// On 29 Oct 2005, Tim Walters wrote:

// cadavre exquis no. 2
// the curry eats itself

(
z = { var x = 1.0.rand;
    { |i|
        { |j| (x * i) + (x * j) % 5 }
    }
};

y = { |q=0.1|

    var a, f, z, c;
    var  distance, pitchRatio, amp, pulser, ratio;    // see hypot help
        pulser = LFNoise0.kr(q.reciprocal).range (0.125, 8);
    distance = hypot(9, LFCub.kr(pulser, 0.5, 100)).reciprocal * 0.001;
    pitchRatio = (distance.squared - Slope.kr(distance)) / distance.squared;
    amp = 300 / distance.squared;
    f = q * Slope.kr(pitchRatio, 0.002).squared  + 150;
     a = (SinOsc.ar(f + { LFNoise0.ar(q.reciprocal*2, 27) }.dup) * amp).distort;
     z = Saw.ar(ZeroCrossing.ar(a) * 0.125).range(0.5, 1);

     ratio=LFNoise2.kr(q).range(0,1);
     c = CombC.ar(a*ratio * z, 1, Lag.kr(q/500, q).max(0.0001) * z, q * z,
     a*ratio.reciprocal * 0.1).clip(-0.25, 0.25);
    Normalizer.ar(Formlet.ar(c, q * 400 + 400, 
    	(q + 1).reciprocal * z * 0.01, z * 0.01), 0.75, 0.1)

};

fork {
    var d = y.play;
    var q, b;
    (1.0.rand + 0.1).wait;
        4.do {
        b = z.value;
        5.do {|i|
            q = b.(i);
            3.do {|j|
                d.set(\q, q.(j));
                ((q.(j) + 1).reciprocal + 0.5).wait
            }
        }
    };
    d.free;
}
)


///////////

// On 29 Oct 2005, Fredrik Olofsson wrote:

// cadavre exquis no. 2
// the country&western curry that ate itself?

(
z = { var x = 1.0.rand;
    { |i|
        { |j| (x * i) + (x * j) % 5 }
    }
};

y = { |q=0.1|
	var a, f, z, c, i, o;
	var  distance, pitchRatio, amp, pulser, ratio;    // see hypot help
	i= LocalIn.ar(2);
	pulser = (Amplitude.kr(i)+q).range(0.15, 2);
	distance = hypot(9, LFCub.kr(pulser, 0.5, 100)).reciprocal * 0.001;
	pitchRatio = (distance.squared - Slope.kr(distance)) / distance.squared;
	amp = 300 / distance.squared;
	f = q * Slope.kr(pitchRatio, 0.002).squared  + 150;
	a = (SinOsc.ar(f + { LFNoise0.ar(q.reciprocal*2, 27) }.dup) * amp).distort;
	z = Saw.ar(ZeroCrossing.ar(a) * 0.125).range(0.5, 1);

	ratio=LFNoise2.kr(q).range(0,1);
	c = CombC.ar(a*ratio * z, 1, Lag.kr(q/500, q).max(0.0001) * z, q * z,
	a*ratio.reciprocal * 0.1).clip(-0.25, 0.25);
	o= Normalizer.ar(Formlet.ar(c, q * 400 + 400, 
		(q + 1).reciprocal * z * 0.01, z * 0.01), 0.75, 0.1);
	LocalOut.ar(DelayN.ar(o, 0.1, 0.1));
	o
};

fork {
	var d = y.play;
	var q, b;
	(1.0.rand + 0.1).wait;
		4.do {
		b = z.value;
		5.do {|i|
			q = b.(i);
			3.do {|j|
				d.set(\q, q.(j));
				((q.(j) + 1).reciprocal + 0.5).wait
			}
		}
	};
	d.free;
}
)

// (couldn't get the banjo sound quite right)
// _f


////////////

// On 29 Oct 2005, John Thompson wrote:

// cadavre exquis no. 2
// the country&western pigeon curry that ate itself?

(
Routine({
7.do({
~zed = { var x = 1.0.rand;
        { |i|
                { |j| (x * i) + (x * j) % 5 }
        }
};

~yed = { |q=0.1|

     var a, f;
     var  distance, pitchRatio, amp;   
        distance = hypot(9, LFSaw.kr(1/6, 0.5, 1000 + 1000.rand));
        pitchRatio = (344 - Slope.kr(distance)) / 344;
        amp = 300 / distance.squared;
        f = q * pitchRatio * 1000 + 200;
        a = SinOsc.ar(f + { LFNoise2.ar(q * q + 0.45, 9) }.dup) * amp * 0.1;
     Pan2.ar(LeakDC.ar(PitchShift.ar(RHPF.ar(
                CombC.ar(a, 0.15, Lag.kr(q, q.squared) 
                	* 0.5, 5 + (q * LFNoise0.kr(15, 100, 808))) * 0.25,
                Lag.kr(q, q.squared) * LFNoise1.kr(15, 1000, 1808) + 50,
                LFNoise1.kr(13, 1.1, (q + 1).reciprocal.squared),
                0.1,
                a
        ), 0.4, 0.5, 2.0, SinOsc.kr(0.1, 0, 0.2,
0.2))), -1.0 + LFNoise1.kr(5, 1, 0))
};

fork {
        var d = ~yed.play;
        var q, b;
        (1.0.rand + 0.1).wait;
                3.do {
                b = ~zed.value;

                3.do {|i|
                        q = b.(i);
                        3.do {|j|
                                d.set(\q, q.(j));
                                2.wait
                        }
                }
        };
        d.free;
};

});

// [code block contniues]
(
z = { var x = 1.0.rand;
     { |i|
         { |j| (x * i) + (x * j) % 5 }
     }
};

y = { |q=0.1|
        var a, f, z, c, i, o;
        var  distance, pitchRatio, amp, pulser, ratio;

        i= LocalIn.ar(2);
        pulser = (Amplitude.kr(i)+q).range(0.15, 2);
        distance = hypot(9, LFCub.kr(pulser, 0.5, 100)).reciprocal * 0.001;
        pitchRatio = (distance.squared - Slope.kr(distance)) / distance.squared;
        amp = 300 / distance.squared;
        f = q * Slope.kr(pitchRatio, 0.002).squared  + 150;
        a = (SinOsc.ar(f + { LFNoise0.ar(q.reciprocal*2, 27) }.dup) * amp).distort;
        z = Saw.ar(ZeroCrossing.ar(a) * 0.125).range(0.5, 1);

        ratio=LFNoise2.kr(q).range(0,1);
        c = CombC.ar(a*ratio * z, 1, Lag.kr(q/500, q).max(0.0001) * z, q * z,
        a*ratio.reciprocal * 0.1).clip(-0.25, 0.25);
        o= Normalizer.ar(
        	Formlet.ar(c, q * 400 + 400, (q + 1).reciprocal * z * 0.01, z * 0.01), 0.75, 0.1
        );
        LocalOut.ar(DelayN.ar(o, 0.1, 0.1));
        o
};

fork {
        var d = y.play;
        var q, b;
        (1.0.rand + 0.1).wait;
                4.do {
                b = z.value;
                5.do {|i|
                        q = b.(i);
                        3.do {|j|
                                d.set(\q, q.(j));
                                ((q.(j) +1).reciprocal + 0.5).wait
                        }
                }
        };
        d.free;
}
);

}).play
)


////////////////////////

// On 31 Oct 2005, Julian Rohrhuber wrote:

// cadavre exquis no. 2
// the country&western pigeon curry that ate itself but the rubbersnake ?

(
var zed, yed, led;
var f1, f2;

zed = { var x = 1.0.rand;
		{ |i|
				{ |j| (x * i) + (x * j) % 5 }
		}
};

yed = { |q=0.1|

	 var a, f;
	 var  distance, pitchRatio, amp;
		distance = hypot(9, LFSaw.kr(1/6, 0.5, 1000 + 1000.rand));
		pitchRatio = (344 - Slope.kr(distance)) / 344;
		amp = 300 / distance.squared;
		f = q * pitchRatio * 1000 + 200;
		a = SinOsc.ar(f + { LFNoise2.ar(q * q + 0.45, 9) }.dup) * amp * 0.1;
	 Pan2.ar(
		LeakDC.ar(
			PitchShift.ar(RHPF.ar(
				CombC.ar(a, 0.15, Lag.kr(q, q.squared) * 0.5, 
					5 + (q * LFNoise0.kr(15, 100, 808))) * 0.25,
				Lag.kr(q, q.squared) * LFNoise1.kr(15, 1000, 1808) + 50,
				LFNoise1.kr(13, 1.1, (q + 1).reciprocal.squared),
				0.1,
				a
		),
		0.4, 0.5, 2.0, SinOsc.kr(0.1, 0, 0.2, 0.2))),
	  -1.0 + LFNoise1.kr(5, 1, 0))
};

led = { |q=0.1| Pan2.ar(BPF.ar(Impulse.ar(10 * (q+1), 3), 
	q * 1000 + 3000, 0.05) * q.lag(1), q * 2 - 1) };

f1 = { |synthFunc, wait=({2.0 + 0.2.rand}), repeat=3|
			var d = synthFunc.play;
			var q, b;
			wait.value.wait;
		 repeat.do {
				b = zed.value;
				3.do {|i|
						q = b.(i);
						3.do {|j|
	d.set(\q, q.(j));
	wait.value.wait;
						}
				}
		};
		d.free;
};

2.do {
		fork { f1.(yed);  f1.(led, { #[1, 2].choose }, 8); };
		fork {
			6.wait;
			f1.(led, { 0.1.rand + 0.1 }, 18);
		};
	};
)

// End of submissions