/* Copyright 1994-2002 The MathWorks, Inc.
 *
 * File: rt_look1d.c    $Revision: 1.7 $	$Date: 2002/04/14 10:14:43 $
 *
 * Abstract:
 *      Real-Time Workshop index finding routine used in the code
 *      generated from SL models involving 1D Lookup Table
 *      blocks.
 */

#include "rtlibsrc.h"

real_T rt_Lookup(const real_T *x, int_T xlen, real_T u,
                 const real_T *y)
{
  int_T idx = rt_GetLookupIndex(x, xlen, u);
  real_T num = y[idx+1] - y[idx];
  real_T den = x[idx+1] - x[idx];
  
  /* Due to the way the binary search is implemented
     in rt_look.c (rt_GetLookupIndex), den cannot be
     0.  Equivalently, m cannot be inf or nan. */
  
  real_T m = num/den;

  return (y[idx] + m * (u - x[idx]));
}

