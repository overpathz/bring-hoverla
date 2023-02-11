package com.hoverla.bring.common;

public class StringConstants {
    private StringConstants() {

    }
    //Base Bring package
    public static final String BASE_BRING_PACKAGE = "com.hoverla.bring";

    //Error messages
    public static final String BEAN_CLASS_ERROR_MESSAGE = "Bean class cannot be null";
    public static final String CONFIGURATION_CLASS_INSTANCE_ERROR_MESSAGE = "Configuration class instance can't be null";
    public static final String CONFIGURATION_BEAN_METHOD_ERROR_MESSAGE = "Configuration bean method can't be null";

    //Error from Reflection API
    //Create messages to give more detail about throwing exception from Reflection library
    public static final String CAN_NOT_GET_FIELD = "Can not get field: %s by instance: %s";
    public static final String CAN_NOT_SET_FIELD = "Can not set field: %s by instance: %s";
    public static final String CAN_NOT_CREATE_INSTANCE = "Can not create instance for constructor: %s";
    public static final String CAN_NOT_CREATE_INSTANCE_WITH_ARGUMENTS = "Can not create instance for constructor: %s with arguments: %s";

    //NoUniqueBeanException
    public static final String NO_UNIQUE_BEAN_EXCEPTION = "There is more than one bean matching the %s type: [%s]. " +
            "Please specify a bean name!";
    public static final String NO_UNIQUE_PRIMARY_BEAN_EXCEPTION = "More than one 'primary' bean found among candidates: %s";

    //NoSuchBeanException
    public static final String NO_SUCH_BEAN_EXCEPTION_BY_TYPE = "Bean with type %s not found";
    public static final String NO_SUCH_BEAN_EXCEPTION_BY_NAME_TYPE = "Bean with name %s and type %s not found";

    //MissingDependencyException
    public static final String MISSING_DEPENDENCY_EXCEPTION = "Dependency of type %s and name %s hasn't been found for [%s] bean";


    //InvokeMethodException
    public static final String INVOKE_METHOD_EXCEPTION = "Can't invoke '%s' method";

    //DefaultConstructorNotFoundException
    public static final String DEFAULT_CONSTRUCTOR_NOT_FOUND_EXCEPTION = "Default constructor hasn't been found for %s";

    //BeanInstanceCreationException
    public static final String BEAN_INSTANCE_CREATION_EXCEPTION = "Bean with name '%s' can't be instantiated";
    public static final String BEAN_INSTANCE_CREATION_SAME_CANDIDATE_EXCEPTION = "Bean named `%s` has a supertype and one of its subtypes" +
            " in dependencies and they have the same candidate for injection";

    //BeanInitializePhaseException
    public static final String CAN_NOT_INITIALIZE_BEANS_EXCEPTION = "Can't initialize beans";

    //BeanDependencyInjectionException
    public static final String BEAN_HAS_NO_MATCHES_DEPENDENCY_EXCEPTION = "'%s' bean has no dependency that matches parameter '%s'";
    public static final String BEAN_DEPENDENCY_INJECTION_EXCEPTION = "Field injection failed for bean instance of type %s. Unresolved fields: %s";
}