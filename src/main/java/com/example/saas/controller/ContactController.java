package com.example.saas.controller;

import com.example.saas.dto.ContactDto;
import com.example.saas.service.ContactService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing contacts.  The tenant ID is part of the URL
 * path; however, the actual tenant isolation is enforced by the
 * {@link com.example.saas.filter.TenantContextFilter} which populates the
 * {@link com.example.saas.context.TenantContext} from the path.  Therefore the
 * controller methods do not explicitly use the tenant ID parameter except for
 * routing.
 */
@RestController
@RequestMapping("/{tenantId}/contacts")
public class ContactController {

    private final ContactService contactService;

    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    @GetMapping
    public ResponseEntity<List<ContactDto>> listContacts(@PathVariable String tenantId) {
        List<ContactDto> contacts = contactService.getAll();
        return ResponseEntity.ok(contacts);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContactDto> getContact(@PathVariable String tenantId, @PathVariable Long id) {
        ContactDto contact = contactService.getById(id);
        return ResponseEntity.ok(contact);
    }

    @PostMapping
    public ResponseEntity<ContactDto> createContact(@PathVariable String tenantId, @Valid @RequestBody ContactDto dto) {
        ContactDto saved = contactService.create(dto);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ContactDto> updateContact(@PathVariable String tenantId, @PathVariable Long id,
                                                     @Valid @RequestBody ContactDto dto) {
        ContactDto updated = contactService.update(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteContact(@PathVariable String tenantId, @PathVariable Long id) {
        contactService.delete(id);
        return ResponseEntity.noContent().build();
    }
}