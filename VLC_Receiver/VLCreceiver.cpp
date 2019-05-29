#include "VLCreceiver.h"

/*Contructor for VLC Receiver*/
VLCreceiver::VLCreceiver(int pinVal)
{
   this->preamble="123456";
   this->message="";
   this->preambleReceived=false;
   this->numSamples=0;
   this->bitsReceived=0;
}

/*Checks for correct preamble*/
bool VLCreceiver::checkPreamble(String pream){
  
  return this->preamble==pream;
  
  }




