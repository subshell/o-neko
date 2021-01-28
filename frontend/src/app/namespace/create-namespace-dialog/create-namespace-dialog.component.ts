import {Component} from "@angular/core";
import {MatDialogRef} from "@angular/material/dialog";
import {MatSnackBar} from "@angular/material/snack-bar";
import {RestService} from "../../rest/rest.service";
import {User} from "../../user/user";
import {UserService} from "../../user/user.service";
import {DefinedNamespace} from "../defined-namespace";
import {TranslateService} from "@ngx-translate/core";

@Component({
  selector: 'create-namespace-dialog',
  templateUrl: './create-namespace-dialog.component.html',
  styleUrls: ['./create-namespace-dialog.component.scss']
})
export class CreateNamespaceDialogComponent {

  public namespace: DefinedNamespace;
  public editingUser: User;
  public readonly NAMESPACE_PREFIX = 'on-';

  constructor(public dialogRef: MatDialogRef<CreateNamespaceDialogComponent>,
              private rest: RestService,
              private userService: UserService,
              private snackBar: MatSnackBar,
              private translateService: TranslateService) {
    this.userService.currentUser().subscribe(cu => this.editingUser = cu);
    this.namespace = new DefinedNamespace();
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
