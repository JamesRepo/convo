package com.jameselner.convo.dto;

import com.jameselner.convo.model.User;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class UserDTO {
    private Long id;
    private String username;
    private String email;
//    private String firstName;
//    private String lastName;
//    private String avatarUrl;
    private String status;
    private LocalDateTime lastSeen;


    // TODO: move to mapper class
    public UserDTO(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
//        this.firstName = user.getFirstName();
//        this.lastName = user.getLastName();
//        this.avatarUrl = user.getAvatarUrl();
        this.status = user.getStatus().toString();
        this.lastSeen = user.getLastSeen();
    }
}