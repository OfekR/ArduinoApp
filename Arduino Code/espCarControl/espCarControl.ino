
/*
 * Mapping to Esp8266:
 * SS -> D2
 * MOSI -> D7
 * MISO -> D6
 * SCK -> D5
 */

#include <SPI.h>
#include <PN532_SPI.h>
#include "PN532.h"

#include <ESP8266HTTPClient.h>
#include <ESP8266WiFi.h>
#include <Servo.h>

/*
static const uint8_t D0   = 16;
static const uint8_t D1   = 5;
static const uint8_t D2   = 4;
static const uint8_t D3   = 0;
static const uint8_t D4   = 2;
static const uint8_t D5   = 14;
static const uint8_t D6   = 12;
static const uint8_t D7   = 13;
static const uint8_t D8   = 15;
static const uint8_t D9   = 3;
static const uint8_t D10  = 1;
*/

  String playerId = "2";
 
 int sensorValue;
 int pos;
 int min_hit =0;
 PN532_SPI pn532spi(SPI, D2);
PN532 nfc(pn532spi);

Servo myservo;  // create servo object to control a servo
// twelve servo objects can be created on most boards


#define _SSID "HOTBOX-D4E7"
#define _PASS "0526797996"

String payload;

/* #################### NFC #############################3 */
void init_nfc() {
  nfc.begin();

  uint32_t versiondata = nfc.getFirmwareVersion();
  if (! versiondata) {
    Serial.print("Didn't find PN53x board");
    while (1); // halt
  }
  // Got ok data, print it out!
  Serial.print("Found chip PN5"); Serial.println((versiondata>>24) & 0xFF, HEX); 
  Serial.print("Firmware ver. "); Serial.print((versiondata>>16) & 0xFF, DEC); 
  Serial.print('.'); Serial.println((versiondata>>8) & 0xFF, DEC);
  
  // configure board to read RFID tags
  nfc.SAMConfig();

  //TODO - this set how many times it try to read the card
  nfc.setPassiveActivationRetries(10);
  
  Serial.println("Waiting for an ISO14443A Card ...");
  
}

void debug_print_tag_full(uint8_t uid[])
{
    Serial.print("  UID Value: ");
    nfc.PrintHex(uid, 4);
    int number0 = uid[0];
    int number1 = uid[1];
    int number2 = uid[2];
    int number3 = uid[3];
    // TODO - consdier using only part of id(probaly two or three number are enough)
    uint32_t total = (number0 << 24)  + (number1 << 16) + (number2 << 8) + (number3);
    Serial.println("");
    Serial.println(number0);
        Serial.println(number1);
            Serial.println(number2);
                Serial.println(number3);
                                Serial.println(total);
}

void dispatch_uid_read(uint32_t uid)
{
  Serial.print("Dispatched ");
  Serial.println(uid);
  //TODO #$#$@#$#$@@#$#$#$#@@#$$ change  id
    String url = "https://us-central1-arduino-a5968.cloudfunctions.net/handleRfidRead?id=" + playerId + "&param=";
  url += String(uid);   
  send_http_request(url, "FD:57:A7:FA:1A:05:44:88:D3:4A:C8:5C:33:1E:5D:9B:78:30:F3:4A");
}

uint32_t last_read_uid = 0;
uint32_t active_read_uid = 0;

void handle_rfid_loop()
{
    uint8_t success;
  uint8_t uid[] = { 0, 0, 0, 0, 0, 0, 0 };  // Buffer to store the returned UID
  uint8_t uidLength;                        // Length of the UID (4 or 7 bytes depending on ISO14443A card type)
    


  
  // Wait for an ISO14443A type cards (Mifare, etc.).  When one is found
  // 'uid' will be populated with the UID, and uidLength will indicate
  // if the uid is 4 bytes (Mifare Classic) or 7 bytes (Mifare Ultralight)

  success = nfc.readPassiveTargetID(PN532_MIFARE_ISO14443A, uid, &uidLength);


  if (success) {
    // Display some basic information about the card
    //debug_print_tag_full(uid);

    // TODO - consdier using only part of id(probaly two or three number are enough)
    uint32_t current_uid = (uid[0] << 8) + (uid[1]);

    if ( current_uid != active_read_uid )
    {
      last_read_uid = current_uid;
      active_read_uid = current_uid;
      dispatch_uid_read(current_uid);
    }
    //else - do nothing    
 }
 else
 {
  if(active_read_uid != 0)
  {
    //finished reading card
    Serial.println("Ended");
    dispatch_uid_read(active_read_uid + 1);
    active_read_uid = 0;
  }
 
 }
}


/* #####################3 WIFI ###########################3 */

void init_wifi(){
    WiFi.begin(_SSID, _PASS);   //WiFi connection
    while (WiFi.status() != WL_CONNECTED) {  //Wait for the WiFI connection completion
      delay(500);
      Serial.println("Waiting for connection");
  }
  Serial.println("Connection Succeded");
}

// get the url , and ssh for connection 
// returning the payload of http req
String send_http_request(String url,String ssh){
   if(WiFi.status()== WL_CONNECTED){   //Check WiFi connection status
    HTTPClient http;    //Declare object of class HTTPClient
    http.begin(url,ssh);      //Specify request destination
    int httpCode = http.POST("message");  
    String _payload = http.getString();                  //Get the response payload
    Serial.println(httpCode);   //Print HTTP return code
    Serial.println(_payload);    //Print request response payload
    http.end();  //Close connection
    return _payload; 
    }
 else{
    Serial.println("Error in WiFi connection");
    return "Error";   
 }
}

#define INTIAL_SERVO_POS 120
void init_normal(){
  min_hit = analogRead(A0);
  for (pos = 30; pos <= 150; pos += 1) { // goes from 0 degrees to 180 degrees
    // in steps of 1 degree
    myservo.write(pos);              // tell servo to go to position in variable 'pos'
    min_hit= min(analogRead(A0),min_hit);
    delay(50);                       // waits 15ms for the servo to reach the position
  }
   myservo.write(INTIAL_SERVO_POS);
}

void checkForHit(int val){
  if( val < 0.9*min_hit){
    Serial.println("Hit");
    String url = "https://us-central1-arduino-a5968.cloudfunctions.net/playerHit?id=" + playerId;
    
  payload = send_http_request(url,"FD:57:A7:FA:1A:05:44:88:D3:4A:C8:5C:33:1E:5D:9B:78:30:F3:4A");
  Serial.print("The payload is --> ");
  Serial.println(payload);
  delay(3000);  //Send a request every 30 seconds
  }
  else{
     Serial.println("Miss");
  }
}

void setup() {
  myservo.attach(16);  // attaches the servo on GIO2 to the servo object
  Serial.begin(9600);
  init_normal();
  init_wifi();
  init_nfc();
}

void loop() {
  handle_rfid_loop();
  sensorValue = analogRead(A0);
//  Serial.println(sensorValue);
  checkForHit(sensorValue);
  delay(50);                       // waits 15ms for the servo to reach the position

  /*
  for (pos = 30; pos <= 150; pos += 1) { // goes from 0 degrees to 180 degrees
    // in steps of 1 degree
    myservo.write(pos);              // tell servo to go to position in variable 'pos'
      sensorValue = analogRead(A0);
  Serial.println(sensorValue);
    checkForHit(sensorValue);
    delay(750);                       // waits 15ms for the servo to reach the position
  }
  for (pos = 150; pos >= 30; pos -= 1) { // goes from 180 degrees to 0 degrees
  myservo.write(pos);              // tell servo to go to position in variable 'pos'
  sensorValue = analogRead(A0);
  Serial.println(sensorValue);
  checkForHit(sensorValue);
  delay(750);                       // waits 15ms for the servo to reach the position
  
  }
  */
}
