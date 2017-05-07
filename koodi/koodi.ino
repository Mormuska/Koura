#include <Servo.h>

String blueToothVal;
int lastvalue;
int nollakohdat[] = {1510, 1500};
Servo servoA;
Servo servoB;
// Servo servoC;

#define outputA 6
#define outputB 7
 int counter = 0;
 int aState;
 int aLastState;  
 
void setup() {
  servoA.attach(9);
  servoB.attach(8);
//servoC.attach(8);
  Serial.begin(9600);

   pinMode (outputA,INPUT);
   pinMode (outputB,INPUT);
   
   Serial.begin (9600);
   // Reads the initial state of the outputA
   aLastState = digitalRead(outputA);   
}

void loop() {
  if(Serial.available()){
    blueToothVal = Serial.readString();
    liikuta(blueToothVal, nollakohdat);
  }


  aState = digitalRead(outputA); // Reads the "current" state of the outputA
   // If the previous and the current state of the outputA are different, that means a Pulse has occured
   if (aState > aLastState){     
     if (digitalRead(outputB) != aState) { 
       counter ++;
     } else {
       counter --;
     }
     Serial.print("Position: ");
     Serial.println(counter);
   } 
   aLastState = aState; // Updates the previous state of the outputA with the current state
}     // If the outputB state is different to the outputA state, that means the encoder is rotating clockwise


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
