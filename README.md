**# RabbitMQ Module for Ignition**

This project is a custom Ignition module that integrates RabbitMQ messaging capabilities into Ignition. The module includes features for both the designer(not completed) and gateway scopes, allowing seamless integration with RabbitMQ for real-time messaging and event handling.

**# Project Structure**

designer/: Contains code specific to the Ignition Designer for configuring and interacting with RabbitMQ. (Currently uncompleted)
gateway/: Contains the gateway module that handles communication with RabbitMQ on the Ignition Gateway.


**# Prerequisites**
Ignition 8.x

Java 11+

RabbitMQ server instance


**# Build Instructions**
Clone the repository:

```
git clone <repository_url>
```
Navigate to the project directory:

```
cd RabbitMQ-module-for-Ignition
```
Use Gradle to build the project:
```
./gradlew build
```
This will compile the source code and create the module .modl file. The file will be located in your build folder.

**# Installation**

Once the module is built, locate the .modl file inside the build directory.
Log into the Ignition Gateway.
Navigate to the Config tab and select Modules.
Click Install or Upgrade a Module and upload the .modl file.
After installation, configure the RabbitMQ settings in the Gateway.
(Note: You need to allow unsigned modules and restart your Gateway before you can install the module)


**# Usage**

Currently this project has three methods you can call: start(), startConsuming(), and shutdown(). Currrently, everytime you want to start a new instance, you need to first call the start() method to intiate, then you can call the startConsuming() method with your information to begin consuming RabbitMQ messages and writing the values to a tag. Then if you want to stop all instances in the current project, you just need to call the shutdown() method.

startConsuming() requires the following parameters: 
startConsuming(String hostName, String username, String password, String virtualHost, String queueName, String tagPath)

**# Adding credentials through an external file**

If you would like to be able to have your credentials ready to use after building the module without the need to input them as parameters manually, you just need to add a resource directory to gateway/src/main and then put a config.properties file inside. In the properties file, you will write the following three lines to the file:
rabbitmq.username=<your_username>
rabbitmq.password=<your_password>
rabbitmq.virtualHost=<your_vh>

With this information the use is exactly the same except when you call the startConsuming() method you will only need 3 parameters: String hostName, String queuename, String tagPath

If you'd like to contribute, feel free to fork the repository and submit a pull request with your changes. Any feedback anyone can provide would be greatly appreciated.
