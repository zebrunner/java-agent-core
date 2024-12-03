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
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.Arrays;

@Slf4j
public class StartSessionInterceptor {

    private static final TestSessionRegistrar REGISTRAR = TestSessionRegistrar.getInstance();
    private static final CapabilitiesCustomizerChain CAPABILITIES_CUSTOMIZER_CHAIN = CapabilitiesCustomizerChain.getInstance();

    @RuntimeType
    public static void onSessionStart(@This RemoteWebDriver driver,
                                      @SuperCall Runnable methodInvocationProxy,
                                      @Argument(0) Capabilities capabilities) throws Exception {
        if (ConfigurationHolder.shouldSubstituteRemoteWebDrivers()) {
            capabilities = customizeCapabilities(methodInvocationProxy, capabilities);
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
}
