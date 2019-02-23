/* IZ 050722 
Coding utilities for writing synthdefs for playing single UGens. Make and return the strings for coding the following:  
- String of args of ar or kr methods of a UGen,   (args)
- Call of aUGen.ar/kr with all argument names; (arString/krString)
- Creating a SynthDef that plays the Ugen at kr or ar and ouputs its output to bus out. 
  (arDef, krDef)

SinOsc.args;
Formlet.args;
Formlet.arString;
Blip.args;
Blip.arString;
Blip.arDef;
Blip.krDef;
*/


+ UGen {

	*arString { ^this.akrString(\ar) }
	
	*krString { ^this.akrString(\kr) }

	*akrString { | rate = \ar |
		var meth, argn, frame, size;
		meth = this.class.methods.detect { |m| m.name.asSymbol == rate };
		if (meth.isNil) { ^super.args(rate) };
		argn = meth.argNames;
		frame = meth.prototypeFrame;
		^String.streamContents { | stream |
			stream << this.name << "." << rate << "(";
			size = argn.size - 2;
			argn[1..].do { | n, i |
				stream << n << 
					(if (size > i) {  ", " } { ")" }); 
			};
		};
//		^meth;
	}

	*args { | rate = \ar |
		var meth, argn, frame, size;
		meth = this.class.methods.detect { |m| m.name.asSymbol == rate };
		if (meth.isNil) { ^super.args(rate) };
		argn = meth.argNames;
		frame = meth.prototypeFrame;
		^String.streamContents { | stream |
//			stream << "| ";
			size = argn.size - 2;
			argn[1..].do { | n, i|
				stream << n << " = " << frame[i + 1] << 
					(if (size > i) {  ", " } { " " }); 
			};
		};
//		^meth;
	}

	*arDef { ^this.akrDef(\ar) }
	 
	*krDef { ^this.akrDef(\kr) }
	
	*akrDef { | rate = \ar |
		if (this.respondsTo(rate).not) {
			Post << this << " does not respond to " << rate << "\n"; 
			^"-";
		}; 
		^String.streamContents { | stream |
			stream << "SynthDef('" << this.name << "', { | out = 0, " 
				<< this.args
				<< "|\n\tvar src;\n\tsrc = "
				<< this.perform((rate ++ \String).asSymbol) << $;
				<< "\n\tOut." << rate << "(out, src);\n});";
		};
	}
}

