Dissonance {
	/** Dissonance curve analisis 
	 * Juan S. Lach Lau, (2006-2007)
	 * 
	 * 	Use: d = Dissonance.make(f, a, start, end, inc, method, max); 
	 * 	where f is an array of frequencies and a is one of amplitudes
	 * 	arguments: start & end are the intervals to sweep thorugh in steps of inc
	 *	the resulting object will contain the analysis in the following instance variables:
	 *
	 * 		dcurve: the dissonance curve itself, use the plot method to visualize
	 *		scale: the scale resulting from the local consonant points (the minima in the plot)
	 *		ratios: the scale expressed in ratios in the form [ [p1,q1], ... , [pn,qn] ]
	 *		roughness: the roughness of each scale degree
	 *		harmonicity: C. Barlow's harmonicity for each ratio
	 *		fund: the fundamental, i.e., the frequency of the first partial, or the one with
	 *			highest amplitude (depending on the max arg being true or false). 
	 *		partials: the originial partials [f,a]
	 *		intervals: the intervals for calcualting the local minima (private).
	 *
	 *		method:  \sethares takes args in freqs-amps and is coarser (and 29% slower)
	 *				\parncutt takes args in freqs (which converts to barks) and sones
	 *				its more precise and faster but you have to calculate the sones first
	 *				(to do that, use the LoudnessModel class and the phonToSone method)
	 *
	 *	For calculating dissonance measures between two spectra use Dissonance.make2 
	 *
	 **/
	
	classvar dStar = 0.24, s1 = 0.0207, s2 = 18.96, c1 = 5.0, c2 = -5.0, a1 = -3.51, a2 = -5.75;
	var 	<>intervals, 
		<>partials,
		<>roughness, 
		<>dcurve, 
		<>scale, 
		<>ratios, 
		<>harmonicity,
		<>matrixH, <>matrixR,
		<>weighs, <>invweighs, <>equweighs,
		<>markov, <>markovStream,
		<>fund; 

	*new{ ^super.new.init }
	
	init { 
		intervals = Array.newClear; 
		roughness = Array.newClear; 
		dcurve = Array.newClear;
		partials = Array.newClear; 
	}
	
	// make a Dissonance object from arrays (f,a) of freqs and amps
	
	*make { |f, a, start = 0.99, end = 2.01, inc = 0.01, method = \parncutt, max = false|
		var esto;
		esto = this.new;
		if (max) 	{esto.fund = f[a.indexOf(a.maxItem)]}
				{esto.fund = f[0]};
		esto.partials = [f,a];
		method.switch(
			\parncutt, {esto.intervalP(f,a,start,end,inc) },
			\sethares, {esto.interval(f,a,start,end,inc)  },
			{^"Invalid method" }
		); 
		esto.scale = esto.minima(esto.dcurve).round(inc);
		esto.ratios = esto.scale.asRatio(inc.reciprocal, false);
		esto.harmonicity = esto.ratios.harmonicity;
		^esto;
	}
	
	// a Dissonance object from 2 different timbres (f,a) & (g,b)
	
	*make2 { |f, g, a, b, start = 0.99, end = 2.01, inc = 0.01, method = \parncutt, max = false|
		var esto;
		esto = this.new;
		if (max) {esto.fund = f[a.indexOf(a.maxItem)]}
				{esto.fund = f[0]};
		esto.partials = [f,g,a,b];
		method.switch(
			\parncutt, {esto.intervalP2(f,g,a,b,start,end,inc)},
			\sethares, {esto.interval_2(f,g,a,b,start,end,inc)},
			{^"Invalid method"}
		);
		esto.scale = esto.minima(esto.dcurve).round(inc);
		esto.ratios = esto.scale.asRatio(inc.reciprocal, false);
		esto.harmonicity = esto.ratios.harmonicity;
		^esto;
	}
		
// same as make but the input is directly an array in the format of a Signal.stft
	*makeFromSpectrum { |f, start = 1.0, end = 2.3, inc = 0.01, method = \parncutt, max = false,
						numPartials = 10, win = 256, sr = 44100|
		var partials = f.asFreqMag(win, sr/2).findNlargest(numPartials, true);
		^Dissonance.make(partials.first, partials.last, start, end, inc, method, max)
	}

	*makeFrom2Spectrums { |f, g, start = 0.99, end = 2.01, inc = 0.01, method = \parncutt, max = false,
						numPartials = 10, win = 256, sr = 44100|
		var partials1 = f.asFreqMag(win, sr/2).findNlargest(numPartials, true),
		    partials2 = g.asFreqMag(win, sr/2).findNlargest(numPartials, true);
		^Dissonance.make2(
			partials1.first, partials2.first, partials1.last, partials2.last, start, end, inc, method, max)
	}	
	
	*load {|path| ^Object.readArchive(path)}
	save {|path| this.writeArchive(path)}

// for use in saving multiple dissonance objects as arrays of dicts: 
	writeZArchive {| akv | var d = ();
		akv = akv.asZArchive;
		d = this.instancesToDict;
		akv.writeItem(d);
		akv.writeClose; 
	}
	
	instancesToDict { var d = ();
		d.intervals = this.intervals;
		d.roughness = this.roughness;
		d.dcurve = this.dcurve;
		d.scale = this.scale;
		d.ratios = this.ratios;
		d.harmonicity = this.harmonicity;
		d.fund = this.fund;
		^d
	}
		
	play { |fund, dur = 0.33, amp = 0.1| fund ?? {fund = this.fund}; 
			Pbind(\freq, Pseq( this.scale * fund, 1), \dur, dur, \amp, amp).play(quant:0)
	}
	plot {dcurve.plot("dissonance curve", (470@370)@(1100@50), false, 1) }
	
	asString {var r; 
			r = this.ratios.collect{|d| d[0].asString ++ "/" ++ d[1].asString}.join(", ");
			^"[" ++ r ++ "]";
	}		
		
	analyseScale {|tolerance = 6, type = \size, maxNum = 81, maxDenom = 80, maxPrime = 31, post = true|
			var classification, res, cents = this.ratios.collect{|x|(x[0]/x[1]).cents} ;
			classification = IntervalTable.classify(cents, tolerance);
			type.switch(
				\size, { // reduce by size of num and denom
					res = classification.collect{|x,i|
						x.reject{|y| (y[0][1] > maxDenom) or: (y[0][0] > maxNum) };
					}
				},
				\prime, { // reduce by max prime factor
					res = classification.collect{|x,i|
							x.reject{|y|  (y[0][0].factors.maxItem > maxPrime) or:
										(y[0][1].factors.maxItem > maxPrime)  }
					}
				},
				{^"Invalid method!"}
			);				
			res = res.collect{|x, i| if( (x == []) or: (x.isNil) )
						{[[this.ratios[i], cents[i], "NO MATCH"]]} {x} };
			if (post) {
				postf("Each scale degree is close to the following intervals by +/-% cents.\n", tolerance);
				type.switch(\size, {postf("Max denominator: %, max numerator: %\n", maxDenom, maxNum)},
							\prime, {postf("Max prime: %\n", maxPrime)});
				res.do{|x, i|
					postf("%> % ( % cents)  ------------------------------\n", 
						i+1, this.ratios[i], cents[i].round(0.001) );
					x.do{|y| postf("\t\t\t\t %/%, % cents, %\n", y[0][0], y[0][1], y[1].round(0.001), y[2])};
				};
			^""	
			}
			^res;
	 }		
	 
// this is only taking into account harmonicity. Should take a function for sorting as an argument, 
// with harmonicity as default...
// search for a way to favor things like 12/11 instead of 35/32
	 correctScale {|tolerance = 6, type = \size, maxNum = 81, maxDenom = 80, maxPrime = 31 |
	 		var candidates = this.analyseScale(tolerance, type, maxNum, maxDenom, maxPrime, false), 
	 			res, harms;
	 		res = candidates.collect{|x,i|
				if (x.size == 1) {	
					x[0][0]
				}{
					harms = x.collect{|y| y[0].harmonicity.abs };
					x[harms.indexOf(harms.maxItem)][0];
				};
			};
			this.ratios = res;
			this.scale = res.collect{|x| x[0]/x[1]};
			this.harmonicity = res.harmonicity;
			^this;	 
	 }
	
// filter scales by eliminating denominators that are too big
	reduce {|maxDenom = 18| 
			this.ratios = this.ratios.reject{|x| x[1] > maxDenom}; 
			this.scale = this.ratios.collect{|x| x[0]/x[1]};
			this.harmonicity = this.ratios.harmonicity;
	}
	
// filter scales by eliminating ratios with a maximum prime factor
	factor {|maxPrime = 7| 
			this.ratios = this.ratios.reject{|x| 
					(x[0].factors.maxItem > maxPrime) or: (x[1].factors.maxItem > maxPrime)}; 
			this.scale = this.ratios.collect{|x| x[0]/x[1]};
			this.harmonicity = this.ratios.harmonicity;
	}
	
// filter out all degrees that contain a certain prime factor:
	filter {|aPrime = 5| 
			this.ratios = this.ratios.reject{|x| 
					x[0].factors.includes(aPrime) or: x[1].factors.includes(aPrime)};
			this.scale = this.ratios.collect{|x| x[0]/x[1]};
			this.harmonicity = this.ratios.harmonicity;
	}
	
	makeMatrix {|harmonicMax = 0.3| // replaces max harmonicity of 1 with 0.3
		var ratioM = [], harmM, weighM, invweighM, equalM; 
		this.ratios.do{|n,i| 
			this.ratios.do{|m, j|
				ratioM = ratioM.add(n.ratioDiv(m))
			};
		};
		ratioM = ratioM.reshape(this.ratios.size, this.ratios.size, 2);
		this.matrixR = ratioM;
		harmM = ratioM.collect{|x| x.harmonicity};
		weighM = harmM.collect{|x| x.abs.collect{|y| if (y == 1) {harmonicMax}{y}}.normalizeSum};
		invweighM = weighM.collect{|x| (1-x).normalizeSum};
		equalM = ({1}!this.ratios.size).normalizeSum;
		this.matrixR = ratioM;
		this.matrixH = harmM;		
		this.weighs = weighM;
		this.invweighs = invweighM;
		this.equweighs = equalM;
		^"READY...";
	}

	calcPolarity {|polarity, filteredScale| var interweighs, data;
		if (filteredScale.isNil) {filteredScale = this.ratios};
		if (this.weighs.isNil) {"use makeMatrix first!".inform; ^this};
		interweighs = this.invweighs.collect{|x,i|
						x.interpolate3(this.equweighs, this.weighs[i], polarity) 
		};
		data = filteredScale.collect{|x,i| [x, filteredScale, interweighs[i] ] };
		this.markov = MarkovSet.new(data);
		this.markov.makeSeeds;
		this.markovStream = this.markov.asStream2;
		^interweighs;
	}
// The following methods provide the actual calculations of the curves. 
// They are called by *make, and *make2 and don't need to be used separately:
					 
//	dissonance measure between arrays of partials f & g (in Hz) with amp arrays a & b
// 	algorithm taken from Sethares[97] out of his BASIC & matlab code
	dissmeasure {|f, g, a, b|
		var d = 0, fdif, s, arg1, arg2, exp1, exp2, dnew;
		f.size.do{|i|
			g.size.do{|j|
				s = dStar / (s1 * min(g[j], f[i]) + s2);
				fdif = absdif(g[j],f[i]);
				arg1 = a1 * s * fdif;
				arg2 = a2 * s * fdif;
				if (arg1 < 88.neg) {exp1 = 0} {exp1 = exp(arg1)};
				if (arg2 < 88.neg) {exp2 = 0} {exp2 = exp(arg2)};
				dnew = min(a[i], b[j]) * ( (c1 * exp1) + (c2 * exp2) );
				d = d + dnew;
			};
		};
		^d
	} 
	
// this is another dissonance measure based on Parncutt and Barlow: D = sqrt(s1 * s2) * P(bk1 - bk2)
// where s1 & s2 are in sones, b1 & b2 in barks and P is the Parncutt dissonance measure
	dissmeasure2 {|bk1, bk2, s1, s2| 
		var diss = 0, freqDiff, dnew;
		bk1.size.do{|i|
			bk2.size.do{|j|
				freqDiff = absdif(bk2[j], bk1[i]);
				dnew =  sqrt(s1[i] * s2[j]) * (4 * freqDiff * (exp(1-(4 * freqDiff))));
				diss = diss + dnew; 
			};
		};
		^diss
	}

// Sweep array for the parncutt dissmeasure method: 
	intervalP {|f, s, startInt = 0.99, endInt = 2.01, inc = 0.01|
		intervals = []; 
		forBy(startInt, endInt, inc, {|alpha|
			intervals = intervals.add(alpha);
			dcurve = dcurve.add(
					this.dissmeasure2(f.asBark, (f * alpha).asBark, s, s)
			);
		});
		^dcurve
	}
	
// Sweep between two different arrays, parncutt dissmeasure method: 
	intervalP2 {|f, g, s, t, startInt = 0.99, endInt = 2.01, inc = 0.01|
		intervals = []; 
		forBy(startInt, endInt, inc, {|alpha|
			intervals = intervals.add(alpha);
			dcurve = dcurve.add(
					this.dissmeasure2(f.asBark, (g * alpha).asBark, s, t)
			);
		});
		^dcurve
	}
	
// Sweep an array of partials f and amps a with itself from startInt to endInt by inc
	interval {|f, a, startInt = 0.99, endInt = 2.01, inc = 0.01| 
		intervals = [];
		forBy(startInt, endInt, inc, {|alpha|
			intervals = intervals.add(alpha);
			dcurve = dcurve.add(this.dissmeasure(f, f * alpha, a, a))
		});
		^dcurve
	}

// Sweep between two different sounds: (f,a) and (g,b), same as previous
	interval_2 {|f, g, a, b, startInt = 1.0, endInt = 2.3, inc = 0.01|
		intervals = [];
		forBy(startInt, endInt, inc, {|alpha|
			intervals = intervals.add(alpha);
			dcurve = dcurve.add(this.dissmeasure(f, g * alpha, a, b));
		});
		^dcurve
	}	
	
// Get the minima of a dissonance curve:
	minima {|dissArray|
		var gradient = dissArray.differentiate,
		prev = gradient.first, res = [], min = [];
		gradient.do{|a| 
			if (a.sign != prev.sign)  // changes in sign indicating changes in curvature
			{ 
				res = res.add(gradient.indexOf(a) - 1); // local minima indexes
				prev = a;
			}
		};
		res.do{|r| // r are the indexes of all inflection points, filter out maxima:
			if (dissArray[r] < dissArray.wrapAt(r - 1)) // only need to check to the left
			{
				min = min.add(this.intervals[r]); // we want the intervals at which these minima occur
				roughness = roughness.add(dissArray[r]); // and their roughness
			}
		};
		^min
	}
}

/*

Dissonance: (2006-2007) jsl

*/