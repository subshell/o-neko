import {Component, Inject} from "@angular/core";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";

export interface ConfirmWithTextDialogData {
  title: string
  message: string
  confirmationText: string
  okButtonText?: string
  cancelButtonText?: string
  confirmationTextPlaceholder?: string
}

@Component({
  selector: 'confirm-with-text-dialog',
  templateUrl: './confirm-with-text-dialog.component.html',
  styleUrls: ['./confirm-with-text-dialog.component.scss']
})
export class ConfirmWithTextDialog {
  public title: string;
  public message: string;
  public okButtonText: string;
  public cancelButtonText: string;
  public confirmationText: string;
  public confirmationTextPlaceholder: string;

  public inputText: string;

  constructor(public dialogRef: MatDialogRef<ConfirmWithTextDialog>, @Inject(MAT_DIALOG_DATA) data: ConfirmWithTextDialogData) {
    this.title = data.title;
    this.message = data.message;
    this.confirmationText = data.confirmationText;
    this.okButtonText = data.okButtonText || "OK";
    this.cancelButtonText = data.cancelButtonText || "Cancel";
    this.confirmationTextPlaceholder = data.confirmationTextPlaceholder || "Confirm";
  }
}
