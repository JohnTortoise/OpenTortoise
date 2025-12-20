package io.github.johntortoise.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

@Configuration
public class ThreadPoolConfig {


    @Value("${thread-pool.common.core-pool-size:5}")
    private int commonCorePoolSize;

    @Value("${thread-pool.common.max-pool-size:20}")
    private int commonMaxPoolSize;

    @Value("${thread-pool.common.queue-capacity:100}")
    private int commonQueueCapacity;

    @Value("${thread-pool.common.keep-alive-seconds:60}")
    private int commonKeepAliveSeconds;

    @Value("${thread-pool.common.await-termination-seconds:60}")
    private int commonAwaitTerminationSeconds;

    
    @Value("${thread-pool.after-chat.core-pool-size:5}")
    private int afterChatCorePoolSize;

    @Value("${thread-pool.after-chat.max-pool-size:20}")
    private int afterChatMaxPoolSize;

    @Value("${thread-pool.after-chat.queue-capacity:100}")
    private int afterChatQueueCapacity;

    @Value("${thread-pool.after-chat.keep-alive-seconds:60}")
    private int afterChatKeepAliveSeconds;

    @Value("${thread-pool.after-chat.await-termination-seconds:60}")
    private int afterChatAwaitTerminationSeconds;

    @Value("${thread-pool.after-chat.thread-name-prefix:Async-Service-}")
    private String afterChatThreadNamePrefix;


    
    @Value("${thread-pool.import-conversation.core-pool-size:5}")
    private int importConversationCorePoolSize;

    @Value("${thread-pool.import-conversation.max-pool-size:20}")
    private int importConversationMaxPoolSize;

    @Value("${thread-pool.import-conversation.queue-capacity:100}")
    private int importConversationQueueCapacity;

    @Value("${thread-pool.import-conversation.keep-alive-seconds:60}")
    private int importConversationKeepAliveSeconds;

    @Value("${thread-pool.import-conversation.await-termination-seconds:60}")
    private int importConversationAwaitTerminationSeconds;

    @Value("${thread-pool.import-conversation.thread-name-prefix:ImportExcel-}")
    private String importConversationThreadNamePrefix;

    
    @Value("${thread-pool.batch-refresh-memory.core-pool-size:5}")
    private int batchRefreshMemoryCorePoolSize;

    @Value("${thread-pool.batch-refresh-memory.max-pool-size:20}")
    private int batchRefreshMemoryMaxPoolSize;

    @Value("${thread-pool.batch-refresh-memory.queue-capacity:100}")
    private int batchRefreshMemoryQueueCapacity;

    @Value("${thread-pool.batch-refresh-memory.keep-alive-seconds:60}")
    private int batchRefreshMemoryKeepAliveSeconds;

    @Value("${thread-pool.batch-refresh-memory.await-termination-seconds:60}")
    private int batchRefreshMemoryAwaitTerminationSeconds;

    @Value("${thread-pool.batch-refresh-memory.thread-name-prefix:BatchRefreshMemoryExecutor-}")
    private String batchRefreshMemoryThreadNamePrefix;


    
    private ThreadPoolTaskExecutor createThreadPoolExecutor(
            int corePoolSize,
            int maxPoolSize,
            int queueCapacity,
            int keepAliveSeconds,
            int awaitTerminationSeconds,
            String threadNamePrefix) {

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setKeepAliveSeconds(keepAliveSeconds);
        executor.setThreadNamePrefix(threadNamePrefix);

        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(awaitTerminationSeconds);

        
        executor.setThreadFactory(r -> {
            Thread thread = new Thread(r);
            thread.setDaemon(false);
            return thread;
        });

        
        executor.initialize();

        return executor;
    }


    
    @Bean(name = "afterChatAsyncTaskExecutor")
    public ThreadPoolTaskExecutor afterChatThreadPool() {
        return createThreadPoolExecutor(
                afterChatCorePoolSize,
                afterChatMaxPoolSize,
                afterChatQueueCapacity,
                afterChatKeepAliveSeconds,
                afterChatAwaitTerminationSeconds,
                afterChatThreadNamePrefix
        );
    }

    
    @Bean(name = "importConversationThreadPool")
    public ThreadPoolTaskExecutor importConversationThreadPool() {
        return createThreadPoolExecutor(
                importConversationCorePoolSize,
                importConversationMaxPoolSize,
                importConversationQueueCapacity,
                importConversationKeepAliveSeconds,
                importConversationAwaitTerminationSeconds,
                importConversationThreadNamePrefix
        );
    }

    
    @Bean(name = "batchRefreshMemoryExecutor")
    public ThreadPoolTaskExecutor batchRefreshMemoryThreadPool() {
        return createThreadPoolExecutor(
                batchRefreshMemoryCorePoolSize,
                batchRefreshMemoryMaxPoolSize,
                batchRefreshMemoryQueueCapacity,
                batchRefreshMemoryKeepAliveSeconds,
                batchRefreshMemoryAwaitTerminationSeconds,
                batchRefreshMemoryThreadNamePrefix
        );
    }

    
    @Bean(name = "commonThreadPool")
    public ThreadPoolTaskExecutor commonThreadPool() {
        return createThreadPoolExecutor(
                commonCorePoolSize,
                commonMaxPoolSize,
                commonQueueCapacity,
                commonKeepAliveSeconds,
                commonAwaitTerminationSeconds,
                "Common-Executor-"
        );
    }
}