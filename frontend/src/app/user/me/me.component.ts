import {Component} from "@angular/core";
import {MatLegacyDialog as MatDialog} from "@angular/material/legacy-dialog";
import {Router} from "@angular/router";

import {RestService} from "../../rest/rest.service";
import {AuthService} from "../../session/auth.service";
import {ConfirmDialog, ConfirmDialogData} from "../../util/confirm-dialog/confirm-dialog.component";
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

  constructor(private rest: RestService,
              private dialog: MatDialog,
              private auth: AuthService,
              private router: Router,
              private readonly translate: TranslateService) {
    this.rest.currentUser().subscribe(user => this.me = user);
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

}
