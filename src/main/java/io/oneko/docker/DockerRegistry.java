package io.oneko.docker;

import java.util.UUID;

/**
 * DockerRegistry domain object. Contains all logic for doing whatever users need to do.<br/>
 * Comes with two implementations:
 * <ul>
 *     <li>{@link ReadableDockerRegistry}</li>
 *     <li>{@link WritableDockerRegistry}</li>
 * </ul>
 */
public interface DockerRegistry {

	UUID getUuid();

	String getName();

	String getRegistryUrl();

	String getUserName();

	String getPassword();

	boolean getTrustInsecureCertificate();

}
