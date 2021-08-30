import {Component, Inject} from "@angular/core";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {TranslateService} from "@ngx-translate/core";

export interface ConfirmDialogData {
  title: string
  message: string
  okButtonText?: string
  cancelButtonText?: string
}

@Component({
  selector: 'confirm-dialog',
  templateUrl: './confirm-dialog.component.html'
})
export class ConfirmDialog {
  public title: string;
  public message: string;
  public okButtonText: string;
  public cancelButtonText: string;

  constructor(public dialogRef: MatDialogRef<ConfirmDialog>,
              @Inject(MAT_DIALOG_DATA) data: ConfirmDialogData,
              readonly translate: TranslateService) {
    this.title = data.title;
    this.message = data.message;
    this.okButtonText = data.okButtonText || translate.instant('general.ok');
    this.cancelButtonText = data.cancelButtonText || translate.instant('general.cancel');
  }
}
