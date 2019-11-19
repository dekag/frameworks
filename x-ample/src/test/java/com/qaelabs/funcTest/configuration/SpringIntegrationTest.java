package com.qaelabs.funcTest.configuration;

import org.springframework.boot.test.context.SpringBootContextLoader;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

/**
 * @author dekag SpringIntegrationTest class - update @ActiveProfiles for
 *         different property configuration
 *
 */
@ContextConfiguration(classes = { BaseConfiguration.class }, loader = SpringBootContextLoader.class)

@SpringBootTest
@ActiveProfiles("test")
public class SpringIntegrationTest {

}
