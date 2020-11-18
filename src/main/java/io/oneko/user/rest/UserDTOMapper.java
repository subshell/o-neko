package io.oneko.user.rest;

import io.oneko.user.User;
import io.oneko.user.WritableUser;
import org.springframework.stereotype.Service;

@Service
public class UserDTOMapper {

	public UserDTO userToDTO(User user) {
		UserDTO dto = new UserDTO();
		dto.setUuid(user.getUuid());
		dto.setUsername(user.getUserName());
		dto.setEmail(user.getEmail());
		dto.setFirstName(user.getFirstName());
		dto.setLastName(user.getLastName());
		dto.setRole(user.getRole());//wtf?
		return dto;
	}

	public WritableUser updateUserFromDTO(WritableUser user, UserDTO userDTO) {
		//id can not be changed
		user.setUserName(userDTO.getUsername());
		user.setEmail(userDTO.getEmail());
		user.setFirstName(userDTO.getFirstName());
		user.setLastName(userDTO.getLastName());
		user.setUserName(userDTO.getUsername());
		user.setRole(userDTO.getRole());
		return user;
	}

}
