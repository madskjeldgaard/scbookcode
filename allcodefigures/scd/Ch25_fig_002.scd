Figure 25.2
#include "SC_PlugIn.h"

static InterfaceTable *ft;

// the struct will hold data which we want to "pass" from one function to another
// e.g. from the constructor to the calc func, 
// or from one call of the calc func to the next
struct Flanger : public Unit  {
	float rate, delaysize, fwdhop, readpos;
	int writepos;
	
	// a pointer to the memory we'll use for our internal delay
	float *delayline;
};

// function declarations, exposed to C
extern "C" {  
	void load(InterfaceTable *inTable);
	void Flanger_Ctor(Flanger *unit);
	void Flanger_next(Flanger *unit, int inNumSamples);
	void Flanger_Dtor(Flanger *unit);
}


void Flanger_Ctor( Flanger *unit ) {
	
	// Here we must initialise state variables in the Flanger struct.
	unit->delaysize = SAMPLERATE * 0.02f; // Fixed 20ms max delay
	// Typically with reference to control-rate/scalar-rate inputs.
	float rate  = IN0(1);
	// Rather than using rate directly, we're going to calculate the size of 
	// jumps we must make each time to scan through the delayline at "rate"
	float delta = (unit->delaysize * rate) / SAMPLERATE;
	unit->fwdhop = delta + 1.0f;
	unit->rate = rate;
	unit->writepos = 0;
	unit->readpos = 0;
	
	// Allocate the delay line
	unit->delayline = (float*)RTAlloc(unit->mWorld, unit->delaysize * sizeof(float));
	// Initialise it to zeroes
	memset(unit->delayline, 0, unit->delaysize * sizeof(float));
	
	// IMPORTANT: This tells scsynth the name of the calculation function 
	//for this UGen.
	SETCALC(Flanger_next);
	
	// Should also calc 1 sample's worth of output – 
	//ensures each ugen's "pipes" are "primed"
	Flanger_next(unit, 1);
}

void Flanger_next( Flanger *unit, int inNumSamples ) {
	
	float *in = IN(0);
	float *out = OUT(0);
	
	float depth = IN0(2);
	
	float rate    = unit->rate;
	float fwdhop  = unit->fwdhop;
	float readpos = unit->readpos;
	float *delayline = unit->delayline;
	int writepos  = unit->writepos;
	int delaysize = unit->delaysize;

	float val, delayed, currate;
	
	currate = IN0(1);
	
	if(rate != currate){
		// rate input needs updating
		rate = currate;
		fwdhop = ((delaysize * rate * 2) / SAMPLERATE) + 1.0f;
	}
	
	for ( int i=0; i<inNumSamples; ++i) {
		val = in[i];
		
		// Write to the delay line
		delayline[writepos++] = val;
		if(writepos==delaysize)
			writepos = 0;
		
		// Read from the delay line
		delayed = delayline[(int)readpos];
		readpos += fwdhop;
		// Update position, NB we may be moving forwards or backwards 
		//(depending on input)
		while((int)readpos >= delaysize)
			readpos -= delaysize;
		while((int)readpos < 0)
			readpos += delaysize;
		
		// Mix dry and wet together, and output them
		out[i] = val + (delayed * depth);
	}
	
	unit->rate = rate;
	unit->fwdhop = fwdhop;
	unit->writepos = writepos;
	unit->readpos = readpos;
}	

void Flanger_Dtor( Flanger *unit ) {
	RTFree(unit->mWorld, unit->delayline);
}	

void load(InterfaceTable *inTable) {
	
	ft = inTable;
	
	DefineDtorUnit(Flanger);
}
