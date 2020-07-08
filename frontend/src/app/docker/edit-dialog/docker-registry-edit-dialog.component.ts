import {Component, Inject} from "@angular/core";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {MatSnackBar} from "@angular/material/snack-bar";
import {of} from "rxjs";
import {flatMap, withLatestFrom} from "rxjs/operators";
import {RestService} from "../../rest/rest.service";
import {User} from "../../user/user";
import {UserService} from "../../user/user.service";
import {DockerRegistry} from "../docker-registry";
import {TranslateService} from "@ngx-translate/core";

@Component({
  selector: 'docker-registry-edit-dialog',
  templateUrl: './docker-registry-edit-dialog.component.html',
  styleUrls: ['./docker-registry-edit-dialog.component.scss']
})
export class DockerRegistryEditDialogComponent {

  public dockerRegistry: DockerRegistry;
  public editingUser: User;
  public newPassword: string = '';
  public passwordVerification: string = '';

  constructor(public dialogRef: MatDialogRef<DockerRegistryEditDialogComponent>,
              @Inject(MAT_DIALOG_DATA) private data: { registry: DockerRegistry, readOnly: boolean },
              private rest: RestService,
              private userService: UserService,
              private snackBar: MatSnackBar,
              private translate: TranslateService) {
    this.userService.currentUser().subscribe(cu => this.editingUser = cu);
    this.dockerRegistry = data ? DockerRegistry.from(data.registry) : new DockerRegistry();
  }

  private _passwordFieldsVisible = false;

  get passwordFieldsVisible(): boolean {
    return this.dockerRegistry.isNew() || this._passwordFieldsVisible;
  }

  set passwordFieldsVisible(value: boolean) {
    this._passwordFieldsVisible = value;
  }

  public isReadonly(): boolean {
    return this.data && this.data.readOnly;
  }

  public save(): void {
    let isNew = this.dockerRegistry.isNew();
    this.rest.docker().persistDockerRegistry(this.dockerRegistry)
      .pipe(flatMap(savedDockerRegistry => {
        if (this.newPassword) {
          return this.rest.docker().changeDockerRegistryPassword(savedDockerRegistry, {password: this.newPassword});
        } else {
          return of(savedDockerRegistry);
        }
      })).subscribe(savedDockerRegistry => {
        const text = this.translate.instant('components.dockerRegistry.editDialog.registryHasBeenModifiedByAction', {registry: savedDockerRegistry.name, action: isNew ? 'created' : 'saved'});
        this.snackBar.open(text, null, {
          duration: 1000
        });
        this.dialogRef.close(savedDockerRegistry);
      });
  }

  public cancel(): void {
    this.dialogRef.close();
  }
}
