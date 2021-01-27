import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, ParamMap, Router} from '@angular/router';
import {switchMap} from 'rxjs/operators';
import {ConfigurationTemplate} from '../../deployable/configuration-template';
import {DockerRegistry} from '../../registries/docker/docker-registry';
import {RestService} from '../../rest/rest.service';
import {User} from '../../user/user';
import {UserService} from '../../user/user.service';
import {DeploymentBehaviour, Project, TemplateVariable} from '../project';
import {ProjectService} from '../project.service';
import {FileDownloadService} from '../../util/file-download.service';

@Component({
  selector: 'edit-project',
  templateUrl: './edit-project.component.html',
  styleUrls: ['./edit-project.component.scss']
})
export class EditProjectComponent implements OnInit {

  public project: Project;
  public dockerRegistries: Array<DockerRegistry> = [];
  public templatesValid = true;
  public HasNewVersionsDeployment = class {
    constructor(private parent: EditProjectComponent) {
    }

    get deploymentBehaviour(): DeploymentBehaviour {
      return this.parent.project.newVersionsDeploymentBehaviour;
    }

    set deploymentBehaviour(deploymentBehaviour: DeploymentBehaviour) {
      this.parent.project.newVersionsDeploymentBehaviour = deploymentBehaviour;
    }
  };
  public newVersionsDeploymentBehaviourAccess = new this.HasNewVersionsDeployment(this);
  private editingUser: User;

  constructor(private rest: RestService,
              private projectService: ProjectService,
              private userService: UserService,
              private route: ActivatedRoute,
              private router: Router) {
    this.userService.currentUser().subscribe(currentUser => this.editingUser = currentUser);
    this.rest.docker().getAllDockerRegistries().subscribe(regs => this.dockerRegistries = regs);
  }

  ngOnInit() {
    this.route.paramMap.pipe(
      switchMap((params: ParamMap) => this.rest.project().getProjectById(params.get('id')))
    ).subscribe(project => {
      this.project = project;
      if (!this.project.defaultConfigurationTemplates) {
        this.project.defaultConfigurationTemplates = [];
      }
      if (!project.defaultLifetimeBehaviour) {
        project.defaultLifetimeBehaviour = {daysToLive: undefined};
      }
    });
  }

  public mayEditProjects(): boolean {
    return this.projectService.isUserAllowedToEditProjects(this.editingUser);
  }

  public onDefaultConfigurationTemplateChange(templates: Array<ConfigurationTemplate>) {
    this.project.defaultConfigurationTemplates = templates;
  }

  public onTemplatesValidationChange(stillValid: boolean) {
    this.templatesValid = stillValid;
  }

  public save() {
    this.projectService.saveProject(this.project, this.editingUser).subscribe(p => this.project = p);
  }

  public delete() {
    this.projectService.deleteProjectInteractively(this.project, this.editingUser).subscribe(() => this.router.navigateByUrl('/projects'));
  }

  onAddTemplateVariable(templateVariable: TemplateVariable) {
    this.project.templateVariables.push(templateVariable);
  }

  onDeleteTemplateVariable(templateVariable: TemplateVariable) {
    const index = this.project.templateVariables.indexOf(templateVariable);
    this.project.templateVariables.splice(index, 1);
  }

  public exportProject(): void {
    this.projectService.exportProject(this.project, this.editingUser).subscribe(projectExport => {
      FileDownloadService.downloadJSON(projectExport, `${projectExport.name}.json`);
    });
  }
}
