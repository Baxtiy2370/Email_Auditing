package uz.b.appjwtrealemailauditing.config;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import uz.b.appjwtrealemailauditing.entity.User;

import java.util.Optional;
import java.util.UUID;

public class SpringSecurityAuditing implements AuditorAware<UUID> {
    @Override
    public Optional<UUID> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication == null || !authentication.isAuthenticated()||
                 "anonymousUser".equals(""+authentication.getPrincipal()))) {
            return Optional.of(((User)authentication.getPrincipal()).getId());
        }
        return Optional.empty();
    }
}
