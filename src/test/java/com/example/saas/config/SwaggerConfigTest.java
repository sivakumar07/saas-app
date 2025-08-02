package com.example.saas.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class SwaggerConfigTest {

    @Test
    void testApi() {
        SwaggerConfig swaggerConfig = new SwaggerConfig();
        OpenAPI openAPI = swaggerConfig.api();

        assertNotNull(openAPI);

        Info info = openAPI.getInfo();
        assertNotNull(info);
        assertEquals("Multi‑Tenant SaaS API", info.getTitle());
        assertEquals("REST API for a multi‑tenant SaaS platform with sharded PostgreSQL databases, Redis‑backed task queues and automatic auditing/soft deletion", info.getDescription());
        assertEquals("v1.0", info.getVersion());

        License license = info.getLicense();
        assertNotNull(license);
        assertEquals("Apache 2.0", license.getName());
        assertEquals("https://www.apache.org/licenses/LICENSE-2.0", license.getUrl());

        Contact contact = info.getContact();
        assertNotNull(contact);
        assertEquals("SaaS Support", contact.getName());
        assertEquals("support@example.com", contact.getEmail());

        ExternalDocumentation externalDocs = openAPI.getExternalDocs();
        assertNotNull(externalDocs);
        assertEquals("Project Repository", externalDocs.getDescription());
        assertEquals("https://example.com/saas-app", externalDocs.getUrl());
    }
}