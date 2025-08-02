package com.example.saas.repository.shard;

import com.example.saas.entity.shard.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for {@link Contact} entities.  Queries are scoped by tenant ID to
 * prevent data leakage across tenants.  Spring Data automatically derives the
 * SQL where clause from the method names.
 */
@Repository
public interface ContactRepository extends JpaRepository<Contact, Long> {
}