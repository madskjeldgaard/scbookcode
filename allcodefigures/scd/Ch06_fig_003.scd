
Figure  6.3. The OSC commands created by the note event
e.asOSC.do{ | osc | osc.postcs };

// the result that would be posted:		
//  [ 0.0, ['s_new', 'sine', 1000, 0, 1, 'out', 0, 'freq', 400.0, 'amp', 0.1, 'pan', 0, 'ar', 2, 'dr', 4] ]
//  [ 2.0, [ 'n_set', 1000, 'gate', 0 ] ] 

