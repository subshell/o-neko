import {Sort} from "@angular/material/sort";
import {LegacyPageEvent as PageEvent} from "@angular/material/legacy-paginator";
import {Registry} from "./registry";

export class RegistryTable<R extends Registry> {
  private _sortedRegistries: Array<R> = [];
  private _pageSettings = {
    pageSize: 10,
    pageSizeOptions: [10, 25, 50, 100]
  };
  private pageEvent: PageEvent;
  private sort: Sort;

  constructor(private registries: Array<R> = []) {
  }

  public get sortedRegistries(): Array<R> {
    return this._sortedRegistries;
  }

  public get pageSettings(): {pageSize: number, pageSizeOptions: Array<number>} {
    return this._pageSettings;
  }

  public addRegistry(registry: R) {
    this.registries.push(registry);
  }

  public removeRegistry(registry: any) {
    this.registries.splice(this.registries.indexOf(registry), 1);
  }

  public sortRegistries(sort?: Sort): Array<R> {
    this.sort = sort;
    let first = 0;
    let pageSize = this.pageSettings.pageSize;
    if (this.pageEvent) {
      first = this.pageEvent.pageIndex * this.pageEvent.pageSize;
      pageSize = this.pageEvent.pageSize;
    }

    this._sortedRegistries = this.getSortedRegistries(sort).slice(first, first + pageSize);
    return this.sortedRegistries;
  }

  public paginationEvent(event: PageEvent) {
    this.pageEvent = event;
    this.sortRegistries(this.sort);
  }

  private getSortedRegistries(sort?: Sort): Array<R> {
    const data = this.registries.slice();
    if (!sort || !sort.active || sort.direction == '') {
      return data;
    }

    return data.sort((a, b) => {
      let isAsc = sort.direction === 'asc';
      if (a[sort.active] !== undefined) {
        return RegistryTable.compare(a[sort.active], b[sort.active], isAsc);
      }

      return 0;
    });
  }

  public updateRegistryInList(registry: R) {
    let idx = this.registries.findIndex(reg => reg.getId() === registry.getId());
    this.registries.splice(idx, 1, registry);
  }

  private static compare(a: any, b: any, isAsc: boolean): number {
    return (a < b ? -1 : 1) * (isAsc ? 1 : -1);
  }

}
