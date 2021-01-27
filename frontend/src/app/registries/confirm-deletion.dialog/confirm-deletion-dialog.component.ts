import {Component, Inject} from "@angular/core";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {MatSnackBar} from "@angular/material/snack-bar";
import {RestService} from "../../rest/rest.service";
import {TranslateService} from "@ngx-translate/core";
import {Registry} from "../registry";
import {Observable} from "rxjs";

export interface ConfirmDeletionDialogData {
  registry: Registry
  projectNames: Array<String>
  translationId: string
  onConfirm: Observable<void>
}

@Component({
  selector: 'confirm-deletion-of-docker-registry-dialog',
  templateUrl: './confirm-deletion-dialog.component.html',
  styleUrls: ['./confirm-deletion-dialog.component.scss']
})
export class ConfirmDeletionDialogComponent {

  confirmedName: string;

  constructor(public dialogRef: MatDialogRef<ConfirmDeletionDialogComponent>,
              @Inject(MAT_DIALOG_DATA) private data: ConfirmDeletionDialogData,
              private rest: RestService,
              private snackBar: MatSnackBar,
              private translate: TranslateService) {

  }

  get registry(): Registry {
    return this.data.registry;
  }

  get translationId(): string {
    return this.data.translationId;
  }

  get projectNames(): Array<String> {
    return this.data.projectNames;
  }

  get onConfirm(): Observable<void> {
    return this.data.onConfirm;
  }

  public confirm(): void {
    this.onConfirm.subscribe(() => {
      const text = this.translate.instant(`components.${this.translationId}.editDialog.registryHasBeenModifiedByAction`, {
        registry: this.registry.name,
        action: 'deleted'
      });
      this.snackBar.open(text, null, {
        duration: 1000
      });
      this.dialogRef.close(this.registry);
    });
  }

  public deny(): void {
    this.dialogRef.close();
  }
}
