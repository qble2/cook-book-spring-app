package qble2.cookbook.role;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import qble2.cookbook.role.model.Role;
import qble2.cookbook.role.model.RoleEnum;

public interface RoleRepository extends JpaRepository<Role, UUID> {

  boolean existsByName(RoleEnum name);

}
