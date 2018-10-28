package org.eclipselabs.real.core.util;

/**
 * This interface represent an interface for an object repository where objects
 * are stored in a map with keys. It follows many of the principles on which
 * the simple Java Map is built like "unique keys", "use equals for keys" etc.
 *
 * But it also provides some additional methods to a simple Map:
 * 1. The repository can be locked to avoid any writes until the lock is released
 * 2. The add/remove methods are atomic - they lock the repository to prevent any modifications to the
 * repository. get methods also cannot return the value until the write lock is released
 * 3. The get methods use a read lock that must be released before any write operation can proceed.
 * The read lock may be obtained by multiple readers if the write lock is not held by anyone.
 *
 */
public interface IKeyedObjectRepository<K,R,W extends R> extends ILockableRepository,
        IKeyedObjectRepositoryRead<K, R>, IKeyedObjectRepositoryWrite<K, W> {

}
