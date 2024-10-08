plugins {
    `java-library`
}


java {
    toolchain {
        languageVersion.set(org.gradle.jvm.toolchain.JavaLanguageVersion.of(11))
    }
}

dependencies {
    compileOnly("com.inductiveautomation.ignitionsdk:ignition-common:${rootProject.extra["sdk_version"]}")
    compileOnly("com.inductiveautomation.ignitionsdk:gateway-api:${rootProject.extra["sdk_version"]}")
    compileOnly(project(":common"))
    // add gateway scoped dependencies here
    // https://mvnrepository.com/artifact/com.rabbitmq/amqp-client
    modlImplementation("com.rabbitmq:amqp-client:5.21.0")
}
