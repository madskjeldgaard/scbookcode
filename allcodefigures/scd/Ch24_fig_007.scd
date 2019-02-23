Figure 24.7

VMGlobals
{
	// global context

	AllocPool	*allocPool;
	// main thread context
	PyrProcess	*process;
	// global symbol table
	SymbolTable	*symbolTable;
	// garbage collector for this process
	PyrGC		*gc;
	// class variable array
	PyrObject	*classvars;

	// next byte code is a tail call
	int		tailCall;

	// true when in 'main' thread
	bool		canCallOS;

	// thread context
	
	PyrThread	*thread;
	PyrMethod	*method;
	PyrBlock	*block;
	PyrFrame	*frame;
	PyrMethod	*primitiveMethod;

	// current instruction pointer
	unsigned char	*ip;
	// current stack pointer
	PyrSlot		*sp;

	// argument pointer for primitive
	PyrSlot		*args;
	// current receiver
	PyrSlot		receiver;
	// interpretation result
	PyrSlot		result;
	// number of args to pop for primitive
	int		numpop;
	// current index into primitive table
	long		primitiveIndex;
	// random number generator state
	RGen		*rgen;
	// handler for unwinding C stack
	jmp_buf		escapeInterpreter;

	// scratch context
	long		execMethod;
};
