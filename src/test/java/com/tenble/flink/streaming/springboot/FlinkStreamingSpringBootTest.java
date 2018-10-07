package com.tenble.flink.streaming.springboot;

import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeutils.base.IntSerializer;
import org.apache.flink.api.common.typeutils.base.StringSerializer;
import org.apache.flink.core.fs.FileSystem;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.FromElementsFunction;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
    FlinkStreamingSpringBootTest.class,
    FlinkStreamingSpringBootTest.FlinkStreamingSpringBootTestPropertiesConfiguration.class,
    FlinkStreamingSpringBootTest.FlinkStreamingSpringBootTestConfiguration.class
})
@ComponentScan("com.tenble.flink.streaming.springboot")
public class FlinkStreamingSpringBootTest {

    @TestConfiguration
    static class FlinkStreamingSpringBootTestPropertiesConfiguration {

        @Bean
        String outputFileName() {
            return "target/FlinkStreamingSpringBootTest.txt";
        }
    }

    @TestConfiguration
    static class FlinkStreamingSpringBootTestConfiguration {

        @Bean("flinkEnvironment")
        @Primary
        StreamExecutionEnvironment getFlinkEnvironment(FlinkProperties flinkProperties) {
            return StreamExecutionEnvironment.createLocalEnvironment();
        }

        @Autowired
        void populateEnv(StreamExecutionEnvironment flinkEnvironment, String outputFileName)
            throws IOException {
            flinkEnvironment
                .addSource(
                    new FromElementsFunction<>(new IntSerializer(), 1, 2, 3),
                    TypeInformation.of(Integer.class))
                .filter((Integer i) -> i % 2 == 0)
                .writeAsText(outputFileName, FileSystem.WriteMode.OVERWRITE)
                .setParallelism(1);
        }
    }

    @Autowired
    FlinkProperties flinkProperties;

    @Autowired
    String outputFileName;

    @Test
    public void localStreamExecution() throws InterruptedException, IOException {
        Thread.sleep(flinkProperties.getTerminationGracePeriodMs() / 2); // fixme

        String outputFileText = new String(Files.readAllBytes(Paths.get(outputFileName))).trim();
        assertThat(outputFileText).isEqualTo("2");
    }

}
