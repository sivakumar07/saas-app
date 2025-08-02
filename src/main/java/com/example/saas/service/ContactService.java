package com.example.saas.service;

import com.example.saas.context.TenantContext;
import com.example.saas.dto.ContactDto;
import com.example.saas.entity.shard.Contact;
import com.example.saas.repository.shard.ContactRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ContactService {

    private final ContactRepository contactRepository;
    private final ContactEventPublisher contactEventPublisher;
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    public ContactService(ContactRepository contactRepository,
                          ContactEventPublisher contactEventPublisher,
                          com.fasterxml.jackson.databind.ObjectMapper objectMapper) {
        this.contactRepository = contactRepository;
        this.contactEventPublisher = contactEventPublisher;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public List<ContactDto> getAll() {
        return contactRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ContactDto getById(Long id) {
        Contact contact = contactRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Contact not found"));
        return toDto(contact);
    }

    @Transactional
    public ContactDto create(ContactDto dto) {
        Contact contact = new Contact();
        contact.setName(dto.getName());
        contact.setEmail(dto.getEmail());
        contact.setPhone(dto.getPhone());
        contactRepository.save(contact);
        try {
            String changes = objectMapper.writeValueAsString(dto);
            contactEventPublisher.publish("CONTACT_CREATED", contact.getId(), changes);
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(ContactService.class)
                    .error("Failed to serialize contact for event", e);
        }
        return toDto(contact);
    }

    @Transactional
    public ContactDto update(Long id, ContactDto dto) {
        Contact contact = contactRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Contact not found"));
        String oldName = contact.getName();
        String oldEmail = contact.getEmail();
        String oldPhone = contact.getPhone();

        contact.setName(dto.getName());
        contact.setEmail(dto.getEmail());
        contact.setPhone(dto.getPhone());
        contactRepository.save(contact);

        java.util.Map<String, Object> changes = new java.util.HashMap<>();
        if (oldName != null ? !oldName.equals(dto.getName()) : dto.getName() != null) {
            changes.put("name", java.util.Map.of("old", oldName, "new", dto.getName()));
        }
        if (oldEmail != null ? !oldEmail.equals(dto.getEmail()) : dto.getEmail() != null) {
            changes.put("email", java.util.Map.of("old", oldEmail, "new", dto.getEmail()));
        }
        if (oldPhone != null ? !oldPhone.equals(dto.getPhone()) : dto.getPhone() != null) {
            changes.put("phone", java.util.Map.of("old", oldPhone, "new", dto.getPhone()));
        }
        try {
            String changesJson = objectMapper.writeValueAsString(changes);
            contactEventPublisher.publish("CONTACT_UPDATED", contact.getId(), changesJson);
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(ContactService.class)
                    .error("Failed to serialize contact changes for event", e);
        }
        return toDto(contact);
    }

    @Transactional
    public void delete(Long id) {
        Contact contact = contactRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Contact not found"));
        contact.setDeletedAt(java.time.Instant.now());
        contact.setDeletedBy(TenantContext.getUserId());
        contactRepository.save(contact);

        try {
            java.util.Map<String, Object> changes = new java.util.HashMap<>();
            changes.put("deleted", true);
            String changesJson = objectMapper.writeValueAsString(changes);
            contactEventPublisher.publish("CONTACT_DELETED", id, changesJson);
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(ContactService.class)
                    .error("Failed to publish contact deletion event", e);
        }
    }

    private ContactDto toDto(Contact contact) {
        return new ContactDto(contact.getId(), contact.getName(), contact.getEmail(), contact.getPhone());
    }
}