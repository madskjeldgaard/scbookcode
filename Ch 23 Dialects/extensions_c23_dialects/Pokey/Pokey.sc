//redFrik
Pokey : UGen {
	*ar {|audf1= 0, audc1= 0, audf2= 0, audc2= 0, audf3= 0, audc3= 0, audf4= 0, audc4= 0, audctl= 0|
		^this.multiNew('audio', audf1, audc1, audf2, audc2, audf3, audc3, audf4, audc4, audctl)
	}
	*categories {^#["UGens>Emulators"]}
}

//requiress my Bit plugins
PokeySquare {		//wrapper class for Pokey
	*ar {|freq1= 0, tone1= 0, vol1= 0, freq2= 0, tone2= 0, vol2= 0, freq3= 0, tone3= 0, vol3= 0, freq4= 0, tone4= 0, vol4= 0, ctrl= 0|
		^Pokey.ar(
			Clip.kr(freq1, 0, 255),
			BitOr.kr(Clip.kr(tone1.round, 0, 7)*32, Clip.kr(vol1.round, 0, 15)),
			Clip.kr(freq2, 0, 255),
			BitOr.kr(Clip.kr(tone2.round, 0, 7)*32, Clip.kr(vol2.round, 0, 15)),
			Clip.kr(freq3, 0, 255),
			BitOr.kr(Clip.kr(tone3.round, 0, 7)*32, Clip.kr(vol3.round, 0, 15)),
			Clip.kr(freq4, 0, 255),
			BitOr.kr(Clip.kr(tone4.round, 0, 7)*32, Clip.kr(vol4.round, 0, 15)),
			(ctrl.round%64-1).round(2)*4+(ctrl.round%2)
		)
	}
	
	
	//-- equivalent conversion - scland helper methods
	*audf {|freq|
		//simple clip to keep within range
		^freq.clip(0, 255)
	}
	*audc {|tone, vol= 15|
		//join tone & vol into an audc byte
		//map vol bits 0-3 to audc bits 0-3
		//ignore audc bit 4
		//map tone bits 0-2 to audc bits 5-7
		^(tone&7).leftShift(5)|(vol&15)
	}
	*audctl {|ctrl|
		//convert to full audctl control byte
		//map ctrl bits 1-5 to audctl bits 3-7
		//ignore audctl bits 1-2
		//map ctrl bit 0 to audctl bit 0
		^(ctrl&62).leftShift(2).setBit(0, ctrl&1==1)
	}
	*categories {^#["UGens>Emulators"]}
}
