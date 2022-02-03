/*This is the final code for the capstone project of Randy Lance O. Zebroff, Eugene John, 
/Joseph Matthew Espinas, and Ramon Carmelo Y. Calimbahin of 12-P S.Y. 2021-2022 @ La Salle Greenhills*/

/*Copyright [2022] [Randy Lance O. Zebroff, Ramon Carmelo Y. Calimbahin, Eugene John, Joseph Matthew Espinas]

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.*/

#include <PulseSensorPlayground.h>
#include <SPI.h>
#include <SD.h>

SoftwareSerial habsSS(3,4); //RX, TX
PulseSensorPlayground pulseModule; //Creates an object in PulseSensorPlayground called "pulseModule".
File bpmFile;

//Array initialization
char str[8];

void setup() {
  pinMode(buzzer, OUTPUT); //Buzzer power line.

  Serial.begin(9600); //Set bitrate for comms between PC and Arduino board.
  habsSS.begin(9600); //Set bitrate for comms between AT-09 and Arduino board.

  //pulseModule object configuration
  pulseModule.analogInput(pulseWire);
  pulseModule.setThreshold(threshold);
  //pulseModule object and module initialization
  if(pulseModule.begin()>0){
    Serial.println("Object pulseModule created.");
  }
  else{
    Serial.println("pulseModule object failed to initialize.");
    while(1); //Do nothing until something happens.
  }

  //SD module initialization
  //This simply checks if the board is connected to the SD card.
  while (!Serial);
  Serial.print("Initializing SD card...");
  if(!SD.begin(sdToCS)){ //Pin 5 set as SD --> CS port.
    Serial.println("Initialization failed!");
    while(1); //Do nothing until something happens.
  }
  Serial.println("S.D. Intialization completed!");
  //This checks if the file "bpm.txt" exists
  if(SD.exists("bpm.txt")){
    Serial.println("File exists.");
  }
  else{
    Serial.println("File does not exist.");
    //This opens/creates the file.
    Serial.println("Creating file...");
    bpmFile = SD.open("bpm.txt", FILE_WRITE);
    bpmFile.close();
  }
}

void sendText(const char * text) {
  Serial.println(text);
  habsSS.write(text);
}

void loop() {
  int myBPM = pulseModule.getBeatsPerMinute(); //Calls function on our pulseModule object that returns BPM as an "int".
                                               //"myBPM" holds the BPM value now. 
  if (pulseModule.sawStartOfBeat()) {          //Tests to see if the signal peaks 525. If the signal goes past 525, then a "beat happened."
    Serial.println("BPM: "+ myBPM);            //Print phrase "BPM: " with BPM included.
    //Add a section here that checks if the bluetooth module is connected to a device. NO NEED FOR THIS
    itoa(myBPM, str, 10);                    //Converts myBPM to a string array.
    sendText(str);                           //Forces the AT-09 to subscribe and send data when this array is updated.
    delay(200); //For stability, data saturation prevention, and longevity of the power supply.
    //change the below code to work with the BLE module and BLE functions.
    if(myBPM<lowBPM||myBPM>highBPM){   //If a person's heartrate goes above highBPM or below lowBPM,
      delay(10000);                    // wait 10 seconds
      pulseModule.getBeatsPerMinute(); //Update myBPM variable
      itoa(myBPM, str, 10);            //Converts myBPM to a string array.
      sendText(str);                   //Forces the AT-09 to subscribe and send data when this array is updated.
      delay(50); //For stability, data saturation prevention, and longevity of the power supply.      
      if(myBPM<lowBPM||myBPM>highBPM){ //if the same is still true, then ring the buzzer and write to microSD card.
        bpmFile = SD.open("bpm.txt", FILE_WRITE); //opens bpm.txt on the microSD card.
        delay(100); //for stability
        bpmFile.println("BPM: "+ myBPM); //prints bpm to file
        delay(100); //for stability
        bpmFile.close(); //closes bpm.txt
        delay(50); //for stability
        digitalWrite(buzzer, HIGH);
        delay(180000); //3 minute delay for the buzzer to run.
        digitalWrite(buzzer, LOW);
      }
    }
  }
}
