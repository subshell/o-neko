import {Component, Inject} from "@angular/core";
import {MAT_LEGACY_DIALOG_DATA as MAT_DIALOG_DATA, MatLegacyDialogRef as MatDialogRef} from "@angular/material/legacy-dialog";
import {zip} from "rxjs";
import {RestService} from "../../rest/rest.service";
import {User} from "../user";
import {UserRole} from "../user-role";

import {UserService} from "../user.service";

@Component({
  selector: 'user-edit-dialog',
  templateUrl: './user-edit-dialog.html',
  styleUrls: ['./user-edit-dialog.scss']
})
export class UserEditDialog {

  public user: User;
  public editingUser: User;
  public passwordVerification: string;
  public roles: Array<{
    value: UserRole,
    name: string
  }> = [];
  public isNewUser: boolean;
  public readonly originalUser: User;

  constructor(public dialogRef: MatDialogRef<UserEditDialog>, @Inject(MAT_DIALOG_DATA) data: User, private rest: RestService, private userService: UserService) {
    this.userService.currentUser().subscribe(cu => this.editingUser = cu);
    this.user = data ? User.from(data) : new User();
    this.isNewUser = !data;
    if (data) {
      this.originalUser = User.from(data);
    }
    this.roles.push({
      value: UserRole.ADMIN,
      name: 'Admin'
    }, {
      value: UserRole.DOER,
      name: 'Doer'
    }, {
      value: UserRole.VIEWER,
      name: 'Viewer'
    });
  }

  private _passwordFieldsVisible = false;

  get passwordFieldsVisible(): boolean {
    return this.isNewUser || this._passwordFieldsVisible;
  }

  set passwordFieldsVisible(value: boolean) {
    this._passwordFieldsVisible = value;
  }

  get canChangeRole(): boolean {
    return this.editingUser ? this.editingUser.hasRolePermission(UserRole.ADMIN) : undefined;
  }

  public close() {
    if (this.isNewUser) {
      this.rest.createUser(this.user).subscribe(created => {
        this.dialogRef.close(created);
      });
    } else {
      if (this.user.password) {
        zip(this.rest.changePassword(this.user.username, {password: this.user.password}), this.rest.updateUser(this.user))
          .subscribe(([ignore, updated]) => {
            this.dialogRef.close(updated);
          });
      } else {
        this.rest.updateUser(this.user, this.originalUser.username).subscribe(updated => {
          this.dialogRef.close(updated);
        });
      }
    }
  }

  public cancel() {
    this.dialogRef.close();
  }
}
