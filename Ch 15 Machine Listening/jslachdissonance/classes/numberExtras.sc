+ SimpleNumber {
//	 The following methods are also defined for SequenceableCollection below.
//	 Methods that can be applied to rational numbers will work with arrays of [p,q]'s

	//ex. 3.cents(2) or 440.cents(60.midicps) or [2,3].cents
	cents { | frq = 1| 
		^1200 * ( (this/frq).log / 2.log )
	}

	//ex. 440.addCents(Array.series(12, 0, 100)).asNote
	addCents { |cents|
		^this * (2**(cents/1200))
	}

	asNote { var residue, octave, note, roundedNote;
		roundedNote = this.cpsmidi.round(1);
		residue  = this.cpsmidi.frac; 
		octave = ((roundedNote / 12).asInteger) - 1;
		note = [NoteNames.names[(roundedNote - 72) % 12].asString ++ octave.asString, 
				residue.round(0.001)];
//		^('#' ++ note).interpret //make into a literal array
		^note
	}

	asBark { var bk;
		if (this <= 219.5) {
			bk = 13.3 * atan( 3 * this / 4000);	// Terhardt 1979
		}{
			bk = ( (26.81 * this) / (1960 + this) ) - 0.53; // Traunmuller
			if (bk > 20.1) { bk = bk + (0.22 * (bk-20.1)) }
		}
		^bk
	}
	
	// this method of conversion comes from the definition of the edges of the critical bandwidth
	barkToFreq {var barkEdge = #[0, 100, 200, 300, 400, 510, 630, 770, 920, 1080, 1270, 1480, 
			1720, 2000, 2320, 2700, 3150, 3700, 4400, 5300, 6400, 7700, 9500, 12000, 15500];
			^barkEdge.blendAt(this)
	}
	
	// this one is the inverse of the Traunmuller approximation function used in asBark
	// differs from asBark below 220 hz
	barkToHz { ^1960 / (26.81 / (this + 0.53) - 1) }
	
	// gives the size (in Hz) of the critical bandwidth given in barks
	criticalBW { ^52548 / (this.squared - (52.56 * this) + 690.39)}

	// freqs to ERB (Equivalent Rectangular Bandwidth, another scale based on the CBW)
	// also called ERB-rate of a tone. It is mainly used for masking analysis. 
	hzToErb { ^11.17 * log( (this + 312) / (this + 14675)) + 43.0}

	phonToSone { ^2**((this - 40) / 10)}
	
	soneToPhon { ^10 * (4 + (this.log10 / 2.log10))}
	
	// calibration: should be 0 if the amp values are in dB spl or a positive number if the values
	// are in dBFS, that is, negative relative to 0, like when translating amps to db with ampdb
	asPhon {|spl, calib = 0| ^LoudnessModel.calc(this, spl + calib)}
	
	asSone {|spl, calib = 0| ^this.asPhon(spl, calib).phonToSone }

	asWavelength  {|c = 343|  ^c/this } // c is speed of sound in m/s @ 20 celsius	
	factorial { // the highest factorial that can be represented as a Float is 171
		if (this == 0) {^1};
		if (this > 0)  { ^this * (this - 1).asFloat.factorial}
			{"Not valid for negative integers".warn}
	}
	
/*	 Return a sequence of largest prime powers for a given harmonicity minimum.
	 Pitch range is in octaves. See formula in highestPower method, below.
	 ex. 0.03.minHarmonicityVector(1,13) yields [12, 8, 3, 2, 1, 1]. They are the maximum
	 powers for 2,3,5,7,11,and 13. See Barlow, 1987:
	 "A maximum powers sequence includes intervals, the harmonicities of which may lie
	 below the minimum suggested [by this method]...The maximum power sequence guarantees merely
	 that all intervals that are more harmonic than a given minimum [harmonicity] value can be 
	 expressed by the sequence. [12, 8, 3, 2, 1, 1] results in as many as 3,964 different intervals 
	 within one octave (!), of which only 211 are truly more harmonic than 0.03"             */ 
	minHarmonicityVector {|pitchRange = 1, maxPrime = 11|      
	     ^Array.primes(maxPrime).collect{|p| p.highestPower(this, pitchRange)} 
	}

	// this is like asFraction but hacked to handle rounding errors for
	// harmonic interpreting of periodic decimals (0.333 will be 1/3 and not 333/1000)
	asRatio {|denominator = 100, fasterBetter = true|
		var num = this, str, a, b;
		str = this.asString;
		if ( (str.contains(".")) and: (str.size > 3) ) // only in pertinent cases
			{ 
				a = str.wrapAt(-2).digit; b = str.last.digit; // get last 2 digits
				if ( (a == b) or: ((a+1) == b) ) // cases like 1.33 and 1.67
					{ num = (str.drop(-1) ++ "".catList(a!12)).asFloat }
			}
		^num.asFraction(denominator, fasterBetter)
	} 

}

+ Integer {
		
	// Clarence Barlow's Indigestibility of an integer 
	// (low vs. high prime factors as a measure of "digestibility"):
	indigestibility {var sum = 0;
		this.factors.asBag.contents.asSortedArray.do
			{|y|
				sum = sum + ((y[1] * ((y[0] - 1).squared)) / y[0])
			};
		^(sum * 2);
	}	
	// note: at prime 46349, 32-bit integer arithmetic overflows
	// and gives wrong (negative) indigestibilites...

	// Clarence Barlow's Harmonicity formula (for an interval p/q):
	harmonicity {|q|
		^(q.indigestibility - this.indigestibility).sign /
		(this.indigestibility + q.indigestibility);
	}
	
	// formula N(p) from "Two Essays on Theory", C.Barlow (CMJ, 1987): (see minHarmVector above)
	highestPower {| minHarmonicity = 1, pitchRange = 1|
		if (this.isPrime.not) {"Number has to be prime".warn; ^nil};
		if (this == 2) { 
			^((pitchRange + (minHarmonicity.reciprocal)) / 
				(1 + (256.log / 27.log))).trunc
		}{
			^((pitchRange + (minHarmonicity.reciprocal)) / 
				(this.indigestibility + (this.log / 2.log))).trunc
		}
	}
			
	multiples {|... primes|
		var factorList, multiples;
		multiples = Array.newClear;
		(2..this).do{|i|
			factorList = i.factors;
			primes.do{|j|
				factorList.occurencesOf(j).do{
							factorList.remove(j)} };
			if (factorList.isEmpty) {multiples = multiples.add(i)} };
		^multiples;
	}

/* 
 USAGE: a_number.harmonics(highest_harmonic)
 returns an Array with the harmonics of a number up to highest
 Ex: 5.harmonics(48) ->  [ 5, 10, 15, 20, 25, 30, 35, 40, 45 ]

*/
	harmonics {|max|
			var n = 1, h = 1, result;
			result = Array.newClear;
			{ h <= max }.while{ 
				h = this * n;
				result = result.add(h);
				n = n + 1;
				};
			result.pop;	
			h = result[0] ? nil;
			if (h.notNil, {^result}, {^nil});
	}
	
/*
USAGE:  a_prime.primeHarmonics(highest_harmonic)
			returns a nested array with all the harmonics up to highest_harmonic
			of a_prime along with all lower primes.
			Ex.	17.primeHarmonics(20) ->
			[ [ 2, 4, 6, 8, 10, 12, 14, 16, 18, 20 ], [ 3, 6, 9, 12, 15, 18 ], [ 5, 10, 15, 20 ], [ 7, 14 ], [ 11 ], [ 13 ], [ 17 ] ]		
*/	
		primeHarmonics {|maxPartial|
			var primeList, result = Array.newClear;
			if (this.isPrime.not) {"Number has to be prime".warn; ^nil};
			primeList = Array.primes(this);
			result = primeList.collect({|i| i.harmonics(maxPartial)}); 
			result.removeAllSuchThat({|n| n.isNil});
			^result;
	} 

/*	
 USAGE: a_prime.listOfHarmonics(highest_harmonic)
		returns an array with all the harmonics (up to highest) of 
		all the primes below (and including) a_prime
		Ex.  7.listOfHarmonics(50)	
 this is equivalent to: highest_harmonic.multiples(array of primes up to a_prime)
*/	
	listOfHarmonics {|max|
			var result;
			result = this.primeHarmonics(max);
			result = result.flatten;
			result = result.sort;
			result.removeAllSuchThat({|n| result.occurencesOf(n) > 1});
			^result
	}
	
	vpNumbers {|primeArray|
			^primeArray.collect{|n|
				n.harmonics(n * this)}
	}
}

+ SequenceableCollection {
	
//	The following two methods deal with rational numbers, expressed as [p,q] arrays: 
	
	// rational division: [p,q] / [r,s]. ex: [5,9].ratioDiv([2,6]) -> [5,3]
	ratioDiv {|that, reduce = true| 
		var div = [ this[0] * that[1], this[1] * that[0] ];
		if (reduce) {^div.reduceRatio}{^div}
	}
	
	// express a rational as [p,q] where p and q are coprime:
	reduceRatio { ^this div: (this[0] gcd: this[1]) }
					
// a shortcut for making arrays of primes:	
	// ex. Array.primes(11) -> [2, 3, 5, 7, 11]
	*primes {|maxPrime = 11| var obj = this.newClear, i = 0;
		{ i < maxPrime}.while{ 
	     	i = i + 1; 
	     	if (i.isPrime) {obj = obj.add(i)}
     	};
		^obj
	}

//	several methods designed to work with arrays of pairs (either rationals or [freq, spl]):

	ratioPost {|char = ", "| 
			^this.collect{|d| d[0].asString ++ "/" ++ d[1].asString}.join(char);
	}		


	// phon values for [freq, spl] pairs: 
		asPhon {|calib = 0| var res;
			if ( (this.size == 2) and: (this[0].isSequenceableCollection.not))
				{
					res = this[0].asPhon(this[1], calib);
				}{
					res = this.collect{|x| x.asPhon(calib) }
				};
			^res
		}

	// just a convenience for not writing asPhon.phonToSone:
		asSone { |calib = 0| var res;
			if ( (this.size == 2) and: (this[0].isSequenceableCollection.not))
				{
					res = this[0].asSone(this[1], calib);
				}{
					res = this.collect{|x| x.asSone(calib) }
				};
			^res
		}
		
	// Convenience method from LoudnessModel. Returns the amplitudes of partials after masking, 
	// should be in the form of [freq, spl] pairs:
		compensateMasking { |gradient = 12| var f, res;
			f = this.flop;
			^LoudnessModel.compensateMasking(f[0], f[1], gradient);
		}		

	// cents value for rationals:
	cents { var res; 
		if ( (this.size == 2) and: (this[0].isSequenceableCollection.not) ) 
			{
				res = this[1].cents(this[0]);
			}{
				res = this.collect({|x| x.cents })
			};
		^res
	}
	
	// Harmonicity (see above for formula)
	// for rational pairs [p,q]:
	harmonicity {|clean = true| var res; 
		if ( (this.size == 2) and: (this[0].isSequenceableCollection.not) ) 
			{
				res = this[1].harmonicity(this[0]);
				if (clean) {if (res.isNaN) { res = 1 }}; // change to another value (say 999)
												// to separate 1/1 from 2/1
			}{
				res = this.collect({|x| x.harmonicity })
			};
		^res
	}

	// James Tenney's Harmonic Distance (a city-block metric of harmonic lattices)
	// for rational pairs [p,q]:
	harmonicDistance { var res;
		if ( (this.size == 2) and: (this[0].isSequenceableCollection.not) ) 
			{
				res = (this[1] * this[0]).log;
			}{
				res = this.collect{|x| x.harmonicDistance }
			};
		^res
	}
	
// the following are methods for SimpleNumber made to work in SequenceableCollection:

	asNote {^this.collect({|x| x.asNote})}
	
	asMidi {^NoteNames.table.atAll(this) }

	addCents {|cents| ^this.collect({|x| x.addCents(cents)})}
	
	asBark {^this.collect({|x| x.asBark})}
	
	barkToFreq {^this.collect{|x| x.barkToFreq } }
	
	barkToHz {^this.collect{|x| x.barkToHz } }
	
	criticalBW {^this.collect{|x| x.criticalBW } }
	
	hzToErb {^this.collect{|x| x.hzToErb } }
	
	phonToSone {^this.collect{|x| x.phonToSone } }
	
	soneToPhon {^this.collect{|x| x.soneToPhon } }
	
	asRatio {|denom = 100, fasterBetter = true| 
		^this.collect{|x| x.asRatio(denom, fasterBetter)}
	}
	
	vpChord {|aprox = 1, prime = 7, max = 24| 
		^this.collect({|x| x.vpChord(aprox, prime, max)}).flatten
	}

	vpChordClosed {|aprox = 1, prime = 7, max = 24|
			^this.collect({|x| x.vpChordClosed(aprox, prime, max)}).flatten
	}

	vpChordOpen {|aprox = 1, prime = 7, max = 24|
			^this.collect({|x| x.vpChordOpen(aprox, prime, max)}).flatten
	}

}

+ Collection {

	removeDuplicates { ^this.asSet.perform( ('as' ++ this.class).asSymbol ) }
	
	asFloatArray {^FloatArray.new(this.size).addAll(this) }
}

+ SimpleNumber {

// variations on Virtual Pitch chords
	vpChord {|aprox = 1, primeArray, maxMultiple = 3| 
			var t, new = Array.newClear;
			t = maxMultiple.vpNumbers(primeArray);
			new = new.add(t[0].choose);
			t.remove(t[0]);
			t.do{|x| 
				var temp; temp = x.choose;
				while {new.includes(temp)} {temp = x.choose};
				new = new.add(temp);
			};
			^(new.sort * this.midicps).cpsmidi.round(aprox);
	}
	
	vpChordClosed {|aprox = 1, primeArray, maxMultiple = 3|
			var t, new = Array.newClear;
			t = maxMultiple.vpNumbers(primeArray);
			new = new.add(t[0].choose);
			t.remove(t[0]);
			(t.size).do {|i|
				var temp; temp = new[i].nearestInList(t[i]);
				while {new.includes(temp)} {temp = t[i].choose};
				new = new.add(temp);
			};
			^(new.sort * this.midicps).cpsmidi.round(aprox);
	}
	
	vpChordOpen {|aprox = 1, primeArray, maxMultiple = 3|
			var t, new = Array.newClear;
			t = maxMultiple.vpNumbers(primeArray);
			new = new.add(t[0].choose);
			t.remove(t[0]);
			(t.size).do{|i|
				var temp; 
				temp = t[i].maxItem({|x| (x-new[i]).abs});
				while {new.includes(temp)} {temp = t[i].choose};
				new = new.add(temp);
			};
			^(new.sort * this.midicps).cpsmidi.round(aprox);
	}

}

// jsl: 2005-2007	
/*	
	TO DO: indispensibility for meters
*/