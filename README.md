# Flink Streaming Spring Boot

The application submits a Flink job via Flink's remote environment. The job is submitted once the spring boot server starts up.

## How to run

See `com.tenble.flink.streaming.springboot.FlinkStreamingSpringBootTest` as an example.

1. Import this project as a dependency for another project.
2. Add `@EnableAutoConfigure` to your spring boot app.
3. During spring's context loading phase, autowire the bean `StreamExecutionEnvironment flinkEnvironment` in another bean and add operations to the environment.
4. Copy the shade plugin step in `pom.xml` of this project to create an executable spring boot jar. See `<transformer implementation="org.apache.maven.plugins.shade.resource.ManifestResourceTransformer" />` to specify a mainClass.  
5. Run `mvn clean package` on your project, which will create an executable spring boot application, with all dependencies put onto the classpath. Can be found in the `target` directory.
6. Run this jar using `java -jar ${JAR_FILE}`. Specify any spring property overrides as jvm or program arguments. 

## Properties
```
flink-properties:
  job-name: "FlinkStreamingSpringBoot"                              # flink job name
  job-manager-url: "localhost"                                      # hostname for the flink job manager
  job-manager-port: 8081                                            # REST port of the flink job manager
  remote-env-jar-files:                                             # any jars to upload to the flink job manager alongside your job
    - "target/flink-streaming-spring-boot-0.0.1-SNAPSHOT.jar"
  max-client-rest-request-size-bytes: 2000000000                    # maximum job size for the REST request to restrict on the client in bytes
  terminate: true                                                   # terminate application after the allocated time has passed
  termination-grace-period-ms: 120000                               # amount of time to wait before this spring boot application terminates
```

## Kubernetes files

The files in the folder `k8s/` are slightly modified files from Flink's [Kubernetes Setup](https://ci.apache.org/projects/flink/flink-docs-stable/ops/deployment/kubernetes.html) page. 
They have been modified to increase the maximum allowable rest request size.

## Caveats

1. Java API for Flink does not have an option to use detached mode, which is why the `termination-grace-period-ms` is necessary.