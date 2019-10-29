import {Component, OnDestroy, OnInit} from "@angular/core";
import {MatDialog} from "@angular/material/dialog";
import {ActivatedRoute, ParamMap} from "@angular/router";
import {Subscription} from "rxjs";

import {flatMap, map} from "rxjs/operators";
import {ConfigurationTemplate} from "../../deployable/configuration-template";
import {EffectiveDeployableConfiguration} from "../../deployable/effective-deployable-configuration";
import {ShowDeployableConfigurationDialog} from "../../deployable/show-deployable-configuration-dialog/show-deployable-configuration-dialog.component";
import {DockerRegistry} from "../../docker/docker-registry";
import {KeyValueChangeEvent} from "../../form/key-value-input/key-value-input.component";
import {
  LabeledLifetimeBehaviour,
  LifetimeBehaviourInputComponent
} from "../../form/lifetime-behaviour/lifetime-behaviour-input.component";
import {ValueInfoChangeEvent, ValueInfoMap} from "../../form/value-input/value-info";

import {DefinedNamespace} from "../../namespace/defined-namespace";
import {Namespace} from "../../namespace/namespace";
import {RestService} from "../../rest/rest.service";
import {User} from "../../user/user";
import {UserService} from "../../user/user.service";
import {WebSocketServiceWrapper} from "../../websocket/web-socket-service-wrapper.service";
import {Project} from "../project";
import {ProjectVersion} from "../project-version";
import {ProjectService} from "../project.service";
import {TemplateVariablesService} from "../template-variables.service";

@Component({
  selector: 'edit-project-version',
  templateUrl: './edit-project-version.component.html',
  styleUrls: ['./edit-project-version.component.scss']
})
export class EditProjectVersionComponent implements OnInit, OnDestroy {

  public project: Project;
  public projectVersion: ProjectVersion;
  public dockerRegistries: Array<DockerRegistry> = [];
  public namespaces: Array<DefinedNamespace> = [];
  public templatesValid = true;
  public lifetimeBehaviourOptions: Array<LabeledLifetimeBehaviour> = [{
    label: 'Inherit from project',
    value: -1
  }].concat(...LifetimeBehaviourInputComponent.defaultLifetimeBehaviourOptions);
  public projectVariables: ValueInfoMap = {};
  public projectVersionVariables: { [key: string]: string } = {};
  private editingUser: User;
  private updateSubscription?: Subscription;

  constructor(private rest: RestService,
              private projectService: ProjectService,
              private userService: UserService,
              private route: ActivatedRoute,
              private dialog: MatDialog,
              private templateVariablesService: TemplateVariablesService,
              private wsService: WebSocketServiceWrapper) {
    this.userService.currentUser().subscribe(currentUser => this.editingUser = currentUser);
    this.rest.docker().getAllDockerRegistries().subscribe(regs => this.dockerRegistries = regs);
    this.rest.namespace().getAllDefinedNamespaces().subscribe(namespaces => this.namespaces = namespaces);
  }

  ngOnInit() {
    this.route.paramMap.subscribe((params: ParamMap) => {
      let projectId = params.get('id');
      let projectVersionId = params.get('versionId');
      this.rest.project().getProjectById(projectId).subscribe(project => {
        this.project = project;
        this.projectVersion = project.versions.find(v => v.uuid === projectVersionId);

        if (!this.projectVersion.lifetimeBehaviour) {
          this.projectVersion.lifetimeBehaviour = {daysToLive: -1};
        }

        this.projectVersionVariables = {...this.projectVersion.templateVariables};

        this.projectVersion.availableTemplateVariables.forEach(variable => {
          delete this.projectVersionVariables[variable.name];
          this.projectVariables[variable.id] = this.templateVariablesService.createValueInfo(variable, this.projectVersion.templateVariables[variable.name]);
        });

        this.updateSubscription = this.wsService.getProjectVersionChanges(this.project.uuid)
          .pipe(
            flatMap(message => this.rest.project().getProjectById(projectId)),
            map(project => project.versions.find(v => v.uuid === this.projectVersion.uuid))
          ).subscribe(version => {
            this.projectVersion.urls = version.urls;
            this.projectVersion.deployment = version.deployment;
            this.projectVersion.outdated = version.outdated;
            //don't overwrite the rest, just in case the form is dirty.
          });
      });
    });
  }

  ngOnDestroy() {
    if (this.updateSubscription) {
      this.updateSubscription.unsubscribe();
    }
  }

  public mayEditProjects(): boolean {
    return this.projectService.isUserAllowedToEditProjects(this.editingUser);
  }

  public onConfigurationTemplateChange(templates: Array<ConfigurationTemplate>) {
    this.projectVersion.configurationTemplates = templates;
  }

  public onTemplatesValidationChange(stillValid: boolean) {
    this.templatesValid = stillValid;
  }

  public save() {
    this.projectService.saveProject(this.project, this.editingUser).subscribe(p => this.project = p);
  }

  public showEffectiveConfiguration() {
    this.rest.project().getCalculatedProjectVersionConfiguration(this.projectVersion, this.project).subscribe((config: EffectiveDeployableConfiguration) =>
      this.dialog.open(ShowDeployableConfigurationDialog, {data: config, width: "80%"})
    );
  }

  public compareNameSpaces(o1: DefinedNamespace | Namespace, o2: DefinedNamespace | Namespace): boolean {
    return o1 && o2 ? o1.name === o2.name : o1 === o2;
  }

  public updateProjectVariables(event: ValueInfoChangeEvent): void {
    this.projectVariables = event.values;
    this.projectVersion.templateVariables[event.changedValue.name] = event.changedValue.selectedValue;
  }

  public updateProjectVersionVariables(event: KeyValueChangeEvent): void {
    if (this.projectVersion.availableTemplateVariables.some(variable => variable.name === event.key)) {
      return;
    }

    if (event.deletion) {
      delete this.projectVersion.templateVariables[event.key];
    } else {
      this.projectVersion.templateVariables[event.key] = event.value;
    }
  }
}
