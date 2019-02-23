Figure 24.4	
PyrObjectHdr
{
	// garbage collector links
	PyrObjectHdr	*prev;
	PyrObjectHdr	*next;
	// class pointer
	PyrClass	*classptr;
	// object size
	int		size;			

	// indexable object format
	unsigned char obj_format;
	// object size class (power of two)
	unsigned char obj_sizeclass;
	// object flags
	unsigned char obj_flags;
	// garbage collector color
	unsigned char gc_color;
	...
};

