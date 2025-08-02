package com.example.saas.service;

import com.example.saas.dto.ContactDto;
import com.example.saas.entity.shard.Contact;
import com.example.saas.repository.shard.ContactRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ContactServiceTest {

    @Mock
    private ContactRepository contactRepository;

    @Mock
    private ContactEventPublisher contactEventPublisher;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private ContactService contactService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getAll() {
        Contact contact = new Contact(1L, "Test User", "test@example.com", "1234567890");
        when(contactRepository.findAll()).thenReturn(Collections.singletonList(contact));

        List<ContactDto> result = contactService.getAll();

        assertEquals(1, result.size());
        assertEquals("Test User", result.getFirst().getName());
    }

    @Test
    void getById_success() {
        Contact contact = new Contact(1L, "Test User", "test@example.com", "1234567890");
        when(contactRepository.findById(1L)).thenReturn(Optional.of(contact));

        ContactDto result = contactService.getById(1L);

        assertEquals("Test User", result.getName());
    }

    @Test
    void getById_notFound() {
        when(contactRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> contactService.getById(1L));
    }

    @Test
    void create() throws Exception {
        ContactDto contactDto = new ContactDto(null, "New Contact", "new@example.com", "0987654321");

        when(contactRepository.save(any(Contact.class))).thenAnswer(invocation -> {
            Contact savedContact = invocation.getArgument(0);
            savedContact.setId(1L);
            return savedContact;
        });
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        ContactDto result = contactService.create(contactDto);

        assertEquals(1L, result.getId());
        assertEquals("New Contact", result.getName());
        verify(contactEventPublisher, times(1)).publish(eq("CONTACT_CREATED"), eq(1L), anyString());
    }

    @Test
    void update_success() throws Exception {
        ContactDto contactDto = new ContactDto(1L, "Updated Contact", "updated@example.com", "1231231234");
        Contact contact = new Contact(1L, "Old Contact", "old@example.com", "1234567890");

        when(contactRepository.findById(1L)).thenReturn(Optional.of(contact));
        when(contactRepository.save(any(Contact.class))).thenReturn(contact);
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        ContactDto result = contactService.update(1L, contactDto);

        assertEquals("Updated Contact", result.getName());
        verify(contactEventPublisher, times(1)).publish(eq("CONTACT_UPDATED"), eq(1L), anyString());
    }

    @Test
    void update_notFound() {
        ContactDto contactDto = new ContactDto(1L, "Updated Contact", "updated@example.com", "1231231234");
        when(contactRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> contactService.update(1L, contactDto));
    }

    @Test
    void delete_success() throws Exception {
        Contact contact = new Contact(1L, "Test Contact", "test@example.com", "1234567890");

        when(contactRepository.findById(1L)).thenReturn(Optional.of(contact));
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        contactService.delete(1L);

        verify(contactRepository, times(1)).save(any(Contact.class));
        verify(contactEventPublisher, times(1)).publish(eq("CONTACT_DELETED"), eq(1L), anyString());
    }

    @Test
    void delete_notFound() {
        when(contactRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> contactService.delete(1L));
    }
}
