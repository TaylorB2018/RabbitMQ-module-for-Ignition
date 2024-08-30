package org.changchun.rabbitmq.gateway;

import com.inductiveautomation.ignition.common.model.values.BasicQualifiedValue;
import com.inductiveautomation.ignition.common.model.values.QualifiedValue;
import com.inductiveautomation.ignition.common.model.values.QualityCode;
import com.inductiveautomation.ignition.common.tags.model.SecurityContext;
import com.inductiveautomation.ignition.common.tags.model.TagPath;
import com.inductiveautomation.ignition.common.tags.model.TagProvider;
import com.inductiveautomation.ignition.common.tags.paths.parser.TagPathParser;
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

public class RabbitMQModule {
    private static final Logger logger = Logger.getLogger(RabbitMQModule.class.getName());
    private GatewayContext context;
    private ExecutorService executorService;

    public RabbitMQModule(GatewayContext context) {
        this.context = context;
        this.executorService = Executors.newFixedThreadPool(10);
        ; // Create a single-thread pool
        logger.info("RabbitMQModule initialized with GatewayContext.");
    }

    public void startConsuming(String hostName, String queueName, String tagPath) {
        // Submit the consuming task to the ExecutorService
        executorService.submit(() -> {
            try {
                consumeMessage(hostName, queueName, tagPath);
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error during message consumption: ", e);
            }
        });
    }


    private void consumeMessage(String hostName, String queueName, String tagPath) throws Exception {
        logger.info(String.format("Starting to consume messages from queue '%s' at host '%s'", queueName, hostName));

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(hostName);

        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {

            logger.info("RabbitMQ connection established.");

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
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
                Thread.sleep(1000); // This keeps the thread alive without consuming too much CPU
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
            executorService.shutdownNow(); // Shutdown the ExecutorService when needed
            logger.info("RabbitMQModule shutdown.");
        }
    }
    public void start() {
        if (executorService == null || executorService.isShutdown()) {
            // Create a new ExecutorService if it is not initialized or if it has been shut down
            executorService = Executors.newFixedThreadPool(10);
        }

        // Submit tasks to the new executor
        executorService.submit(() -> {
            // Your task code here
        });
    }
}
