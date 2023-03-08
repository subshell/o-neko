import {Component} from "@angular/core";
import {MatLegacyDialog as MatDialog} from "@angular/material/legacy-dialog";
import {RestService} from "../../../rest/rest.service";
import {User} from "../../../user/user";
import {UserRole} from "../../../user/user-role";
import {UserService} from "../../../user/user.service";
import {
  ConfirmDeletionDialogComponent,
  ConfirmDeletionDialogData
} from "../../confirm-deletion.dialog/confirm-deletion-dialog.component";
import {DockerRegistry} from "../docker-registry";
import {DockerRegistryEditDialogComponent} from "../edit-dialog/docker-registry-edit-dialog.component";
import {RegistryTable} from "../../registry-table";

@Component({
  selector: 'docker-registry-list',
  templateUrl: './docker-registry-list.component.html',
  styleUrls: ['./docker-registry-list.component.scss']
})
export class DockerRegistryListComponent {
  public registryTable: RegistryTable<DockerRegistry>;

  public pageSettings = {
    pageSize: 10,
    pageSizeOptions: [10, 25, 50, 100]
  };
  private editingUser: User;

  constructor(private rest: RestService,
              private userService: UserService,
              private dialog: MatDialog) {
    this.registryTable = new RegistryTable();
    this.userService.currentUser().subscribe(currentUser => this.editingUser = currentUser);
    this.rest.docker().getAllDockerRegistries().subscribe(registries => {
      this.registryTable = new RegistryTable(registries);
      this.registryTable.sortRegistries();
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
        this.registryTable.addRegistry(result);
        this.registryTable.sortRegistries();
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
        this.registryTable.updateRegistryInList(result);
        this.registryTable.sortRegistries();
      }
    });
  }

  public deleteDockerRegistry(registry: DockerRegistry) {
    this.rest.docker().getNamesOfProjectsUsingRegistry(registry).subscribe(names => {
      this.dialog.open<ConfirmDeletionDialogComponent, ConfirmDeletionDialogData>(ConfirmDeletionDialogComponent, {
        width: '60%',
        data: {
          registry,
          projectNames: names,
          translationId: 'dockerRegistry',
          onConfirm: this.rest.docker().deleteDockerRegistry(registry)
        },
      }).afterClosed().subscribe(result => {
        if (result) {
          this.registryTable.removeRegistry(result);
          this.registryTable.sortRegistries();
        }
      });
    });
  }

}
