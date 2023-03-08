import {Component} from "@angular/core";
import {MatLegacyDialog as MatDialog} from "@angular/material/legacy-dialog";
import {LegacyPageEvent as PageEvent} from "@angular/material/legacy-paginator";
import {RestService} from "../../rest/rest.service";
import {User} from "../../user/user";
import {UserRole} from "../../user/user-role";
import {UserService} from "../../user/user.service";
import {CreateNamespaceDialogComponent} from "../create-namespace-dialog/create-namespace-dialog.component";
import {Namespace} from "../namespace";
import {DeleteNamespaceDialogComponent} from "../delete-namespace-dialog/delete-namespace-dialog.component";

@Component({
  selector: 'defined-namespaces-list',
  templateUrl: './defined-namespaces-list.component.html',
  styleUrls: ['./defined-namespaces-list.component.scss']
})
export class DefinedNamespacesListComponent {

  public sortedDefinedNamespaces: Array<Namespace>;
  public definedNamespaces: Array<Namespace> = [];
  public pageSettings = {
    pageSize: 10,
    pageSizeOptions: [10, 25, 50, 100]
  };
  private editingUser: User;
  private pageEvent: PageEvent;

  constructor(private rest: RestService,
              private userService: UserService,
              private dialog: MatDialog) {
    this.userService.currentUser().subscribe(currentUser => this.editingUser = currentUser);
    this.rest.namespace().getAllDefinedNamespaces().subscribe(namespaces => {
      this.definedNamespaces = namespaces;
      this.sortDefinedNamespaces();
    });
  }

  public mayCreateDefinedNamespace(): boolean {
    return this.editingUser && this.editingUser.hasRolePermission(UserRole.ADMIN);
  }

  public mayDeleteDefinedNamespace(): boolean {
    return this.editingUser && this.editingUser.hasRolePermission(UserRole.ADMIN);
  }

  public createDefinedNamespace() {
    this.dialog.open(CreateNamespaceDialogComponent, {
      width: '50%'
    }).afterClosed().subscribe(result => {
      if (result) {
        this.definedNamespaces.push(result);
        this.sortDefinedNamespaces();
      }
    });
  }

  public deleteDefinedNamespace(namespace: Namespace) {
    this.dialog.open(DeleteNamespaceDialogComponent, {
      width: '50%',
      data: {namespace}
    }).afterClosed().subscribe(result => {
      if (result) {
        this.definedNamespaces.splice(this.definedNamespaces.indexOf(result), 1);
        this.sortDefinedNamespaces();
      }
    });
  }

  public sortDefinedNamespaces() {
    let first = 0;
    let pageSize = this.pageSettings.pageSize;
    if (this.pageEvent) {
      first = this.pageEvent.pageIndex * this.pageEvent.pageSize;
      pageSize = this.pageEvent.pageSize;
    }
    this.sortedDefinedNamespaces = this.getSortedDefinedNamespaces().slice(first, first + pageSize);
  }

  public paginationEvent(event: PageEvent) {
    this.pageEvent = event;
    this.sortDefinedNamespaces();
  }

  public getSortedDefinedNamespaces(): Array<Namespace> {
    const data = this.definedNamespaces.slice();
    return data.sort();
  }

}
