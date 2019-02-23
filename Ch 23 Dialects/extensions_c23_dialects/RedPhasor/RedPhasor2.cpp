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

//RedPhasor2 by redFrik 090110



#include "SC_PlugIn.h"
static InterfaceTable *ft;

struct RedPhasor2 : public Unit {
	double mLevel;
	float m_previn;
	int ppdir;											//direction for pingpong
};

extern "C" {
	void load(InterfaceTable *inTable);
	void RedPhasor2_Ctor(RedPhasor2 *unit);
	void RedPhasor2_next_kk(RedPhasor2 *unit, int inNumSamples);
	void RedPhasor2_next_ak(RedPhasor2 *unit, int inNumSamples);
	void RedPhasor2_next_aa(RedPhasor2 *unit, int inNumSamples);
}

void load(InterfaceTable *inTable) {
	ft= inTable;
	DefineSimpleUnit(RedPhasor2);
}

void RedPhasor2_Ctor(RedPhasor2 *unit) {
	if(unit->mCalcRate==calc_FullRate) {
		if(INRATE(0)==calc_FullRate) {
			if(INRATE(1)==calc_FullRate) {
				SETCALC(RedPhasor2_next_aa);
			} else {
				SETCALC(RedPhasor2_next_ak);
			}
		} else {
			SETCALC(RedPhasor2_next_kk);
		}
	} else {
		SETCALC(RedPhasor2_next_ak);
	}
	unit->m_previn= ZIN0(0);
	unit->ppdir= 1;
	ZOUT0(0)= unit->mLevel= ZIN0(2);
}

void RedPhasor2_next_kk(RedPhasor2 *unit, int inNumSamples) {
	float *out= ZOUT(0);
	float in= ZIN0(0);
	float rate= ZIN0(1);
	double start= ZIN0(2);
	double end= ZIN0(3);
	float loop= ZIN0(4);
	float previn= unit->m_previn;
	double level= unit->mLevel;
	double reset= start;
	
	if(end<start) {
		double endTemp= end;
		end= start;
		start= endTemp;
	}
	
	if((previn<=0.f)&&(in>0.f)) {
		level= reset;
	}
	
	if(loop<=0.f) {										//kk off
		LOOP(inNumSamples,
			ZXP(out)= level;
			level+= rate;
			level= sc_clip(level, start, end);
		);
	
	} else if(loop<=1.f) {								//kk forward
		double loopstart= ZIN0(5);
		double loopend= ZIN0(6);
		if(loopend<loopstart) {
			double loopendTemp= loopend;
			loopend= loopstart;
			loopstart= loopendTemp;
		}
		LOOP(inNumSamples,
			ZXP(out)= level;
			level+= rate;
			if((rate>=0.f)&&(level>loopend)) {
				level= level-(loopend-loopstart);
			} else if((rate<0.f)&&(level<loopstart)) {
				level= level+(loopend-loopstart);
			}
			level= sc_clip(level, start, end);
		);
	
	} else /*if(loop<=2.f)*/ {							//kk pingpong
		double loopstart= ZIN0(5);
		double loopend= ZIN0(6);
		int ppdir= unit->ppdir;
		if(loopend<loopstart) {
			double loopendTemp= loopend;
			loopend= loopstart;
			loopstart= loopendTemp;
		}
		LOOP(inNumSamples,
			ZXP(out)= level;
			if((level>=loopstart)&&(level<=loopend)) {
				level+= rate*ppdir;
				if(level>loopend) {
					level= loopend;
					ppdir= ppdir * -1;
				} else if(level<loopstart) {
					level= loopstart;
					ppdir= ppdir * -1;
				}
			} else {
				level+= rate;
			}
			level= sc_clip(level, start, end);
		);
		unit->ppdir= ppdir;
	}
		
	unit->m_previn= in;
	unit->mLevel= level;
}

void RedPhasor2_next_ak(RedPhasor2 *unit, int inNumSamples) {
	float *out= ZOUT(0);
	float *in= ZIN(0);
	float rate= ZIN0(1);
	double start= ZIN0(2);
	double end= ZIN0(3);
	float loop= ZIN0(4);
	float previn= unit->m_previn;
	double level= unit->mLevel;
	double reset= start;
	
	if(end<start) {
		double endTemp= end;
		end= start;
		start= endTemp;
	}
	
	if(loop<=0.f) {										//ak off
		LOOP(inNumSamples,
			float curin= ZXP(in);
			if((previn<=0.f)&&(curin>0.f)) {
				float frac= 1.f-(previn/(curin-previn));
				level= reset+(frac*rate);
			}
			ZXP(out)= level;
			level+= rate;
			level= sc_clip(level, start, end);
			previn= curin;
		);
	
	} else if(loop<=1.f) {								//ak forward
		double loopstart= ZIN0(5);
		double loopend= ZIN0(6);
		if(loopend<loopstart) {
			double loopendTemp= loopend;
			loopend= loopstart;
			loopstart= loopendTemp;
		}
		LOOP(inNumSamples,
			float curin= ZXP(in);
			if((previn<=0.f)&&(curin>0.f)) {
				float frac= 1.f-(previn/(curin-previn));
				level= reset+(frac*rate);
			}
			ZXP(out)= level;
			level+= rate;
			if((rate>=0.f)&&(level>loopend)) {
				level= level-(loopend-loopstart);
			} else if((rate<0.f)&&(level<loopstart)) {
				level= level+(loopend-loopstart);
			}
			level= sc_clip(level, start, end);
			previn= curin;
		);
		
	} else /*if(loop<=2.f)*/ {							//ak pingpong
		double loopstart= ZIN0(5);
		double loopend= ZIN0(6);
		int ppdir= unit->ppdir;
		if(loopend<loopstart) {
			double loopendTemp= loopend;
			loopend= loopstart;
			loopstart= loopendTemp;
		}
		LOOP(inNumSamples,
			float curin= ZXP(in);
			if((previn<=0.f)&&(curin>0.f)) {
				float frac= 1.f-(previn/(curin-previn));
				level= reset+(frac*rate);
				ppdir= -1;
			}
			ZXP(out)= level;
			if((level>=loopstart)&&(level<=loopend)) {
				level+= rate*ppdir;
				if(level>loopend) {
					level= loopend;
					ppdir= ppdir * -1;
				} else if(level<loopstart) {
					level= loopstart;
					ppdir= ppdir * -1;
				}
			} else {
				level+= rate;
			}
			level= sc_clip(level, start, end);
			previn= curin;
		);
		unit->ppdir= ppdir;
	}
	
	unit->m_previn= previn;
	unit->mLevel= level;
}

void RedPhasor2_next_aa(RedPhasor2 *unit, int inNumSamples) {
	float *out= ZOUT(0);
	float *in= ZIN(0);
	float *rate= ZIN(1);
	double start= ZIN0(2);
	double end= ZIN0(3);
	float loop= ZIN0(4);
	float previn= unit->m_previn;
	double level= unit->mLevel;
	double reset= start;
	
	if(end<start) {
		double endTemp= end;
		end= start;
		start= endTemp;
	}
	
	if(loop<=0.f) {										//aa off
		LOOP(inNumSamples,
			float curin= ZXP(in);
			float zrate= ZXP(rate);
			if((previn<=0.f)&&(curin>0.f)) {
				float frac= 1.f-(previn/(curin-previn));
				level= reset+(frac*zrate);
			}
			ZXP(out)= level;
			level+= zrate;
			level= sc_clip(level, start, end);
			previn= curin;
		);
	
	} else if(loop<=1.f) {								//aa forward
		double loopstart= ZIN0(5);
		double loopend= ZIN0(6);
		if(loopend<loopstart) {
			double loopendTemp= loopend;
			loopend= loopstart;
			loopstart= loopendTemp;
		}
		LOOP(inNumSamples,
			float curin= ZXP(in);
			float zrate= ZXP(rate);
			if((previn<=0.f)&&(curin>0.f)) {
				float frac= 1.f-(previn/(curin-previn));
				level= reset+(frac*zrate);
			}
			ZXP(out)= level;
			level+= zrate;
			if((zrate>=0.f)&&(level>loopend)) {
				level= level-(loopend-loopstart);
			} else if((zrate<0.f)&&(level<loopstart)) {
				level= level+(loopend-loopstart);
			}
			level= sc_clip(level, start, end);
			previn= curin;
		);
	
	} else /*if(loop<=2.f)*/ {							//aa pingpong
		double loopstart= ZIN0(5);
		double loopend= ZIN0(6);
		int ppdir= unit->ppdir;
		if(loopend<loopstart) {
			double loopendTemp= loopend;
			loopend= loopstart;
			loopstart= loopendTemp;
		}
		LOOP(inNumSamples,
			float curin= ZXP(in);
			float zrate= ZXP(rate);
			if((previn<=0.f)&&(curin>0.f)) {
				float frac= 1.f-(previn/(curin-previn));
				level= reset+(frac*zrate);
				ppdir= -1;
			}
			ZXP(out)= level;
			if((level>=loopstart)&&(level<=loopend)) {
				level+= zrate*ppdir;
				if(level>loopend) {
					level= loopend;
					ppdir= ppdir * -1;
				} else if(level<loopstart) {
					level= loopstart;
					ppdir= ppdir * -1;
				}
			} else {
				level+= zrate;
			}
			level= sc_clip(level, start, end);
			previn= curin;
		);
		unit->ppdir= ppdir;
	}
	
	unit->m_previn= previn;
	unit->mLevel= level;
}
