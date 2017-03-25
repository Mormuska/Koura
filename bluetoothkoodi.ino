#include <Servo.h>

String blueToothVal;
int lastvalue;
Servo myservo;
Servo myservo2;


void setup() {
  // put your setup code here, to run once:
  myservo.attach(9);
  myservo2.attach(8);
  Serial.begin(9600);
}

void loop() {
  // put your main code here, to run repeatedly:
  if(Serial.available()){
    blueToothVal = Serial.readString();
    //Serial.println(Serial.read());
    Serial.println((blueToothVal));
  }
  if(blueToothVal=="1"){
    myservo.writeMicroseconds (1500);
  }
  if(blueToothVal=="0"){
    myservo.writeMicroseconds (2000);
  }
  if(blueToothVal=="111111"){
  myservo.writeMicroseconds (1000);
  }
  if(blueToothVal=="a"){
    myservo2.writeMicroseconds (100);
  }
  if(blueToothVal=="b"){
    myservo2.writeMicroseconds (3000);
  }
  if(blueToothVal=="ab"){
  myservo2.writeMicroseconds (1500);
  }
  delay(500);
}
