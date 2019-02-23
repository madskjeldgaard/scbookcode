1.14 Nested do to Generate a 12-Tone Matrix

(
var row, inversion, pitchClass;
row = Array.series(11, 1).scramble.insert(0, 0); 
// or enter your own row, e.g. Webern's Op 27
// row = [0, 11, 8, 2, 1, 7, 9, 10, 4, 3, 5, 6]; 
row.postln;
inversion = 12 - row;
// I add spaces to the strings for a tidy row
pitchClass = ["C  ", "C# ", "D  ", "Eb ", 
	"E  ", "F  ", "F# ", "G  ", "Ab ", "A  ", "Bb ", "B  "];
inversion.do({arg eachInv;
	var trans;
	trans = (row + eachInv); 
	// prints just pitch class
	trans.do({arg scaleDegree; pitchClass.wrapAt(scaleDegree).post});
	//"".postln; // uncomment these line if you want to do both
	// prints just numbers
	//trans.do({arg scaleDegree; (scaleDegree%12).post; " ".post});
	"".postln;
	});
"".postln
)

