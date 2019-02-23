/*
 * File: CU_PulseLookUpTables.h
 *
 * Real-Time Workshop code generated for Simulink model CU_PulseLookUpTables.
 *
 * Model version                        : 1.112
 * Real-Time Workshop file version      : 6.4.1  (R2006a+)  29-Mar-2006
 * Real-Time Workshop file generated on : Fri Jun  9 17:37:55 2006
 * TLC version                          : 6.4 (Jan 31 2006)
 * C source code generated on           : Fri Jun  9 17:37:58 2006
 */

#ifndef _RTW_HEADER_CU_PulseLookUpTables_h_
#define _RTW_HEADER_CU_PulseLookUpTables_h_

#ifndef _CU_PulseLookUpTables_COMMON_INCLUDES_
# define _CU_PulseLookUpTables_COMMON_INCLUDES_
#include <math.h>
#include <stddef.h>
#include "rtwtypes.h"
#include "rtw_continuous.h"
#include "rtw_solver.h"
#include "rtlibsrc.h"
#endif                                  /* _CU_PulseLookUpTables_COMMON_INCLUDES_ */

#include "CU_PulseLookUpTables_types.h"

/* Macros for accessing real-time model data structure  */

#ifndef rtmGetErrorStatus
# define rtmGetErrorStatus(rtm) ((rtm)->errorStatus)
#endif

#ifndef rtmSetErrorStatus
# define rtmSetErrorStatus(rtm, val) ((rtm)->errorStatus = (val))
#endif

/* Constant parameters (auto storage) */
typedef struct {
  /* Computed Parameter: InputValues
   * Referenced by blocks:
   * '<Root>/Lookup Table'
   * '<Root>/Lookup Table1'
   * '<Root>/Lookup Table2'
   * '<Root>/Lookup Table3'
   * '<Root>/Lookup Table4'
   * '<Root>/Lookup Table5'
   * '<Root>/Lookup Table6'
   * '<Root>/Lookup Table7'
   * '<Root>/Lookup Table8'
   * '<Root>/Lookup Table9'
   */
  real_T pooled1[139];
  /* Computed Parameter: OutputValues
   * '<Root>/Lookup Table'
   */
  real_T LookupTable_YData[139];
  /* Computed Parameter: OutputValues
   * '<Root>/Lookup Table1'
   */
  real_T LookupTable1_YData[139];
  /* Computed Parameter: OutputValues
   * '<Root>/Lookup Table2'
   */
  real_T LookupTable2_YData[139];
  /* Computed Parameter: OutputValues
   * '<Root>/Lookup Table3'
   */
  real_T LookupTable3_YData[139];
  /* Computed Parameter: OutputValues
   * '<Root>/Lookup Table4'
   */
  real_T LookupTable4_YData[139];
  /* Computed Parameter: OutputValues
   * '<Root>/Lookup Table5'
   */
  real_T LookupTable5_YData[139];
  /* Computed Parameter: OutputValues
   * '<Root>/Lookup Table6'
   */
  real_T LookupTable6_YData[139];
  /* Computed Parameter: OutputValues
   * '<Root>/Lookup Table7'
   */
  real_T LookupTable7_YData[139];
  /* Computed Parameter: OutputValues
   * '<Root>/Lookup Table8'
   */
  real_T LookupTable8_YData[139];
  /* Computed Parameter: OutputValues
   * '<Root>/Lookup Table9'
   */
  real_T LookupTable9_YData[139];
} ConstParam_CU_PulseLookUpTables;

/* External inputs (root inport signals with auto storage) */
typedef struct {
  real_T In1;                           /* '<Root>/In1' */
} ExternalInputs_CU_PulseLookUpTa;

/* External outputs (root outports fed by signals with auto storage) */
typedef struct {
  real_T Out1;                          /* '<Root>/Out1' */
  real_T Out2;                          /* '<Root>/Out2' */
  real_T Out3;                          /* '<Root>/Out3' */
  real_T Out4;                          /* '<Root>/Out4' */
  real_T Out5;                          /* '<Root>/Out5' */
  real_T Out6;                          /* '<Root>/Out6' */
  real_T Out7;                          /* '<Root>/Out7' */
  real_T Out8;                          /* '<Root>/Out8' */
  real_T Out9;                          /* '<Root>/Out9' */
  real_T Out10;                         /* '<Root>/Out10' */
} ExternalOutputs_CU_PulseLookUpT;

/* Real-time Model Data Structure */
struct RT_MODEL_CU_PulseLookUpTables {
  const char_T *errorStatus;
};

/* External inputs (root inport signals with auto storage) */
extern ExternalInputs_CU_PulseLookUpTa CU_PulseLookUpTables_U;

/* External outputs (root outports fed by signals with auto storage) */
extern ExternalOutputs_CU_PulseLookUpT CU_PulseLookUpTables_Y;

/* Constant parameters (auto storage) */
extern const ConstParam_CU_PulseLookUpTables CU_PulseLookUpTables_ConstP;

/* Model entry point functions */
extern void CU_PulseLookUpTables_initialize(boolean_T firstTime);
extern void CU_PulseLookUpTables_step(void);
extern void CU_PulseLookUpTables_terminate(void);

/* Real-time Model object */
extern RT_MODEL_CU_PulseLookUpTables *CU_PulseLookUpTables_M;

/* 
 * The generated code includes comments that allow you to trace directly 
 * back to the appropriate location in the model.  The basic format
 * is <system>/block_name, where system is the system number (uniquely
 * assigned by Simulink) and block_name is the name of the block.
 *
 * Use the MATLAB hilite_system command to trace the generated code back
 * to the model.  For example,
 *
 * hilite_system('<S3>')    - opens system 3
 * hilite_system('<S3>/Kp') - opens and selects block Kp which resides in S3
 *
 * Here is the system hierarchy for this model
 *
 * '<Root>' : CU_PulseLookUpTables
 */

#endif                                  /* _RTW_HEADER_CU_PulseLookUpTables_h_ */

/* File trailer for Real-Time Workshop generated code.
 *
 * [EOF]
 */


