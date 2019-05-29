#ifndef VLCtransmitter_h
#define VLCtransmitter_h

#include "Arduino.h"
#include "FIFO.h"

class VLCtransmitter
{
  
  public:
  static const long long int standardDelayInMicroseconds = 10000;
  static const char STARTBIT = '1';
  static const char ENDBIT = '0';
  FIFO commBuff;
  
  VLCtransmitter(int signalPin);
  void sendStringToReceiver(String message);
  void sendCharacterToReceiver(String character);
  String convertCharacterToByteString(char character);
  void sendByteString(String byteString);
  void sendBit(char bitToSend);
  
  private:
  int transistorSignalPin;
  
};


#endif
