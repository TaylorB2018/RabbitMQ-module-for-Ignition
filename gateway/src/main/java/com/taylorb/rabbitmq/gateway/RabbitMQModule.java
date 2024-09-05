package com.taylorb.rabbitmq.gateway;

import com.inductiveautomation.ignition.common.model.values.BasicQualifiedValue;
import com.inductiveautomation.ignition.common.model.values.QualifiedValue;
import com.inductiveautomation.ignition.common.model.values.QualityCode;
import com.inductiveautomation.ignition.common.tags.model.SecurityContext;
import com.inductiveautomation.ignition.common.tags.model.TagPath;
import com.inductiveautomation.ignition.common.tags.model.TagProvider;
import com.inductiveautomation.ignition.common.tags.paths.parser.TagPathParser;
//import com.inductiveautomation.ignition.designer.model.DesignerContext;
import com.inductiveautomation.ignition.gateway.model.GatewayContext;
import com.inductiveautomation.ignition.gateway.tags.model.GatewayTagManager;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class RabbitMQModule {
    private static final Logger logger = Logger.getLogger(RabbitMQModule.class.getName());
    private final GatewayContext context;
    private ExecutorService executorService;


    public RabbitMQModule(GatewayContext context) {
        this.context = context;
        this.executorService = Executors.newFixedThreadPool(10);
         // Create a single-thread pool
        logger.info("RabbitMQModule initialized with GatewayContext.");
    }


    // This is if you do not wish to use an external file and just want to directly write your credentials
    public void startConsuming(String hostName, String username, String password, String virtualHost, String queueName, String tagPath) {
        // Submit the consuming task to the ExecutorService
        executorService.submit(() -> {
            try {
                consumeMessage(hostName, username, password, virtualHost, queueName, tagPath);
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error during message consumption: ", e);
            }
        });
    }


    private void consumeMessage(String hostName, String username, String password, String virtualHost, String queueName, String tagPath) throws Exception {
        logger.info(String.format("Starting to consume messages from queue '%s' at host '%s'", queueName, hostName));
        ConnectionFactory factory = new ConnectionFactory();
        factory.setUsername(username);
        factory.setPassword(password);
        factory.setHost(hostName);
        factory.setVirtualHost(virtualHost);

        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {

            logger.info("RabbitMQ connection established.");

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                if (Thread.currentThread().isInterrupted()) {
                    return;
                }
                String message = new String(delivery.getBody(), "UTF-8");
                logger.info(String.format("Message received from queue '%s': %s", queueName, message));

                // Process the message
                processMessage(message, tagPath);
            };

            // Start consuming messages continuously
            channel.basicConsume(queueName, true, deliverCallback, consumerTag -> {
            });

            // Keep the thread alive to listen for messages
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(1000); // Shorter sleep to check for interruption frequently
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // Reset interrupted flag and break the loop
                    break;
                } // This keeps the thread alive without consuming too much CPU
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error consuming messages from RabbitMQ: ", e);
        }
    }

    private void processMessage(String message, String tagPath) {
        try {
            // Obtain TagManager and TagProvider
            GatewayTagManager tagManager = context.getTagManager();
            TagProvider provider = tagManager.getTagProvider("default"); // Adjust if needed

            // Prepare tag path and new value
            TagPath tagPathObj = TagPathParser.parse(tagPath);
            logger.info("Parsed tag path: " + tagPathObj.toString());

            QualifiedValue newQv = new BasicQualifiedValue(message);

            // Write out a new value asynchronously
            logger.info(String.format("Writing value '%s' to tag '%s'", message, tagPath));

            List<QualityCode> writeResults = provider.writeAsync(
                    Arrays.asList(tagPathObj),
                    Arrays.asList(newQv),
                    SecurityContext.emptyContext()).get(30, TimeUnit.SECONDS);

            QualityCode writeQualityCode = writeResults.get(0);
            if (writeQualityCode.isNotGood()) {
                logger.severe("Failed to write tag value: " + writeQualityCode.toString());
                throw new Exception("Write operation returned bad quality: " + writeQualityCode.toString());
            }

            logger.info("Write operation successful.");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error during tag write/read operation: ", e);
        }
    }

    public void shutdown() {
        if (executorService != null && !executorService.isShutdown()) {
            logger.info("Attempting to shutdown RabbitMQ module executor service.");

            executorService.shutdownNow(); // Attempt to stop all actively executing tasks
            try {
                if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                    logger.warning("Executor service did not terminate within the timeout.");
                } else {
                    logger.info("Executor service shut down gracefully.");
                }
            } catch (InterruptedException e) {
                logger.log(Level.SEVERE, "Interrupted while waiting for shutdown.", e);
                Thread.currentThread().interrupt();  // Preserve interrupt status
            }
        }
    }
    public void start() {
        if (executorService == null || executorService.isShutdown()) {
            // Create a new ExecutorService if it is not initialized or if it has been shut down
            executorService = Executors.newFixedThreadPool(10);
        }

        // Submit tasks to the new executor
        executorService.submit(() -> {
            // task code here don't know if I need to add code here
        });
    }
    // TODO: finish the code here
    public void exchangeDeclare(String exchangeName, String exchangeType, Boolean autoDelete){

    }

    //If you would like to make use an external file to keep credentials
    private Properties loadProperties() throws IOException {
        Properties properties = new Properties();
        try (InputStream input = RabbitMQModule.class.getClassLoader().getResourceAsStream("config.properties")) {  // this is where you add the path to external file, just put the file in the resources dir before building
            properties.load(input);
        }
        return properties;
    }

    private String getRabbitMQUsername(Properties properties) {
        return properties.getProperty("rabbitmq.username");
    }

    private String getRabbitMQPassword(Properties properties) {
        return properties.getProperty("rabbitmq.password");
    }

    private String getRabbitMQVirtualHost(Properties properties) {return properties.getProperty("rabbitmq.virtualHost");}


    // Overloaded function in case you make use of an external file

    public void startConsuming(String hostName, String queueName, String tagPath) throws IOException {
        // Submit the consuming task to the ExecutorService

            Properties properties = loadProperties();

            String username = getRabbitMQUsername(properties);
            String password = getRabbitMQPassword(properties);
            String virtualHost = getRabbitMQVirtualHost(properties);

            // Use these credentials in your RabbitMQ setup

        executorService.submit(() -> {
            try {
                consumeMessage(hostName, username, password, virtualHost, queueName, tagPath);
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error during message consumption: ", e);
            }
        });
    }

}
