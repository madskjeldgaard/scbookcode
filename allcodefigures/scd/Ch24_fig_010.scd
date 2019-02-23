Figure 24.10
PyrMethodRaw
{
	// special method index
	unsigned specialIndex;
	// method type
	unsigned methType;
	// prototype frame size
	unsigned frameSize;

	// number of arguments
	unsigned numargs;
	// 1 if has variable number of arguments
	unsigned varargs;
	// number of keyword and variable defaults
	unsigned numvars;
	// number of temporary (local) variables
	unsigned numtemps;
	// true when frame needs to be heap-allocated
	unsigned needsHeapContext;
	// numargs + varargs
	unsigned posargs;
};

