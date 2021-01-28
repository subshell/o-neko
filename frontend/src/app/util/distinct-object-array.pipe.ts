import {Pipe, PipeTransform} from "@angular/core";

@Pipe({
    name: 'distinctObjects',
    pure: false
})
export class DistinctObjectArrayPipe implements PipeTransform {
    transform<T>(values: Array<T>, key: string): Array<T> {
      if (!values || !key) {
        return [];
      }

      const tmp = {};
      values.forEach(value => {
        tmp[value[key]] = value;
      });

      return Object.values(tmp);
    }
}
