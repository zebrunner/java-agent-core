package com.zebrunner.agent.core.webdriver;

import com.zebrunner.agent.core.config.ConfigurationHolder;
import com.zebrunner.agent.core.registrar.TestSessionRegistrar;
import com.zebrunner.agent.core.registrar.descriptor.SessionStartDescriptor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.implementation.bind.annotation.Argument;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;
import net.bytebuddy.implementation.bind.annotation.This;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.UsernameAndPassword;
import org.openqa.selenium.remote.HttpCommandExecutor;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.http.ClientConfig;
import org.openqa.selenium.remote.http.netty.NettyClient;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.function.Consumer;

@Slf4j
public class StartSessionInterceptor {

    private static final TestSessionRegistrar REGISTRAR = TestSessionRegistrar.getInstance();
    private static final CapabilitiesCustomizerChain CAPABILITIES_CUSTOMIZER_CHAIN = CapabilitiesCustomizerChain.getInstance();
    private static final ThreadLocal<Consumer<SessionRegisterDescriptor>> SESSION_REGISTER_DESCRIPTOR_CONSUMER = new ThreadLocal<>();

    public static void setSessionRegisterConsumer(Consumer<SessionRegisterDescriptor> consumer) {
        SESSION_REGISTER_DESCRIPTOR_CONSUMER.set(consumer);
    }

    @RuntimeType
    public static void onSessionStart(@This RemoteWebDriver driver,
                                      @SuperCall Runnable methodInvocationProxy,
                                      @Argument(0) Capabilities capabilities) throws Exception {
        if (ConfigurationHolder.shouldSubstituteRemoteWebDrivers()) {
            substituteSeleniumHub(driver);
            capabilities = customizeCapabilities(methodInvocationProxy, capabilities);
        }

        SessionRegisterDescriptor sessionRegisterDescriptor = new SessionRegisterDescriptor();
        sessionRegisterDescriptor.setSessionStartDescriptor(SessionStartDescriptor.initiatedWith(capabilities.asMap()));

        try {
            methodInvocationProxy.run();

            String sessionId = driver.getSessionId().toString();
            if (sessionId.length() >= 64) {
                // use case with GoGridRouter so we have to cut first 32 symbols!
                // have no idea what it actually means, but Vadim Delendik can provide more information
                sessionId = sessionId.substring(32);
            }

            Capabilities driverCapabilities = driver.getCapabilities();
            // ChromeDriver, ChromiumDriver, FirefoxDriver has its own capabilities field, but at the current stage they are null,
            // so we try to get capabilities from RemoteWebDriver forcibly
            if (driverCapabilities == null) {
                Field capabilitiesField = Arrays.stream(RemoteWebDriver.class.getDeclaredFields())
                        .filter(field -> Capabilities.class.equals(field.getType()))
                        .peek(field -> field.setAccessible(true))
                        .findFirst()
                        .orElseThrow(() -> new NoSuchFieldException("Cannot find RemoteWebDriver capabilities field"));

                driverCapabilities = (Capabilities) capabilitiesField.get(driver);
            }

            sessionRegisterDescriptor.getSessionStartDescriptor()
                    .successfullyStartedWith(sessionId, driverCapabilities.asMap());
        } catch (Exception e) {
            sessionRegisterDescriptor.setException(e);
            StringWriter errorMessageStringWriter = new StringWriter();
            e.printStackTrace(new PrintWriter(errorMessageStringWriter));
            sessionRegisterDescriptor.getSessionStartDescriptor()
                    .failedToStart(errorMessageStringWriter.toString());
            throw e;
        } finally {
            if (SESSION_REGISTER_DESCRIPTOR_CONSUMER.get() == null) {
                REGISTRAR.registerStart(sessionRegisterDescriptor.getSessionStartDescriptor());
            } else {
                Consumer<SessionRegisterDescriptor> consumer = SESSION_REGISTER_DESCRIPTOR_CONSUMER.get();
                SESSION_REGISTER_DESCRIPTOR_CONSUMER.remove();
                sessionRegisterDescriptor.setTestSessionRegistrar(REGISTRAR);
                consumer.accept(sessionRegisterDescriptor);
            }
        }
    }

    private static void substituteSeleniumHub(RemoteWebDriver driver) throws NoSuchFieldException, IllegalAccessException, URISyntaxException {
        URL seleniumHubUrl = RemoteWebDriverFactory.getSeleniumHubUrl();
        if (driver.getCommandExecutor() instanceof HttpCommandExecutor && seleniumHubUrl != null) {
            log.debug("Selenium Hub URL will be substituted by the value provided from Zebrunner.");

            HttpCommandExecutor commandExecutor = (HttpCommandExecutor) driver.getCommandExecutor();
            setFieldValue(commandExecutor, "remoteServer", seleniumHubUrl);

            Object clientObject = getFieldValue(commandExecutor, "client");
            if (clientObject instanceof NettyClient) {
                String userInfo = seleniumHubUrl.getUserInfo();
                if (userInfo != null && !userInfo.isEmpty()) {
                    String[] credentials = userInfo.split(":", 2);
                    String username = credentials[0];
                    String password = credentials.length > 1 ? credentials[1] : "";

                    ClientConfig clientConfig = ((ClientConfig) getFieldValue(clientObject, "config"))
                            .baseUri(seleniumHubUrl.toURI())
                            .authenticateAs(new UsernameAndPassword(username, password));
                    setFieldValue(clientObject, "config", clientConfig);
                }
            } else {
                log.debug("Could not substitute address of remote selenium hub because of unknown http client.");
            }
        }
    }

    private static Object getFieldValue(Object targetObject, String fieldName) throws NoSuchFieldException, IllegalAccessException {
        Field remoteServer = findField(targetObject.getClass(), fieldName);
        remoteServer.setAccessible(true);
        Object value = remoteServer.get(targetObject);
        remoteServer.setAccessible(false);
        return value;
    }

    private static void setFieldValue(Object targetObject, String fieldName, Object value) throws NoSuchFieldException, IllegalAccessException {
        Field remoteServer = findField(targetObject.getClass(), fieldName);
        remoteServer.setAccessible(true);
        remoteServer.set(targetObject, value);
        remoteServer.setAccessible(false);
    }

    private static Field findField(Class<?> targetClass, String fieldName) throws NoSuchFieldException {
        try {
            return targetClass.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            Class<?> superclass = targetClass.getSuperclass();
            if (superclass != Object.class) {
                return findField(superclass, fieldName);
            }
            throw e;
        }
    }

    private static Capabilities customizeCapabilities(Runnable methodInvocationProxy, Capabilities capabilities) {
        Class<? extends Runnable> methodInvocationProxyClass = methodInvocationProxy.getClass();
        log.debug("Class of the #startSession() invocation proxy is {}", methodInvocationProxyClass.getName());

        try {
            // this field should be the only argument of #startSession()
            Field argument1Field = methodInvocationProxyClass.getDeclaredField("argument1");
            argument1Field.setAccessible(true);

            Object methodArgument = argument1Field.get(methodInvocationProxy);
            if (methodArgument instanceof Capabilities) {
                capabilities = (Capabilities) methodArgument;
                capabilities = CAPABILITIES_CUSTOMIZER_CHAIN.customize(capabilities);

                argument1Field.set(methodInvocationProxy, capabilities);
                argument1Field.setAccessible(false);
            } else {
                log.debug("#startSession() argument has unexpected type, thus it will not be modified. " +
                        "Capabilities from Zebrunner will not be taken into account.");
            }
        } catch (NoSuchFieldException e) {
            log.debug("#startSession() invocation proxy class does not contain an expected field. " +
                    "Capabilities from Zebrunner will not be taken into account.");
        } catch (IllegalAccessException e) {
            log.debug("Could not get access to the original argument of #startSession() method. " +
                    "Capabilities from Zebrunner will not be taken into account.");
        }

        return capabilities;
    }

    @Data
    @NoArgsConstructor
    @RequiredArgsConstructor
    public static class SessionRegisterDescriptor {
        private TestSessionRegistrar testSessionRegistrar;
        private SessionStartDescriptor sessionStartDescriptor;
        private Exception exception;
    }
}
