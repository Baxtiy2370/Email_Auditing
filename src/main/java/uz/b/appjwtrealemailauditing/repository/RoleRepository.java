package uz.b.appjwtrealemailauditing.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.b.appjwtrealemailauditing.entity.Role;
import uz.b.appjwtrealemailauditing.entity.enums.RoleName;

public interface RoleRepository extends JpaRepository<Role,Long> {
    Role findByRoleName(RoleName roleName);
}
