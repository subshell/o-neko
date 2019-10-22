import {Component, Inject} from "@angular/core";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {MatSnackBar} from "@angular/material/snack-bar";
import {RestService} from "../../rest/rest.service";
import {DefinedNamespace} from "../defined-namespace";

@Component({
  selector: 'delete-namespace-dialog',
  templateUrl: './delete-namespace-dialog.component.html',
  styleUrls: ['./delete-namespace-dialog.component.scss']
})
export class DeleteNamespaceDialogComponent {

  confirmedName: string;

  constructor(public dialogRef: MatDialogRef<DeleteNamespaceDialogComponent>,
              @Inject(MAT_DIALOG_DATA) private data: { namespace: DefinedNamespace },
              private rest: RestService,
              private snackBar: MatSnackBar) {
  }

  get namespace(): DefinedNamespace {
    return this.data.namespace;
  }

  public confirm(): void {
    this.rest.namespace().deleteDefinedNamespace(this.namespace).subscribe(() => {
      this.snackBar.open(`The namespace ${this.namespace.name} has been deleted.`, null, {
        duration: 1000
      });
      this.dialogRef.close(this.namespace);
    });
  }

  public deny(): void {
    this.dialogRef.close();
  }
}
