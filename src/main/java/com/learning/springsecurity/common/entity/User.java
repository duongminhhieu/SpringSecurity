package com.learning.springsecurity.common.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.Set;

@Entity
@Table(name = "t_user")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@NamedEntityGraph(
        name = "User.roles",
        attributeNodes = {
                @NamedAttributeNode(value = "roles", subgraph = "roles.permissions"),
        },
        subgraphs = {
                @NamedSubgraph(name = "roles.permissions",
                        attributeNodes = @NamedAttributeNode("permissions"))
        })
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String firstName;

    private String lastName;

    private LocalDate dob;

    @Column(unique = true)
    private String email;

    private String password;

    @ManyToMany(fetch = FetchType.LAZY)
    private Set<Role> roles;

}