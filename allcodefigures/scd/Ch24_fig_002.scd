Figure 24.2
union PyrSlot
{
	double f;					// double
	struct {
		int tag;
		union {
			int		c;		// character
			int		i;		// integer
			void		*ptr;	// raw pointer
			PyrObject	*o;		// object pointer
			PyrSymbol	*s;		// symbol pointer
			...				// other object pointers
		} u;
	} s;
};

