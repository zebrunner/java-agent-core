package com.zebrunner.agent.core.agent;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.pool.TypePool;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.lang.instrument.Instrumentation;

import static net.bytebuddy.matcher.ElementMatchers.named;

public class CoreAgent {

    private static final String START_SESSION_METHOD_MAME = "startSession";
    private static final String QUIT_METHOD_MAME = "quit";
    private static final String CLOSE_METHOD_MAME = "close";

    public static void premain(String args, Instrumentation instrumentation) {
        installZebrunnerTransformer(instrumentation);
    }

    private static void installZebrunnerTransformer(Instrumentation instrumentation) {
        final TypeDescription startSession = TypePool.Default.ofSystemLoader()
                                                             .describe(StartSessionProxy.class.getName())
                                                             .resolve();
        final TypeDescription quitSession = TypePool.Default.ofSystemLoader()
                                                             .describe(QuitSessionProxy.class.getName())
                                                             .resolve();
        final TypeDescription closeSession = TypePool.Default.ofSystemLoader()
                                                            .describe(CloseSessionProxy.class.getName())
                                                            .resolve();

        new AgentBuilder.Default()
                .with(new AgentBuilder.InitializationStrategy.SelfInjection.Eager())
                .type(ElementMatchers.named(RemoteWebDriver.class.getName()))
                .transform((builder, type, classloader, module) -> builder.method(named(START_SESSION_METHOD_MAME))
                                                                          .intercept(MethodDelegation.to(startSession))
                                                                          .method(named(QUIT_METHOD_MAME))
                                                                          .intercept(MethodDelegation.to(quitSession))
                                                                          .method(named(CLOSE_METHOD_MAME))
                                                                          .intercept(MethodDelegation.to(closeSession)))
                .installOn(instrumentation);
    }
}
