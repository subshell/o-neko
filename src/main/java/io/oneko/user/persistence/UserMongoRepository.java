package io.oneko.user.persistence;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import io.oneko.Profiles;
import io.oneko.event.EventDispatcher;
import io.oneko.security.UserRole;
import io.oneko.user.ReadableUser;
import io.oneko.user.User;
import io.oneko.user.WritableUser;
import io.oneko.user.auth.PasswordBasedUserAuthentication;
import io.oneko.user.auth.UserAuthentication;
import io.oneko.user.event.EventAwareUserRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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
	public Mono<ReadableUser> getById(UUID userId) {
		return this.innerUserRepo.findById(userId)
				.flatMap(this::fromUserMongo);
	}

	@Override
	public Mono<ReadableUser> getByUserName(String userName) {
		return this.innerUserRepo.findByUsername(userName).flatMap(this::fromUserMongo);
	}

	@Override
	public Mono<ReadableUser> getByUserEmail(String userEmail) {
		return this.innerUserRepo.findByEmail(userEmail).flatMap(this::fromUserMongo);
	}

	@Override
	public Flux<ReadableUser> getAll() {
		return this.innerUserRepo.findAll().flatMap(this::fromUserMongo);
	}

	@Override
	protected Mono<ReadableUser> addInternally(WritableUser user) {
		return this.innerUserRepo.save(this.toUserMongo(user))
				.zipWith(this.innerPasswordRepo.save(this.toPasswordMongo(user)), this::fromMongo);
	}

	@Override
	protected Mono<Void> removeInternally(User user) {
		return Mono.zip(innerUserRepo.deleteById(user.getId()),
				innerPasswordRepo.deleteById(user.getId())).then();
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
	private Mono<ReadableUser> fromUserMongo(UserMongo userMongo) {
		return this.innerPasswordRepo.findById(userMongo.getUserUuid())
				.map(p -> this.fromMongo(userMongo, p));
	}
}
