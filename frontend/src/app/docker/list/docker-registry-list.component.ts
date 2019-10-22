import {Component} from "@angular/core";
import {MatDialog} from "@angular/material/dialog";
import {PageEvent} from "@angular/material/paginator";
import {Sort} from "@angular/material/sort";
import {RestService} from "../../rest/rest.service";
import {User} from "../../user/user";
import {UserRole} from "../../user/user-role";
import {UserService} from "../../user/user.service";
import {ConfirmDeletionDialogComponent} from "../confirm-deletion.dialog/confirm-deletion-dialog.component";
import {DockerRegistry} from "../docker-registry";
import {DockerRegistryEditDialogComponent} from "../edit-dialog/docker-registry-edit-dialog.component";

@Component({
  selector: 'docker-registry-list',
  templateUrl: './docker-registry-list.component.html',
  styleUrls: ['./docker-registry-list.component.scss']
})
export class DockerRegistryListComponent {

  public sortedDockerRegistries: Array<DockerRegistry>;
  public dockerRegistries: Array<DockerRegistry> = [];
  public pageSettings = {
    pageSize: 10,
    pageSizeOptions: [10, 25, 50, 100]
  };
  private editingUser: User;
  private pageEvent: PageEvent;
  private sort: Sort;

  constructor(private rest: RestService,
              private userService: UserService,
              private dialog: MatDialog) {
    this.userService.currentUser().subscribe(currentUser => this.editingUser = currentUser);
    this.rest.docker().getAllDockerRegistries().subscribe(registries => {
      this.dockerRegistries = registries;
      this.sortDockerRegistries();
    });
  }

  public mayShowDockerRegistryDetails(): boolean {
    return this.editingUser && this.editingUser.hasRolePermission(UserRole.DOER) && !this.editingUser.hasRolePermission(UserRole.ADMIN);
  }

  public mayCreateDockerRegistry(): boolean {
    return this.editingUser && this.editingUser.hasRolePermission(UserRole.ADMIN);
  }

  public mayEditDockerRegistry(): boolean {
    return this.editingUser && this.editingUser.hasRolePermission(UserRole.ADMIN);
  }

  public mayDeleteDockerRegistry(): boolean {
    return this.editingUser && this.editingUser.hasRolePermission(UserRole.ADMIN);
  }

  public createDockerRegistry() {
    this.dialog.open(DockerRegistryEditDialogComponent, {
      width: '80%'
    }).afterClosed().subscribe(result => {
      if (result) {
        this.dockerRegistries.push(result);
        this.sortDockerRegistries();
      }
    });
  }

  public showDockerRegistryDetails(registry: DockerRegistry) {
    this.dialog.open(DockerRegistryEditDialogComponent, {
      width: '80%',
      data: {registry, readOnly: true}
    }).afterClosed().subscribe();
  }

  public editDockerRegistry(registry: DockerRegistry) {
    this.dialog.open(DockerRegistryEditDialogComponent, {
      width: '80%',
      data: {registry, readOnly: false}
    }).afterClosed().subscribe(result => {
      if (result) {
        this.updateDockerRegistryInList(result);
        this.sortDockerRegistries();
      }
    });
  }

  public deleteDockerRegistry(registry: DockerRegistry) {
    this.rest.docker().getNamesOfProjectsUsingRegistry(registry).subscribe(names => {
      this.dialog.open(ConfirmDeletionDialogComponent, {
        width: '60%',
        data: {registry, projectNames: names}
      }).afterClosed().subscribe(result => {
        if (result) {
          this.dockerRegistries.splice(this.dockerRegistries.indexOf(result), 1);
          this.sortDockerRegistries();
        }
      });
    });
  }

  public sortDockerRegistries(sort?: Sort) {
    this.sort = sort;
    let first = 0;
    let pageSize = this.pageSettings.pageSize;
    if (this.pageEvent) {
      first = this.pageEvent.pageIndex * this.pageEvent.pageSize;
      pageSize = this.pageEvent.pageSize;
    }
    this.sortedDockerRegistries = this.getSortedDockerRegistries(sort).slice(first, first + pageSize);
  }

  public paginationEvent(event: PageEvent) {
    this.pageEvent = event;
    this.sortDockerRegistries(this.sort);
  }

  public getSortedDockerRegistries(sort?: Sort): Array<DockerRegistry> {
    const data = this.dockerRegistries.slice();
    if (!sort || !sort.active || sort.direction == '') {
      return data;
    }

    return data.sort((a, b) => {
      let isAsc = sort.direction === 'asc';
      if (a[sort.active] !== undefined) {
        return this.compare(a[sort.active], b[sort.active], isAsc);
      } else {
        return 0;
      }
    });
  }

  private updateDockerRegistryInList(dockerRegistry: DockerRegistry) {
    let idx = this.dockerRegistries.findIndex(reg => reg.uuid === dockerRegistry.uuid);
    this.dockerRegistries.splice(idx, 1, dockerRegistry);
  }

  private compare(a: any, b: any, isAsc: boolean): number {
    return (a < b ? -1 : 1) * (isAsc ? 1 : -1);
  }

}
