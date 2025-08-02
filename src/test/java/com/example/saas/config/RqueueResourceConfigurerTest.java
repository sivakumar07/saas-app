package com.example.saas.config;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;

import static org.mockito.Mockito.*;

class RqueueResourceConfigurerTest {

    @Test
    void testAddResourceHandlersWithPrefix() {
        RqueueResourceConfigurer configurer = new RqueueResourceConfigurer();
        ReflectionTestUtils.setField(configurer, "rqueueWebUrlPrefix", "/rqueue");

        ResourceHandlerRegistry registry = mock(ResourceHandlerRegistry.class);
        ResourceHandlerRegistration registration = mock(ResourceHandlerRegistration.class);
        when(registry.addResourceHandler("/rqueue/**")).thenReturn(registration);

        configurer.addResourceHandlers(registry);

        verify(registry).addResourceHandler("/rqueue/**");
        verify(registration).addResourceLocations("classpath:/public/");
    }

    @Test
    void testAddResourceHandlersWithoutPrefix() {
        RqueueResourceConfigurer configurer = new RqueueResourceConfigurer();
        ReflectionTestUtils.setField(configurer, "rqueueWebUrlPrefix", "");

        ResourceHandlerRegistry registry = mock(ResourceHandlerRegistry.class);
        ResourceHandlerRegistration registration = mock(ResourceHandlerRegistration.class);
        when(registry.hasMappingForPattern("/**")).thenReturn(false);
        when(registry.addResourceHandler("/**")).thenReturn(registration);

        configurer.addResourceHandlers(registry);

        verify(registry).addResourceHandler("/**");
        verify(registration).addResourceLocations("classpath:/public/");
    }

    @Test
    void testAddResourceHandlersWithoutPrefixAndExistingMapping() {
        RqueueResourceConfigurer configurer = new RqueueResourceConfigurer();
        ReflectionTestUtils.setField(configurer, "rqueueWebUrlPrefix", "");

        ResourceHandlerRegistry registry = mock(ResourceHandlerRegistry.class);
        when(registry.hasMappingForPattern("/**")).thenReturn(true);

        configurer.addResourceHandlers(registry);

        verify(registry, never()).addResourceHandler(anyString());
    }
}