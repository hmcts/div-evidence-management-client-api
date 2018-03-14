package uk.gov.hmcts.reform.emclient.service;

import static java.lang.reflect.Modifier.isPrivate;

import static org.junit.Assert.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.junit.Test;

public class UploadRequestBuilderTest {

    @Test
    public void testUploadRequestUtilityClassConstructor() {
        final Constructor<?>[] constructors = UploadRequestBuilder.class.getDeclaredConstructors();
        for (Constructor<?> constructor : constructors) {
            assertTrue(isPrivate(constructor.getModifiers()));
        }
    }

    @Test(expected = InvocationTargetException.class)
    public void testUploadRequestUtilityClassObjectCreation() throws Exception {
        Constructor<UploadRequestBuilder> constructor = UploadRequestBuilder.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        constructor.newInstance();
    }

}
