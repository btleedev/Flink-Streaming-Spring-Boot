package com.tenble.flink.streaming.springboot;

import lombok.extern.slf4j.Slf4j;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.task.TaskExecutor;

@Slf4j
public class FlinkExecutor {

    private final ApplicationContext appCtx;
    private final TaskExecutor taskExecutor;
    private final FlinkProperties flinkProperties;
    private final StreamExecutionEnvironment flinkEnv;

    FlinkExecutor(ApplicationContext appCtx, TaskExecutor taskExecutor,
        FlinkProperties flinkProperties, StreamExecutionEnvironment flinkEnv) {

        this.appCtx = appCtx;
        this.taskExecutor = taskExecutor;
        this.flinkProperties = flinkProperties;
        this.flinkEnv = flinkEnv;
    }

    @EventListener
    public void handleContextRefresh(ContextRefreshedEvent event) {
        taskExecutor.execute(() -> {    // execute in another thread so we don't hold it up
            try {
                log.info("Running flink job " + flinkProperties.getJobName());
                taskExecutor.execute(this::executeFlinkJob);
                Thread.sleep(flinkProperties.getTerminationGracePeriodMs());
                conditionallyExitSpringApp(0);
            } catch (InterruptedException e) {
                log.error("Failed to submit flink job", e);
                conditionallyExitSpringApp(1);
            }
        });
    }

    private void executeFlinkJob() {
        try {
            flinkEnv.execute(flinkProperties.getJobName());
        } catch (Exception e) {
            log.error("Failed to submit flink job", e);
            conditionallyExitSpringApp(1);
        }
    }

    private void conditionallyExitSpringApp(int exitCode) {
        if (flinkProperties.isTerminate()) {
            log.info("Terminating flink spring application with application code " + exitCode);
            System.exit(SpringApplication.exit(appCtx, (ExitCodeGenerator) () -> exitCode));
        }
    }
}
