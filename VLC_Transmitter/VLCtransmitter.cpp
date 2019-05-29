#include "VLCtransmitter.h"
#include "FIFO.h"

#define NOP __asm__ __volatile__ ("nop\n\t")


/***VLC Transmitter Initialization Code ******/
VLCtransmitter::VLCtransmitter(int signalPin)
{
  this->transistorSignalPin = signalPin;
  pinMode(signalPin, OUTPUT);
  digitalWrite(signalPin, LOW);
}


void VLCtransmitter::sendStringToReceiver(String message)
{ 

    sendCharacterToReceiver(message);   

}

void VLCtransmitter::sendCharacterToReceiver(String code)
{
  sendByteString(code); 
}


void VLCtransmitter::sendByteString(String byteString)
{
    //Split String into bits
  for(int bitIndex = 0; bitIndex < byteString.length(); bitIndex++)
  {
    char bitToSend = byteString[bitIndex];
    sendBit(bitToSend);
  }
 
}

void VLCtransmitter::sendBit(char bitToSend)
{
 
  //Manchester Encodes before adding to the buffer
  if(bitToSend == '1') 
  {
   
    commBuff.push(0);
    commBuff.push(1);
   
  }
  else
  {
    commBuff.push(1);
    commBuff.push(0);
    

  }
 

}

