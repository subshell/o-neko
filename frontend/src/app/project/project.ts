import {isNil, map} from 'lodash';
import {ConfigurationTemplate} from "../deployable/configuration-template";
import {ValueInfo} from "../form/value-input/value-info";
import {ProjectVersion} from "./project-version";

export type DeploymentBehaviour = 'automatically' | 'manually';

export enum AggregatedDeploymentStatus {
  Ok = "Ok", Pending = "Pending", Error = "Error", NotDeployed = "NotDeployed"
}

export interface LifetimeBehaviour {
  daysToLive: number;
}

export interface ProjectDTO {
  uuid?: string;
  name?: string;
  imageName: string;
  newVersionsDeploymentBehaviour: DeploymentBehaviour;
  defaultConfigurationTemplates: Array<ConfigurationTemplate>;
  templateVariables: TemplateVariable[];
  dockerRegistryUUID?: string;
  versions: Array<ProjectVersion>;
  status: AggregatedDeploymentStatus;
  defaultLifetimeBehaviour?: LifetimeBehaviour;
}

export interface TemplateVariable {
  id: string;
  name: string;
  label: string;
  values: Array<string>;
  useValues: boolean;
  defaultValue: string;
  showOnDashboard: boolean;
}

export class Project implements ProjectDTO {
  name?: string;
  imageName: string;
  newVersionsDeploymentBehaviour: DeploymentBehaviour;
  defaultConfigurationTemplates: Array<ConfigurationTemplate>;
  templateVariables: Array<TemplateVariable> = [];
  dockerRegistryUUID?: string;
  versions: Array<ProjectVersion>;
  status: AggregatedDeploymentStatus;
  defaultLifetimeBehaviour?: LifetimeBehaviour;

  constructor(public readonly uuid?: string) {
  }

  static from(from: ProjectDTO): Project {
    let project = new Project(from.uuid);
    project.name = from.name;

    project.imageName = from.imageName;
    project.newVersionsDeploymentBehaviour = from.newVersionsDeploymentBehaviour;
    project.defaultConfigurationTemplates = from.defaultConfigurationTemplates || [];
    project.templateVariables = from.templateVariables || [];
    project.dockerRegistryUUID = from.dockerRegistryUUID;
    project.versions = map(from.versions, version => ProjectVersion.from(version));
    project.status = from.status;
    project.defaultLifetimeBehaviour = from.defaultLifetimeBehaviour;

    return project;
  }

  /**
   * Informs, whether this is a new project (thus a project that has never been persisted so far).
   *
   * @returns {boolean}
   */
  public isNew(): boolean {
    return isNil(this.uuid);
  }

  public isOrphan(): boolean {
    return !this.dockerRegistryUUID;
  }
}
