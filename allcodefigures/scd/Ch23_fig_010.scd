
// Figure 23.10

(
var dict, maxLength = 0;
dict = (
	ab: { (note: [4, 0, 7], legato: 0.1, dur: 1) },
	ba: { (note: [4, 9, 8], legato: 0.3, dur: 0.3) },
	aaa: { (note: 5, legato:1.5) },
	bbb: { (note: 0, legato:2.5, dur: 0.25) }
);

dict.keys.do { |key| maxLength = max(maxLength, key.asString.size) };

f = { |str|
	var i = 0, n = 0, substr, event;
	while { i < str.size } {
		substr = str[i..i + n];
		event = dict[substr.asSymbol].value;
		if(event.notNil) {
			substr.postln;
			i = i + n + 1;
			n = 0;
			event.postln.play;
			event.delta.wait;
		} {
			if(n + 1 < maxLength) { n = n + 1 } { n = n - 1; i = i + 1 }
		};
	};
};
)

// play some sequences
fork { f.value("abbbbaab"); }
fork { f.value("aaabbbabbaaaabbabaaaaba"); };

