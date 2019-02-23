Figure 6.6.  Using Event types
(
	(type: 	\group, 	
id: 2
).play;					// create a group with nodeID 2

	(	type:		\note, 		// play note		
	 	sustain:	100, 		// lasting 100 seconds				
	 	group: 		2		// in group 2

	 ).play;	
	 										
)
(
	(type: 	\off, 	id: 2).play;		// release all notes in the group
	(type:	 \kill,	id: 2,  lag: 3).play;	// and free the group  3 seconds later	
)


