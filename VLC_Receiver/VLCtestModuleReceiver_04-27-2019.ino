//CURRENT VERSION: 04/27/2019
#include "VLCreceiver.h"
#include <TimerOne.h>
#define SYMBOL_PERIOD 400   //Specify Symbol Period
#define MESSAGE_LENGTH 10   //Specify Message Length  
String data[180];           //Buffer used for error correcting
int THRESHOLD = 93;         //Defined Receiver Threshold
String preambleCorrect="111111";  //Preamble Tracker

//Initialize the Receiver
VLCreceiver receiver = VLCreceiver(A14);
int globalCount=1;
bool onMessageWhole=false;
int msgCount=0;
int rightCount=0;
int totCount=0;

//Initialize Message Buffer as String
String finalMessage=""; 

//Initialize Interval Timer
IntervalTimer myTimer;

int tpin = A14;     //Analog input value on MCU

//Used for Error Correcting
int count=0;
String mess="";
String prevMessage="";
int stopcount=0;


void setup() 
{

 /*///////////MESSAGE INITIALIZATION Check///////////////////////*
  * 
   String ma="10011001";
   String me="10111101";
 
   //message 3
   //mess="11000110000001010001011000001101111100110100010101011101101101001011110011000000101000001011110010111001111010001111101001010110";
  //mess="1100011000000101000101100000110111110011010001010101110110110100";
   //message 2
    //mess="10001000010100001000111001010110100101101100010010100000110010101010011011010000101011001101101010110010111000100110011011101001";
   // mess="1000100001010000100011100101011010010110110001001010000011001010";
   for(int i=0; i<16; i++){
    mess+=ma;
    mess+=me;
    }
   */
   pinMode(tpin,INPUT);   //setup pin
   Serial.begin(115200);  //Initialize serial monitor
   pinMode(LED_BUILTIN, OUTPUT);  //setup heartbeat
     
   cli();//stop interrupts
   myTimer.begin(receive_half_bit,SYMBOL_PERIOD); 
   sei();//allow interrupts
}

void loop() 
{
 //Wait For Interrupt
}

//Receiver Interrupt
void receive_half_bit(){

    
    int sample=analogRead(tpin);    //Read in Photodiode Value
    digitalWriteFast(LED_BUILTIN, !digitalReadFast(LED_BUILTIN)); //Toggle Heartbeat

    ///Search for Preamble
  if(receiver.preambleReceived==false){
   
    if(sample>THRESHOLD){
        receiver.preamble=receiver.preamble.substring(1)+=("1");   
    }
    else{
      receiver.preamble=receiver.preamble.substring(1)+=("0");
     }
     
     if(receiver.checkPreamble(preambleCorrect)){
        receiver.preambleReceived=true;
      }

      return;
  }
    //Preamble Found, begin receiving and reconstructing packets
   else if(receiver.preambleReceived==true)
   {

         //Manchester Decode
        if(receiver.numSamples<1){
              receiver.prevSample=sample;
              receiver.numSamples=1;
          }
        else{
              int diff=sample-receiver.prevSample;
              receiver.bitsReceived+=1;
              if(diff>0){
                receiver.message=receiver.message+=("1");
                
                }
              else{
                 receiver.message=receiver.message+=("0");
                }
               
                receiver.numSamples=0;

                
                if(receiver.bitsReceived==MESSAGE_LENGTH){

                  //detect message preamble
                  if(onMessageWhole==false && receiver.message=="1010101010"){   
                    onMessageWhole=true;
                    msgCount=0;
                    }
                  else if(onMessageWhole==true && msgCount<32){
                    finalMessage+= receiver.message.substring(1,9);
                    msgCount++;
                    }

                  if(msgCount==32){ 
                    //Serially transmit final message to device
                      Serial.println(finalMessage);
                     
                    //reset variables
                    finalMessage="";
                    msgCount=0;
                    onMessageWhole=false;
                     }

                     /************** Evaluation code****************************
                    if(finalMessage==mess){
                     
                      rightCount++;
                    }
                    totCount++;
                    if(totCount ==200){
                      Serial.println((float)rightCount/totCount);
                      rightCount=0;
                      totCount=0;}
                    finalMessage="";
                    msgCount=0;
                    onMessageWhole=false;
                    }

                    */
                  //reset variables
                  receiver.bitsReceived=0;
                  receiver.message="";
                  receiver.preambleReceived=false;
                  receiver.preamble="123456";
                }
          
          }
        
        
    
    }
  
  
 }


