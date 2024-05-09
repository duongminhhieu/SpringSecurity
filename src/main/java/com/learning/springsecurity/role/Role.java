package com.learning.springsecurity.role;


import com.learning.springsecurity.permission.Permission;
import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Entity
@Table(name = "t_role")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@NamedEntityGraph(name = "Role.permissions", attributeNodes = @NamedAttributeNode("permissions"))
public class Role {

    @Id
    private String name;

    private String description;

    @ManyToMany(fetch = FetchType.LAZY)
    private Set<Permission> permissions;


}
