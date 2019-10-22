package io.oneko.security;

public enum UserRole {
	/**
	 * Users with ADMIN role have unlimited control over O-Neko
	 */
	ADMIN,
	/**
	 * DOERs can manage projects but can't control users or registries.
	 */
	DOER,
	/**
	 * VIEWER only have reading access
	 */
	VIEWER
}
