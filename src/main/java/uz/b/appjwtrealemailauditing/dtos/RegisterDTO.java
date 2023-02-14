package uz.b.appjwtrealemailauditing.dtos;

import lombok.Data;

import javax.persistence.Column;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
public class RegisterDTO {
    @NotNull
    private String firstName;
    @NotNull
    private String lastName;
    @NotNull
    private String middleName;
    @NotNull
    @Email
    private String email;

    @Size(min = 8, max = 8)
    @NotNull
    private String password;
}
