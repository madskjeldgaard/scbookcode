Figure 23.9

(
var x;
x = { |rates=#[1, 1]| Ringz.ar(Impulse.ar(rates) * 0.1, rates * 80, 1 / rates) }.play;
fork {
	var str = {:[x, y], 
		x<-(40..2), 
		y<-(x + 1..40),
		gcd(x, y) == 1,
		x.isPrime.not and: y.isPrime.not
	};
	0.5.wait;
	str.do { |primes|
		x.setn(\rates, primes.postln);
		(primes.product / primes.sum / 20).wait;
	}
};
)

