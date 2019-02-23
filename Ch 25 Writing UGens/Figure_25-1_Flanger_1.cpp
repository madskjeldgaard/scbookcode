#include "SC_PlugIn.h"

static InterfaceTable *ft;

// the struct will hold data which we want to "pass" from one function to another
// e.g. from the constructor to the calc func, 
// or from one call of the calc func to the next
struct Flanger : public Unit  {
	float rate, delaysize, fwdhop, readpos;
	int writepos;
};

// function declarations, exposed to C
extern "C" {  
	void load(InterfaceTable *inTable);
	void Flanger_Ctor(Flanger *unit);
	void Flanger_next(Flanger *unit, int inNumSamples);
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
	unit->rate  = rate;
	
	// IMPORTANT: This tells scsynth the name of the calculation function for this UGen.
	SETCALC(Flanger_next);
	
	// Should also calc 1 sample's worth of output - ensures each ugen's "pipes" are "primed"
	Flanger_next(unit, 1);
}

void Flanger_next( Flanger *unit, int inNumSamples ) {
	
	float *in = IN(0);
	float *out = OUT(0);
	
	float depth = IN0(2);
	
	float rate    = unit->rate;
	float fwdhop  = unit->fwdhop;
	float readpos = unit->readpos;
	int writepos  = unit->writepos;
	int delaysize = unit->delaysize;

	float val, delayed;
	
	for ( int i=0; i<inNumSamples; ++i) {
		val = in[i];
		
		// Do something to the signal before outputting
		// (not yet done)
		
		out[i] = val;
	}
	
	unit->writepos = writepos;
	unit->readpos = readpos;
}	


void load(InterfaceTable *inTable) {
	
	ft = inTable;
	
	DefineSimpleUnit(Flanger);
}
