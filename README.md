# Bring

---

## What is Bring?

---
**Bring is a dependency injection framework**. It uses IoC (Inversion of Control) container, which is represented by
[ApplicationContext](src/main/java/com/bobocode/hoverla/bring/context/ApplicationContext.java),
where [beans](#bean) are managed. The framework can create beans for you and inject them as dependencies to other beans

## Quick Start

---
To install Bring locally you should:

* clone repo ```https://github.com/hoverla-bobocode/Bring.git```
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

```java
ApplicationContext it's main API for working with bean's at runtime.
Example:

private ApplicationContext getApplicationContext(String packageToScan) {
	return new AnnotationApplicationContextImpl(List.of(new BeanAnnotationScanner(new BeanDefinitionMapper(), packageToScan)), new BeanInitializer(new BeanDependencyNameResolver()));
}
```

