import {Component, Inject} from "@angular/core";
import {MAT_SNACK_BAR_DATA, MatSnackBarRef} from "@angular/material/snack-bar";

@Component({
  selector: 'timeout-snackbar',
  templateUrl: './timeout-snackbar.html',
})
export class TimeoutSnackbarComponent {
  public text: string;
  public duration: number;

  constructor(@Inject(MAT_SNACK_BAR_DATA) public data: { text }, public snackBarRef: MatSnackBarRef<TimeoutSnackbarComponent>) {
    this.duration = snackBarRef.containerInstance.snackBarConfig.duration;
    this.text = data.text;
  }

  dismiss(): void {
    this.snackBarRef.dismiss();
  }
}
