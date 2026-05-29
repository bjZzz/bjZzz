package com.nanda.acceptance.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks an acceptance test class as covering all REQs mapped to the given business process group.
 * See {@link com.nanda.acceptance.support.ReqCatalog#resolveGroup(String)}.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CoversReqGroup {

    String[] value();
}
