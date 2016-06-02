### Synopsis 

This project tries to simulate a SmartRoad environment based on IoT. It uses MQTT broker to handle the communication among the different objects, and PAHO as MQTT client. The structure of the messages has been builded using JSON format. 


### Dependencies

Mosquitto
[http://mosquitto.org/](http://mosquitto.org/)
Mosquitto is the broker I have chosen to this project. 
Just becuase I was already familiar with it. 

Paho Client 
[https://eclipse.org/paho/](https://eclipse.org/paho/)
All the communication in this project uses the Java library of Paho 
to communicate the several devices through Mqtt.  

GSON 
[https://github.com/google/gson](https://github.com/google/gson)
My favorite library to the JSON treatment. 

Jackson 
[https://github.com/FasterXML/jackson](https://github.com/FasterXML/jackson)
Jackson is used in this project to convert Java objects to JSON and vice versa. 

### Updates 

Last update: 2/06/2016