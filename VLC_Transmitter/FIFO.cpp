/* Generic Class for a circular FIFO*/

#include "FIFO.h"

FIFO::FIFO() {
  head = 0;
  tail = 0;
  numElements = 0;
  reader=0;
}

FIFO::~FIFO() {
}

void FIFO::push(int data) {

  
  if(numElements == FIFO_SIZE) {
    return;
  }
  else {
    //Increment size
    numElements++;

    //Only move the tail if there is more than one element
    if(numElements > 1) {
      //Increment tail location
      tail++;

      //Make sure tail is within the bounds of the array
      tail %= FIFO_SIZE;
    }
  
    //Store data into array
    buff[tail] = data;
  }
}

int FIFO::pop() {
  if(numElements == 0) {
    return -1;
  }
  else {
    //Decrement size
    numElements--;

    int data = buff[head];

    if(numElements >= 1) {
      //Move head up one position
      head++;

      //Make sure head is within the bounds of the array
      head %= FIFO_SIZE;
    }

    return data;
  }
}

int FIFO::sizeOfBuffer() {
  return numElements;
}

int FIFO::readBuffer(){
  int data=buff[reader];

  reader++;
  reader%=FIFO_SIZE;
  //Serial.println(reader);
  return data;

  }

