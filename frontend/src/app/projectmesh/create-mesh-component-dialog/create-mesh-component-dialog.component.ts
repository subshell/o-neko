import {Component, Inject, OnDestroy, OnInit} from "@angular/core";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import { MAT_DIALOG_DATA, MatDialogRef } from "@angular/material/dialog";
import {ReplaySubject, Subject} from "rxjs";
import {takeUntil} from "rxjs/operators";
import {Project} from "../../project/project";
import {ProjectVersion} from "../../project/project-version";
import {MeshComponent} from "../mesh-component";
import {ProjectMesh} from "../project-mesh";

export type CreateMeshComponentDialogData = { mesh: ProjectMesh, projects: Array<Project> };

@Component({
  selector: 'create-mesh-component-dialog',
  templateUrl: './create-mesh-component-dialog.component.html',
  styleUrls: ['./create-mesh-component-dialog.component.scss']
})
export class CreateMeshComponentDialogComponent implements OnInit, OnDestroy {

  public projectMesh: ProjectMesh;
  public projects: Array<Project> = [];

  componentNameFormGroup: FormGroup;
  projectFormGroup: FormGroup;
  projectVersionGroup: FormGroup;
  public filteredProjects: ReplaySubject<Array<Project>> = new ReplaySubject<Array<Project>>(1);
  public filteredProjectVersions: ReplaySubject<Array<ProjectVersion>> = new ReplaySubject<Array<ProjectVersion>>(1);
  private newComponent: MeshComponent = new MeshComponent();
  private _onDestroy = new Subject<void>();

  constructor(public dialogRef: MatDialogRef<CreateMeshComponentDialogComponent>,
              @Inject(MAT_DIALOG_DATA) data: CreateMeshComponentDialogData,
              private formBuilder: FormBuilder) {
    this.projectMesh = data.mesh;
    this.projects = data.projects;
  }

  ngOnInit() {
    this.componentNameFormGroup = this.formBuilder.group({
      nameCtrl: ['', Validators.required]
    });
    this.projectFormGroup = this.formBuilder.group({
      projectIdCtrl: ['', Validators.required],
      projectFilterCtrl: ['']
    });
    this.projectVersionGroup = this.formBuilder.group({
      projectVersionIdCtrl: ['', Validators.required],
      projectVersionFilterCtrl: ['']
    });
    this.filteredProjects.next(this.projects.slice());
    this.filteredProjectVersions.next([]);
    this.projectFormGroup.controls['projectIdCtrl'].valueChanges
      .pipe(takeUntil(this._onDestroy))
      .subscribe(selectedId => {
        let selectedProject = this.projects.find(p => p.uuid === selectedId);
        if (selectedProject) {
          this.filteredProjectVersions.next(selectedProject.versions.slice());
        }
      });
  }

  ngOnDestroy(): void {
    this._onDestroy.next();
    this._onDestroy.complete();
  }

  public filterProjects($event) {
    if (!this.projects) {
      return;
    }
    let search: string = $event;
    if (!search) {
      this.filteredProjects.next(this.projects.slice());
      return;
    } else {
      search = search.toLowerCase();
    }
    this.filteredProjects.next(this.projects.filter(p => p.name.toLocaleLowerCase().indexOf(search) > -1));
  }

  public cancel() {
    this.dialogRef.close();
  }

  public isProjectSelected(): boolean {
    return !!this.projectFormGroup.value['projectIdCtrl'];
  }

  public finish() {
    this.newComponent.name = this.getNameFromForm();
    this.newComponent.projectId = this.getProjectFromForm().uuid;
    this.newComponent.projectVersionId = this.getProjectVersionFromForm().uuid;
    this.dialogRef.close(this.newComponent);
  }

  private filterProjectVersions($event) {
    if (!this.isProjectSelected()) {
      this.filteredProjectVersions.next([]);
      return;
    }
    let search: string = $event;
    if (!search) {
      this.filteredProjectVersions.next(this.getProjectFromForm().versions.slice());
      return;
    } else {
      search = search.toLowerCase();
    }
    this.filteredProjectVersions.next(this.getProjectFromForm().versions.filter(v => v.name.toLocaleLowerCase().indexOf(search) > -1));
  }

  private getNameFromForm(): string {
    return this.componentNameFormGroup.value['nameCtrl'];
  }

  private getProjectFromForm(): Project {
    let projectId = this.projectFormGroup.value['projectIdCtrl'];
    if (projectId) {
      return this.projects.find(p => p.uuid === projectId);
    }
  }

  private getProjectVersionFromForm(): ProjectVersion {
    let versionId = this.projectVersionGroup.value['projectVersionIdCtrl'];
    let projectFromForm = this.getProjectFromForm();
    if (versionId && projectFromForm) {
      return projectFromForm.versions.find(v => v.uuid === versionId);
    }
  }

}
