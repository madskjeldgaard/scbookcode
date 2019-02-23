/*
 * File: CU_PulseLookUpTables.cpp
 *
 * Real-Time Workshop code generated for Simulink model CU_PulseLookUpTables.
 *
 * Model version                        : 1.112
 * Real-Time Workshop file version      : 6.4.1  (R2006a+)  29-Mar-2006
 * Real-Time Workshop file generated on : Fri Jun  9 17:37:55 2006
 * TLC version                          : 6.4 (Jan 31 2006)
 * C source code generated on           : Fri Jun  9 17:37:58 2006
 */

#include "CU_PulseLookUpTables.h"
#include "CU_PulseLookUpTables_private.h"

/* External inputs (root inport signals with auto storage) */
ExternalInputs_CU_PulseLookUpTa CU_PulseLookUpTables_U;

/* External outputs (root outports fed by signals with auto storage) */
ExternalOutputs_CU_PulseLookUpT CU_PulseLookUpTables_Y;

/* Real-time model */
RT_MODEL_CU_PulseLookUpTables CU_PulseLookUpTables_M_;
RT_MODEL_CU_PulseLookUpTables *CU_PulseLookUpTables_M = &CU_PulseLookUpTables_M_;

/* Model step function */

void CU_PulseLookUpTables_step(void)
{

  /* Outport: '<Root>/Out1' incorporates:
   *  Inport: '<Root>/In1'
   *  Lookup: '<Root>/Lookup Table'
   */
  CU_PulseLookUpTables_Y.Out1 = rt_Lookup(CU_PulseLookUpTables_ConstP.pooled1,
   139, CU_PulseLookUpTables_U.In1,
   CU_PulseLookUpTables_ConstP.LookupTable_YData);

  /* Outport: '<Root>/Out2' incorporates:
   *  Inport: '<Root>/In1'
   *  Lookup: '<Root>/Lookup Table1'
   */
  CU_PulseLookUpTables_Y.Out2 = rt_Lookup(CU_PulseLookUpTables_ConstP.pooled1,
   139, CU_PulseLookUpTables_U.In1,
   CU_PulseLookUpTables_ConstP.LookupTable1_YData);

  /* Outport: '<Root>/Out3' incorporates:
   *  Inport: '<Root>/In1'
   *  Lookup: '<Root>/Lookup Table2'
   */
  CU_PulseLookUpTables_Y.Out3 = rt_Lookup(CU_PulseLookUpTables_ConstP.pooled1,
   139, CU_PulseLookUpTables_U.In1,
   CU_PulseLookUpTables_ConstP.LookupTable2_YData);

  /* Outport: '<Root>/Out4' incorporates:
   *  Inport: '<Root>/In1'
   *  Lookup: '<Root>/Lookup Table3'
   */
  CU_PulseLookUpTables_Y.Out4 = rt_Lookup(CU_PulseLookUpTables_ConstP.pooled1,
   139, CU_PulseLookUpTables_U.In1,
   CU_PulseLookUpTables_ConstP.LookupTable3_YData);

  /* Outport: '<Root>/Out5' incorporates:
   *  Inport: '<Root>/In1'
   *  Lookup: '<Root>/Lookup Table4'
   */
  CU_PulseLookUpTables_Y.Out5 = rt_Lookup(CU_PulseLookUpTables_ConstP.pooled1,
   139, CU_PulseLookUpTables_U.In1,
   CU_PulseLookUpTables_ConstP.LookupTable4_YData);

  /* Outport: '<Root>/Out6' incorporates:
   *  Inport: '<Root>/In1'
   *  Lookup: '<Root>/Lookup Table5'
   */
  CU_PulseLookUpTables_Y.Out6 = rt_Lookup(CU_PulseLookUpTables_ConstP.pooled1,
   139, CU_PulseLookUpTables_U.In1,
   CU_PulseLookUpTables_ConstP.LookupTable5_YData);

  /* Outport: '<Root>/Out7' incorporates:
   *  Inport: '<Root>/In1'
   *  Lookup: '<Root>/Lookup Table6'
   */
  CU_PulseLookUpTables_Y.Out7 = rt_Lookup(CU_PulseLookUpTables_ConstP.pooled1,
   139, CU_PulseLookUpTables_U.In1,
   CU_PulseLookUpTables_ConstP.LookupTable6_YData);

  /* Outport: '<Root>/Out8' incorporates:
   *  Inport: '<Root>/In1'
   *  Lookup: '<Root>/Lookup Table7'
   */
  CU_PulseLookUpTables_Y.Out8 = rt_Lookup(CU_PulseLookUpTables_ConstP.pooled1,
   139, CU_PulseLookUpTables_U.In1,
   CU_PulseLookUpTables_ConstP.LookupTable7_YData);

  /* Outport: '<Root>/Out9' incorporates:
   *  Inport: '<Root>/In1'
   *  Lookup: '<Root>/Lookup Table8'
   */
  CU_PulseLookUpTables_Y.Out9 = rt_Lookup(CU_PulseLookUpTables_ConstP.pooled1,
   139, CU_PulseLookUpTables_U.In1,
   CU_PulseLookUpTables_ConstP.LookupTable8_YData);

  /* Outport: '<Root>/Out10' incorporates:
   *  Inport: '<Root>/In1'
   *  Lookup: '<Root>/Lookup Table9'
   */
  CU_PulseLookUpTables_Y.Out10 = rt_Lookup(CU_PulseLookUpTables_ConstP.pooled1,
   139, CU_PulseLookUpTables_U.In1,
   CU_PulseLookUpTables_ConstP.LookupTable9_YData);
}

/* Model initialize function */

void CU_PulseLookUpTables_initialize(boolean_T firstTime)
{
  if (firstTime) {

    /* Registration code */

    /* initialize error status */
    rtmSetErrorStatus(CU_PulseLookUpTables_M, (const char_T *)0);

    /* external inputs */

    CU_PulseLookUpTables_U.In1 = 0.0;

    /* external outputs */
    CU_PulseLookUpTables_Y.Out1 = 0.0;
    CU_PulseLookUpTables_Y.Out2 = 0.0;
    CU_PulseLookUpTables_Y.Out3 = 0.0;
    CU_PulseLookUpTables_Y.Out4 = 0.0;
    CU_PulseLookUpTables_Y.Out5 = 0.0;
    CU_PulseLookUpTables_Y.Out6 = 0.0;
    CU_PulseLookUpTables_Y.Out7 = 0.0;
    CU_PulseLookUpTables_Y.Out8 = 0.0;
    CU_PulseLookUpTables_Y.Out9 = 0.0;
    CU_PulseLookUpTables_Y.Out10 = 0.0;
  }
}

/* Model terminate function */

void CU_PulseLookUpTables_terminate(void)
{
  /* (no terminate code required) */
}

/* File trailer for Real-Time Workshop generated code.
 *
 * [EOF]
 */


