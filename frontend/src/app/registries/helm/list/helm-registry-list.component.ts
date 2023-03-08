import {Component} from "@angular/core";
import {MatLegacyDialog as MatDialog} from "@angular/material/legacy-dialog";
import {RestService} from "../../../rest/rest.service";
import {User} from "../../../user/user";
import {UserRole} from "../../../user/user-role";
import {UserService} from "../../../user/user.service";
import {HelmRegistry} from "../helm-registry";
import {HelmRegistryEditDialogComponent} from "../edit-dialog/helm-registry-edit-dialog.component";
import {
  ConfirmDeletionDialogComponent,
  ConfirmDeletionDialogData
} from "../../confirm-deletion.dialog/confirm-deletion-dialog.component";
import {RegistryTable} from "../../registry-table";

@Component({
  selector: 'helm-registry-list',
  templateUrl: './helm-registry-list.component.html',
  styleUrls: ['./helm-registry-list.component.scss']
})
export class HelmRegistryListComponent {
  public registryTable: RegistryTable<HelmRegistry>;

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
    this.rest.helm().getAllHelmRegistries().subscribe(registries => {
      this.registryTable = new RegistryTable(registries);
      this.registryTable.sortRegistries();
    });
  }

  public mayShowHelmRegistryDetails(): boolean {
    return this.editingUser && this.editingUser.hasRolePermission(UserRole.DOER) && !this.editingUser.hasRolePermission(UserRole.ADMIN);
  }

  public mayCreateHelmRegistry(): boolean {
    return this.editingUser && this.editingUser.hasRolePermission(UserRole.ADMIN);
  }

  public mayEditHelmRegistry(): boolean {
    return this.editingUser && this.editingUser.hasRolePermission(UserRole.ADMIN);
  }

  public mayDeleteHelmRegistry(): boolean {
    return this.editingUser && this.editingUser.hasRolePermission(UserRole.ADMIN);
  }

  public createHelmRegistry() {
    this.dialog.open(HelmRegistryEditDialogComponent, {
      width: '80%'
    }).afterClosed().subscribe(result => {
      if (result) {
        this.registryTable.addRegistry(result);
        this.registryTable.sortRegistries();
      }
    });
  }

  public showHelmRegistryDetails(registry: HelmRegistry) {
    this.dialog.open(HelmRegistryEditDialogComponent, {
      width: '80%',
      data: {registry, readOnly: true}
    }).afterClosed().subscribe();
  }

  public editHelmRegistry(registry: HelmRegistry) {
    this.dialog.open(HelmRegistryEditDialogComponent, {
      width: '80%',
      data: {registry, readOnly: false}
    }).afterClosed().subscribe(result => {
      if (result) {
        this.registryTable.updateRegistryInList(result);
        this.registryTable.sortRegistries();
      }
    });
  }

  public deleteHelmRegistry(registry: HelmRegistry) {
    this.rest.helm().getNamesOfProjectsUsingRegistry(registry).subscribe(names => {
      this.dialog.open<ConfirmDeletionDialogComponent, ConfirmDeletionDialogData>(ConfirmDeletionDialogComponent, {
        width: '60%',
        data: {
          registry,
          projectNames: names,
          translationId: 'helmRegistry',
          onConfirm: this.rest.helm().deleteHelmRegistry(registry)
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
