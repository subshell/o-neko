import {Component, Inject} from "@angular/core";
import {MAT_LEGACY_SNACK_BAR_DATA as MAT_SNACK_BAR_DATA, MatLegacySnackBarRef as MatSnackBarRef} from "@angular/material/legacy-snack-bar";

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
