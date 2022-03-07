package net.thesilkminer.mc.austin.api

import net.minecraftforge.api.distmarker.Dist
import org.codehaus.groovy.transform.GroovyASTTransformationClass

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

@GroovyASTTransformationClass('net.thesilkminer.mc.austin.ast.EventBusSubscriberAstTransform')
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@interface EventBusSubscriber {

    enum Bus {
        MOJO,
        MOD,
        FORGE;
    }

    String modId()

    Bus bus()

    Dist[] dist() default [Dist.CLIENT, Dist.DEDICATED_SERVER]
}
