### Synopsis 

This project tries to simulate a SmartRoad environment based on IoT. It uses Mosquitto as broker to handle the communication among the different objects (devices), and PAHO as MQTT Java client. 
The structure of the messages has been builded following the JSON format. 


### Dependencies

###### Mosquitto [(link)](http://mosquitto.org/)
Mosquitto is the broker I have chosen to this project. 
Just becuase I was already familiar with it. 

 
###### Paho Client [(link)](https://eclipse.org/paho/)
All the communication in this project uses the Java library of Paho 
to communicate the several devices through Mqtt.  


###### GSON [(link)](https://github.com/google/gson)
My favorite library to the JSON treatment. 


###### Jackson [(link)](https://github.com/FasterXML/jackson-datatype-jsr310)
Jackson is used in this project to convert Java objects to JSON and vice versa. 	


**Last update: 07/06/2016** 

