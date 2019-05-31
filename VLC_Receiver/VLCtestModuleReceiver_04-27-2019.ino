//CURRENT VERSION: 04/27/2019
#include "VLCreceiver.h"
#include <TimerOne.h>
#define SYMBOL_PERIOD 400   //Specify Symbol Period
#define MESSAGE_LENGTH 10   //Specify Message Length  
String data[180];           //Buffer used for error correcting
int THRESHOLD = 90;         //Defined Receiver Threshold
String preambleCorrect="111111";  //Preamble Tracker

//Initialize the Receiver
VLCreceiver receiver = VLCreceiver(A14);
int globalCount=1;
bool onMessageWhole=false;
int msgCount=0;
int rightCount=0;
int totCount=0;
char sub[8];

//Initialize the array checker
String checker[3];
int num=0;

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
  //   Serial.println(sample); // threshold value
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
                  else if(onMessageWhole==true && msgCount<16){
                    finalMessage+= receiver.message.substring(1,9);
                    msgCount++;
                    }

                  if(msgCount==16){ 
                    //Serially transmit final message to device
                   
                     
                      num++;
                      checker[num-1]=finalMessage;
                      if(num==3){
                         num =0;
                        
                          if(checker[0]==checker[1]&&checker[0]==checker[2])
                          {
                            charArray();
                          }
                          
                        
                      }
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
 void charArray()
 {

  char finalMessageArray [128]; // extra 1 one null character
  finalMessage.toCharArray(finalMessageArray, 129);
  

  for(int k=0;k<128;k+=8)
  {
    extractElements(finalMessageArray,  k, k+8); // 0-8, 8,-16,16-24,24-32,32-40,40-48,48-56,
    
  
    Serial.write(sub,8);
    Serial.send_now();
 //   Serial.println(sub); 
    delay(1000);
  }
 }

void extractElements(char src[], int n, int m)
{
  
  for(int j=0;j<8;j++)
  {
    sub[j]=src[j+n];
  }
  
}
