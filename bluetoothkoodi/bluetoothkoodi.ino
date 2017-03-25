#include <Servo.h>

String blueToothVal;
int lastvalue;
int nollakohdat[] = {1500, 1500};
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
    funktio(blueToothVal, nollakohdat);
  }
  delay(500);
}

void funktio(String blueVal,int nollakohdat[]){
  int servo = int(blueVal.charAt(0)-'0');
  blueVal.remove(0,1);
  int nopeus = nollakohdat[servo] + blueVal.toInt();
  Serial.println((blueVal));
  Serial.println((nopeus));
  
  //Servo member = a;   (  this doesnt work   :(  )
  //member.writeMicroseconds (nopeus);
  
  if(servo == 1){
    myservo.writeMicroseconds (nopeus);
    Serial.println((blueVal));
    }
  if(servo == 2){
    myservo2.writeMicroseconds (nopeus);
  }
  
}
