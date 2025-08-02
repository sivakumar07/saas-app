package com.example.saas.repository.global;

import com.example.saas.entity.global.Shard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for {@link Shard} entities.
 */
@Repository
public interface ShardRepository extends JpaRepository<Shard, Long> {
    Optional<Shard> findByShardKey(String shardKey);
}