
package com.hoverla.bring.context;

import com.hoverla.bring.context.bean.definition.BeanDefinitionMapper;
import com.hoverla.bring.context.bean.dependency.BeanDependencyNameResolver;
import com.hoverla.bring.context.bean.initializer.BeanInitializer;
import com.hoverla.bring.context.bean.scanner.BeanAnnotationScanner;
import com.hoverla.bring.context.fixtures.autowired.success.AutowiredService;
import com.hoverla.bring.context.fixtures.autowired.success.TestService;
import com.hoverla.bring.context.fixtures.bean.ChildService;
import com.hoverla.bring.context.fixtures.bean.NotABean;
import com.hoverla.bring.context.fixtures.bean.ParentService;
import com.hoverla.bring.context.fixtures.bean.success.A;
import com.hoverla.bring.context.fixtures.bean.success.B;
import com.hoverla.bring.context.fixtures.bean.success.ChildServiceBeanOne;
import com.hoverla.bring.context.fixtures.bean.success.ChildServiceBeanTwo;
import com.hoverla.bring.context.fixtures.bean.success.TestBeanWithName;
import com.hoverla.bring.context.fixtures.bean.success.TestBeanWithoutName;
import com.hoverla.bring.context.fixtures.bean.primary.Animal;
import com.hoverla.bring.context.fixtures.bean.primary.AnimalService;
import com.hoverla.bring.context.fixtures.bean.primary.Tiger;
import com.hoverla.bring.context.fixtures.bean.primary.Wolf;
import com.hoverla.bring.context.fixtures.bean.primary.error.AnimalError;
import com.hoverla.bring.context.fixtures.value.success.BeanWithValueAnnotation;
import com.hoverla.bring.exception.MissingDependencyException;
import com.hoverla.bring.exception.NoSuchBeanException;
import com.hoverla.bring.exception.NoUniqueBeanException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AnnotationApplicationContextImplTest {

    private static final String CHILD_SERVICE_BEAN_ONE_NAME = ChildServiceBeanOne.class.getName();
    private static final String CHILD_SERVICE_BEAN_TWO_NAME = "childServiceBean";

    private ApplicationContext applicationContext;

    @Test
    @Order(1)
    @DisplayName("Bean is successfully retrieved from the context by it's type")
    void getBeanByClassReturnsCorrectBean() {
        applicationContext = getApplicationContext("com.hoverla.bring.context.fixtures.bean");
        TestBeanWithName beanWithName = applicationContext.getBean(TestBeanWithName.class);
        assertNotNull(beanWithName);

        TestBeanWithoutName beanWithoutName = applicationContext.getBean(TestBeanWithoutName.class);
        assertNotNull(beanWithoutName);

        A aBean = applicationContext.getBean(A.class);
        assertNotNull(aBean);
    }

    @Test
    @Order(2)
    @DisplayName("NoSuchBeanException is thrown if there is no such bean")
    void getBeanByTypeWhenIfThereIsNoSuchBean() {
        applicationContext = getApplicationContext("com.hoverla.bring.context.fixtures.bean");
        NoSuchBeanException noSuchBeanException =
                assertThrows(NoSuchBeanException.class, () -> applicationContext.getBean(NotABean.class));
        assertEquals("Bean with type NotABean not found", noSuchBeanException.getMessage());
    }

    @Test
    @Order(3)
    @DisplayName("NoUniqueBeanException is thrown if there are > 1 bean od the same type")
    void getBeanByTypeIfThereIsADuplicateBean() {
        applicationContext = getApplicationContext("com.hoverla.bring.context.fixtures.bean");
        NoUniqueBeanException noUniqueBeanException =
                assertThrows(NoUniqueBeanException.class, () -> applicationContext.getBean(ParentService.class));

        assertEquals("There is more than one bean matching the ParentService type: " +
            "[childServiceBean: ChildServiceBeanTwo, " +
            "com.hoverla.bring.context.fixtures.bean.success.ChildServiceBeanOne: ChildServiceBeanOne]." +
            " Please specify a bean name!", noUniqueBeanException.getMessage());
    }

    @Test
    @Order(4)
    @DisplayName("Bean is successfully retrieved from the context by it's name")
    void getBeanByNameReturnsCorrectBean() {
        applicationContext = getApplicationContext("com.hoverla.bring.context.fixtures.bean");
        TestBeanWithName beanWithName = applicationContext.getBean("BeanName", TestBeanWithName.class);
        assertNotNull(beanWithName);

        TestBeanWithoutName beanWithoutName = applicationContext
            .getBean("com.hoverla.bring.context.fixtures.bean.success.TestBeanWithoutName",
                TestBeanWithoutName.class);
        assertNotNull(beanWithoutName);

        B bBean = applicationContext.getBean("C", B.class);
        assertNotNull(bBean);
    }

    @Test
    @Order(5)
    @DisplayName("NoSuchBeanException is thrown if there is no such bean with a name")
    void getBeanByNameIfThereIsNoSuchBean() {
        applicationContext = getApplicationContext("com.hoverla.bring.context.fixtures.bean");
        NoSuchBeanException noSuchBeanException =
                assertThrows(NoSuchBeanException.class, () -> applicationContext.getBean("Ho", TestBeanWithName.class));
        assertEquals("Bean with name Ho and type TestBeanWithName not found", noSuchBeanException.getMessage());

        noSuchBeanException =
                assertThrows(NoSuchBeanException.class, () -> applicationContext.getBean("ver", TestBeanWithoutName.class));
        assertEquals("Bean with name ver and type TestBeanWithoutName not found", noSuchBeanException.getMessage());

        noSuchBeanException =
                assertThrows(NoSuchBeanException.class, () -> applicationContext.getBean("la", NotABean.class));
        assertEquals("Bean with name la and type NotABean not found", noSuchBeanException.getMessage());
    }

    @Test
    @Order(6)
    @DisplayName("Bean is successfully retrieved by it's name and superclass")
    void getBeanByNameAndSuperClassReturnsCorrectBean() {
        applicationContext = getApplicationContext("com.hoverla.bring.context.fixtures.bean");
        ChildServiceBeanOne childServiceBeanOne = (ChildServiceBeanOne) applicationContext
                .getBean(CHILD_SERVICE_BEAN_ONE_NAME, ParentService.class);
        assertNotNull(childServiceBeanOne);

        ChildServiceBeanTwo childServiceBeanTwo = (ChildServiceBeanTwo) applicationContext
                .getBean(CHILD_SERVICE_BEAN_TWO_NAME, ParentService.class);
        assertNotNull(childServiceBeanTwo);
    }

    @Test
    @Order(7)
    @DisplayName("Get all beans by type returns the matching objects")
    void getAllBeansReturnsCorrectMap() {
        applicationContext = getApplicationContext("com.hoverla.bring.context.fixtures.bean");
        Map<String, ParentService> services = applicationContext.getAllBeans(ParentService.class);

        assertEquals(2, services.size());

        assertTrue(services.containsKey(CHILD_SERVICE_BEAN_ONE_NAME));
        assertTrue(services.containsKey(CHILD_SERVICE_BEAN_TWO_NAME));

        ChildServiceBeanOne autowiredService = (ChildServiceBeanOne) services.get(CHILD_SERVICE_BEAN_ONE_NAME);
        ChildServiceBeanTwo testService = (ChildServiceBeanTwo) services.get(CHILD_SERVICE_BEAN_TWO_NAME);
        assertNotNull(autowiredService);
        assertNotNull(testService);

        //ChildService is a subclass of ParentService, but it is not a bean
        assertTrue(ParentService.class.isAssignableFrom(ChildService.class));
        assertFalse(services.containsKey("childService"));
    }

    @Test
    @Order(8)
    @DisplayName("Autowiring has been successful")
    void autowiringFieldIsSetCorrectly() {
        applicationContext =
            getApplicationContext("com.hoverla.bring.context.fixtures.autowired.success");
        AutowiredService autowiredService = applicationContext.getBean(AutowiredService.class);
        assertNotNull(autowiredService);
        TestService testService = applicationContext.getBean(TestService.class);
        assertEquals("A,B,C", testService.getLetters());
    }

    @Test
    @Order(9)
    @DisplayName("MissingDependencyException is thrown if there is no bean which can be autowired")
    void autowiringFieldIfThereIsNoSuchBean() {
        MissingDependencyException exception = assertThrows(MissingDependencyException.class, () ->
            getApplicationContext("com.hoverla.bring.context.fixtures.autowired.nosuchbean"));

        assertEquals("Dependency of type class " +
            "com.hoverla.bring.context.fixtures.autowired.nosuchbean.NotABeanService and name " +
            "com.hoverla.bring.context.fixtures.autowired.nosuchbean.NotABeanService hasn't been found for " +
            "[com.hoverla.bring.context.fixtures.autowired.nosuchbean.NoSuchBeanService] bean", exception.getMessage());
    }

    @Test
    @Order(10)
    @DisplayName("NoUniqueBeanException is thrown if there are > 1 bean of the same type for autowiring")
    void autowiringFieldIThereIsNoUniqueBean() {
        NoUniqueBeanException exception = assertThrows(NoUniqueBeanException.class, () ->
            getApplicationContext("com.hoverla.bring.context.fixtures.autowired.nouniquebean"));

        assertEquals("There is more than one bean matching the JustAnotherService type: " +
            "[com.hoverla.bring.context.fixtures.autowired.nouniquebean.ChildServiceTwo: ChildServiceTwo, " +
            "com.hoverla.bring.context.fixtures.autowired.nouniquebean.ChildServiceOne: ChildServiceOne]." +
            " Please specify a bean name!", exception.getMessage());
    }
    @Test
    @Order(11)
    @DisplayName("MissingDependencyException if no default constructor is found and the dependency can't be found for another one")
    void applicationContextInitializationExceptionIfPackageDoesNotExist() {
        MissingDependencyException e = assertThrows(MissingDependencyException.class, () ->
            getApplicationContext("com.hoverla.bring.context.fixtures.initFailure"));

        assertEquals("Dependency of type class java.lang.String and name java.lang.String hasn't been found" +
            " for [com.hoverla.bring.context.fixtures.initFailure.ClassWithoutDefaultConstructor] bean", e.getMessage());
    }

    @Test
    @Order(12)
    @DisplayName("Properties with value annotation should be initialized from application.properties file")
    void initializePropertiesWithValueAnnotation() {
        applicationContext =
            getApplicationContext("com.hoverla.bring.context.fixtures.value.success");
        BeanWithValueAnnotation valueBean = applicationContext.getBean("beanWithValue", BeanWithValueAnnotation.class);
        assertNotNull(valueBean);
        assertEquals("Value message", valueBean.getValueMessage());
        assertEquals("My message", valueBean.getMessage());
    }

    @Test
    @Order(13)
    @DisplayName("MissingDependencyException should be thrown if we have one or more non default constructor with no beans to autowire")
    void throwNoDEfaultConstructor() {
        MissingDependencyException exception = assertThrows(MissingDependencyException.class, () ->
            getApplicationContext("com.hoverla.bring.context.fixtures.value.fail"));

        assertEquals("Dependency of type int and name int hasn't been found for " +
            "[com.hoverla.bring.context.fixtures.value.fail.BeanWithOneDeclaredConstructor] bean", exception.getMessage());
    }

    @Test
    @Order(14)
    @DisplayName("If the primary bean exists all interface sub-beans should be created")
    void allBeansShouldAvailableIfPrimaryBeanExist() {
        ApplicationContext applicationContext = getApplicationContext("com.hoverla.bring.context.fixtures.bean.primary");
        Animal primaryBean = applicationContext.getBean(Animal.class);
        Wolf wolf = applicationContext.getBean(Wolf.class);
        Tiger tiger = applicationContext.getBean(Tiger.class);

        assertNotNull(primaryBean);
        assertNotNull(wolf);
        assertNotNull(tiger);
        assertEquals(100, primaryBean.strongPoints());
        assertEquals(100, tiger.strongPoints());
        assertEquals(70, wolf.strongPoints());
    }

    @Test
    @Order(15)
    @DisplayName("If two beans with one parent are declared in context it should inject and return the primary bean if present")
    void shouldReturnPrimaryBean() {
        ApplicationContext applicationContext = getApplicationContext("com.hoverla.bring.context.fixtures.bean.primary");
        Animal primaryBean = applicationContext.getBean(Animal.class);

        assertNotNull(primaryBean);
        assertEquals(100, primaryBean.strongPoints());
    }

    @Test
    @Order(16)
    @DisplayName("If annotation @Primary exists should inject the correct bean")
    void shouldReturnPrimaryBeanInAutowiredField() {
        ApplicationContext applicationContext = getApplicationContext("com.hoverla.bring.context.fixtures.bean.primary");
        AnimalService animalService = applicationContext.getBean(AnimalService.class);
        Class<? extends Animal> aClass = animalService.animal.getClass();

        assertNotNull(animalService);
        assertEquals(Tiger.class, aClass);
    }

    @Test
    @Order(17)
    @DisplayName("Should throw an exception if @Primary annotation exists more than one time exists")
    void shouldThrowErrorIfPrimaryAnnotationMoreThanOneTimeExists() {
        ApplicationContext applicationContext = getApplicationContext("com.hoverla.bring.context.fixtures.bean.primary.error");
        assertThrows(NoUniqueBeanException.class, () -> applicationContext.getBean(AnimalError.class));
    }

    private ApplicationContext getApplicationContext(String packageToScan) {
        return new AnnotationApplicationContextImpl(
            List.of(new BeanAnnotationScanner(new BeanDefinitionMapper(), packageToScan)),
            new BeanInitializer(new BeanDependencyNameResolver()));
    }
}

