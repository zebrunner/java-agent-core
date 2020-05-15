package com.zebrunner.agent.core.agent;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.pool.TypePool;

import java.lang.instrument.Instrumentation;

import static net.bytebuddy.matcher.ElementMatchers.named;

public class CoreAgent {

    private static final String SESSION_ID_CLASS_NAME = "org.openqa.selenium.remote.SessionId";
    private static final String REMOTE_DRIVER_CLASS_NAME = "org.openqa.selenium.remote.RemoteWebDriver";

    private static final String START_SESSION_METHOD_MAME = "startSession";

    public static void premain(String args, Instrumentation instrumentation) {
        installZebrunnerTransformer(instrumentation);
    }

    private static void installZebrunnerTransformer(Instrumentation instrumentation) {
        final TypeDescription startSession = TypePool.Default.ofSystemLoader()
                                                             .describe(StartSessionProxy.class.getName())
                                                             .resolve();

        new AgentBuilder.Default()
                .with(new AgentBuilder.InitializationStrategy.SelfInjection.Eager())
                .type(ElementMatchers.named(SESSION_ID_CLASS_NAME))
                .transform((builder, typeDescription, classLoader, module) -> builder
                        .constructor(ElementMatchers.takesArguments(TypeDescription.STRING))
                        .intercept(Advice.to(NewSessionProxy.class)))
                .type(ElementMatchers.named(REMOTE_DRIVER_CLASS_NAME))
                .transform((builder, type, classloader, module) -> builder.method(named(START_SESSION_METHOD_MAME))
                                                                          .intercept(MethodDelegation.to(startSession)))
                .installOn(instrumentation);
    }
}
