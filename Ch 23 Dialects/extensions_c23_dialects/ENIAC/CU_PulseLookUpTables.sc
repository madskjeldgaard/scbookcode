CU_PulseLookUpTables : MultiOutUGen {
	
	*ar { arg in1;
		^this.multiNew('audio', in1)
	}

	init { arg ... theInputs;
		inputs = theInputs;		
		channels = [ OutputProxy(\audio,this,0), OutputProxy(\audio,this,1),
					OutputProxy(\audio,this,2), OutputProxy(\audio,this,3),
					OutputProxy(\audio,this,4), OutputProxy(\audio,this,5),
					OutputProxy(\audio,this,6), OutputProxy(\audio,this,7),
					OutputProxy(\audio,this,8),OutputProxy(\audio,this,9) ];
		^channels
	}	
}