package com.tenble.flink.streaming.springboot;

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;

@Configuration
@EnableConfigurationProperties(FlinkProperties.class)
public class FlinkAutoConfiguration {

    @Bean
    FlinkExecutor flinkExecutor(ApplicationContext appCtx, TaskExecutor taskExecutor,
        FlinkProperties flinkProperties, StreamExecutionEnvironment flinkEnvironment) {

        return new FlinkExecutor(appCtx, taskExecutor, flinkProperties, flinkEnvironment);
    }

    @Bean("taskExecutor")
    TaskExecutor taskExecutor() {
        return new SimpleAsyncTaskExecutor();
    }

    @Bean("flinkEnvironment")
    StreamExecutionEnvironment getFlinkEnvironment(FlinkProperties flinkProperties) {
        long maxBytes = flinkProperties.getMaxClientRestRequestSizeBytes();
        org.apache.flink.configuration.Configuration config = new org.apache.flink.configuration.Configuration();
        config.setString("rest.address", flinkProperties.getJobManagerUrl());
        config.setInteger("rest.port", flinkProperties.getJobManagerPort());
        config.setLong("rest.client.max-content-length", maxBytes);
        config.setLong("rest.server.max-content-length", maxBytes);
        config.setString("akka.framesize", maxBytes + "b");

        return StreamExecutionEnvironment.createRemoteEnvironment(
            flinkProperties.getJobManagerUrl(),
            flinkProperties.getJobManagerPort(),
            config,
            flinkProperties.getRemoteEnvJarFiles().stream().toArray(String[]::new));
    }
}
