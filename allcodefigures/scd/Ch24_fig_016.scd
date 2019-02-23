Figure 24.16
#include <math.h>
#include "GC.h"
#include "PyrKernel.h"
#include "PyrPrimitive.h"

// Primitive implementation of atan2,
// calling the function from libm.
static int prAtan2(struct VMGlobals *g, int numArgsPushed)
{
	// Pointer to arguments
	PyrSlot *args = g->sp - numArgsPushed + 1;
	// Pointer to receiver (self)
	PyrSlot* self = args + 0;
	// Pointer to argument
	PyrSlot* arg  = args + 1;

	double x, y;
	int err;
	
	// Get receiver value
err = slotDoubleVal(self, &x);
// Signal error for invalid input type
	if (err != errNone) return err;
	
	// Get argument value
	err = slotDoubleVal(arg, &y);
// Signal error for invalid input type
	if (err != errNone) return err;
	
	// Compute result
double result = atan2(x, y);

	// Set top of stack to return value
	SetFloat(self, result);
	
	// Signal success
	return errNone;
}

// Call this function during initialization,
// e.g. from initPrimitives() in PyrPrimitive.cpp
void initMyPrimitives()
{
	// Initialize primitive indices
	int base = nextPrimitiveIndex(), index = 0;

	// Define primitive with two arguments (self, operand)
	definePrimitive(base, index++, "_MyFloatAtan2", prAtan2, 2, 0);
	// ... define more primitives here ...
}


