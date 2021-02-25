package com.zebrunner.agent.core.webdriver;

import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.NameMatcher;
import net.bytebuddy.pool.TypePool;

import java.lang.instrument.Instrumentation;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static net.bytebuddy.implementation.MethodDelegation.to;
import static net.bytebuddy.matcher.ElementMatchers.isPublic;
import static net.bytebuddy.matcher.ElementMatchers.isStatic;
import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.not;

@Slf4j
public class DriverSessionsAgent {

    private static final String REMOTE_WEB_DRIVER_CLASS_MAME = "org.openqa.selenium.remote.RemoteWebDriver";

    private static final String START_SESSION_METHOD_MAME = "startSession";
    private static final String QUIT_METHOD_MAME = "quit";

    // getSessionId and getCapabilities are used by the agent interceptors
    private static final Set<String> PUBLIC_METHODS_TO_NOT_INTERCEPT = new HashSet<>(Arrays.asList(
            START_SESSION_METHOD_MAME, QUIT_METHOD_MAME, "getSessionId", "getCapabilities",
            "wait", "equals", "hashCode", "getClass", "notify", "notifyAll", "toString"
    ));
    // the rest of the public methods
    // setFileDetector, getErrorHandler, setErrorHandler, getCommandExecutor, getTitle, getCurrentUrl, getScreenshotAs,
    // findElements, findElement, findElementById, findElementsById, findElementByLinkText, findElementsByLinkText,
    // findElementByPartialLinkText, findElementsByPartialLinkText, findElementByTagName, findElementsByTagName,
    // findElementByName, findElementsByName, findElementByClassName, findElementsByClassName, findElementByCssSelector,
    // findElementsByCssSelector, findElementByXPath, findElementsByXPath, getPageSource, getWindowHandles,
    // getWindowHandle, executeScript, executeAsyncScript, switchTo, navigate, manage, setLogLevel, perform,
    // resetInputState, getKeyboard, getMouse, getFileDetector, get, close

    public static void premain(String args, Instrumentation instrumentation) {
        try {
            log.info("Zebrunner driver sessions agent is enabled.");
            new AgentBuilder.Default()
                    .with(new AgentBuilder.InitializationStrategy.SelfInjection.Eager())
                    .type(named(REMOTE_WEB_DRIVER_CLASS_MAME))
                    .transform((builder, type, classloader, module) -> addInterceptors(builder))
                    .installOn(instrumentation);
        } catch (Exception e) {
            log.error("Could not add interceptors for RemoteWebDriver", e);
        }
    }

    public static ElementMatcher<? super MethodDescription> isPublicMethodToIntercept() {
        return isPublic()
                .and(not(isStatic()))
                .and(not(new NameMatcher<>(PUBLIC_METHODS_TO_NOT_INTERCEPT::contains)));
    }

    private static DynamicType.Builder<?> addInterceptors(DynamicType.Builder<?> builder) {
        log.info("Zebrunner driver sessions agent is adding interceptors for RemoteWebDriver.");
        return builder.method(isPublicMethodToIntercept())
                      .intercept(to(publicMethodsInterceptor()))
                      .method(named(START_SESSION_METHOD_MAME))
                      .intercept(to(startSessionInterceptor()))
                      .method(named(QUIT_METHOD_MAME))
                      .intercept(to(quitSessionInterceptor()));
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

    private static TypeDescription quitSessionInterceptor() {
        log.debug("Creating interceptor for 'quit' and 'close' methods.");
        return TypePool.Default.ofSystemLoader()
                               .describe(QuitSessionInterceptor.class.getName())
                               .resolve();
    }

}
