import {Component, Inject} from "@angular/core";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {MatSnackBar} from "@angular/material/snack-bar";
import {RestService} from "../../rest/rest.service";
import {DockerRegistry} from "../docker-registry";

@Component({
  selector: 'confirm-deletion-of-docker-registry-dialog',
  templateUrl: './confirm-deletion-dialog.component.html',
  styleUrls: ['./confirm-deletion-dialog.component.scss']
})
export class ConfirmDeletionDialogComponent {

  confirmedName: string;

  constructor(public dialogRef: MatDialogRef<ConfirmDeletionDialogComponent>,
              @Inject(MAT_DIALOG_DATA) private data: { registry: DockerRegistry, projectNames: Array<String> },
              private rest: RestService,
              private snackBar: MatSnackBar) {
  }

  get dockerRegistry(): DockerRegistry {
    return this.data.registry;
  }

  get projectNames(): Array<String> {
    return this.data.projectNames;
  }

  public confirm(): void {
    this.rest.docker().deleteDockerRegistry(this.dockerRegistry).subscribe(() => {
      this.snackBar.open(`Docker registry ${this.dockerRegistry.name} has been deleted.`, null, {
        duration: 1000
      });
      this.dialogRef.close(this.dockerRegistry);
    });
  }

  public deny(): void {
    this.dialogRef.close();
  }
}
