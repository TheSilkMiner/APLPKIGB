package net.thesilkminer.mc.austin.api

import org.codehaus.groovy.transform.GroovyASTTransformationClass

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

@GroovyASTTransformationClass('net.thesilkminer.mc.austin.ast.GrabEventBusAstTransform')
@Retention(RetentionPolicy.SOURCE)
@SuppressWarnings('unused')
@Target(ElementType.FIELD)
@interface GrabEventBus {
    EventBus value()
}
