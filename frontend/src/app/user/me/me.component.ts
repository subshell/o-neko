import {Component} from "@angular/core";
import {MatLegacyDialog as MatDialog} from "@angular/material/legacy-dialog";
import {MatLegacySnackBar as MatSnackBar} from "@angular/material/legacy-snack-bar";
import {Router} from "@angular/router";

import {RestService} from "../../rest/rest.service";
import {AuthService} from "../../session/auth.service";
import {ConfirmDialog, ConfirmDialogData} from "../../util/confirm-dialog/confirm-dialog.component";
import {CreateApiTokenDialogComponent} from "../api-token-dialog/create-api-token-dialog.component";
import {ApiToken} from "../api-token";
import {UserEditDialog} from "../edit-dialog/user-edit-dialog.component";
import {User} from "../user";
import {zip} from "rxjs";
import {TranslateService} from "@ngx-translate/core";

@Component({
  selector: 'me',
  templateUrl: './me.component.html',
  styleUrls: ['./me.component.scss']
})
export class MeComponent {

  public me: User;
  public apiTokens: ApiToken[] = [];

  constructor(private rest: RestService,
              private dialog: MatDialog,
              private snackBar: MatSnackBar,
              private auth: AuthService,
              private router: Router,
              private readonly translate: TranslateService) {
    this.rest.currentUser().subscribe(user => {
      this.me = user;
      this.loadTokens();
    });
  }

  public editAccount() {
    this.dialog.open(UserEditDialog, {
      data: this.me,
      width: "80%"
    }).afterClosed().subscribe((result: User) => {
      if (result) {
        if (result.username !== this.me.username) {
          this.rest.logout().subscribe().add(() => this.router.navigate(['login']));
        } else {
          this.me = result;
        }
      }
    });
  }

  public deleteAccount() {
    this.dialog.open(ConfirmDialog, {
      data: <ConfirmDialogData>{
        title: this.translate.instant('components.user.me.deleteDialog.title'),
        message: this.translate.instant('components.user.me.deleteDialog.message'),
        okButtonText: this.translate.instant('components.user.me.deleteDialog.okButtonText')
      },
      width: "50%"
    }).afterClosed().subscribe(result => {
      if (result === true) {
        zip(this.rest.deleteUser(this.me), this.rest.logout()).subscribe().add(() => this.router.navigate(['login']));
      }
    });
  }

  public createApiToken() {
    const dialogRef = this.dialog.open(CreateApiTokenDialogComponent, {
      width: "500px"
    });
    dialogRef.componentInstance.username = this.me.username;
    dialogRef.afterClosed().subscribe(created => {
      if (created) {
        this.loadTokens();
      }
    });
  }

  public deleteApiToken(token: ApiToken) {
    this.dialog.open(ConfirmDialog, {
      data: <ConfirmDialogData>{
        title: this.translate.instant('components.user.me.apiTokens.deleteTitle'),
        message: this.translate.instant('components.user.me.apiTokens.deleteMessage', {name: token.name}),
        okButtonText: this.translate.instant('components.user.me.apiTokens.deleteConfirm')
      },
      width: "50%"
    }).afterClosed().subscribe(result => {
      if (result === true) {
        this.rest.deleteApiToken(this.me.username, token.id).subscribe(() => {
          this.loadTokens();
          this.snackBar.open(this.translate.instant('components.user.me.apiTokens.deleted'), null, {duration: 2000});
        });
      }
    });
  }

  private loadTokens() {
    if (this.me) {
      this.rest.getApiTokens(this.me.username).subscribe(tokens => {
        this.apiTokens = tokens;
      });
    }
  }
}
