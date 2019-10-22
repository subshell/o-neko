export class ArrayUtils {
  public static flatMap<T, U>(arr: T[], lambda: (value: T, index: number, array: T[]) => U): U[] {
    return Array.prototype.concat.apply([], arr.map(lambda));
  }

  public static move<T>(arr: T[], from: number, to: number): void {
    arr.splice(to, 0, arr.splice(from, 1)[0]);
  }

  public static clear<T>(arr: T[]): void {
    arr.length = 0;
  }

  public static removeAtIndex<T>(arr: T[], index: number): void {
    arr.splice(index, 1);
  }

  public static find<T>(arr: T[], predicate: (element: T, index: number, array: T[]) => T | boolean | undefined): T | undefined {
    for (let i = 0; i < arr.length; i++) {
      let value = arr[i];
      if (predicate(value, i, arr)) {
        return value;
      }
    }
    return undefined;
  }

  public static addAll<T>(arr: T[], toAdd: T[]): void {
    toAdd.forEach(element => arr.push(element));
  }

  public static replaceBy<T>(arr: T[], replacement: T, predicate: (element: T, index: number, array: T[]) => boolean): boolean {
    for (let i = 0; i < arr.length; i++) {
      let value = arr[i];
      if (predicate(value, i, arr)) {
        arr[i] = replacement;
        return true;
      }
    }
    return false;
  }

  public static isEmpty<T>(arr: T[]): boolean {
    return arr && arr.length === 0;
  }

  public static contains<T>(arr: T[], element: T): boolean {
    return arr.includes(element);
  }

  public static containsAll<T>(arr: T[], elements: T[]): boolean {
    return elements.every(element => ArrayUtils.contains(arr, element));
  }

  public static containsAny<T>(arr: T[], elements: T[]): boolean {
    return elements.some(element => ArrayUtils.contains(arr, element));
  }

  public static last<T>(arr: T[]): T {
    if (ArrayUtils.isEmpty(arr)) {
      return undefined;
    }
    return arr[arr.length - 1];
  }

  public static isOnlyElement<T>(arr: Array<T>, element?: T): boolean {
    return arr.length === 1 && arr[0] === element;
  }
}
