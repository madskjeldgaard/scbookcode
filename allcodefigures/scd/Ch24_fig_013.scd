Figure 24.13

PyrFrame : PyrObjectHdr
{
	// defining method
	PyrSlot method;
	// calling context
	PyrSlot caller;
	// closure context
	PyrSlot context;
	// method context
	PyrSlot homeContext;
	// instruction pointer
	PyrSlot ip;
	// temporary variable storage
	PyrSlot vars[1];
};

