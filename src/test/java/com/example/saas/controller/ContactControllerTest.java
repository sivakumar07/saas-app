package com.example.saas.controller;

import com.example.saas.dto.ContactDto;
import com.example.saas.service.ContactService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ContactControllerTest {

    @Mock
    private ContactService contactService;

    @InjectMocks
    private ContactController contactController;

    @Test
    public void testListContacts() {
        ContactDto contactDto = new ContactDto(1L, "Test", "User", "test@example.com");
        List<ContactDto> contacts = Collections.singletonList(contactDto);
        when(contactService.getAll()).thenReturn(contacts);

        ResponseEntity<List<ContactDto>> response = contactController.listContacts("tenant1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(contacts, response.getBody());
    }

    @Test
    public void testGetContact() {
        ContactDto contactDto = new ContactDto(1L, "Test", "User", "test@example.com");
        when(contactService.getById(1L)).thenReturn(contactDto);

        ResponseEntity<ContactDto> response = contactController.getContact("tenant1", 1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(contactDto, response.getBody());
    }

    @Test
    public void testCreateContact() {
        ContactDto contactDto = new ContactDto(null, "Test", "User", "test@example.com");
        ContactDto savedContact = new ContactDto(1L, "Test", "User", "test@example.com");
        when(contactService.create(contactDto)).thenReturn(savedContact);

        ResponseEntity<ContactDto> response = contactController.createContact("tenant1", contactDto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(savedContact, response.getBody());
    }

    @Test
    public void testUpdateContact() {
        ContactDto contactDto = new ContactDto(1L, "Updated", "User", "updated@example.com");
        when(contactService.update(1L, contactDto)).thenReturn(contactDto);

        ResponseEntity<ContactDto> response = contactController.updateContact("tenant1", 1L, contactDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(contactDto, response.getBody());
    }

    @Test
    public void testDeleteContact() {
        doNothing().when(contactService).delete(1L);

        ResponseEntity<Void> response = contactController.deleteContact("tenant1", 1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }
}