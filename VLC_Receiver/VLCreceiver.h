#ifndef VLCreceiver_h
#define VLCreceiver_h

#include "Arduino.h"

class VLCreceiver
{

  public:
  VLCreceiver(int pinVal);
  String preamble;                      //stores preamble
  bool preambleReceived;                //stores if preamble is receiver
  String message;                       //stores message
  bool checkPreamble(String pream);     //checks for correct preamble
  int numSamples;                       //number of samples
  int prevSample;                       //previous sample
  int bitsReceived;                     //number of bits receive
  

  
};


#endif



