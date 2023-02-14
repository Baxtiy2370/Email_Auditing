package uz.b.appjwtrealemailauditing.service;


import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import uz.b.appjwtrealemailauditing.dtos.ApiResponse;
import uz.b.appjwtrealemailauditing.dtos.RegisterDTO;
import uz.b.appjwtrealemailauditing.entity.User;
import uz.b.appjwtrealemailauditing.entity.enums.RoleName;
import uz.b.appjwtrealemailauditing.repository.RoleRepository;
import uz.b.appjwtrealemailauditing.repository.UserRepository;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {
    private final UserRepository userRepository
            ;
    private final PasswordEncoder passwordEncoder;

    private final RoleRepository roleRepository;

    private final JavaMailSender javaMailSender;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, RoleRepository roleRepository, JavaMailSender javaMailSender) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
        this.javaMailSender = javaMailSender;
    }


    public ApiResponse registerUser(RegisterDTO dto) {
        if (userRepository
                .existsByEmail(dto.getEmail())) {
            return new ApiResponse("This email is already registered",false);
        }

        User user = new User();
                user.setFirstName(dto.getFirstName());
                user.setMiddleName(dto.getMiddleName());
                user.setLastName(dto.getLastName());
                user.setEmail(dto.getEmail());
                user.setPassword(passwordEncoder.encode(dto.getPassword()));
                user.setRoleList(Collections.singleton(roleRepository.findByRoleName(RoleName.ROLE_USER)));
                user.setEmailCode(UUID.randomUUID().toString());

        userRepository
                .save(user);
        sendEmail(user.getEmail(), user.getEmailCode() );
        return new ApiResponse("Successfully registered. For activation accept you email!",true);
    }


    public Boolean sendEmail(String sendingEmail, String emailCode){
        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setFrom("Baxtiyor");
            mailMessage.setTo(sendingEmail);
            mailMessage.setSubject("ACCOUNT TASDIQLASH KODI");
            mailMessage.setText("<a href = 'http://localhost:8082/api/auth/verifyEmail?emailCode=" +
                    emailCode + "&email=" + sendingEmail + "'>Confirm</a>");

            javaMailSender.send(mailMessage);
            return true;
        }catch (Exception e){
            return false;
        }
    }

    public ApiResponse verifyEmail(String emailCode, String email) {
        Optional<User> optionalUser = userRepository.findByEmailAndEmailCode(email, emailCode);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            user.setEnabled(true);
            user.setEmailCode(null);
            userRepository.save(user);
            return new ApiResponse("Account is confirmed",true);
        }
        return new ApiResponse("Account is already confirmed",false);
    }
}
