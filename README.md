# A java client for docker registry api v2

### Usage：
```java
RegistryClient.authBasic("localhost:5000", "admin", "123456");

RegistryClient.authDockerHub("DOCKER_USERNAME", "DOCKER_PASSWORD");

RegistryClient.push("C:\\tmp\\docker.tar", "localhost:5000/test:v3");

RegistryClient.pull("localhost:5000/test:v1", "C:\\tmp\\docker2.tar");

RegistryClient.copy("localhost:5000/test:v1", "localhost:5000/test2:v1");

RegistryClient.digest("localhost:5000/test:v1");

RegistryClient.delete("localhost:5000/test@sha256:b8604a3fe8543c9e6afc29550de05b36cd162a97aa9b2833864ea8a5be11f3e2");

List<String> tags = RegistryClient.tags("registry");
```
maven
```xml
<dependency>
  <groupId>io.github.ya-b</groupId>
  <artifactId>registry-client</artifactId>
  <version>1.0.0</version>
</dependency>
```