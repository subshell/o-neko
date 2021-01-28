import {Component, Inject} from "@angular/core";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {MatSnackBar} from "@angular/material/snack-bar";
import {of} from "rxjs";
import {mergeMap} from "rxjs/operators";
import {RestService} from "../../../rest/rest.service";
import {User} from "../../../user/user";
import {UserService} from "../../../user/user.service";
import {HelmRegistry} from "../helm-registry";
import {TranslateService} from "@ngx-translate/core";

@Component({
  selector: 'helm-registry-edit-dialog',
  templateUrl: './helm-registry-edit-dialog.component.html',
  styleUrls: ['./helm-registry-edit-dialog.component.scss']
})
export class HelmRegistryEditDialogComponent {

  public helmRegistry: HelmRegistry;
  public editingUser: User;
  public newPassword: string = '';
  public passwordVerification: string = '';

  constructor(public dialogRef: MatDialogRef<HelmRegistryEditDialogComponent>,
              @Inject(MAT_DIALOG_DATA) private data: { registry: HelmRegistry, readOnly: boolean },
              private rest: RestService,
              private userService: UserService,
              private snackBar: MatSnackBar,
              private translate: TranslateService) {
    this.userService.currentUser().subscribe(cu => this.editingUser = cu);
    this.helmRegistry = data ? HelmRegistry.from(data.registry) : new HelmRegistry();
  }

  private _passwordFieldsVisible = false;

  get passwordFieldsVisible(): boolean {
    return this.helmRegistry.isNew() || this._passwordFieldsVisible;
  }

  set passwordFieldsVisible(value: boolean) {
    this._passwordFieldsVisible = value;
  }

  public isReadonly(): boolean {
    return this.data && this.data.readOnly;
  }

  public save(): void {
    let isNew = this.helmRegistry.isNew();
    this.rest.helm().persistHelmRegistry(this.helmRegistry, !isNew && this.newPassword)
      .pipe(mergeMap(savedHelmRegistry => {
        if (!isNew && this.newPassword) {
          return this.rest.helm().changeHelmRegistryPassword(savedHelmRegistry, {password: this.newPassword});
        } else {
          return of(savedHelmRegistry);
        }
      })).subscribe(savedHelmRegistry => {
        const text = this.translate.instant('components.helmRegistry.editDialog.registryHasBeenModifiedByAction', {registry: savedHelmRegistry.name, action: isNew ? 'created' : 'saved'});
        this.snackBar.open(text, null, {
          duration: 1000
        });
        this.dialogRef.close(savedHelmRegistry);
      });
  }

  public cancel(): void {
    this.dialogRef.close();
  }
}
