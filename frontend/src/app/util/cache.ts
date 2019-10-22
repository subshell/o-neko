import {hash} from "hash-it";
import {cloneDeep} from "lodash";
import {interval, Observable, of, Subscription} from "rxjs";
import {shareReplay} from "rxjs/operators";
import {LogService} from "./log.service";

interface CacheEntry {
  timestamp: Date
  expiryDate?: Date
  value: any
}

export class Cache {
  private log = LogService.getLogger(Cache);
  private cache = new Map<number, CacheEntry>();
  private pendingCacheEntryCalculations = new Map<number, Observable<any>>();
  private subscriptions: Array<Subscription> = [];

  constructor() {
    this.subscriptions.push(interval(60000).subscribe(() => this.deleteExpiredItems()));
  }

  destroy() {
    this.subscriptions.forEach(subscription => subscription.unsubscribe());
  }

  put<T>(key: any, value: T, maxAge?: number): T {
    const now = new Date();
    const entry = {
      timestamp: now,
      value: value,
      expiryDate: maxAge ? new Date(now.getTime() + maxAge) : null
    };
    this.cache.set(hash(key), entry);
    this.log.trace({msg: "Stored element for key: ", data: key});
    return value;
  }

  getAll<T>(): Array<T> {
    return Array.from(this.cache.values()).map(entry => entry.value);
  }

  get<T>(key: any): T {
    let entry = this.cache.get(hash(key));
    if (entry) {
      if (this.isExpired(entry)) {
        this.cache.delete(hash(key));
        this.log.debug({msg: "Removing expired item from cache. Key:", data: key});
        return null;
      }

      try {
        this.log.trace({msg: "Retrieved cached element for key: ", data: key});
        return cloneDeep(entry.value) as T;
      } catch (e) {
        this.log.error({msg: "Failed to cast cached item into expected type. Key:", data: key});
      }
    }
    return null;
  }

  getOrElse<T>(key: any, supplier: Observable<T>, maxAge?: number): Observable<T> {
    let value: T = this.get(key); // check cache
    if (value) {
      return of(value);
    }

    let hashedKey = hash(key);
    let pendingCalculation: Observable<T> = this.pendingCacheEntryCalculations.get(hashedKey); // check if this element is being calculated right now
    if (pendingCalculation) {
      this.log.debug("A request for a cached element is already running. Returning the current observable.");
      return pendingCalculation;
    }

    this.log.trace({msg: "Using supplier to generate requested value for key:", data: key});
    let observable = supplier.pipe(shareReplay());
    this.pendingCacheEntryCalculations.set(hashedKey, observable);
    observable.subscribe(value => {
      this.put(key, value, maxAge);
      this.pendingCacheEntryCalculations.delete(hashedKey)
    });
    return observable;
  }

  invalidate(key: any) {
    let hashedKey = hash(key);
    if (this.cache.has(hashedKey)) {
      this.log.debug({msg: "Removing item from cache. Key:", data: key});
      this.pendingCacheEntryCalculations.delete(hashedKey);
      this.cache.delete(hashedKey);
    }
  }

  invalidateAll() {
    this.log.debug("Invalidating cache.");
    this.pendingCacheEntryCalculations.clear();
    this.cache.clear();
  }

  private deleteExpiredItems() {
    let expired: Array<any> = [];
    let now = new Date();
    this.cache.forEach((value: CacheEntry, key) => {
      if (this.isExpired(value)) {
        expired.push(key);
      }
    });
    expired.forEach(key => this.cache.delete(key));
    if (expired.length) {
      this.log.debug(`Removed ${expired.length} expired items from cache.`);
    }
  }

  private isExpired(entry: CacheEntry, date: Date = new Date()): boolean {
    return entry.expiryDate && entry.expiryDate < date;
  }

}
