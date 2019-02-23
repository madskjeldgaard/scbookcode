Figure 24.9

PyrBlock : PyrObjectHdr
{
	// pointer to PyrMethodRaw
	PyrSlot rawData1;			
	// byte codes, nil if inlined
	PyrSlot code;
	// method selectors, class names, closures table
	PyrSlot selectors;
	// literal constants table
	PyrSlot constants;
	// temporary variable default values
	PyrSlot prototypeFrame;
	// defining block context
	// (nil for methods and toplevel)
	PyrSlot contextDef;
	// arguments to block
	PyrSlot argNames;
	// variables in block
	PyrSlot varNames;
	// source code (for closed functions)
	PyrSlot sourceCode;
};
