/*
	SuperCollider real time audio synthesis system
    Copyright (c) 2002 James McCartney. All rights reserved.
	http://www.audiosynth.com

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

//RedPhasor by redFrik

//--changes 090110:
//removed unused boolean flag in struct
//rate and zrate are now floats instead of double
//some code cosmetics
//--changes 071214:
//complete rewrite but should not break existing code.
//added different looping types (pingpong).
//corrected bug when start < end.  also loop points.

#include "SC_PlugIn.h"
static InterfaceTable *ft;

struct RedPhasor : public Unit {
	double mLevel;
	float m_previn;
	int ppdir;											//direction for pingpong
};

extern "C" {
	void load(InterfaceTable *inTable);
	void RedPhasor_Ctor(RedPhasor *unit);
	void RedPhasor_next_kk(RedPhasor *unit, int inNumSamples);
	void RedPhasor_next_ak(RedPhasor *unit, int inNumSamples);
	void RedPhasor_next_aa(RedPhasor *unit, int inNumSamples);
}

void load(InterfaceTable *inTable) {
	ft= inTable;
	DefineSimpleUnit(RedPhasor);
}

void RedPhasor_Ctor(RedPhasor *unit) {
	if(unit->mCalcRate==calc_FullRate) {
		if(INRATE(0)==calc_FullRate) {
			if(INRATE(1)==calc_FullRate) {
				SETCALC(RedPhasor_next_aa);
			} else {
				SETCALC(RedPhasor_next_ak);
			}
		} else {
			SETCALC(RedPhasor_next_kk);
		}
	} else {
		SETCALC(RedPhasor_next_ak);
	}
	unit->m_previn= ZIN0(0);
	unit->ppdir= 1;
	ZOUT0(0)= unit->mLevel= ZIN0(2);
}

void RedPhasor_next_kk(RedPhasor *unit, int inNumSamples) {
	float *out= ZOUT(0);
	float in= ZIN0(0);
	float rate= sc_max(0, ZIN0(1));
	double start= ZIN0(2);
	double end= ZIN0(3);
	float loop= ZIN0(4);
	float previn= unit->m_previn;
	double level= unit->mLevel;
	
	if((previn<=0.f)&&(in>0.f)) {
		level= start;
	}
	
	if(loop<=0.f) {										//kk off
		if(end<start) {
			LOOP(inNumSamples,
				ZXP(out)= level;
				level-= rate;
				level= sc_clip(level, end, start);
			);
		} else {
			LOOP(inNumSamples,
				ZXP(out)= level;
				level+= rate;
				level= sc_clip(level, start, end);
			);
		}
	
	} else if(loop<=1.f) {								//kk forward
		double loopstart= ZIN0(5);
		double loopend= ZIN0(6);
		if(end<start) {
			if(loopend<loopstart) {
				LOOP(inNumSamples,
					ZXP(out)= level;
					level-= rate;
					if(level<loopstart) {
						level= sc_wrap(level, loopend, loopstart);
					}
				);
			} else {
				LOOP(inNumSamples,
					ZXP(out)= level;
					if((level>=loopstart)&&(level<=loopend)) {
						level+= rate;
						if(level>loopend) {
							level= loopstart;
						}
					} else {
						level-= rate;
						if(level<loopstart) {
							level= loopend;
						}
					}
				);
			}
		} else {
			if(loopend<loopstart) {
				LOOP(inNumSamples, 
					ZXP(out)= level;
					if((level>=loopend)&&(level<=loopstart)) {
						level-= rate;
						if(level<loopend) {
							level= loopstart;
						}
					} else {
						level+= rate;
						if(level>loopstart) {
							level= loopstart;
						}
					}
				);
			} else {
				LOOP(inNumSamples,
					ZXP(out)= level;
					level+= rate;
					if(level>loopend) {
						level= sc_wrap(level, loopstart, loopend);
					}
				);
			}
		}
	
	} else /*if(loop<=2.f)*/ {							//kk pingpong
		double loopstart= ZIN0(5);
		double loopend= ZIN0(6);
		int ppdir= unit->ppdir;
		if(end<start) {
			if(loopend<loopstart) {
				LOOP(inNumSamples,
					ZXP(out)= level;
					if((level>=loopend)&&(level<=loopstart)) {
						level+= rate*ppdir;
						if(level<loopend) {
							level= loopend;
							ppdir= 1;
						} else if(level>loopstart) {
							level= loopstart;
							ppdir= -1;
						}
					} else {
						level-= rate;
						if(level<loopend) {
							level= loopstart;
						}
					}
				);
			} else {
				LOOP(inNumSamples,
					ZXP(out)= level;
					if((level>=loopstart)&&(level<=loopend)) {
						level+= rate*ppdir;
						if(level>loopend) {
							level= loopend;
							ppdir= -1;
						} else if(level<loopstart) {
							level= loopstart;
							ppdir= 1;
						}
					} else {
						level-= rate;
						if(level<loopstart) {
							level= loopend;
						}
					}
				);
			}
		} else {
			if(loopend<loopstart) {
				LOOP(inNumSamples,
					ZXP(out)= level;
					if((level>=loopend)&&(level<=loopstart)) {
						level+= rate*ppdir;
						if(level<loopend) {
							level= loopend;
							ppdir= 1;
						} else if(level>loopstart) {
							level= loopstart;
							ppdir= -1;
						}
					} else {
						level+= rate;
						if(level>loopstart) {
							level= loopstart;
						}
					}
				);
			} else {
				LOOP(inNumSamples,
					ZXP(out)= level;
					if((level>=loopstart)&&(level<=loopend)) {
						level+= rate*ppdir;
						if(level>loopend) {
							level= loopend;
							ppdir= -1;
						} else if(level<loopstart) {
							level= loopstart;
							ppdir= 1;
						}
					} else {
						level+= rate;
						if(level>loopend) {
							level= loopstart;
						}
					}
				);
			}
		}
		unit->ppdir= ppdir;
	}
	
	unit->m_previn= in;
	unit->mLevel= level;
}

void RedPhasor_next_ak(RedPhasor *unit, int inNumSamples) {
	float *out= ZOUT(0);
	float *in= ZIN(0);
	float rate= sc_max(0, ZIN0(1));
	double start= ZIN0(2);
	double end= ZIN0(3);
	float loop= ZIN0(4);
	float previn= unit->m_previn;
	double level= unit->mLevel;
	
	if(loop<=0.f) {										//ak off
		if(end<start) {
			LOOP(inNumSamples,
				float curin= ZXP(in);
				if((previn<=0.f)&&(curin>0.f)) {
					float frac= 1.f-(previn/(curin-previn));
					level= start+(frac*rate);
				}
				ZXP(out)= level;
				level-= rate;
				level= sc_clip(level, end, start);
				previn= curin;
			);
		} else {
			LOOP(inNumSamples,
				float curin= ZXP(in);
				if((previn<=0.f)&&(curin>0.f)) {
					float frac= 1.f-(previn/(curin-previn));
					level= start+(frac*rate);
				}
				ZXP(out)= level;
				level+= rate;
				level= sc_clip(level, start, end);
				previn= curin;
			);
		}
	
	} else if(loop<=1.f) {								//ak forward
		double loopstart= ZIN0(5);
		double loopend= ZIN0(6);
		if(end<start) {
			if(loopend<loopstart) {
				LOOP(inNumSamples,
					float curin= ZXP(in);
					if((previn<=0.f)&&(curin>0.f)) {
						float frac= 1.f-(previn/(curin-previn));
						level= start+(frac*rate);
					} 
					ZXP(out)= level;
					level-= rate;
					if(level<loopstart) {
						level= sc_wrap(level, loopend, loopstart);
					}
					previn= curin;
				);
			} else {
				LOOP(inNumSamples,
					float curin= ZXP(in);
					if((previn<=0.f)&&(curin>0.f)) {
						float frac= 1.f-(previn/(curin-previn));
						level= start+(frac*rate);
					} 
					ZXP(out)= level;
					if((level>=loopstart)&&(level<=loopend)) {
						level+= rate;
						if(level>loopend) {
							level= loopstart;
						}
					} else {
						level-= rate;
						if(level<loopstart) {
							level= loopend;
						}
					}
					previn= curin;
				);
			}
		} else {
			if(loopend<loopstart) {
				LOOP(inNumSamples,
					float curin= ZXP(in);
					if((previn<=0.f)&&(curin>0.f)) {
						float frac= 1.f-(previn/(curin-previn));
						level= start+(frac*rate);
					} 
					ZXP(out)= level;
					if((level>=loopend)&&(level<=loopstart)) {
						level-= rate;
						if(level<loopend) {
							level= loopstart;
						}
					} else {
						level+= rate;
						if(level>loopstart) {
							level= loopstart;
						}
					}
					previn= curin;
				);
			} else {
				LOOP(inNumSamples,
					float curin= ZXP(in);
					if((previn<=0.f)&&(curin>0.f)) {
						float frac= 1.f-(previn/(curin-previn));
						level= start+(frac*rate);
					} 
					ZXP(out)= level;
					level+= rate;
					if(level>loopend) {
						level= sc_wrap(level, loopstart, loopend);
					}
					previn= curin;
				);
			}
		}
		
	} else /*if(loop<=2.f)*/ {							//ak pingpong
		double loopstart= ZIN0(5);
		double loopend= ZIN0(6);
		int ppdir= unit->ppdir;
		if(end<start) {
			if(loopend<loopstart) {
				LOOP(inNumSamples,
					float curin= ZXP(in);
					if((previn<=0.f)&&(curin>0.f)) {
						float frac= 1.f-(previn/(curin-previn));
						level= start+(frac*rate);
						ppdir= -1;
					} 
					ZXP(out)= level;
					if((level>=loopend)&&(level<=loopstart)) {
						level+= rate*ppdir;
						if(level<loopend) {
							level= loopend;
							ppdir= 1;
						} else if(level>loopstart) {
							level= loopstart;
							ppdir= -1;
						}
					} else {
						level-= rate;
						if(level<loopend) {
							level= loopstart;
						}
					}
					previn= curin;
				);
			} else {
				LOOP(inNumSamples,
					float curin= ZXP(in);
					if((previn<=0.f)&&(curin>0.f)) {
						float frac= 1.f-(previn/(curin-previn));
						level= start+(frac*rate);
						ppdir= -1;
					} 
					ZXP(out)= level;
					if((level>=loopstart)&&(level<=loopend)) {
						level+= rate*ppdir;
						if(level>loopend) {
							level= loopend;
							ppdir= -1;
						} else if(level<loopstart) {
							level= loopstart;
							ppdir= 1;
						}
					} else {
						level-= rate;
						if(level<loopstart) {
							level= loopend;
						}
					}
					previn= curin;
				);
			}
		} else {
			if(loopend<loopstart) {
				LOOP(inNumSamples,
					float curin= ZXP(in);
					if((previn<=0.f)&&(curin>0.f)) {
						float frac= 1.f-(previn/(curin-previn));
						level= start+(frac*rate);
						ppdir= 1;
					}
					ZXP(out)= level;
					if((level>=loopend)&&(level<=loopstart)) {
						level+= rate*ppdir;
						if(level<loopend) {
							level= loopend;
							ppdir= 1;
						} else if(level>loopstart) {
							level= loopstart;
							ppdir= -1;
						}
					} else {
						level+= rate;
						if(level>loopstart) {
							level= loopstart;
						}
					}
					previn= curin;
				);
			} else {
				LOOP(inNumSamples,
					float curin= ZXP(in);
					if((previn<=0.f)&&(curin>0.f)) {
						float frac= 1.f-(previn/(curin-previn));
						level= start+(frac*rate);
					}
					ZXP(out)= level;
					if((level>=loopstart)&&(level<=loopend)) {
						level+= rate*ppdir;
						if(level>loopend) {
							level= loopend;
							ppdir= -1;
						} else if(level<loopstart) {
							level= loopstart;
							ppdir= 1;
						}
					} else {
						level+= rate;
						if(level>loopend) {
							level= loopstart;
						}
					}
					previn= curin;
				);
			}
		}
		unit->ppdir= ppdir;
	}
	
	unit->m_previn= previn;
	unit->mLevel= level;
}

void RedPhasor_next_aa(RedPhasor *unit, int inNumSamples) {
	float *out= ZOUT(0);
	float *in= ZIN(0);
	float *rate= ZIN(1);
	double start= ZIN0(2);
	double end= ZIN0(3);
	float loop= ZIN0(4);
	float previn= unit->m_previn;
	double level= unit->mLevel;
	
	if(loop<=0.f) {										//aa off
		if(end<start) {
			LOOP(inNumSamples,
				float curin= ZXP(in);
				float zrate= ZXP(rate);
				if((previn<=0.f)&&(curin>0.f)) {
					float frac= 1.f-(previn/(curin-previn));
					level= start+(frac*zrate);
				}
				ZXP(out)= level;
				level-= zrate;
				level= sc_clip(level, end, start);
				previn= curin;
			);
		} else {
			LOOP(inNumSamples,
				float curin= ZXP(in);
				float zrate= ZXP(rate);
				if((previn<=0.f)&&(curin>0.f)) {
					float frac= 1.f-(previn/(curin-previn));
					level= start+(frac*zrate);
				}
				ZXP(out)= level;
				level+= zrate;
				level= sc_clip(level, start, end);
				previn= curin;
			);
		}
	
	} else if(loop<=1.f) {								//aa forward
		double loopstart= ZIN0(5);
		double loopend= ZIN0(6);
		if(end<start) {
			if(loopend<loopstart) {
				LOOP(inNumSamples,
					float curin= ZXP(in);
					float zrate= ZXP(rate);
					if((previn<=0.f)&&(curin>0.f)) {
						float frac= 1.f-(previn/(curin-previn));
						level= start+(frac*zrate);
					} 
					ZXP(out)= level;
					level-= zrate;
					if(level<loopstart) {
						level= sc_wrap(level, loopend, loopstart);
					}
					previn= curin;
				);
			} else {
				LOOP(inNumSamples,
					float curin= ZXP(in);
					float zrate= ZXP(rate);
					if((previn<=0.f)&&(curin>0.f)) {
						float frac= 1.f-(previn/(curin-previn));
						level= start+(frac*zrate);
					} 
					ZXP(out)= level;
					if((level>=loopstart)&&(level<=loopend)) {
						level+= zrate;
						if(level>loopend) {
							level= loopstart;
						}
					} else {
						level-= zrate;
						if(level<loopstart) {
							level= loopend;
						}
					}
					previn= curin;
				);
			}
		} else {
			if(loopend<loopstart) {
				LOOP(inNumSamples,
					float curin= ZXP(in);
					float zrate= ZXP(rate);
					if((previn<=0.f)&&(curin>0.f)) {
						float frac= 1.f-(previn/(curin-previn));
						level= start+(frac*zrate);
					} 
					ZXP(out)= level;
					if((level>=loopend)&&(level<=loopstart)) {
						level-= zrate;
						if(level<loopend) {
							level= loopstart;
						}
					} else {
						level+= zrate;
						if(level>loopstart) {
							level= loopstart;
						}
					}
					previn= curin;
				);
			} else {
				LOOP(inNumSamples,
					float curin= ZXP(in);
					float zrate= ZXP(rate);
					if((previn<=0.f)&&(curin>0.f)) {
						float frac= 1.f-(previn/(curin-previn));
						level= start+(frac*zrate);
					} 
					ZXP(out)= level;
					level+= zrate;
					if(level>loopend) {
						level= sc_wrap(level, loopstart, loopend);
					}
					previn= curin;
				);
			}
		}
		
	} else /*if(loop<=2.f)*/ {							//aa pingpong
		double loopstart= ZIN0(5);
		double loopend= ZIN0(6);
		int ppdir= unit->ppdir;
		if(end<start) {
			if(loopend<loopstart) {
				LOOP(inNumSamples,
					float curin= ZXP(in);
					float zrate= ZXP(rate);
					if((previn<=0.f)&&(curin>0.f)) {
						float frac= 1.f-(previn/(curin-previn));
						level= start+(frac*zrate);
						ppdir= -1;
					} 
					ZXP(out)= level;
					if((level>=loopend)&&(level<=loopstart)) {
						level+= zrate*ppdir;
						if(level<loopend) {
							level= loopend;
							ppdir= 1;
						} else if(level>loopstart) {
							level= loopstart;
							ppdir= -1;
						}
					} else {
						level-= zrate;
						if(level<loopend) {
							level= loopstart;
						}
					}
					previn= curin;
				);
			} else {
				LOOP(inNumSamples,
					float curin= ZXP(in);
					float zrate= ZXP(rate);
					if((previn<=0.f)&&(curin>0.f)) {
						float frac= 1.f-(previn/(curin-previn));
						level= start+(frac*zrate);
						ppdir= -1;
					} 
					ZXP(out)= level;
					if((level>=loopstart)&&(level<=loopend)) {
						level+= zrate*ppdir;
						if(level>loopend) {
							level= loopend;
							ppdir= -1;
						} else if(level<loopstart) {
							level= loopstart;
							ppdir= 1;
						}
					} else {
						level-= zrate;
						if(level<loopstart) {
							level= loopend;
						}
					}
					previn= curin;
				);
			}
		} else {
			if(loopend<loopstart) {
				LOOP(inNumSamples,
					float curin= ZXP(in);
					float zrate= ZXP(rate);
					if((previn<=0.f)&&(curin>0.f)) {
						float frac= 1.f-(previn/(curin-previn));
						level= start+(frac*zrate);
						ppdir= 1;
					}
					ZXP(out)= level;
					if((level>=loopend)&&(level<=loopstart)) {
						level+= zrate*ppdir;
						if(level<loopend) {
							level= loopend;
							ppdir= 1;
						} else if(level>loopstart) {
							level= loopstart;
							ppdir= -1;
						}
					} else {
						level+= zrate;
						if(level>loopstart) {
							level= loopstart;
						}
					}
					previn= curin;
				);
			} else {
				LOOP(inNumSamples,
					float curin= ZXP(in);
					float zrate= ZXP(rate);
					if((previn<=0.f)&&(curin>0.f)) {
						float frac= 1.f-(previn/(curin-previn));
						level= start+(frac*zrate);
					}
					ZXP(out)= level;
					if((level>=loopstart)&&(level<=loopend)) {
						level+= zrate*ppdir;
						if(level>loopend) {
							level= loopend;
							ppdir= -1;
						} else if(level<loopstart) {
							level= loopstart;
							ppdir= 1;
						}
					} else {
						level+= zrate;
						if(level>loopend) {
							level= loopstart;
						}
					}
					previn= curin;
				);
			}
		}
		unit->ppdir= ppdir;
	}
	
	unit->m_previn= previn;
	unit->mLevel= level;
}
