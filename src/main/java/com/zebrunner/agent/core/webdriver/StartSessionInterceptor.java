package com.zebrunner.agent.core.webdriver;

import com.zebrunner.agent.core.config.ConfigurationHolder;
import com.zebrunner.agent.core.registrar.TestSessionRegistrar;
import com.zebrunner.agent.core.registrar.descriptor.SessionStartDescriptor;
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

@Slf4j
public class StartSessionInterceptor {

    private static final TestSessionRegistrar REGISTRAR = TestSessionRegistrar.getInstance();

    @RuntimeType
    public static void onSessionStart(@This RemoteWebDriver driver,
                                      @SuperCall Runnable methodInvocationProxy,
                                      @Argument(0) Capabilities capabilities) throws Exception {
        if (ConfigurationHolder.shouldSubstituteRemoteWebDrivers()) {
            substituteSeleniumHub(driver);
            capabilities = mergeZebrunnerCapabilities(methodInvocationProxy, capabilities);
        }

        SessionStartDescriptor startDescriptor = SessionStartDescriptor.initiatedWith(capabilities.asMap());
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

            startDescriptor.successfullyStartedWith(sessionId, driverCapabilities.asMap());
        } catch (Exception e) {
            StringWriter errorMessageStringWriter = new StringWriter();
            e.printStackTrace(new PrintWriter(errorMessageStringWriter));
            startDescriptor.failedToStart(errorMessageStringWriter.toString());
            throw e;
        } finally {
            REGISTRAR.registerStart(startDescriptor);
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

    private static Capabilities mergeZebrunnerCapabilities(Runnable methodInvocationProxy, Capabilities capabilities) {
        Class<? extends Runnable> methodInvocationProxyClass = methodInvocationProxy.getClass();
        log.debug("Class of the #startSession() invocation proxy is {}", methodInvocationProxyClass.getName());

        try {
            // this field should be the only argument of #startSession()
            Field argument1Field = methodInvocationProxyClass.getDeclaredField("argument1");
            argument1Field.setAccessible(true);

            Object methodArgument = argument1Field.get(methodInvocationProxy);
            if (methodArgument instanceof Capabilities) {
                capabilities = (Capabilities) methodArgument;
                Capabilities zebrunnerCapabilities = RemoteWebDriverFactory.getCapabilities();

                if (!zebrunnerCapabilities.asMap().isEmpty()) {
                    log.debug("Capabilities will be modified with the values provided from Zebrunner.");

                    capabilities = capabilities.merge(zebrunnerCapabilities);
                    argument1Field.set(methodInvocationProxy, capabilities);
                }

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

}
