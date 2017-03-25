#include <Servo.h>

String blueToothVal;
int lastvalue;
int nollakohdat[] = {1500, 1500};
Servo myservo;
Servo myservo2;

void setup() {
  myservo.attach(9);
  myservo2.attach(8);
  Serial.begin(9600);
}

void loop() {
  if(Serial.available()){
    blueToothVal = Serial.readString();
    funktio(blueToothVal, nollakohdat);
  }
  delay(500);
}

void funktio(String blueVal,int nollakohdat[]){
  //Ottaakäytäjän syötteen ja servoille kalibroidut nollakohdat, joissa ne pysähtyvät, ja lähettää nopeudet servoille.
  //Syötteen ensimmäinen arvo kertoo käytettävän servon, loput nopeuden. Negatiivinen nopeus kääntää suuntaa.
  
  int servo = int(blueVal.charAt(0)-'0');
  blueVal.remove(0,1);
  int nopeus = nollakohdat[servo] + blueVal.toInt();
  
  char text[100]; //Serialin kanssa pitää vähän kikkailla että saa testiarvot ulos.
  snprintf(text, 100, "\nServon %d nopeus on %d", servo, nopeus);
  Serial.println(text);
  
  //Servo member = a;   (  this doesnt work   :(  )
  //member.writeMicroseconds (nopeus);
  
  if(servo == 1){
    myservo.writeMicroseconds (nopeus);
    }
  if(servo == 2){
    myservo2.writeMicroseconds (nopeus);
  }
  
}
