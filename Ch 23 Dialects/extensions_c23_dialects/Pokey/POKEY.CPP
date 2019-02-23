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
	
	--
	Pokey - POKEY Chip Sound Simulator for SC
	Author Ron Fries, Ported by Fredrik Olofsson 070222
	(update 070417 - ifndef powerpc to fix big endian problem on intel)
*/

#include "SC_PlugIn.h"
static InterfaceTable *ft;


/*****************************************************************************/
/*                                                                           */
/* Module:  POKEY Chip Emulator, V2.3                                        */
/* Purpose: To emulate the sound generation hardware of the Atari POKEY chip.*/
/* Author:  Ron Fries                                                        */
/*                                                                           */
/* Revision History:                                                         */
/*                                                                           */
/* 09/22/96 - Ron Fries - Initial Release                                    */
/* 01/14/97 - Ron Fries - Corrected a minor problem to improve sound quality */
/*                        Also changed names from POKEY11.x to POKEY.x       */
/* 01/17/97 - Ron Fries - Added support for multiple POKEY chips.            */
/* 03/31/97 - Ron Fries - Made some minor mods for MAME (changed to signed   */
/*                        8-bit sample, increased gain range, removed        */
/*                        _disable() and _enable().)                         */
/* 04/06/97 - Brad Oliver - Some cross-platform modifications. Added         */
/*                          big/little endian #defines, removed <dos.h>,     */
/*                          conditional defines for TRUE/FALSE               */
/* 01/19/98 - Ron Fries - Changed signed/unsigned sample support to a        */
/*                        compile-time option.  Defaults to unsigned -       */
/*                        define SIGNED_SAMPLES to create signed.            */
/*                                                                           */
/* V2.0 Detailed Changes                                                     */
/* ---------------------                                                     */
/*                                                                           */
/* Now maintains both a POLY9 and POLY17 counter.  Though this slows the     */
/* emulator in general, it was required to support mutiple POKEYs since      */
/* each chip can individually select POLY9 or POLY17 operation.  Also,       */
/* eliminated the Poly17_size variable.                                      */
/*                                                                           */
/* Changed address of POKEY chip.  In the original, the chip was fixed at    */
/* location D200 for compatibility with the Atari 800 line of 8-bit          */
/* computers. The update function now only examines the lower four bits, so  */
/* the location for all emulated chips is effectively xxx0 - xxx8.           */
/*                                                                           */
/* The Update_pokey_sound function has two additional parameters which       */
/* selects the desired chip and selects the desired gain.                    */
/*                                                                           */
/* Added clipping to reduce distortion, configurable at compile-time.        */
/*                                                                           */
/* The Pokey_sound_init function has an additional parameter which selects   */
/* the number of pokey chips to emulate.                                     */
/*                                                                           */
/* The output will be amplified by gain/16.  If the output exceeds the       */
/* maximum value after the gain, it will be limited to reduce distortion.    */
/* The best value for the gain depends on the number of POKEYs emulated      */
/* and the maximum volume used.  The maximum possible output for each        */
/* channel is 15, making the maximum possible output for a single chip to    */
/* be 60.  Assuming all four channels on the chip are used at full volume,   */
/* a gain of 64 can be used without distortion.  If 4 POKEY chips are        */
/* emulated and all 16 channels are used at full volume, the gain must be    */
/* no more than 16 to prevent distortion.  Of course, if only a few of the   */
/* 16 channels are used or not all channels are used at full volume, a       */
/* larger gain can be used.                                                  */
/*                                                                           */
/* The Pokey_process routine automatically processes and mixes all selected  */
/* chips/channels.  No additional calls or functions are required.           */
/*                                                                           */
/* The unoptimized Pokey_process2() function has been removed.               */
/*                                                                           */
/*****************************************************************************/
/*                                                                           */
/*                 License Information and Copyright Notice                  */
/*                 ========================================                  */
/*                                                                           */
/* PokeySound is Copyright(c) 1996-1998 by Ron Fries                         */
/*                                                                           */
/* This library is free software; you can redistribute it and/or modify it   */
/* under the terms of version 2 of the GNU Library General Public License    */
/* as published by the Free Software Foundation.                             */
/*                                                                           */
/* This library is distributed in the hope that it will be useful, but       */
/* WITHOUT ANY WARRANTY; without even the implied warranty of                */
/* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Library */
/* General Public License for more details.                                  */
/* To obtain a copy of the GNU Library General Public License, write to the  */
/* Free Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.   */
/*                                                                           */
/* Any permitted reproduction of these routines, in whole or in part, must   */
/* bear this legend.                                                         */
/*                                                                           */
/*****************************************************************************/

//#include <stdio.h>
#include <stdlib.h>
//#include <time.h>

#include "pokey.h"

#ifndef __POWERPC__
#undef BIG_ENDIAN
#endif

/* CONSTANT DEFINITIONS */

/* definitions for AUDCx (D201, D203, D205, D207) */
#define NOTPOLY5    0x80     /* selects POLY5 or direct CLOCK */
#define POLY4       0x40     /* selects POLY4 or POLY17 */
#define PURE        0x20     /* selects POLY4/17 or PURE tone */
#define VOL_ONLY    0x10     /* selects VOLUME OUTPUT ONLY */
#define VOLUME_MASK 0x0f     /* volume mask */

/* definitions for AUDCTL (D208) */
#define POLY9       0x80     /* selects POLY9 or POLY17 */
#define CH1_179     0x40     /* selects 1.78979 MHz for Ch 1 */
#define CH3_179     0x20     /* selects 1.78979 MHz for Ch 3 */
#define CH1_CH2     0x10     /* clocks channel 1 w/channel 2 */
#define CH3_CH4     0x08     /* clocks channel 3 w/channel 4 */
#define CH1_FILTER  0x04     /* selects channel 1 high pass filter */
#define CH2_FILTER  0x02     /* selects channel 2 high pass filter */
#define CLOCK_15    0x01     /* selects 15.6999kHz or 63.9210kHz */

/* for accuracy, the 64kHz and 15kHz clocks are exact divisions of
   the 1.79MHz clock */
#define DIV_64      28       /* divisor for 1.79MHz clock to 64 kHz */
#define DIV_15      114      /* divisor for 1.79MHz clock to 15 kHz */

/* the size (in entries) of the 4 polynomial tables */
#define POLY4_SIZE  0x000f
#define POLY5_SIZE  0x001f
#define POLY9_SIZE  0x01ff

#ifdef COMP16                  /* if 16-bit compiler */
#define POLY17_SIZE 0x00007fffL    /* reduce to 15 bits for simplicity */
#else
#define POLY17_SIZE 0x0001ffffL    /* else use the full 17 bits */
#endif

/* channel/chip definitions */
#define CHAN1       0
#define CHAN2       1
#define CHAN3       2
#define CHAN4       3
#define CHIP1       0
#define CHIP2       4
#define CHIP3       8
#define CHIP4      12
#define SAMPLE    127

// LBO - changed for cross-platform support
#ifndef FALSE
#define FALSE       0
#endif
#ifndef TRUE
#define TRUE        1
#endif

/* GLOBAL VARIABLE DEFINITIONS */

/* number of pokey chips currently emulated */
static uint8 Num_pokeys;


/* Initialze the bit patterns for the polynomials. */

/* The 4bit and 5bit patterns are the identical ones used in the pokey chip. */
/* Though the patterns could be packed with 8 bits per byte, using only a */
/* single bit per byte keeps the math simple, which is important for */
/* efficient processing. */

static uint8 bit4[POLY4_SIZE] =
      { 1,1,0,1,1,1,0,0,0,0,1,0,1,0,0 };

static uint8 bit5[POLY5_SIZE] =
      { 0,0,1,1,0,0,0,1,1,1,1,0,0,1,0,1,0,1,1,0,1,1,1,0,1,0,0,0,0,0,1 };

//static uint8 bit17[POLY17_SIZE];  /* Rather than have a table with 131071 */
                            /* entries, I use a random number generator. */
                            /* It shouldn't make much difference since */
                            /* the pattern rarely repeats anyway. */



struct Pokey : public Unit {
	
	/* structures to hold the 9 pokey control bytes */
	uint8 AUDF[4 * MAXPOKEYS];			/* AUDFx (D200, D202, D204, D206) */
	uint8 AUDC[4 * MAXPOKEYS];			/* AUDCx (D201, D203, D205, D207) */
	uint8 AUDCTL[MAXPOKEYS];			/* AUDCTL (D208) */
	
	uint8 AUDV[4 * MAXPOKEYS];			/* Channel volume - derived */
	uint8 Outbit[4 * MAXPOKEYS];		/* current state of the output (high or low) */
	uint8 Outvol[4 * MAXPOKEYS];		/* last output volume for each channel */
	
	uint32 Poly_adjust;					/* the amount that the polynomial will need */
										/* to be adjusted to process the next bit */
	uint32 P4;							/* Global position pointer for the 4-bit  POLY array */
	uint32 P5;							/* Global position pointer for the 5-bit  POLY array */
	//uint32 P9;							/* Global position pointer for the 9-bit  POLY array */
	//uint32 P17;							/* Global position pointer for the 17-bit POLY array */
	uint32 Div_n_cnt[4 * MAXPOKEYS];	/* Divide by n counter. one for each channel */
	uint32 Div_n_max[4 * MAXPOKEYS];	/* Divide by n maximum, one for each channel */
	uint32 Samp_n_max;					/* Sample max.  For accuracy, it is *256 */
	uint32 Samp_n_cnt[2];				/* Sample cnt. */
	uint32 Base_mult[MAXPOKEYS];		/* selects either 64Khz or 15Khz clock mult */
	float last0;
	float last1;
	float last2;
	float last3;
	float last4;
	float last5;
	float last6;
	float last7;
	float last8;
};

extern "C" {
	void load(InterfaceTable *inTable);
	void Pokey_Ctor(Pokey *unit);
	void Pokey_next_k(Pokey *unit, int inNumSamples);
	void Pokey_sound_init (Pokey *unit, uint32 freq17, uint16 playback_freq, uint8 num_pokeys);
	void Update_pokey_sound (Pokey *unit, uint16 addr, uint8 val, uint8 chip, uint8 gain);
	void Pokey_process (Pokey *unit, float *buffer, register uint16 n);
};

void load(InterfaceTable *inTable) {
	ft= inTable;
	DefineSimpleUnit(Pokey);
}

void Pokey_Ctor(Pokey *unit) {
	unit->last0= 0.f;
	unit->last1= 0.f;
	unit->last2= 0.f;
	unit->last3= 0.f;
	unit->last4= 0.f;
	unit->last5= 0.f;
	unit->last6= 0.f;
	unit->last7= 0.f;
	unit->last8= 0.f;
	SETCALC(Pokey_next_k);
	Pokey_sound_init(unit, FREQ_17_APPROX, FREQ_17_APPROX/56, 1);	//sample_freq, playback_freq, num_pokeys
	Pokey_next_k(unit, 1);
}

void Pokey_next_k(Pokey *unit, int inNumSamples) {
	float *out= OUT(0);
	if(IN0(0)!=unit->last0) {
		unit->last0= IN0(0);
		Update_pokey_sound(unit, AUDF1_C, unit->last0, 0, 12);
	}
	if(IN0(1)!=unit->last1) {
		unit->last1= IN0(1);
		Update_pokey_sound(unit, AUDC1_C, unit->last1, 0, 12);
	}
	if(IN0(2)!=unit->last2) {
		unit->last2= IN0(2);
		Update_pokey_sound(unit, AUDF2_C, unit->last2, 0, 12);
	}
	if(IN0(3)!=unit->last3) {
		unit->last3= IN0(3);
		Update_pokey_sound(unit, AUDC2_C, unit->last3, 0, 12);
	}
	if(IN0(4)!=unit->last4) {
		unit->last4= IN0(4);
		Update_pokey_sound(unit, AUDF3_C, unit->last4, 0, 12);
	}
	if(IN0(5)!=unit->last5) {
		unit->last5= IN0(5);
		Update_pokey_sound(unit, AUDC3_C, unit->last5, 0, 12);
	}
	if(IN0(6)!=unit->last6) {
		unit->last6= IN0(6);
		Update_pokey_sound(unit, AUDF4_C, unit->last6, 0, 12);
	}
	if(IN0(7)!=unit->last7) {
		unit->last7= IN0(7);
		Update_pokey_sound(unit, AUDC4_C, unit->last7, 0, 12);
	}
	if(IN0(8)!=unit->last8) {
		unit->last8= IN0(8);
		Update_pokey_sound(unit, AUDCTL_C, unit->last8, 0, 12);
	}
	Pokey_process(unit, out, inNumSamples);							//unit, buffer, n
}





/*****************************************************************************/
/* In my routines, I treat the sample output as another divide by N counter  */
/* For better accuracy, the Samp_n_cnt has a fixed binary decimal point      */
/* which has 8 binary digits to the right of the decimal point.  I use a two */
/* byte array to give me a minimum of 40 bits, and then use pointer math to  */
/* reference either the 24.8 whole/fraction combination or the 32-bit whole  */
/* only number.  This is mainly used to keep the math simple for             */
/* optimization. See below:                                                  */
/*                                                                           */
/* Representation on little-endian machines:                                 */
/* xxxxxxxx xxxxxxxx xxxxxxxx xxxxxxxx | xxxxxxxx xxxxxxxx xxxxxxxx xxxxxxxx */
/* fraction   whole    whole    whole      whole   unused   unused   unused  */
/*                                                                           */
/* Samp_n_cnt[0] gives me a 32-bit int 24 whole bits with 8 fractional bits, */
/* while (uint32 *)((uint8 *)(&Samp_n_cnt[0])+1) gives me the 32-bit whole   */
/* number only.                                                              */
/*                                                                           */
/* Representation on big-endian machines:                                    */
/* xxxxxxxx xxxxxxxx xxxxxxxx xxxxxxxx | xxxxxxxx xxxxxxxx xxxxxxxx.xxxxxxxx */
/*  unused   unused   unused    whole      whole    whole    whole  fraction */
/*                                                                           */
/* Samp_n_cnt[1] gives me a 32-bit int 24 whole bits with 8 fractional bits, */
/* while (uint32 *)((uint8 *)(&Samp_n_cnt[0])+3) gives me the 32-bit whole   */
/* number only.                                                              */
/*****************************************************************************/


/*****************************************************************************/
/* Module:  Pokey_sound_init()                                               */
/* Purpose: to handle the power-up initialization functions                  */
/*          these functions should only be executed on a cold-restart        */
/*                                                                           */
/* Author:  Ron Fries                                                        */
/* Date:    January 1, 1997                                                  */
/*                                                                           */
/* Inputs:  freq17 - the value for the '1.79MHz' Pokey audio clock           */
/*          playback_freq - the playback frequency in samples per second     */
/*          num_pokeys - specifies the number of pokey chips to be emulated  */
/*                                                                           */
/* Outputs: Adjusts local globals - no return value                          */
/*                                                                           */
/*****************************************************************************/

void Pokey_sound_init (Pokey *unit, uint32 freq17, uint16 playback_freq, uint8 num_pokeys)
{
   uint8 chan,chip;

   /* fill the 17bit polynomial with random bits */
   //f0 replaced with random number in process routine due to cpu spike at init
   //for (n=0; n<POLY17_SIZE; n++)
   //{
   //   bit17[n] = rand() & 0x01;       /* fill poly 17 with random bits */
   //}

   /* disable interrupts to handle critical sections */
/*    _disable();*/     /* RSF - removed for portability 31-MAR-97 */

   /* start all of the polynomial counters at zero */
   unit->Poly_adjust = 0;
   unit->P4 = 0;
   unit->P5 = 0;
   //unit->P9 = 0;
   //unit->P17 = 0;

   /* calculate the sample 'divide by N' value based on the playback freq. */
   unit->Samp_n_max = ((uint32)freq17 << 8) / playback_freq;

   unit->Samp_n_cnt[0] = 0;  /* initialize all bits of the sample */
   unit->Samp_n_cnt[1] = 0;  /* 'divide by N' counter */

   for (chan = 0; chan < (MAXPOKEYS * 4); chan++)
   {
      unit->Outvol[chan] = 0;
      unit->Outbit[chan] = 0;
      unit->Div_n_cnt[chan] = 0;
      unit->Div_n_max[chan] = 0x7fffffffL;
      unit->AUDC[chan] = 0;
      unit->AUDF[chan] = 0;
      unit->AUDV[chan] = 0;
   }

   for (chip = 0; chip < MAXPOKEYS; chip++)
   {
      unit->AUDCTL[chip] = 0;
      unit->Base_mult[chip] = DIV_64;
   }

   /* set the number of pokey chips currently emulated */
   Num_pokeys = num_pokeys;

/*    _enable();*/      /* RSF - removed for portability 31-MAR-97 */
}


/*****************************************************************************/
/* Module:  Update_pokey_sound()                                             */
/* Purpose: To process the latest control values stored in the AUDF, AUDC,   */
/*          and AUDCTL registers.  It pre-calculates as much information as  */
/*          possible for better performance.  This routine has not been      */
/*          optimized.                                                       */
/*                                                                           */
/* Author:  Ron Fries                                                        */
/* Date:    January 1, 1997                                                  */
/*                                                                           */
/* Inputs:  addr - the address of the parameter to be changed                */
/*          val - the new value to be placed in the specified address        */
/*          gain - specified as an 8-bit fixed point number - use 1 for no   */
/*                 amplification (output is multiplied by gain)              */
/*                                                                           */
/* Outputs: Adjusts local globals - no return value                          */
/*                                                                           */
/*****************************************************************************/

void Update_pokey_sound (Pokey *unit, uint16 addr, uint8 val, uint8 chip, uint8 gain)
{
    uint32 new_val = 0;
    uint8 chan;
    uint8 chan_mask;
    uint8 chip_offs;

    /* disable interrupts to handle critical sections */
/*    _disable();*/     /* RSF - removed for portability 31-MAR-97 */

    /* calculate the chip_offs for the channel arrays */
    chip_offs = chip << 2;

    /* determine which address was changed */
    switch (addr & 0x0f)
    {
       case AUDF1_C:
          unit->AUDF[CHAN1 + chip_offs] = val;
          chan_mask = 1 << CHAN1;

          if (unit->AUDCTL[chip] & CH1_CH2)    /* if ch 1&2 tied together */
            chan_mask |= 1 << CHAN2;   /* then also change on ch2 */
          break;

       case AUDC1_C:
          unit->AUDC[CHAN1 + chip_offs] = val;
               /* RSF - changed gain (removed >> 4) 31-MAR-97 */
          unit->AUDV[CHAN1 + chip_offs] = (val & VOLUME_MASK) * gain;
          chan_mask = 1 << CHAN1;
          break;

       case AUDF2_C:
          unit->AUDF[CHAN2 + chip_offs] = val;
          chan_mask = 1 << CHAN2;
          break;

       case AUDC2_C:
          unit->AUDC[CHAN2 + chip_offs] = val;
               /* RSF - changed gain (removed >> 4) 31-MAR-97 */
          unit->AUDV[CHAN2 + chip_offs] = (val & VOLUME_MASK) * gain;
          chan_mask = 1 << CHAN2;
          break;

       case AUDF3_C:
          unit->AUDF[CHAN3 + chip_offs] = val;
          chan_mask = 1 << CHAN3;

          if (unit->AUDCTL[chip] & CH3_CH4)   /* if ch 3&4 tied together */
             chan_mask |= 1 << CHAN4;  /* then also change on ch4 */
          break;

       case AUDC3_C:
          unit->AUDC[CHAN3 + chip_offs] = val;
               /* RSF - changed gain (removed >> 4) 31-MAR-97 */
          unit->AUDV[CHAN3 + chip_offs] = (val & VOLUME_MASK) * gain;
          chan_mask = 1 << CHAN3;
          break;

       case AUDF4_C:
          unit->AUDF[CHAN4 + chip_offs] = val;
          chan_mask = 1 << CHAN4;
          break;

       case AUDC4_C:
          unit->AUDC[CHAN4 + chip_offs] = val;
               /* RSF - changed gain (removed >> 4) 31-MAR-97 */
          unit->AUDV[CHAN4 + chip_offs] = (val & VOLUME_MASK) * gain;
          chan_mask = 1 << CHAN4;
          break;

       case AUDCTL_C:
          unit->AUDCTL[chip] = val;
          chan_mask = 15;       /* all channels */

          /* determine the base multiplier for the 'div by n' calculations */
          if (unit->AUDCTL[chip] & CLOCK_15)
             unit->Base_mult[chip] = DIV_15;
          else
             unit->Base_mult[chip] = DIV_64;

          break;

       default:
          chan_mask = 0;
          break;
    }

    /************************************************************/
    /* As defined in the manual, the exact Div_n_cnt values are */
    /* different depending on the frequency and resolution:     */
    /*    64 kHz or 15 kHz - AUDF + 1                           */
    /*    1 MHz, 8-bit -     AUDF + 4                           */
    /*    1 MHz, 16-bit -    AUDF[CHAN1]+256*AUDF[CHAN2] + 7    */
    /************************************************************/

    /* only reset the channels that have changed */

    if (chan_mask & (1 << CHAN1))
    {
       /* process channel 1 frequency */
       if (unit->AUDCTL[chip] & CH1_179)
          new_val = unit->AUDF[CHAN1 + chip_offs] + 4;
       else
          new_val = (unit->AUDF[CHAN1 + chip_offs] + 1) * unit->Base_mult[chip];

       if (new_val != unit->Div_n_max[CHAN1 + chip_offs])
       {
          unit->Div_n_max[CHAN1 + chip_offs] = new_val;

          if (unit->Div_n_cnt[CHAN1 + chip_offs] > new_val)
          {
             unit->Div_n_cnt[CHAN1 + chip_offs] = new_val;
          }
       }
    }

    if (chan_mask & (1 << CHAN2))
    {
       /* process channel 2 frequency */
       if (unit->AUDCTL[chip] & CH1_CH2)
          if (unit->AUDCTL[chip] & CH1_179)
             new_val = unit->AUDF[CHAN2 + chip_offs] * 256 +
                       unit->AUDF[CHAN1 + chip_offs] + 7;
          else
             new_val = (unit->AUDF[CHAN2 + chip_offs] * 256 +
                        unit->AUDF[CHAN1 + chip_offs] + 1) * unit->Base_mult[chip];
       else
          new_val = (unit->AUDF[CHAN2 + chip_offs] + 1) * unit->Base_mult[chip];

       if (new_val != unit->Div_n_max[CHAN2 + chip_offs])
       {
          unit->Div_n_max[CHAN2 + chip_offs] = new_val;

          if (unit->Div_n_cnt[CHAN2 + chip_offs] > new_val)
          {
             unit->Div_n_cnt[CHAN2 + chip_offs] = new_val;
          }
       }
    }

    if (chan_mask & (1 << CHAN3))
    {
       /* process channel 3 frequency */
       if (unit->AUDCTL[chip] & CH3_179)
          new_val = unit->AUDF[CHAN3 + chip_offs] + 4;
       else
          new_val= (unit->AUDF[CHAN3 + chip_offs] + 1) * unit->Base_mult[chip];

       if (new_val!= unit->Div_n_max[CHAN3 + chip_offs])
       {
          unit->Div_n_max[CHAN3 + chip_offs] = new_val;

          if (unit->Div_n_cnt[CHAN3 + chip_offs] > new_val)
          {
             unit->Div_n_cnt[CHAN3 + chip_offs] = new_val;
          }
       }
    }

    if (chan_mask & (1 << CHAN4))
    {
       /* process channel 4 frequency */
       if (unit->AUDCTL[chip] & CH3_CH4)
          if (unit->AUDCTL[chip] & CH3_179)
             new_val = unit->AUDF[CHAN4 + chip_offs] * 256 +
                       unit->AUDF[CHAN3 + chip_offs] + 7;
          else
             new_val = (unit->AUDF[CHAN4 + chip_offs] * 256 +
                        unit->AUDF[CHAN3 + chip_offs] + 1) * unit->Base_mult[chip];
       else
          new_val = (unit->AUDF[CHAN4 + chip_offs] + 1) * unit->Base_mult[chip];

       if (new_val != unit->Div_n_max[CHAN4 + chip_offs])
       {
          unit->Div_n_max[CHAN4 + chip_offs] = new_val;

          if (unit->Div_n_cnt[CHAN4 + chip_offs] > new_val)
          {
             unit->Div_n_cnt[CHAN4 + chip_offs] = new_val;
          }
       }
    }

    /* if channel is volume only, set current output */
    for (chan = CHAN1; chan <= CHAN4; chan++)
    {
       if (chan_mask & (1 << chan))
       {
          /* I've disabled any frequencies that exceed the sampling
             frequency.  There isn't much point in processing frequencies
             that the hardware can't reproduce.  I've also disabled
             processing if the volume is zero. */

          /* if the channel is volume only */
          /* or the channel is off (volume == 0) */
          /* or the channel freq is greater than the playback freq */
          if ((unit->AUDC[chan + chip_offs] & VOL_ONLY) ||
             ((unit->AUDC[chan + chip_offs] & VOLUME_MASK) == 0) ||
              (unit->Div_n_max[chan + chip_offs] < (unit->Samp_n_max >> 8)))
          {
             /* indicate the channel is 'on' */
             unit->Outvol[chan + chip_offs] = 1;
             /* and set channel freq to max to reduce processing */
             unit->Div_n_max[chan + chip_offs] = 0x7fffffffL;
             unit->Div_n_cnt[chan + chip_offs] = 0x7fffffffL;
          }
       }
    }

/*    _enable();*/      /* RSF - removed for portability 31-MAR-97 */
}


/*****************************************************************************/
/* Module:  Pokey_process()                                                  */
/* Purpose: To fill the output buffer with the sound output based on the     */
/*          pokey chip parameters.                                           */
/*                                                                           */
/* Author:  Ron Fries                                                        */
/* Date:    January 1, 1997                                                  */
/*                                                                           */
/* Inputs:  *buffer - pointer to the buffer where the audio output will      */
/*                    be placed                                              */
/*          n - size of the playback buffer                                  */
/*          num_pokeys - number of currently active pokeys to process        */
/*                                                                           */
/* Outputs: the buffer will be filled with n bytes of audio - no return val  */
/*                                                                           */
/*****************************************************************************/

void Pokey_process (Pokey *unit, float *buffer, register uint16 n)
{
    register uint32 *div_n_ptr;
    register uint32 *samp_cnt_w_ptr;
    register uint32 event_min;
    register uint8 next_event;
//#ifdef CLIP                    /* if clipping is selected */	//f0 had to comment out here
    register int16 cur_val;    /* then we have to count as 16-bit signed */
//#else                       
//    register uint8 cur_val;    /* otherwise we'll simplify as 8-bit unsigned */
//#endif
    register uint8 *out_ptr;
    register uint8 audc;
    register uint8 toggle;
    register uint8 count;
    register uint8 *vol_ptr;
	
	RGen& rgen= *unit->mParent->mRGen;		//added this /f0
	
    /* set a pointer to the whole portion of the samp_n_cnt */
#ifdef BIG_ENDIAN
    samp_cnt_w_ptr = (uint32 *)((uint8 *)(&unit->Samp_n_cnt[0])+3);
#else
    samp_cnt_w_ptr = (uint32 *)((uint8 *)(&unit->Samp_n_cnt[0])+1);
#endif

    /* set a pointer for optimization */
    out_ptr = unit->Outvol;
    vol_ptr = unit->AUDV;

    /* The current output is pre-determined and then adjusted based on each */
    /* output change for increased performance (less over-all math). */
    /* add the output values of all 4 channels */
    cur_val = SAMP_MID;

    count = Num_pokeys;
    do
    {
       cur_val -= *vol_ptr/2;
       if (*out_ptr++)
         cur_val += *vol_ptr;
       vol_ptr++;

       cur_val -= *vol_ptr/2;
       if (*out_ptr++)
         cur_val += *vol_ptr;
       vol_ptr++;

       cur_val -= *vol_ptr/2;
       if (*out_ptr++)
         cur_val += *vol_ptr;
       vol_ptr++;

       cur_val -= *vol_ptr/2;
       if (*out_ptr++)
         cur_val += *vol_ptr;
       vol_ptr++;

       count--;
    } while (count);

    /* loop until the buffer is filled */
    while (n)
    {
       /* Normally the routine would simply decrement the 'div by N' */
       /* counters and react when they reach zero.  Since we normally */
       /* won't be processing except once every 80 or so counts, */
       /* I've optimized by finding the smallest count and then */
       /* 'accelerated' time by adjusting all pointers by that amount. */

       /* find next smallest event (either sample or chan 1-4) */
       next_event = SAMPLE;
       event_min = *samp_cnt_w_ptr;

       div_n_ptr = unit->Div_n_cnt;

       count = 0;
       do
       {
          /* Though I could have used a loop here, this is faster */
          if (*div_n_ptr <= event_min)
          {
             event_min = *div_n_ptr;
             next_event = CHAN1 + (count<<2);
          }
          div_n_ptr++;
          if (*div_n_ptr <= event_min)
          {
             event_min = *div_n_ptr;
             next_event = CHAN2 + (count<<2);
          }
          div_n_ptr++;
          if (*div_n_ptr <= event_min)
          {
             event_min = *div_n_ptr;
             next_event = CHAN3 + (count<<2);
          }
          div_n_ptr++;
          if (*div_n_ptr <= event_min)
          {
             event_min = *div_n_ptr;
             next_event = CHAN4 + (count<<2);
          }
          div_n_ptr++;

          count++;
       } while (count < Num_pokeys);


       count = Num_pokeys;
       do
       {
          /* decrement all counters by the smallest count found */
          /* again, no loop for efficiency */
          div_n_ptr--;
          *div_n_ptr -= event_min;
          div_n_ptr--;
          *div_n_ptr -= event_min;
          div_n_ptr--;
          *div_n_ptr -= event_min;
          div_n_ptr--;
          *div_n_ptr -= event_min;

          count--;
       } while (count);

       *samp_cnt_w_ptr -= event_min;

       /* since the polynomials require a mod (%) function which is
          division, I don't adjust the polynomials on the SAMPLE events,
          only the CHAN events.  I have to keep track of the change,
          though. */
       unit->Poly_adjust += event_min;

       /* if the next event is a channel change */
       if (next_event != SAMPLE)
       {
          /* shift the polynomial counters */
          unit->P4  = (unit->P4  + unit->Poly_adjust) % POLY4_SIZE;
          unit->P5  = (unit->P5  + unit->Poly_adjust) % POLY5_SIZE;
          //unit->P9  = (unit->P9  + unit->Poly_adjust) % POLY9_SIZE;
          //unit->P17 = (unit->P17 + unit->Poly_adjust) % POLY17_SIZE;

          /* reset the polynomial adjust counter to zero */
          unit->Poly_adjust = 0;

          /* adjust channel counter */
          unit->Div_n_cnt[next_event] += unit->Div_n_max[next_event];

          /* get the current AUDC into a register (for optimization) */
          audc = unit->AUDC[next_event];

          /* set a pointer to the current output (for opt...) */
          out_ptr = &unit->Outvol[next_event];

          /* assume no changes to the output */
          toggle = FALSE;

          /* From here, a good understanding of the hardware is required */
          /* to understand what is happening.  I won't be able to provide */
          /* much description to explain it here. */

          /* if the output is pure or the output is poly5 and the poly5 bit */
          /* is set */
          if ((audc & NOTPOLY5) || bit5[unit->P5])
          {
             /* if the PURE bit is set */
             if (audc & PURE)
             {
                /* then simply toggle the output */
                toggle = TRUE;
             }
             /* otherwise if POLY4 is selected */
             else if (audc & POLY4)
             {
                /* then compare to the poly4 bit */
                toggle = (bit4[unit->P4] == !(*out_ptr));
             }
             else
             {
                /* if 9-bit poly is selected on this chip */
                if (unit->AUDCTL[next_event >> 2] & POLY9)
                {
                   /* compare to the poly9 bit */
                   //toggle = (bit17[unit->P9] == !(*out_ptr));	//replaced with rand /f0
				   toggle = (rgen.irand(2) == !(*out_ptr));
                }
                else
                {
                   /* otherwise compare to the poly17 bit */
                   //toggle = (bit17[unit->P17] == !(*out_ptr));	//replaced with rand /f0
				   toggle = (rgen.irand(2) == !(*out_ptr));
                }
             }
          }

          /* At this point I haven't emulated the filters.  Though I don't
             expect it to be complicated, I don't believe this feature is
             used much anyway.  I'll work on it later. */
          /* if ((next_event == CHAN1) || (next_event == CHAN3)) */
          /* {                                                   */
          /*    INSERT FILTER HERE                               */
          /* }                                                   */

          /* if the current output bit has changed */
          if (toggle)
          {
             if (*out_ptr)
             {
                /* remove this channel from the signal */
                cur_val -= unit->AUDV[next_event];

                /* and turn the output off */
                *out_ptr = 0;
             }
             else
             {
                /* turn the output on */
                *out_ptr = 1;

                /* and add it to the output signal */
                cur_val += unit->AUDV[next_event];
             }
          }
       }
       else /* otherwise we're processing a sample */
       {
          /* adjust the sample counter - note we're using the 24.8 integer
             which includes an 8 bit fraction for accuracy */
#ifdef BIG_ENDIAN
          *(unit->Samp_n_cnt + 1) += unit->Samp_n_max;
#else
          *unit->Samp_n_cnt += unit->Samp_n_max;
#endif

#ifdef CLIP                               /* if clipping is selected */
          if (cur_val > SAMP_MAX)         /* then check high limit */
          {
             *buffer++ = (uint8)SAMP_MAX; /* and limit if greater */
          }
          else if (cur_val < SAMP_MIN)    /* else check low limit */
          {
             *buffer++ = (uint8)SAMP_MIN; /* and limit if less */
          }
          else                            /* otherwise use raw value */
          {
             //*buffer++ = (uint8)cur_val;
			 *buffer++ = ((float)cur_val)*0.00390625;	//changed here /f0
          }
#else
          //*buffer++ = (uint8)cur_val;   /* clipping not selected, use value */
		  *buffer++ = ((float)cur_val)*0.00390625;     //changed here /f0
#endif

          /* and indicate one less byte in the buffer */
          n--;
       }
    }
}
