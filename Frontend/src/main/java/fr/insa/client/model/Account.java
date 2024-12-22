package fr.insa.client.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Account {
    private String name;
    private String surname;
    private String email;
    private String password;
    private String birthdate;
    private String type;
    private String sex;
}
