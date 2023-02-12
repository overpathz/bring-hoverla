# Bring

## What is Bring?


**Bring is a dependency injection framework**.
It uses IoC (Inversion of Control) container, which is represented by ApplicationContext where beans are managed. The framework can create beans for you and inject them as dependencies to other beans

## Quick Start


To install Bring locally you should:

* clone repo ```https://github.com/overpathz/bring-hoverla```
* go to the root of Bring project ```cd <path_to_bring>/Bring```
* build jar with ```mvn clean install -DskipTests```
* add jar to your maven project

```
<dependency>
    <groupId>com.bobocode.hoverla</groupId>
    <artifactId>bring</artifactId>
    <version>1.0-SNAPSHOT</version> 
</dependency>
```
To easily start an application with Bring, use the BringApplication.loadContext("package.to.scan") method

```java
import com.bobocode.hoverla.bring.BringApplication;
import com.bobocode.hoverla.bring.context.ApplicationContext;

public class DemoApp {
    public static void main(String[] args) {
        ApplicationContext applicationContext = BringApplication.loadContext("packages.to.scan"); // provide packages to be scanned for beans 
    }
}
```

If you need to customize loading of ApplicationContext, you can do it with ApplicationContextBuilder. Here's an example of its usage:

```java
import ch.qos.logback.classic.Level;
import com.bobocode.hoverla.bring.BringApplication;
import com.bobocode.hoverla.bring.context.ApplicationContext;

public class DemoApp {
    public static void main(String[] args) {
        ApplicationContext applicationContext = BringApplication.getContextBuilder()
                .logLevel(Level.DEBUG)                  // provide logging level
                .packagesToScan("packages.to.scan")     // provide packages to be scanned for beans
                .build();
    }
}
```

---
Creation components of ApplicationContext:

* BeanDefinitionMapper. Using to create BeanDefinition.
* BeanAnnotationScanner. Using for scan packages to find classes annotated with Bean and create it at ApplicationContext.
* BeanDependencyNameResolver. Its util class using to resolve BeanDependency names.
* BeanInitializer. Using for initialize beans.
---
Main API (annotations):
* Autowired
* Bean
* Configuration
* Primary
* Value
