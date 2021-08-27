import {Component} from "@angular/core";
import {MatDialog} from "@angular/material/dialog";
import {PageEvent} from "@angular/material/paginator";
import {Sort} from "@angular/material/sort";
import {RestService} from "../../rest/rest.service";
import {
  ConfirmWithTextDialog,
  ConfirmWithTextDialogData
} from "../../util/confirm-with-text-dialog/confirm-with-text-dialog.component";
import {LogService} from "../../util/log.service";
import {UserEditDialog} from "../edit-dialog/user-edit-dialog.component";
import {User} from "../user";
import {UserService} from "../user.service";
import {TranslateService} from "@ngx-translate/core";


@Component({
  selector: 'user-list',
  templateUrl: './user-list.component.html',
  styleUrls: ['./user-list.component.scss']
})
export class UserListComponent {

  public sortedUsers: Array<User>;
  public users: Array<User> = [];
  public selectedColumns = ['username', 'email', 'role'];

  public readonly columns: Array<{ label: string, value: string }>;
  public pageSettings = {
    pageSize: 10,
    pageSizeOptions: [10, 25, 50, 100]
  };
  private pageEvent: PageEvent;
  private sort: Sort;
  private log = LogService.getLogger(UserListComponent);

  constructor(private rest: RestService,
              private userService: UserService,
              private dialog: MatDialog,
              private readonly translate: TranslateService) {
    this.columns = [{
      label: this.translate.instant('components.user.list.username'),
      value: 'username'
    }, {
      label: this.translate.instant('components.user.list.email'),
      value: 'email'
    }, {
      label: this.translate.instant('components.user.list.firstName'),
      value: 'firstName'
    }, {
      label: this.translate.instant('components.user.list.lastName'),
      value: 'lastName'
    }, {
      label: this.translate.instant('components.user.list.role'),
      value: 'role'
    }];

    this.rest.allUsers().subscribe(users => {
      this.users = users;
      this.sortUsers();
    });
  }

  public createUser() {
    this.dialog.open(UserEditDialog, {
      width: "80%"
    }).afterClosed().subscribe(result => {
      if (result) {
        this.users.push(result);
        this.sortUsers();
      }
    });
  }

  public editUser(user: User) {
    this.dialog.open(UserEditDialog, {
      data: user,
      width: "80%"
    }).afterClosed().subscribe(result => {
      if (result) {
        this.updateUserInList(result);
        this.sortUsers();
      }
    });
  }

  public deleteUser(user: User) {
    this.dialog.open(ConfirmWithTextDialog, {
      data: <ConfirmWithTextDialogData>{
        title: this.translate.instant('components.user.list.deleteDialog.title', {username: user.username}),
        message: this.translate.instant('components.user.list.deleteDialog.message'),
        confirmationTextPlaceholder: this.translate.instant('components.user.list.deleteDialog.confirmationPlaceholder'),
        confirmationText: user.username,
        okButtonText: this.translate.instant('components.user.list.deleteDialog.okButtonText')
      },
      width: "50%"
    }).afterClosed().subscribe(result => {
      if (result === true) {
        this.rest.deleteUser(user).subscribe(res => {
          this.log.info(`User ${user.username} has been deleted.`);
          this.users.splice(this.users.indexOf(user), 1);
          this.sortUsers();
        });
      }
    });
  }

  public sortUsers(sort?: Sort) {
    this.sort = sort;
    let first = 0;
    let pageSize = this.pageSettings.pageSize;
    if (this.pageEvent) {
      first = this.pageEvent.pageIndex * this.pageEvent.pageSize;
      pageSize = this.pageEvent.pageSize;
    }
    this.sortedUsers = this.getSortedUsers(sort).slice(first, first + pageSize);
  }

  public paginationEvent(event: PageEvent) {
    this.pageEvent = event;
    this.sortUsers(this.sort);
  }

  public getSortedUsers(sort?: Sort): Array<User> {
    const data = this.users.slice();
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

  public columnActive(col: string): boolean {
    return this.selectedColumns.indexOf(col) !== -1;
  }

  private updateUserInList(user: User) {
    let idx = this.users.findIndex(usr => usr.uuid === user.uuid);
    this.users.splice(idx, 1, user);
  }

  private compare(a: any, b: any, isAsc: boolean): number {
    return (a < b ? -1 : 1) * (isAsc ? 1 : -1);
  }

}
