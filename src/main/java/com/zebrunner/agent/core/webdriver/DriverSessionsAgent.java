package com.zebrunner.agent.core.webdriver;

import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.pool.TypePool;

import java.lang.instrument.Instrumentation;

import static net.bytebuddy.implementation.MethodDelegation.to;
import static net.bytebuddy.matcher.ElementMatchers.isPublic;
import static net.bytebuddy.matcher.ElementMatchers.isStatic;
import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.namedOneOf;
import static net.bytebuddy.matcher.ElementMatchers.not;

@Slf4j
public class DriverSessionsAgent {

    private static final String REMOTE_WEB_DRIVER_CLASS_MAME = "org.openqa.selenium.remote.RemoteWebDriver";

    private static final String START_SESSION_METHOD_MAME = "startSession";
    private static final String QUIT_METHOD_MAME = "quit";
    private static final String CLOSE_METHOD_MAME = "close";

    // getSessionId and getCapabilities are used by the agent interceptors
    private static final String[] PUBLIC_METHODS_TO_NOT_INTERCEPT = {
            START_SESSION_METHOD_MAME, QUIT_METHOD_MAME, CLOSE_METHOD_MAME, "getSessionId", "getCapabilities",
            "wait", "equals", "hashCode", "getClass", "notify", "notifyAll", "toString"
    };
    // the rest of the public methods
    // setFileDetector, getErrorHandler, setErrorHandler, getCommandExecutor, getTitle, getCurrentUrl, getScreenshotAs,
    // findElements, findElement, findElementById, findElementsById, findElementByLinkText, findElementsByLinkText,
    // findElementByPartialLinkText, findElementsByPartialLinkText, findElementByTagName, findElementsByTagName,
    // findElementByName, findElementsByName, findElementByClassName, findElementsByClassName, findElementByCssSelector,
    // findElementsByCssSelector, findElementByXPath, findElementsByXPath, getPageSource, getWindowHandles,
    // getWindowHandle, executeScript, executeAsyncScript, switchTo, navigate, manage, setLogLevel, perform,
    // resetInputState, getKeyboard, getMouse, getFileDetector, get

    public static void premain(String args, Instrumentation instrumentation) {
        log.debug("Web sessions agent starts to add interceptors for RemoteWebDriver");
        new AgentBuilder.Default()
                .with(new AgentBuilder.InitializationStrategy.SelfInjection.Eager())
                .type(named(REMOTE_WEB_DRIVER_CLASS_MAME))
                .transform((builder, type, classloader, module) -> addInterceptors(builder))
                .installOn(instrumentation);
    }

    private static DynamicType.Builder<?> addInterceptors(DynamicType.Builder<?> builder) {
        return builder.method(isPublic().and(not(isStatic())).and(not(namedOneOf(PUBLIC_METHODS_TO_NOT_INTERCEPT))))
                      .intercept(to(publicMethodsInterceptor()))
                      .method(named(START_SESSION_METHOD_MAME))
                      .intercept(to(startSessionInterceptor()))
                      .method(named(QUIT_METHOD_MAME))
                      .intercept(to(closeSessionInterceptor()))
                      .method(named(CLOSE_METHOD_MAME))
                      .intercept(to(closeSessionInterceptor()));
    }

    private static TypeDescription publicMethodsInterceptor() {
        log.debug("Creating interceptor for public methods.");
        return TypePool.Default.ofSystemLoader()
                               .describe(PublicMethodInvocationInterceptor.class.getName())
                               .resolve();
    }

    private static TypeDescription startSessionInterceptor() {
        log.debug("Creating interceptor for 'start' method.");
        return TypePool.Default.ofSystemLoader()
                               .describe(StartSessionInterceptor.class.getName())
                               .resolve();
    }

    private static TypeDescription closeSessionInterceptor() {
        log.debug("Creating interceptor for 'quit' and 'close' methods.");
        return TypePool.Default.ofSystemLoader()
                               .describe(CloseSessionInterceptor.class.getName())
                               .resolve();
    }

}
