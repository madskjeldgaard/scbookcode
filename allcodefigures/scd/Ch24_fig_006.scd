Figure 24.6
PyrSymbol
{
	// symbol name
	char	*name;
	// hash value
	long	hash;
	// special selector index
	short	specialIndex;
	// symbol flags
	uint8	flags;
	// length of symbol name
	uint8	length;
	union {
		// index in method table or primitive table
		long index;
		// pointer to class with this name
		struct PyrClass *classobj; name.
		....
	} u;
	// class dependancy (used during compilation)
	ClassDependancy *classdep;	
};

