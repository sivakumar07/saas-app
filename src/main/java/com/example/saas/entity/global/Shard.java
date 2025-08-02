package com.example.saas.entity.global;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "shards")
@Data
@NoArgsConstructor
public class Shard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shard_key", unique = true, nullable = false)
    private String shardKey;

    @Column(nullable = false)
    private String jdbcUrl;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    public Shard(String shardKey, String url, String username, String password) {
        this.shardKey = shardKey;
        this.jdbcUrl = url;
        this.username = username;
        this.password = password;
    }
}