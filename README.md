RabbitMQ Module for Ignition
This project is a custom Ignition module that integrates RabbitMQ messaging capabilities into Ignition. The module includes features for both the designer and gateway scopes, allowing seamless integration with RabbitMQ for real-time messaging and event handling.

Project Structure
designer/: Contains code specific to the Ignition Designer for configuring and interacting with RabbitMQ. (Currently uncompleted)
gateway/: Contains the gateway module that handles communication with RabbitMQ on the Ignition Gateway.
Prerequisites
Ignition 8.x
Java 11+
RabbitMQ server instance
Build Instructions
Clone the repository:

bash
git clone <repository_url>
Navigate to the project directory:

bash
cd RabbitMQ-module-for-Ignition
Use Gradle to build the project:

bash
複製程式碼
./gradlew build
This will compile the source code and create the module .modl file in the build/libs/ directory.

Installation
Once the module is built, locate the .modl file in the build/libs/ directory.
Log into the Ignition Gateway.
Navigate to the Config tab and select Modules.
Click Install or Upgrade a Module and upload the .modl file.
After installation, configure the RabbitMQ settings in the Gateway.
Usage
Currently this project has three methods you can call: start(), startConsuming(), and shutdown(). Currrently, everytime you want to start a new instance, you need to first call the start() method to intiate, then you can call the startConsuming() method with your information to begin consuming RabbitMQ messages and write the values to a tag. 

startConsuming() requires the following parameters: 
startConsuming(String hostName, String username, String password, String virtualHost, String queueName, String tagPath)

If you'd like to contribute, feel free to fork the repository and submit a pull request with your changes. Any feedback anyone can provide would be greatly appreciated.
