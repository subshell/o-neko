package io.oneko.project;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.collections4.map.AbstractReferenceMap;
import org.apache.commons.collections4.map.ReferenceMap;
import org.springframework.stereotype.Component;

@Component
public class ProjectVersionLock {

	private final ThreadLocal<Set<UUID>> versionsPerThread = ThreadLocal.withInitial(HashSet::new);
	private final Map<UUID, ReentrantLock> locks = Collections.synchronizedMap(new ReferenceMap<>(
			AbstractReferenceMap.ReferenceStrength.HARD, AbstractReferenceMap.ReferenceStrength.WEAK
	));

	public boolean currentThreadHasLock(UUID versionUUID) {
		return this.versionsPerThread.get().contains(versionUUID);
	}

	public void doWithProjectVersionLock(ProjectVersion<?, ?> version, Runnable runnable) {
		doWithProjectVersionLock(version, () -> {
			runnable.run();
			return null;
		});
	}

	public <T> T doWithProjectVersionLock(ProjectVersion<?, ?> version, Callable<T> callable) {
		Lock lock = getLock(version);

		try {
			lock.lock();
			versionsPerThread.get().add(version.getId());
			return callable.call();
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			lock.unlock();
			versionsPerThread.get().remove(version.getId());
		}
	}

	private Lock getLock(ProjectVersion<?, ?> version) {
		return locks.computeIfAbsent(version.getId(), k -> new ReentrantLock());
	}
}
