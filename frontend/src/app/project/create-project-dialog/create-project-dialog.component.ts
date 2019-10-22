import {Component, Inject, OnInit} from "@angular/core";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {DockerRegistry} from "../../docker/docker-registry";
import {RestService} from "../../rest/rest.service";
import {Project} from "../project";

@Component({
  selector: 'create-project-dialog',
  templateUrl: './create-project-dialog.component.html',
  styleUrls: ['./create-project-dialog.component.scss']
})
export class CreateProjectDialogComponent implements OnInit {

  public yetExistingProjects: Array<Project> = [];
  public dockerRegistries: Array<DockerRegistry> = [];

  projectNameFormGroup: FormGroup;
  dockerRegistryFormGroup: FormGroup;
  imageNameFormGroup: FormGroup;

  private newProject: Project = new Project();

  constructor(public dialogRef: MatDialogRef<CreateProjectDialogComponent>,
              @Inject(MAT_DIALOG_DATA) data: Array<Project>,
              private rest: RestService,
              private formBuilder: FormBuilder) {
    this.rest.docker().getAllDockerRegistries().subscribe(regs => this.dockerRegistries = regs);
    this.yetExistingProjects = data;
  }

  ngOnInit() {
    this.projectNameFormGroup = this.formBuilder.group({
      nameCtrl: ['', Validators.required]
    });
    this.dockerRegistryFormGroup = this.formBuilder.group({
      registryUuidCtrl: ['', Validators.required]
    });
    this.imageNameFormGroup = this.formBuilder.group({
      imageNameCtrl: ['', Validators.required]
    });
  }

  public getCollidingProjectName(): string {
    let projectNameFromForm = this.getProjectNameFromForm();
    if (!projectNameFromForm) {
      return null;
    } else {
      let normalizedName = projectNameFromForm.toLocaleLowerCase().trim();
      for (let yetExistingProject of this.yetExistingProjects) {
        if (yetExistingProject.name.toLocaleLowerCase().trim() === normalizedName) {
          return yetExistingProject.name;
        }
      }
    }
  }

  public cancel() {
    this.dialogRef.close();
  }

  public finish() {
    this.newProject.name = this.getProjectNameFromForm();
    this.newProject.dockerRegistryUUID = this.dockerRegistryFormGroup.value['registryUuidCtrl'];
    this.newProject.imageName = this.imageNameFormGroup.value['imageNameCtrl'];
    //TODO: check duplicate names maybe?
    this.dialogRef.close(this.newProject);
  }

  private getProjectNameFromForm(): string {
    return this.projectNameFormGroup.value['nameCtrl'];
  }

}
