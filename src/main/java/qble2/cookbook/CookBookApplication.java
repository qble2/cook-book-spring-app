package qble2.cookbook;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import lombok.extern.slf4j.Slf4j;
import qble2.cookbook.role.RoleService;
import qble2.cookbook.role.dto.RoleDto;
import qble2.cookbook.role.model.RoleEnum;
import qble2.cookbook.user.UserService;
import qble2.cookbook.user.dto.UserDto;

@SpringBootApplication
@Slf4j
public class CookBookApplication {

  public static void main(String[] args) {
    SpringApplication.run(CookBookApplication.class, args);
  }

  // placed here instead of SecurityConfiguration to avoid: The dependencies of some of the beans in
  // the application context form a cycle
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  @Profile("!test")
  public CommandLineRunner initAdminUser(RoleService roleService, UserService userService) {
    return args -> {
      try {
        // role
        RoleDto adminRole = RoleDto.builder().name(RoleEnum.ROLE_ADMIN).build();
        RoleDto createdRole = roleService.createRole(adminRole);
        log.info("Created role: (name: {} , id: {})", createdRole.getId(), createdRole.getName());

        // user
        UserDto adminUser = UserDto.builder().username("admin").password("admin").firstName("a")
            .lastName("b").email("b@k.com").build();

        UserDto createdUser = userService.createUser(adminUser);
        log.info("Created user: (username: {} , id: {})", createdUser.getUsername(),
            createdUser.getId());

        // add role to user
        roleService.addRoleToUser(createdUser.getId(), createdRole.getId());
        log.info("Role {} has been added to user {}", createdRole.getName(),
            createdUser.getUsername());
      } catch (Exception e) {
        log.warn(e.getMessage());
      }
    };
  }

}
