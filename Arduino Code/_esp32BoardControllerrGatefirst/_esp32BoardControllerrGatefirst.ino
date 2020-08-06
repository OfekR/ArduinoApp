
#include <WiFiMulti.h>
#include <HTTPClient.h>
#include <Arduino_JSON.h>


#define TIMER_WIDTH 16
#define LEDC_TIMER_13_BIT  13
#define LEDC_BASE_FREQ     5000

WiFiMulti WiFiMulti;

// wifi varable 

const char* ssid = "HOTBOX-D4E7"; // Wifi SSID
const char* password = "0526797996"; // Wifi Password



void init_led(){
  pinMode (12, OUTPUT);
  pinMode (14, OUTPUT);
  pinMode (27, OUTPUT);
  pinMode (26, OUTPUT);
  pinMode (25, OUTPUT);
  pinMode (33, OUTPUT);
}



//value received by cloud function indicate if there is a change (2nd bit) what change(1st bit) and on which compenent (see format above each function)
    // 00 - do nothing
    // 01 - do nothing
    // 10 - turn off
    // 11 - turn on


//XX XX XX XX
//B3 B4 B8 B5
void handleGate(String num){
  Serial.print("Gate ");
  Serial.println(num);
  if(num[1] == '1')
  {
    //B5
    (num[0] == '1')?ledcWrite(1,7500):ledcWrite(1, 4200); 
  }

  if(num[3] == '1')
  {
    //B8
    (num[2] == '1')?ledcWrite(3,5555):ledcWrite(3, 1800); 
  }

  if(num[4] == '1')
  {
    //B4
    (num[3] == '1')?ledcWrite(4,1500):ledcWrite(4, 5200);    
  }

  if(num[6] == '1')
  {
    //B3
    (num[5] == '1')?ledcWrite(2,4444):ledcWrite(2, 1500);
  }
}

//XX XX XX
//M4 M1 M5
void handleMine(String num){
  if(num[1] == '1')
  {
    //M5
    (num[0] == '1')?digitalWrite(33, HIGH):digitalWrite(33, LOW); 
  }

  if(num[3] == '1')
  {
    //M1
    (num[2] == '1')?digitalWrite(27, HIGH):digitalWrite(27, LOW); 
  }

  if(num[4] == '1')
  {
    //M4
    (num[3] == '1')?digitalWrite(25, HIGH):digitalWrite(25, LOW); 
  }
}

//XX XX XX
//L5 L2 L1
void handleLootbox(String num,int val){
  if(num[1] == '1')
  {
    //L1
    (num[0] == '1')?digitalWrite(14, HIGH):digitalWrite(14, LOW);
  }

  if(num[3] == '1')
  {
    //L2
    (num[2] == '1')? digitalWrite(26, HIGH):digitalWrite(26, LOW);
  }

  if(num[4] == '1')
  {
    //L5
    (num[3] == '1')? digitalWrite(12, HIGH):digitalWrite(12, LOW);
  }

}




void initGate(){
   Serial.println("********** Start to init servo  ");
   ledcSetup(1, 50, TIMER_WIDTH); // channel 1, 50 Hz, 16-bit width
   ledcAttachPin(23, 1);   // GPIO 22 assigned to channel 1
   ledcSetup(2, 50, TIMER_WIDTH); // channel 1, 50 Hz, 16-bit width
   ledcAttachPin(22, 2);   // GPIO 22 assigned to channel 1
   ledcSetup(3, 50, TIMER_WIDTH); // channel 1, 50 Hz, 16-bit width
   ledcAttachPin(19, 3);   // GPIO 22 assigned to channel 1
   ledcSetup(4, 50, TIMER_WIDTH); // channel 1, 50 Hz, 16-bit width
   ledcAttachPin(18, 4);   // GPIO 22 assigned to channel 1
  // delay(2000);
   ledcWrite(1, 4200);       // sweep servo 1
   ledcWrite(2, 4444);       // sweep servo 1
   ledcWrite(3, 1800);       // sweep servo 1
   ledcWrite(4, 5200);       // sweep servo 
   Serial.println("********** Finish to init servo  ");
}

void send_http_request(String url,char id){
    HTTPClient ask;
    int i=0;
    String _id ="";
    ask.begin(url); //Specify the URL
    int httpCode = ask.GET();           
    if (httpCode > 0) {
       string value = ask.getString();
        if(id == 'M'){
          handleMine(value);
          }
        else if(id == 'B'){
          handleGate(value);

        }
        else{ 
            //id=='L'
           handleLootbox(value);
      }
    else {
        Serial.println("Error on HTTP request");
    }
    ask.end();
}


void checkRfid(){
    // Create a URL for updating module1 and module 2
    static String gate_url = "https://us-central1-arduino-a5968.cloudfunctions.net/barrierRequestNew";
    static String mine_url = "https://us-central1-arduino-a5968.cloudfunctions.net/mineRequestNew";
    static String lootbox_url = "https://us-central1-arduino-a5968.cloudfunctions.net/lootboxRequestNew";      
    Serial.print("********** requesting URL: ");
    send_http_request(gate_url,'B');
    delay(500);
    send_http_request(mine_url,'M');
    delay(500);
    send_http_request(lootbox_url,'L');
    delay(500);
      
}

void init_wifi(){
   WiFiMulti.addAP(ssid, password);
  while (WiFiMulti.run() != WL_CONNECTED) {
    Serial.print(".");
    delay(500);
  }
  // connected
  Serial.println("WiFi connected");
  Serial.println("IP address: ");
  Serial.println(WiFi.localIP());
}


void setup() {
  Serial.begin(9600); // Initialize serial communications with the PC
  init_led();
  init_wifi();
  initGate();  
}
  
void loop() {
  checkRfid();
  delay(1000); 
}
