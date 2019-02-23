Figure 24.8

ClassDependancy
{
	// next link in list
	ClassDependancy	*next;
	// superclass dependency
	ClassDependancy	*superClassDep;
	// subclass list (linked via 'next')
	ClassDependancy	*subclasses;
	// class name symbol
	PyrSymbol		*className;
	// superclass name symbol
	PyrSymbol		*superClassName;
	// file name symbol
	PyrSymbol		*fileSym;
	// start character position of definition
	int			startPos;
	// end character position of definition
	int			endPos;
	// line number of definition
	int			lineOffset;
};

