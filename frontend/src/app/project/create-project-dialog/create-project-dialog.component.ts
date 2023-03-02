import {Component, Inject, OnInit} from '@angular/core';
import {UntypedFormBuilder, UntypedFormGroup, Validators} from '@angular/forms';
import {MAT_LEGACY_DIALOG_DATA as MAT_DIALOG_DATA, MatLegacyDialogRef as MatDialogRef} from '@angular/material/legacy-dialog';
import {DockerRegistry} from '../../registries/docker/docker-registry';
import {RestService} from '../../rest/rest.service';
import {Project} from '../project';
import {FileReaderService} from '../../form/upload/file-reader.service';
import {MatLegacySnackBar as MatSnackBar} from '@angular/material/legacy-snack-bar';
import {ProjectExportDTO} from '../project-export';
import {TranslateService} from "@ngx-translate/core";

export interface CreateProjectDialogComponentData {
  projects: Array<Project>;
  showImport: boolean
}

@Component({
  selector: 'create-project-dialog',
  templateUrl: './create-project-dialog.component.html',
  styleUrls: ['./create-project-dialog.component.scss']
})
export class CreateProjectDialogComponent implements OnInit {
  public showImport: boolean;
  public yetExistingProjects: Array<Project> = [];
  public dockerRegistries: Array<DockerRegistry> = [];

  public projectExport?: ProjectExportDTO;
  public projectImportFormGroup: UntypedFormGroup;

  public projectNameFormGroup: UntypedFormGroup;
  public dockerRegistryFormGroup: UntypedFormGroup;
  public imageNameFormGroup: UntypedFormGroup;

  private newProject: Project = new Project();

  constructor(public readonly dialogRef: MatDialogRef<CreateProjectDialogComponent>,
              @Inject(MAT_DIALOG_DATA) readonly data: CreateProjectDialogComponentData,
              private readonly rest: RestService,
              private readonly formBuilder: UntypedFormBuilder,
              private readonly snackBar: MatSnackBar,
              private readonly translate: TranslateService) {
    this.rest.docker().getAllDockerRegistries().subscribe(regs => this.dockerRegistries = regs);
    this.yetExistingProjects = data.projects;
    this.showImport = data.showImport;
  }

  ngOnInit() {
    this.projectImportFormGroup = this.formBuilder.group({
      file: [null]
    });
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

  public async onProjectExportUpload($event: FileList | File[]) {
    const [file] = await new FileReaderService().readAll($event);

    if (typeof file.content !== 'string') {
      this.snackBar.open(this.translate.instant('components.project.createProjectDialog.couldNotUploadFile', {name: file.name}), null, {
        duration: 1000
      });
      return;
    }

    try {
      const projectExport = JSON.parse(file.content);
      this.newProject = Project.fromProjectExport(projectExport);
      this.projectExport = projectExport;
    } catch (e) {
      console.error('Error while parsing exported project', e);
      this.snackBar.open(this.translate.instant('components.project.createProjectDialog.errorParsingConfiguration'), null, {
        duration: 1000
      });
      return;
    }

    this.projectImportFormGroup.setValue({file: file});
    this.projectNameFormGroup.setValue({nameCtrl: this.projectExport.name});
    this.imageNameFormGroup.setValue({imageNameCtrl: this.projectExport.imageName});

    const dockerRegistry = this.dockerRegistries.find(dockerRegistry => dockerRegistry.uuid === this.projectExport.dockerRegistryUUID);
    if (dockerRegistry) {
      this.dockerRegistryFormGroup.setValue({registryUuidCtrl: dockerRegistry.uuid});
    }
  }

  private getProjectNameFromForm(): string {
    return this.projectNameFormGroup.value['nameCtrl'];
  }

}
