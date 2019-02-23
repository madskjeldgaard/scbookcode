/*
by Scott Wilson as part of the BEASTMulch project
Development funded in part by the AHRC http://www.ahrc.ac.uk
*/

PlayBufSendIndex : MultiOutUGen {	
	*ar { arg numChannels, bufnum=0, rate=1.0, trigger=1.0, startPos=0.0, loop = 0.0, 
		indFreq = 10, id = 0;
		^this.multiNew('audio', numChannels, bufnum, rate, trigger, startPos, loop, indFreq, id)
	}
	
	init { arg argNumChannels ... theInputs;
		inputs = theInputs;
		^this.initOutputs(argNumChannels, rate);
	}
	argNamesInputsOffset { ^2 }
}

DiskInSendIndex : MultiOutUGen {
	*ar { arg numChannels, bufnum, indFreq = 10, id = 0;
		^this.multiNew('audio', numChannels, bufnum, indFreq, id)
	}
	//init { arg numChannels, bufnum;
//		inputs = [bufnum];
	init { arg numChannels ... theInputs;
		inputs = theInputs;
		^this.initOutputs(numChannels, rate)
	}
}