import {Component} from "@angular/core";
import {MatLegacyDialogRef as MatDialogRef} from "@angular/material/legacy-dialog";
import {MatLegacySnackBar as MatSnackBar} from "@angular/material/legacy-snack-bar";
import {RestService} from "../../rest/rest.service";
import {User} from "../../user/user";
import {UserService} from "../../user/user.service";
import {Namespace} from "../namespace";
import {TranslateService} from "@ngx-translate/core";

@Component({
  selector: 'create-namespace-dialog',
  templateUrl: './create-namespace-dialog.component.html',
  styleUrls: ['./create-namespace-dialog.component.scss']
})
export class CreateNamespaceDialogComponent {

  public namespace: Namespace;
  public editingUser: User;
  public readonly NAMESPACE_PREFIX = 'on-';

  constructor(public dialogRef: MatDialogRef<CreateNamespaceDialogComponent>,
              private rest: RestService,
              private userService: UserService,
              private snackBar: MatSnackBar,
              private translateService: TranslateService) {
    this.userService.currentUser().subscribe(cu => this.editingUser = cu);
    this.namespace = new Namespace();
  }

  public save(): void {
    this.rest.namespace().persistDefinedNamespace(this.namespace)
      .subscribe(savedNamespace => {
        const text = this.translateService.instant('components.namespace.namespaceAction', {
          namespace: this.NAMESPACE_PREFIX + savedNamespace.name,
          action: 'created'
        });
        this.snackBar.open(text, null, {
          duration: 1000
        });
        this.dialogRef.close(savedNamespace);
      });
  }

  public cancel(): void {
    this.dialogRef.close();
  }
}
