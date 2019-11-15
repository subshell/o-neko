import {Component, OnDestroy, OnInit} from "@angular/core";
import {FormControl, FormGroup, Validators} from "@angular/forms";
import {MatDialog} from "@angular/material";
import {ActivatedRoute, ParamMap} from "@angular/router";
import {Subscription} from "rxjs";
import {flatMap, switchMap} from "rxjs/operators";
import {ConfigurationTemplate} from "../../deployable/configuration-template";
import {EffectiveDeployableConfiguration} from "../../deployable/effective-deployable-configuration";
import {ShowDeployableConfigurationDialog} from "../../deployable/show-deployable-configuration-dialog/show-deployable-configuration-dialog.component";
import {KeyValueChangeEvent} from "../../form/key-value-input/key-value-input.component";
import {
  createValueInfoFromTemplateVariable,
  ValueInfoChangeEvent,
  ValueInfoMap
} from "../../form/value-input/value-info";
import {Project} from "../../project/project";
import {ProjectVersion} from "../../project/project-version";
import {RestService} from "../../rest/rest.service";
import {User} from "../../user/user";
import {UserService} from "../../user/user.service";
import {WebSocketServiceWrapper} from "../../websocket/web-socket-service-wrapper.service";
import {CreateMeshComponentDialogComponent} from "../create-mesh-component-dialog/create-mesh-component-dialog.component";
import {MeshComponent} from "../mesh-component";
import {ProjectMesh} from "../project-mesh";
import {ProjectMeshService} from "../project-mesh.service";

@Component({
  selector: 'manage-mesh-components',
  templateUrl: './manage-mesh-components.component.html',
  styleUrls: ['./manage-mesh-components.component.scss']
})
export class ManageMeshComponentsComponent implements OnInit, OnDestroy {

  public defaultConfigurationTemplatesCache: {[key: string]: ConfigurationTemplate[]} = {};
  public templateVariables: Map<string, ValueInfoMap> = new Map();
  public ownTemplateVariables: Map<string, string> = new Map();
  public projectMesh: ProjectMesh;
  public uberForm: FormGroup = new FormGroup({});

  private editingUser: User;
  private projects: Array<Project> = [];
  private updateSubscription?: Subscription;

  constructor(private rest: RestService,
              private projectMeshService: ProjectMeshService,
              private userService: UserService,
              private route: ActivatedRoute,
              private dialog: MatDialog,
              private websocket: WebSocketServiceWrapper) {
    this.userService.currentUser().subscribe(currentUser => this.editingUser = currentUser);
  }

  ngOnInit() {
    this.rest.project().getAllProjects().subscribe(projects => {
      this.projects = projects;

      this.route.paramMap.pipe(
        switchMap((params: ParamMap) => this.rest.projectMesh().getProjectMeshById(params.get('id')))
      ).subscribe(mesh => {
        this.projectMesh = mesh;
        for (let component of mesh.components) {
          const projectVersion = this.getSelectedProjectVersionForComponent(component);
          this.addFormForComponent(component);
          this.defaultConfigurationTemplatesCache[component.id] = this.getDefaultTemplatesForComponent(component);

          const templateVariableInfoMap: ValueInfoMap = {};
          this.templateVariables.set(component.id, templateVariableInfoMap);
          this.ownTemplateVariables = new Map(Object.entries(component.templateVariables));
          projectVersion.availableTemplateVariables.forEach(templateVariable => {
            this.ownTemplateVariables.delete(templateVariable.name);

            const value = projectVersion.templateVariables[templateVariable.name] === undefined
              ? component.templateVariables[templateVariable.name]
              : projectVersion.templateVariables[templateVariable.name];
            templateVariableInfoMap[templateVariable.name] = createValueInfoFromTemplateVariable(templateVariable, value);
          });
        }

        this.updateSubscription = this.websocket.getMeshComponentChanges(this.projectMesh.id)
          .pipe(flatMap(message => this.rest.projectMesh().getProjectMeshById(message.ownerId)))
          .subscribe(updatedMesh => {
            for (let updatedComponent of updatedMesh.components) {
              let componentToUpdate = this.projectMesh.components.find(comp => comp.id === updatedComponent.id);
              if (componentToUpdate) {
                componentToUpdate.urls = updatedComponent.urls;
                componentToUpdate.deployment = updatedComponent.deployment;
                componentToUpdate.outdated = updatedComponent.outdated;
              }
            }
          });
      });
    });
  }

  ngOnDestroy(): void {
    if (this.updateSubscription) {
      this.updateSubscription.unsubscribe();
    }
  }

  public mayEditComponents(): boolean {
    return this.projectMeshService.isUserAllowedToEditProjectMeshes(this.editingUser);
  }

  public onDefaultConfigurationTemplateChange(templates: Array<ConfigurationTemplate>, component: MeshComponent) {
    component.configurationTemplates = templates;
  }

  public onTemplatesValidationChange(stillValid: boolean) {
    //TODO...
  }

  public hasComponents(): boolean {
    return this.projectMesh.components.length > 0;
  }

  public getProjectOf(component: MeshComponent): Project {
    return this.projects.find(p => p.uuid === component.projectId);
  }

  public getProjectVersionsFor(component: MeshComponent): Array<ProjectVersion> {
    let p = this.getProjectOf(component);
    if (p) {
      return p.versions;
    } else {
      return [];
    }
  }

  public getSelectedProjectVersionForComponent(component: MeshComponent): ProjectVersion {
    return this.getProjectVersionsFor(component).find(version => version.uuid === component.projectVersionId);
  }

  public getDefaultTemplatesForComponent(component: MeshComponent): Array<ConfigurationTemplate> {
    const projectConfigurationTemplates: ConfigurationTemplate[] = this.getProjectOf(component).defaultConfigurationTemplates || [];
    const projectVersionConfigurationTemplates: ConfigurationTemplate[] = this.getSelectedProjectVersionForComponent(component).configurationTemplates || [];
    const projectVersionConfigurationTemplateNames = projectVersionConfigurationTemplates.map(t => t.name);

    return [
      ...projectConfigurationTemplates.filter(template => projectVersionConfigurationTemplateNames.indexOf(template.name) === -1),
      ...projectVersionConfigurationTemplates
    ];
  }

  public addComponent(): void {
    this.dialog.open(CreateMeshComponentDialogComponent, {
      width: "80%",
      data: {mesh: this.projectMesh, projects: this.projects}
    }).afterClosed().subscribe((newComponent: MeshComponent) => {
      if (newComponent) {
        this.projectMesh.components.push(newComponent);
        this.addFormForComponent(newComponent);
      }
    });
  }

  public removeComponent(component: MeshComponent): void {
    let index = this.projectMesh.components.indexOf(component, 0);
    if (index == -1) {
      return;
    }
    this.projectMesh.components.splice(index, 1);
    this.uberForm.removeControl(<any>index);
    //this.changeDetection.detectChanges();
  }

  public showEffectiveConfigurationOf(meshComponent: MeshComponent) {
    this.rest.projectMesh().getCalculatedMeshComponentConfiguration(meshComponent, this.projectMesh).subscribe((config: EffectiveDeployableConfiguration) =>
      this.dialog.open(ShowDeployableConfigurationDialog, {data: config, width: "80%"})
    );
  }

  public save() {
    this.projectMeshService.saveProjectMesh(this.projectMesh, this.editingUser).subscribe(m => this.projectMesh = m);
  }

  public updateTemplateVariables(component: MeshComponent, $event: ValueInfoChangeEvent) {
    component.templateVariables[$event.id] = $event.changedValue.selectedValue;
  }

  public updateOwnTemplateVariables(component: MeshComponent, $event: KeyValueChangeEvent) {
    if ($event.deletion) {
      delete component.templateVariables[$event.key];
      this.ownTemplateVariables.delete($event.key);
      return;
    }

    this.ownTemplateVariables.set($event.key, $event.value);
    component.templateVariables[$event.key] = $event.value;
  }

  private addFormForComponent(component: MeshComponent) {
    let nameCtrl = new FormControl(component.name, Validators.required);
    nameCtrl.valueChanges.subscribe(newName => component.name = newName);
    let projectIdCtrl = new FormControl(component.projectId, Validators.required);
    projectIdCtrl.disable();
    let projectVersionIdCtrl = new FormControl(component.projectVersionId, Validators.required);
    projectVersionIdCtrl.valueChanges.subscribe(newVersionId => {
        console.log("Changing project version id on " + component.name + " from " + component.projectVersionId + " to " + newVersionId);
        component.projectVersionId = newVersionId;
      }
    );
    if (!this.mayEditComponents()) {
      nameCtrl.disable();
      projectVersionIdCtrl.disable();
    }
    let subFormGroup = new FormGroup({nameCtrl, projectIdCtrl, projectVersionIdCtrl});
    let index = Object.keys(this.uberForm.controls).length;
    this.uberForm.addControl("" + index, subFormGroup);
  }
}
