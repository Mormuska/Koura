#include <Servo.h>

String blueToothVal;
int lastvalue;
int nollakohdat[] = {1510, 1510};
Servo servoA;
Servo servoB;
// Servo servoC;

void setup() {
  servoA.attach(9);
  servoB.attach(8);
//servoC.attach(8);
  Serial.begin(9600);
}

void loop() {
  if(Serial.available()){
    blueToothVal = Serial.readString();
    liikuta(blueToothVal, nollakohdat);
  }
  delay(100);
}

void liikuta(String blueVal,int nollakohdat[]){
  //Ottaakäytäjän syötteen ja servoille kalibroidut nollakohdat, joissa ne pysähtyvät, ja lähettää nopeudet servoille.
  //Syötteen ensimmäinen arvo kertoo käytettävän servon, loput nopeuden. Negatiivinen nopeus kääntää suuntaa.
  
  int servo = int(blueVal.charAt(0)-'0');
  blueVal.remove(0,1);
  int nopeus = nollakohdat[servo-1] + blueVal.toInt();

  if(servo < 3 && servo > 0){
    char text[100]; //Serialin kanssa pitää vähän kikkailla että saa testiarvot ulos.
    snprintf(text, 100, "\nServon %d nopeus on %d", servo, nopeus);
    Serial.println(text);
  }else{
    Serial.println("\nError: Ei minulla ole sellaista servoa! >:I ");}
  
  
  switch(servo) {
    case 1:
      servoA.writeMicroseconds (nopeus);
      break; // Pitää laittaa!
    case 2:
      servoB.writeMicroseconds (nopeus);
      break;
/*    case 3:
      servoC.writeMicroseconds (nopeus);
      break;
*/
  }
}
