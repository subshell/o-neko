import {Component, Inject, OnInit} from "@angular/core";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import { MAT_DIALOG_DATA, MatDialogRef } from "@angular/material/dialog";
import {DockerRegistry} from "../../docker/docker-registry";
import {RestService} from "../../rest/rest.service";
import {ProjectMesh} from "../project-mesh";

@Component({
  selector: 'create-project-mesh-dialog',
  templateUrl: './create-project-mesh-dialog.component.html',
  styleUrls: ['./create-project-mesh-dialog.component.scss']
})
export class CreateProjectMeshDialogComponent implements OnInit {

  public yetExistingProjectMeshes: Array<ProjectMesh> = [];
  public dockerRegistries: Array<DockerRegistry> = [];

  meshNameFormGroup: FormGroup;

  private newMesh: ProjectMesh = new ProjectMesh();

  constructor(public dialogRef: MatDialogRef<CreateProjectMeshDialogComponent>,
              @Inject(MAT_DIALOG_DATA) data: Array<ProjectMesh>,
              private rest: RestService,
              private formBuilder: FormBuilder) {
    this.rest.docker().getAllDockerRegistries().subscribe(regs => this.dockerRegistries = regs);
    this.yetExistingProjectMeshes = data;
  }

  ngOnInit() {
    this.meshNameFormGroup = this.formBuilder.group({
      nameCtrl: ['', Validators.required]
    });
  }

  public getCollidingProjectMeshName(): string {
    let nameFromForm = this.getMeshNameFromForm();
    if (!nameFromForm) {
      return null;
    } else {
      let normalizedName = nameFromForm.toLocaleLowerCase().trim();
      for (let yetExistingProjectMesh of this.yetExistingProjectMeshes) {
        if (yetExistingProjectMesh.name.toLocaleLowerCase().trim() === normalizedName) {
          return yetExistingProjectMesh.name;
        }
      }
    }
  }

  public cancel() {
    this.dialogRef.close();
  }

  public finish() {
    this.newMesh.name = this.getMeshNameFromForm();
    this.dialogRef.close(this.newMesh);
  }

  private getMeshNameFromForm(): string {
    return this.meshNameFormGroup.value['nameCtrl'];
  }

}
