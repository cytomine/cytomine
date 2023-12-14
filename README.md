# A java client for docker registry api v2

### Usage：
```java
RegistryClient.authBasic("localhost:5000", "admin", "123456");

RegistryClient.config("http" , "localhost" , "5000");

RegistryClient.authenticate("user" , "password");

RegistryClient.authDockerHub("DOCKER_USERNAME", "DOCKER_PASSWORD");

RegistryClient.push("C:\\tmp\\docker.tar", "test:v3");

RegistryClient.pull("test:v1", "C:\\tmp\\docker2.tar");

RegistryClient.copy("test:v1", "test2:v1");

RegistryClient.digest("test:v1");

RegistryClient.delete("test@sha256:b8604a3fe8543c9e6afc29550de05b36cd162a97aa9b2833864ea8a5be11f3e2");

List<String> tags = RegistryClient.tags("registry");
```
maven
```xml
<dependency>
    <groupId>com.cytomine</groupId>
    <artifactId>registry-client</artifactId>
    <version>1.0.0</version>
</dependency>
```