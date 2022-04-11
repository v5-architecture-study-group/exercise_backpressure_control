package foo.v5archstudygroup.exercises.backpressure.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This class is responsible for sending the messages to the server. You are allowed to change this class in any
 * way you like as long as all messages are delivered successfully to the server.
 */
public class ProcessingWorker {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessingWorker.class);
    private final ExecutorService executorService;
    private final ProcessingRequestGenerator requestGenerator;
    private final RestClient client;
    private final AtomicBoolean done = new AtomicBoolean(false);
    private int sent = 0;
    private int errors = 0;

    public ProcessingWorker(ExecutorService executorService,
                            ProcessingRequestGenerator requestGenerator,
                            RestClient client) {
        this.executorService = executorService;
        this.requestGenerator = requestGenerator;
        this.client = client;
    }

    public void run() {
        executorService.submit(() -> {
            try {
                while (requestGenerator.hasNext()) {
                    var message = requestGenerator.next();
                    try {
                        sent++;
                        client.sendToServer(message);
                    } catch (Exception ex) {
                        LOGGER.error("Error sending request {}: {}", message.getUuid(), ex.getMessage());
                        errors++;
                    }
                }
                LOGGER.info("Finished sending {} requests with {} errors", sent, errors);
            } finally {
                done.set(true);
            }
        });
    }

    public boolean isDone() {
        return done.get();
    }
}
