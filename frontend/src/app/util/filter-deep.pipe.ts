import {Pipe} from "@angular/core";

@Pipe({ name: 'filterDeep' })
export class FilterDeepPipe {
  public transform<T>(arr: Array<T>, key: string, value: any): Array<T>  {
    if (!arr) {
      return [];
    }
    return arr.filter(item => item[key] === value);
  }
}
