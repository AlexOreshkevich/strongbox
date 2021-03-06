package org.carlspring.strongbox.rest;

import org.carlspring.strongbox.rest.context.IntegrationTest;
import org.carlspring.strongbox.utils.CustomAntPathMatcher;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Several test cases just to make sure that path variable parsing is correct.
 *
 * @author Alex Oreshkevich
 */
@IntegrationTest
@RunWith(SpringJUnit4ClassRunner.class)
public class CustomAntPathMatcherTest
{

    public static final Logger logger = LoggerFactory.getLogger(CustomAntPathMatcherTest.class);

    @Inject
    @Named("customAntPathMatcher")
    CustomAntPathMatcher antPathMatcher;

    @Test
    public void testSimplePathMatching()
            throws Exception
    {
        final String artifactPath = "org/carlspring/fake/mock/jar/par/test-jar-1.0.5.123.3232.2221123.3.2.1.jar";
        final String pattern =
                "/storages/{storageId}/{repositoryId}/{path:" + CustomAntPathMatcher.TWO_STARS_ANALOGUE + "}";
        final String path = "/storages/storageId/repositoryId/" + artifactPath;

        doTest(artifactPath, path, pattern);
    }

    @Test
    public void testPathMatchingForMultipleSubPaths()
    {
        final String artifactPath = ".trash/org/carlspring/maven/test-project/1.0.5/test-project-1.0.5.jar";
        final String pattern =
                "/storages/{storageId}/{repositoryId}/{path:" + CustomAntPathMatcher.TWO_STARS_ANALOGUE + "}";
        final String path = "/storages/storage0/releases-with-trash/" + artifactPath;

        doTest(artifactPath, path, pattern);
    }

    @Test
    public void testPathMatchingForComplexPath()
    {
        final String artifactPath = "org/test/mock/spring/23-123f--,,342&*#$/~276409~!$#%^&*(-=/3/2/1/3.jar";
        final String pattern = "/storages/{storageId}/{repositoryId}/{path:" + CustomAntPathMatcher.TWO_STARS_ANALOGUE + "}";
        final String path = "/storages/storage0/repositoryId/" + artifactPath;

        doTest(artifactPath, path, pattern);
    }

    private void doTest(String artifactPath,
                        String path,
                        String pattern)
    {

        final Map<String, String> uriTemplateVariables = new HashMap<>();

        antPathMatcher.doMatch(pattern, path, true, uriTemplateVariables);

        String pathVariable = uriTemplateVariables.get("path");
        assertNotNull("Unable to find path variable. uriTemplateVariables " + uriTemplateVariables, pathVariable);

        assertEquals(artifactPath, pathVariable);
    }
}
