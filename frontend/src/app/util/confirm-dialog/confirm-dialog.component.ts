import {Component, Inject} from "@angular/core";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";

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

  constructor(public dialogRef: MatDialogRef<ConfirmDialog>, @Inject(MAT_DIALOG_DATA) data: ConfirmDialogData) {
    this.title = data.title;
    this.message = data.message;
    this.okButtonText = data.okButtonText || "OK";
    this.cancelButtonText = data.cancelButtonText || "Cancel";
  }
}
