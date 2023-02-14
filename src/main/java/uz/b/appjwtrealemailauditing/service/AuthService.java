package uz.b.appjwtrealemailauditing.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import uz.b.appjwtrealemailauditing.dtos.ApiResponse;
import uz.b.appjwtrealemailauditing.dtos.LoginDTO;
import uz.b.appjwtrealemailauditing.dtos.RegisterDTO;
import uz.b.appjwtrealemailauditing.entity.User;
import uz.b.appjwtrealemailauditing.entity.enums.RoleName;
import uz.b.appjwtrealemailauditing.repository.RoleRepository;
import uz.b.appjwtrealemailauditing.repository.UserRepository;
import uz.b.appjwtrealemailauditing.security.JWTProvider;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService implements UserDetailsService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final RoleRepository roleRepository;

    private final JavaMailSender javaMailSender;
    private final AuthenticationManager authenticationManager;
    @Autowired
    JWTProvider jwtProvider;
    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            RoleRepository roleRepository,
            JavaMailSender javaMailSender,
            AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
        this.javaMailSender = javaMailSender;
        this.authenticationManager = authenticationManager;
    }


    public ApiResponse registerUser(RegisterDTO dto) {
        if (userRepository
                .existsByEmail(dto.getEmail())) {
            return new ApiResponse("This email is already registered", false);
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
        sendEmail(user.getEmail(), user.getEmailCode());
        return new ApiResponse("Successfully registered. For activation accept you email!", true);
    }


    public Boolean sendEmail(String sendingEmail, String emailCode) {
        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setFrom("Baxtiyor");
            mailMessage.setTo(sendingEmail);
            mailMessage.setSubject("ACCOUNT TASDIQLASH KODI");
            mailMessage.setText("<a href = 'http://localhost:8082/api/auth/verifyEmail?emailCode=" +
                    emailCode + "&email=" + sendingEmail + "'>Confirm</a>");

            javaMailSender.send(mailMessage);
            return true;
        } catch (Exception e) {
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
            return new ApiResponse("Account is confirmed", true);
        }
        return new ApiResponse("Account is already confirmed", false);
    }

    public ApiResponse login(LoginDTO loginDTO) {
        try {
            Authentication authenticate = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    loginDTO.getEmail(),
                    loginDTO.getPassword()));
            User user = (User)authenticate.getPrincipal();
            String token = jwtProvider.generateToken(loginDTO.getEmail(), user.getRoleList());
            return new ApiResponse("Token",true,token);
        }catch (BadCredentialsException e){
            return new ApiResponse("Bad Credentials", false);
        }

    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> optional = userRepository.findByEmail(username);
        if (optional.isPresent()) {
            return optional.get();
        }
        throw new UsernameNotFoundException(username + " not found!");
    }
}
