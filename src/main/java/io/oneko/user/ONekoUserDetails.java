package io.oneko.user;

import org.springframework.security.core.userdetails.UserDetails;

public interface ONekoUserDetails extends UserDetails {
	User getUser();
}
