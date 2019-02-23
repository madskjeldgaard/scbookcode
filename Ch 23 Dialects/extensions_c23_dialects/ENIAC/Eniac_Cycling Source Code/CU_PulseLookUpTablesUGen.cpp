#include "SC_PlugIn.h"
#include "CU_PulseLookUpTables.h"
//include "sfcn_bridge.h"

static InterfaceTable *ft;
//static ExternalInputs_CU_PulseLookUpTables ExternalInputsCU_PulseLookUpTablesDummy;
//static ExternalInputs_CU_PulseLookUpTables CU_PulseLookUpTables_Inputs;
//static ExternalOutputs_CU_PulseLookUpTables ExternalOutputsCU_PulseLookUpTablesDummy;
//static ExternalOutputs_CU_PulseLookUpTables CU_PulseLookUpTables_Outputs;
struct CU_PulseLookUpTables : public Unit {
  //RT_MODEL_CU_PulseLookUpTables CU_PulseLookUpTables_M; empty? geht das?
  int i;
};

extern "C" {
  void load(InterfaceTable *inTable);
  void CU_PulseLookUpTables_Ctor(CU_PulseLookUpTables *unit);
  void CU_PulseLookUpTables_Dtor(CU_PulseLookUpTables *unit);
  void CU_PulseLookUpTables_process(CU_PulseLookUpTables *unit, int
   inNumSamples);
};

//Ctor setzt die Prozessierfunktion und berechnet 1 (in Worten: ein) Sample
void CU_PulseLookUpTables_Ctor(CU_PulseLookUpTables* unit) {
  //Setzen der Prozessierfunktion
  SETCALC(CU_PulseLookUpTables_process);
  //Initialisieren des Modells
    CU_PulseLookUpTables_initialize(true);

  //Berechnen des ersten Samples
    CU_PulseLookUpTables_process(unit, 1);
    ClearUnitOutputs(unit, 0);
}

//Dtor -- Dekonstruktor:
void CU_PulseLookUpTables_Dtor(CU_PulseLookUpTables* unit) {
  //TODO
}

//Anzahl der Ausgaenge: 10
//Anzahl der Eingaenge: 1

void CU_PulseLookUpTables_process(CU_PulseLookUpTables* unit, int inNumSamples) {
  for (int i = 0; i < inNumSamples; i++) {

      CU_PulseLookUpTables_U.In1 = IN(0)[i];
      // 0-ter Eingang: In1
      CU_PulseLookUpTables_step();
      OUT(0)[i] = CU_PulseLookUpTables_Y.Out1;
      OUT(1)[i] = CU_PulseLookUpTables_Y.Out2;
      OUT(2)[i] = CU_PulseLookUpTables_Y.Out3;
      OUT(3)[i] = CU_PulseLookUpTables_Y.Out4;
      OUT(4)[i] = CU_PulseLookUpTables_Y.Out5;
      OUT(5)[i] = CU_PulseLookUpTables_Y.Out6;
      OUT(6)[i] = CU_PulseLookUpTables_Y.Out7;
      OUT(7)[i] = CU_PulseLookUpTables_Y.Out8;
      OUT(8)[i] = CU_PulseLookUpTables_Y.Out9;
      OUT(9)[i] = CU_PulseLookUpTables_Y.Out10;
  }
}

void load(InterfaceTable *inTable) {
  ft = inTable;
  DefineSimpleUnit(CU_PulseLookUpTables);
//  DefineDtorCantAliasUnit(CU_PulseLookUpTables);
}



