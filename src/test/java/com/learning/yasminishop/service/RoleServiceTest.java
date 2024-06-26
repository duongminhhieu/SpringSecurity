package com.learning.yasminishop.service;

import com.learning.yasminishop.common.entity.Permission;
import com.learning.yasminishop.common.entity.Role;
import com.learning.yasminishop.permission.PermissionRepository;
import com.learning.yasminishop.permission.dto.response.PermissionResponse;
import com.learning.yasminishop.role.RoleRepository;
import com.learning.yasminishop.role.RoleService;
import com.learning.yasminishop.role.dto.request.RoleRequest;
import com.learning.yasminishop.role.dto.response.RoleResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;


@SpringBootTest
@Slf4j
@TestPropertySource("/test.properties")
class RoleServiceTest {

    @Autowired
    private RoleService roleService;

    @MockBean
    private RoleRepository roleRepository;

    @MockBean
    private PermissionRepository permissionRepository;

    private RoleRequest roleRequest;

    private List<Permission> permissionList;

    @BeforeEach
    void setUp() {
        roleRequest = RoleRequest.builder()
                .name("USER")
                .description("User role")
                .permissions(Set.of("READ"))
                .build();

        PermissionResponse permissionResponse = PermissionResponse.builder()
                .name("READ")
                .description("Read permission")
                .build();

        RoleResponse roleResponse = RoleResponse.builder()
                .name("USER")
                .description("User role")
                .permissions(Set.of(permissionResponse))
                .build();

        List<RoleResponse> roleResponseList = List.of(roleResponse);

        permissionList = List.of(Permission.builder()
                .name("READ")
                .description("Read permission")
                .build());
    }

    @Nested
    class HappyCase {
        @Test
        @WithMockUser(username = "admin@test.com", roles = {"ADMIN"})
        void create_validRequest_success() {
            // GIVEN
            when(permissionRepository.findAllById(any())).thenReturn(permissionList);
            when(roleRepository.save(any())).thenReturn(Role.builder()
                    .name("USER")
                    .description("User role")
                    .permissions(new HashSet<>(permissionList))
                    .build());

            // WHEN
            RoleResponse response = roleService.create(roleRequest);

            // THEN
            assertThat(response).isNotNull();
            assertThat(response.getName()).isEqualTo("USER");
            assertThat(response.getDescription()).isEqualTo("User role");
            assertThat(response.getPermissions()).isNotEmpty();
            assertThat(response.getPermissions()).hasSize(1);
        }

        @Test
        @WithMockUser(username = "admin@test.com", roles = {"ADMIN"})
        void getAll_validRequest_success() {
            // GIVEN
            when(roleRepository.findAll()).thenReturn(List.of(Role.builder()
                    .name("USER")
                    .description("User role")
                    .permissions(new HashSet<>(permissionList))
                    .build()));

            // WHEN
            List<RoleResponse> response = roleService.getAll();

            // THEN
            assertThat(response).isNotNull()
                    .isNotEmpty()
                    .hasSize(1);
        }

        @Test
        @WithMockUser(username = "admin@test.com", roles = {"ADMIN"})
        void delete_validRequest_success() {
            // GIVEN
            doNothing().when(roleRepository).deleteById(anyString());

            // WHEN
            roleService.delete("USER");

            // THEN
            verify(roleRepository, times(1)).deleteById("USER");
        }
    }

}
