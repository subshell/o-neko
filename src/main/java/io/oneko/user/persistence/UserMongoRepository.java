package io.oneko.user.persistence;

import io.oneko.Profiles;
import io.oneko.event.EventDispatcher;
import io.oneko.security.UserRole;
import io.oneko.user.ReadableUser;
import io.oneko.user.User;
import io.oneko.user.WritableUser;
import io.oneko.user.auth.PasswordBasedUserAuthentication;
import io.oneko.user.auth.UserAuthentication;
import io.oneko.user.event.EventAwareUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Mongo implementation of UserRepository
 */
@Service
@Profile(Profiles.MONGO)
class UserMongoRepository extends EventAwareUserRepository {
	private final UserMongoSpringRepository innerUserRepo;
	private final PasswordBasedUserAuthenticationMongoSpringRepository innerPasswordRepo;
	private final PasswordEncoder passwordEncoder;

	@Autowired
	public UserMongoRepository(UserMongoSpringRepository innerUserRepo, PasswordBasedUserAuthenticationMongoSpringRepository innerPasswordRepo, PasswordEncoder passwordEncoder, EventDispatcher eventDispatcher) {
		super(eventDispatcher);
		this.innerUserRepo = innerUserRepo;
		this.innerPasswordRepo = innerPasswordRepo;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public Optional<ReadableUser> getById(UUID userId) {
		return this.innerUserRepo.findById(userId)
				.map(this::fromUserMongo);
	}

	@Override
	public Optional<ReadableUser> getByUserName(String userName) {
		return this.innerUserRepo.findByUsername(userName).map(this::fromUserMongo);
	}

	@Override
	public Optional<ReadableUser> getByUserEmail(String userEmail) {
		return this.innerUserRepo.findByEmail(userEmail).map(this::fromUserMongo);
	}

	@Override
	public List<ReadableUser> getAll() {
		return this.innerUserRepo.findAll().stream().map(this::fromUserMongo).collect(Collectors.toList());
	}

	@Override
	protected ReadableUser addInternally(WritableUser user) {
		UserMongo savedUser = this.innerUserRepo.save(this.toUserMongo(user));
		PasswordBasedUserAuthenticationMongo savedAuth = this.innerPasswordRepo.save(this.toPasswordMongo(user));
		return fromMongo(savedUser, savedAuth);
	}

	@Override
	protected void removeInternally(User user) {
		innerUserRepo.deleteById(user.getId());
		innerPasswordRepo.deleteById(user.getId());
	}

	/*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * Mapping stuff
     ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/

	private UserMongo toUserMongo(User user) {
		UserMongo mongoUser = new UserMongo();
		mongoUser.setUserUuid(user.getId());
		mongoUser.setUsername(user.getUserName());
		mongoUser.setEmail(user.getEmail());
		mongoUser.setFirstName(user.getFirstName());
		mongoUser.setLastName(user.getLastName());
		mongoUser.setRole(user.getRole().name());

		return mongoUser;
	}

	private PasswordBasedUserAuthenticationMongo toPasswordMongo(User user) {
		UserAuthentication<?> authentication = user.getAuthentication();
		PasswordBasedUserAuthenticationMongo passwordMongo = null;
		if (authentication instanceof PasswordBasedUserAuthentication) {
			passwordMongo = new PasswordBasedUserAuthenticationMongo();
			passwordMongo.setPassword(((PasswordBasedUserAuthentication) authentication).getHashedPassword());
			passwordMongo.setUserUuid(user.getId());
		}
		return passwordMongo;
	}

	private ReadableUser fromMongo(UserMongo userMongo, PasswordBasedUserAuthenticationMongo passwordMongo) {
		ReadableUser[] user = new ReadableUser[1];
		user[0] = ReadableUser.builder().uuid(userMongo.getUserUuid())
				.userName(userMongo.getUsername())
				.email(userMongo.getEmail())
				.firstName(userMongo.getFirstName())
				.lastName(userMongo.getLastName())
				.role(UserRole.valueOf(userMongo.getRole()))
				.authentication(new PasswordBasedUserAuthentication(userMongo.getUserUuid(), () -> user[0], passwordMongo.getPassword(), this.passwordEncoder))
				.build();
		return user[0];
	}

	/**
	 * Convenience mapper from UserMongo to User that implicitly loads the password entity and adds it to the user.
	 */
	private ReadableUser fromUserMongo(UserMongo userMongo) {
		//TODO: what if this is null?
		return fromMongo(userMongo, this.innerPasswordRepo.findById(userMongo.getUserUuid()).orElse(null));
	}
}
