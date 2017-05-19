#include <Servo.h>

String input;
int servo, speed;
// Servo stop values
const int speedOffset[] = {1510, 1500};


const int pins[] = {9,8};
const char DELIMITER_CHAR = '#';



const int servo_count = sizeof(pins)/sizeof(pins[0]);
Servo servos[servo_count];


void setup() {
  attach_pins();

  Serial.begin(9600);
  Serial.setTimeout(100);
}

void attach_pins() {
  // Executes servo.attach(pin_number)
  for(int i = 0; i < servo_count; i++) {
    servos[i].attach(pins[i]);
  }
}


void loop() {
  while(Serial.available()){
    // Timeout delay is invoked after Serial stream is cleared
    input = Serial.readStringUntil(DELIMITER_CHAR); 
    Serial.print("\nReceived: ");
    Serial.print(input);
    
    // Parse input string
    servo = parseServoId(input);
    speed = parseSpeed(input) + speedOffset[servo-1];
    
    setSpeed(servo, speed);
  }
  delay(100);
}

void setSpeed(int servo, int speed) {
  if(servo > 0 && servo <= servo_count) {
    servos[servo-1].writeMicroseconds(speed);
    Serial.print("\nServo "); 
    Serial.print(servo);
    Serial.print(" set to speed ");
    Serial.print(speed);
  } else {
    Serial.print("\nInvalid servo id: ");
    Serial.print(servo);
  }
}

int parseSpeed(String &str) {
  // Skips first index of str and returns rest of string as int
  return str.substring(1).toInt();
}

int parseServoId(String &str) {
  // Parse first char of string, deduce offset of '0' to produce integer from charset
  return int(str.charAt(0)-'0');
}
