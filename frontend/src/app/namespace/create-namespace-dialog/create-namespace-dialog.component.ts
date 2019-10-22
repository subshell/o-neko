import {Component} from "@angular/core";
import {MatDialogRef} from "@angular/material/dialog";
import {MatSnackBar} from "@angular/material/snack-bar";
import {RestService} from "../../rest/rest.service";
import {User} from "../../user/user";
import {UserService} from "../../user/user.service";
import {DefinedNamespace} from "../defined-namespace";

@Component({
  selector: 'create-namespace-dialog',
  templateUrl: './create-namespace-dialog.component.html',
  styleUrls: ['./create-namespace-dialog.component.scss']
})
export class CreateNamespaceDialogComponent {

  public namespace: DefinedNamespace;
  public editingUser: User;

  constructor(public dialogRef: MatDialogRef<CreateNamespaceDialogComponent>,
              private rest: RestService,
              private userService: UserService,
              private snackBar: MatSnackBar) {
    this.userService.currentUser().subscribe(cu => this.editingUser = cu);
    this.namespace = new DefinedNamespace();
  }

  public save(): void {
    this.rest.namespace().persistDefinedNamespace(this.namespace)
      .subscribe(savedNamespace => {
        this.snackBar.open(`Namespace ${savedNamespace.name} has been created`, null, {
          duration: 1000
        });
        this.dialogRef.close(savedNamespace);
      });
  }

  public cancel(): void {
    this.dialogRef.close();
  }
}
