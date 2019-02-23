/* IZ 2007-02-05 { SC3

The present feature has not been tested yet. 

Binding Parameters refers to the linking of the values between Parameters of different Scripts in the Client. This means, whenever the value of a Parameter x of one Script w (the "writer") changes by the method Parameter:set or Script:set, the Parameter y of another Script r (the "reader") will receive and be set to the new value of x. This is done by code only at the moment: 

aScript.bindParameter(this_scripts_parameter_name, other_script, other_scripts_parameter_name);
aScript.unbindParameter(this_scripts_parameter_name, other_script, other_scripts_parameter_name);

Note: Cyclic settings (so that both Scripts w and r are at the same time "reader" and "writer" and their bound parameters always have the same value, independently of which of the two Scripts is changing them) are not permitted at present, as they will result in an infinite loop. To prevent this, one could check whether the values of the parameters are already equal before setting, but this has not been implemented yet. 

} */
