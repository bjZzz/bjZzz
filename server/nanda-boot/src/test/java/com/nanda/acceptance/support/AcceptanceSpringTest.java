package com.nanda.acceptance.support;

import com.nanda.NandaApplication;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Tag("acceptance")
@EnabledIf("com.nanda.acceptance.support.AcceptanceConditions#mysqlAvailable")
@SpringBootTest(classes = NandaApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("acceptance")
public @interface AcceptanceSpringTest {
}
