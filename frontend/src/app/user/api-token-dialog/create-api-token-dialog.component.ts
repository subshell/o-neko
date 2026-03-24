import {Component} from "@angular/core";
import {MatLegacyDialogRef as MatDialogRef} from "@angular/material/legacy-dialog";
import {MatLegacySnackBar as MatSnackBar} from "@angular/material/legacy-snack-bar";
import {Clipboard} from "@angular/cdk/clipboard";
import {TranslateService} from "@ngx-translate/core";
import {RestService} from "../../rest/rest.service";
import {CreateApiTokenResponse} from "../api-token";

@Component({
  selector: 'create-api-token-dialog',
  templateUrl: './create-api-token-dialog.component.html',
  styleUrls: ['./create-api-token-dialog.component.scss']
})
export class CreateApiTokenDialogComponent {

  tokenName: string = '';
  expiresAt: Date | null = null;
  username: string;
  minDate: Date = new Date();
  maxDate: Date = new Date(new Date().setFullYear(new Date().getFullYear() + 1));

  createdToken: CreateApiTokenResponse | null = null;
  isCreating: boolean = false;

  constructor(public dialogRef: MatDialogRef<CreateApiTokenDialogComponent>,
              private rest: RestService,
              private snackBar: MatSnackBar,
              private clipboard: Clipboard,
              private readonly translate: TranslateService) {
  }

  createToken(): void {
    if (!this.tokenName || this.isCreating) {
      return;
    }

    this.isCreating = true;
    const request = {
      name: this.tokenName,
      expiresAt: this.expiresAt ? this.expiresAt.toISOString() : null
    };

    this.rest.createApiToken(this.username, request).subscribe({
      next: (response) => {
        this.createdToken = response;
        this.isCreating = false;
      },
      error: () => {
        this.isCreating = false;
        this.snackBar.open(this.translate.instant('components.user.me.apiTokens.createError'), null, {duration: 3000});
      }
    });
  }

  copyToken(): void {
    if (this.createdToken) {
      this.clipboard.copy(this.createdToken.rawToken);
      this.snackBar.open(this.translate.instant('components.user.me.apiTokens.copied'), null, {duration: 2000});
    }
  }

  close(): void {
    this.dialogRef.close(this.createdToken != null);
  }
}
