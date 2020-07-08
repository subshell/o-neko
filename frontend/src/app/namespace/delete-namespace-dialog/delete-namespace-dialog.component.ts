import {Component, Inject} from "@angular/core";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {MatSnackBar} from "@angular/material/snack-bar";
import {RestService} from "../../rest/rest.service";
import {DefinedNamespace} from "../defined-namespace";
import {TranslateService} from "@ngx-translate/core";

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
              private snackBar: MatSnackBar,
              private translateService: TranslateService) {
  }

  get namespace(): DefinedNamespace {
    return this.data.namespace;
  }

  public confirm(): void {
    this.rest.namespace().deleteDefinedNamespace(this.namespace).subscribe(() => {
      const text = this.translateService.instant('components.namespace.namespaceAction', {
        namespace: this.namespace.name,
        action: 'deleted'
      });
      this.snackBar.open(text, null, {
        duration: 1000
      });
      this.dialogRef.close(this.namespace);
    });
  }

  public deny(): void {
    this.dialogRef.close();
  }
}
