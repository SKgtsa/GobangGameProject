package com.clankalliance.backbeta.entity.user;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;


import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;


@Data
@AllArgsConstructor
@MappedSuperclass
public class User {
    @Id
    private String id;

    private String nickName;

    private String phone;

    //男false女true
    private boolean gender;

    @JsonIgnore
    @NotBlank
    @Size(max = 50)
    private String password;

}
