export const flatten = (input: any, prefix: string = '', result: any = {}): any =>
  Object.entries(input).reduce((flattenedObject, [key, value]) => {
    const prefixedKey = `${prefix}${key}`;
    if (typeof value === 'object') {
      flatten(value, `${prefixedKey}.`, flattenedObject);
    } else {
      result[prefixedKey] = value;
    }
    return flattenedObject;
  }, result);
