#include "Arduino.h"
#include <Servo.h>

#define LASER 8
#define SERVO_H 10
#define SERVO_V 9
Servo myServoH;
Servo myServoV;


#define START_H 0
#define END_H 80
#define START_V 40
#define END_V 160


#define PIN        6 // On Trinket or Gemma, suggest changing this to 1
#define NUMPIXELS 12 // Popular NeoPixel ring size
#define DELAYVAL 500 // Time (in milliseconds) to pause between pixels

const int bigChangeV = 4;
const int smallChangeV = 2;
const int bigChangeH = 2;
const int smallChangeH = 1;




int posH = START_H;   
int posV = START_V;

int Motor_A_Enable = 5;   // FOWARD ENGINE LEFT
int IN2 = 4;
int IN1 = 3;

int Motor_B_Enable = 11; // BACKWARD ENGINE RIGHT
int IN3 = 12;
int IN4 = 13;



void forward()
{
    digitalWrite(IN1, HIGH);
    digitalWrite(IN2, LOW);
    digitalWrite(IN3, HIGH);
    digitalWrite(IN4, LOW);
}

void backward()
{
    digitalWrite(IN1, LOW);
    digitalWrite(IN2, HIGH);
    digitalWrite(IN3, LOW);
    digitalWrite(IN4, HIGH);
}

void right()
{
    digitalWrite(IN1, HIGH);
    digitalWrite(IN2, LOW);
    digitalWrite(IN3, LOW);
    digitalWrite(IN4, LOW);
}

void left()
{
    digitalWrite(IN1, LOW);
    digitalWrite(IN2, LOW);
    digitalWrite(IN3, HIGH);
    digitalWrite(IN4, LOW);
}




void stop()
{
    digitalWrite(IN1, LOW);
    digitalWrite(IN2, LOW);
    digitalWrite(IN3, LOW);
    digitalWrite(IN4, LOW);


}

void VerifyLimits(int* currentH = NULL, int* currentV = NULL)
{
  if(currentH != NULL)
  {
    if(*currentH > END_H)
    {
      *currentH = END_H;
    }
    else if (*currentH < START_H)
    {
      *currentH = START_H;
    }
  }

    if(currentV != NULL)
  {
    if(*currentV > END_V)
    {
      *currentV = END_V;
    }
    else if (*currentV < START_V)
    {
      *currentV = START_V;
    }
  }
}

void MoveServo(int changeV,int changeH)
{

  posH += changeH;
  posV += changeV;
  VerifyLimits(&posH,&posV);

  if(changeH != 0)
  {
    myServoH.write(posH);
  }
  if(changeV != 0)
  {
    myServoV.write(posV);
  }
  
  
}

void setup() {
  // put your setup code here, to run once:
  Serial.begin(9600);
      Serial.write("Start!!");

  pinMode(Motor_A_Enable, OUTPUT);
  pinMode(IN1, OUTPUT);
  pinMode(IN2, OUTPUT);

  pinMode(Motor_B_Enable, OUTPUT);
  pinMode(IN3, OUTPUT);
  pinMode(IN4, OUTPUT);

  
  myServoV.attach(SERVO_V); 
  myServoH.attach(SERVO_H); 

  myServoV.write(START_V);
  myServoH.write(START_H);  

  pinMode(LASER, OUTPUT);    

}

float speed_factor = 0.6;

const int speedWeakLow = 60 * speed_factor;
const int speedWeakMid = 100 * speed_factor;
const int speedWeakHigh = 140 * speed_factor;
const int speedWeakMax = 180 * speed_factor;

const int speedStrongLow = 75 * speed_factor;
const int speedStrongMid = 125 * speed_factor;
const int speedStrongHigh = 175 * speed_factor;
const int speedStrongMax = 225 * speed_factor;


void loop() {
        

          //TODO - when done, optimize calculation, for example calulate which quarter of circle to determine sign, and the all case are the same
  if(Serial.available() > 0)
  {
    char data;
    data = Serial.read();
    //Serial.write(Serial.read());
    Serial.write(data);
    switch (data)
    {

            case '!': 
        stop();
        analogWrite(Motor_A_Enable, 0);
        analogWrite(Motor_B_Enable, 0);
        break;
    /////////// ******* Right ********** ////////////////
  
  //Weak right  
    case 'Q':
    case 'o':
        right();   
        analogWrite(Motor_A_Enable, speedWeakHigh);      
        //analogWrite(Motor_B_Enable, speedWeakHigh);
        break;
    //Strong right  
    case 'R':
    case 'p':
        right();   
        analogWrite(Motor_A_Enable, speedStrongHigh);      
        //analogWrite(Motor_B_Enable, speedStrongHigh);
        break;

        
  /////////// ******* Top Right ********** ////////////////

  //Weak right - top  
  case 'S':
    forward();
    analogWrite(Motor_A_Enable, speedWeakHigh);      
    analogWrite(Motor_B_Enable, speedWeakLow);
    break;
    //Strong right - top  
  case 'T':
    forward();
    analogWrite(Motor_A_Enable, speedStrongHigh);      
    analogWrite(Motor_B_Enable, speedStrongLow);
    break;
    
  
  
  //Weak top - right  
  case 'U':
    forward();
    analogWrite(Motor_A_Enable, speedWeakHigh);      
    analogWrite(Motor_B_Enable, speedWeakMid); 
    break;
  //Strong top - right  
  case 'V':
    forward();
    analogWrite(Motor_A_Enable, speedStrongHigh);      
    analogWrite(Motor_B_Enable, speedStrongMid);
    break;

    /////////// ******* Top ********** ////////////////

  //Weak top
  case 'W':
  case 'Y':
    forward();
    analogWrite(Motor_A_Enable, speedWeakHigh);      
    analogWrite(Motor_B_Enable, speedWeakHigh);
    break;
    //Strong top 
  case 'X':
  case 'Z':
    forward();
    analogWrite(Motor_A_Enable, speedStrongHigh);      
    analogWrite(Motor_B_Enable, speedStrongHigh);
    break;
    
  /////////// ******* Top Left ********** ////////////////
    
  //Weak top - left
  case '[':
    forward();
    analogWrite(Motor_A_Enable, speedWeakMid);      
    analogWrite(Motor_B_Enable, speedWeakHigh);
        break;
    //Strong top - left
  case '\\':
    forward();
    analogWrite(Motor_A_Enable, speedStrongMid);      
        analogWrite(Motor_B_Enable, speedStrongHigh);
        break;
    
  //Weak left - top  
  case ']':
    forward();
    analogWrite(Motor_A_Enable, speedWeakLow);      
    analogWrite(Motor_B_Enable, speedWeakHigh);
    break;
    //Strong left - top  
  case '^':
    forward();
    analogWrite(Motor_A_Enable, speedStrongLow);      
    analogWrite(Motor_B_Enable, speedStrongHigh);  
    break;

  /////////// ******* Left ********** ////////////////

    
  //Weak left  
    case '_':
    case 'a':
        left();   
        //analogWrite(Motor_A_Enable, speedWeakHigh);      
        analogWrite(Motor_B_Enable, speedWeakHigh);
        break;
    //Strong left  
    case '`':
    case 'b':
        left();   
        //analogWrite(Motor_A_Enable, speedStrongHigh);      
        analogWrite(Motor_B_Enable, speedStrongHigh); 
        break;

  /////////// ******* Bottom Left ********** ////////////////
    
  //Weak bottom - left
  case 'c':
    backward();
    analogWrite(Motor_A_Enable, speedWeakMid);      
    analogWrite(Motor_B_Enable, speedWeakHigh);
        break;
    //Strong top - left
  case 'd':
    backward();
    analogWrite(Motor_A_Enable, speedStrongMid);      
        analogWrite(Motor_B_Enable, speedStrongHigh);
        break;
    
  //Weak left - top  
  case 'e':
    backward();
    analogWrite(Motor_A_Enable, speedWeakLow);      
    analogWrite(Motor_B_Enable, speedWeakHigh);
    break;
    //Strong left - top  
  case 'f':
    backward();
    analogWrite(Motor_A_Enable, speedStrongLow);      
    analogWrite(Motor_B_Enable, speedStrongHigh);  
    break;
      
    /////////// ******* Bottom ********** ////////////////

  //Weak top
  case 'g':
  case 'i':
    backward();
    analogWrite(Motor_A_Enable, speedWeakHigh);      
    analogWrite(Motor_B_Enable, speedWeakHigh);
    break;
    //Strong top 
  case 'h':
  case 'j':
    backward();
    analogWrite(Motor_A_Enable, speedStrongHigh);      
    analogWrite(Motor_B_Enable, speedStrongHigh);
    break;

    /////////// ******* Bottom Right ********** ////////////////

  //Weak right - bottom  
  case 'k':
    backward();
    analogWrite(Motor_A_Enable, speedWeakHigh);      
    analogWrite(Motor_B_Enable, speedWeakLow);
    break;
    //Strong right - bottom  
  case 'l':
    backward();
    analogWrite(Motor_A_Enable, speedStrongHigh);      
    analogWrite(Motor_B_Enable, speedStrongLow);
    break;
    
  
  
  //Weak bottom - right  
  case 'm':
    backward();
    analogWrite(Motor_A_Enable, speedWeakHigh);      
    analogWrite(Motor_B_Enable, speedWeakMid); 
    break;
  //Strong bottom - right  
  case 'n':
    backward();
    analogWrite(Motor_A_Enable, speedStrongHigh);      
    analogWrite(Motor_B_Enable, speedStrongMid);
    break;

    /////////// ******* Servo ********** ////////////////

    //Servo
    case 'O':
    case 'A': //Weak Right
        MoveServo(-1*smallChangeV,0);
        break;
    case 'P':
    case 'B': //Strong Right
        MoveServo(-1*bigChangeV,0);
        break;

    case 'C': //Weak Top 
    case 'E':
        MoveServo(0,-smallChangeH);
        break;
    case 'D': //Strong Top
    case 'F': 
        MoveServo(0,-bigChangeH);
        break;

    case 'G':
    case 'I': //Weak Left
        MoveServo(smallChangeV,0);
        break;
    case 'H':
    case 'J': //Strong Left
        MoveServo(bigChangeV,0);
        break;       
        
    case 'K': //Weak Bottom 
    case 'M':
        MoveServo(0,smallChangeH);
        break;
    case 'L': //Strong Bottom
    case 'N': 
        MoveServo(0,bigChangeH);
        break;
        
    /////////// ******* Laser ********** ////////////////
    case '#':
          digitalWrite(LASER,HIGH);
          break;

    case '$':
         digitalWrite(LASER,LOW);
         break;
        
       //TODO Consider removing this
      default: //If bluetooth module receives any value not listed above, both motors turn off
        stop();
        analogWrite(Motor_A_Enable, 0);
        analogWrite(Motor_B_Enable, 0);
    }
  }
}
