#include "VLCtransmitter.h"
#include "FIFO.h"
#include <TimerOne.h>

#define MESSAGE_LENGTH 20   //2 times actual message length (Storing half bits)

int transmitterPin = 2;     //define GPIO pin for transmission
VLCtransmitter transmitter = VLCtransmitter(transmitterPin);  //Declare and Initialize transmitter object
IntervalTimer myTimer;    //create interval timer
#define SYMBOL_PERIOD 800 /* Defined a symbol period in us*/


//Initialize transmitter variables
int first_half=0;
int preambleCount=0;          //keeps track of preamble
int messageCount=0;           
unsigned long time1=0;
String tempFinalMessage= "";
String randomString ="";
int randomNumber=0;


void setup() 
{  
 // Serial.begin(115200);
  //randomSeed(9600); // for starters
  transmitter.commBuff=FIFO();      //create transmit FIFO
   randomSeed(analogRead(0));
   
   //edit 256 bit message here
     for(int x =0 ;x<127; x++){
    randomNumber = random(0,2);
    randomString.concat(randomNumber);
   }

   //check to see if preamble is in message
    if(randomString.indexOf("10101010") >= 0)
      {
        //Serial.println("THROWING OUT");
           randomString =""; 
           for(int x =0 ;x<127; x++)
           {
              randomNumber = random(0,2);
              randomString.concat(randomNumber);
           }
      }
     randomString = "10010000101010001011001110111100010100010101011100010011000101001000100000001011101000101111100101110111011000011011000101011110";
    //add the extra one's and zeroes
    //Serial.println(randomString); 
   String final_message="1010101010"; //append 8-bit frame message preamble

 /*********This code constructs preambles for each frame, making them 10 bits***********/
   int indexOrig=0;
    for(int i=0; i<16; i++){
      final_message+=1;
      int indexfin=indexOrig+8;
      while(indexfin>indexOrig){
        final_message+=randomString[indexOrig];
        indexOrig++;
        }
       final_message+=0;
       
    }
  
    //last piece of first preamble
    //String message_pream="1010101010";
    //final_message=message_pream.concat(final_message);
    //String bit_final32=final_message.substring(0,100);
    //String bit_final64=final_message.substring(0,180);
   transmitter.sendStringToReceiver(final_message);
   //transmitter.sendStringToReceiver(bit_final32);
   //transmitter.sendStringToReceiver(ma);
 //transmitter.sendStringToReceiver("1010010010");
   //new ends here
   pinMode(LED_BUILTIN, OUTPUT);
   Serial.begin(115200);
   pinMode(transmitterPin,OUTPUT);
   //delay(500);
   cli();//stop interrupts
   myTimer.begin(emit_half_bit,SYMBOL_PERIOD);
// Timer1.initialize(SYMBOL_PERIOD); //1200 bauds
  // Timer1.attachInterrupt(emit_half_bit); 
 
   sei();//allow interrupts
   tempFinalMessage = final_message;
   //tempFinalMessage = "XXXXXX";
   
   
}

void loop() 
{
//Serial.println(tempFinalMessage);
//delay(1000);
}

/**********emit_half_bit is the ISR for the transmitter*********/
void emit_half_bit(){
   
  digitalWriteFast(LED_BUILTIN, !digitalReadFast(LED_BUILTIN));   //heartbeat

  //preamble send
  if(preambleCount<6){
      //Inverted Logic High
     digitalWriteFast(transmitterPin,LOW);
      preambleCount++;
    
  }
  //message send
  else{
     messageCount++;
     if(transmitter.commBuff.sizeOfBuffer()!=0){
   
        //send the half bits based on light signal
        int halfbit=transmitter.commBuff.readBuffer();
   
        if(halfbit==1 ){      
            //Inverted Logic High
             digitalWriteFast(transmitterPin,LOW);
        }
        else{
          //Inverted Logic Low
          digitalWriteFast(transmitterPin,HIGH);
        }
        
     }
     else{
        //consistently send high
        digitalWrite(transmitterPin,HIGH);
      }

      //reset the transmitter
      if(messageCount==MESSAGE_LENGTH){
        preambleCount=0;
        messageCount=0;
        }
  }
}
