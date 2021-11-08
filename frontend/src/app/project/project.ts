import {ConfigurationTemplate, ConfigurationTemplateDTO} from '../deployable/configuration-template';
import {ProjectVersion} from './project-version';
import {ProjectExportDTO, SUPPORTED_VERSION} from './project-export';

export type DeploymentBehaviour = 'automatically' | 'manually';

export enum AggregatedDeploymentStatus {
  Ok = "Ok", Pending = "Pending", Error = "Error", NotDeployed = "NotDeployed"
}

export type LifetimeType = 'UNTIL_TONIGHT' | 'UNTIL_WEEKEND' | 'DAYS' | 'INFINITE' | 'INHERIT';

export interface LifetimeBehaviour {
  type: LifetimeType
  value?: number
}

export interface ProjectDTO {
  uuid?: string;
  name?: string;
  imageName: string;
  newVersionsDeploymentBehaviour: DeploymentBehaviour;
  urlTemplates: Array<string>;
  defaultConfigurationTemplates: Array<ConfigurationTemplate>;
  templateVariables: TemplateVariable[];
  dockerRegistryUUID?: string;
  versions: Array<ProjectVersion>;
  status: AggregatedDeploymentStatus;
  defaultLifetimeBehaviour?: LifetimeBehaviour;
  namespace: string;
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
  urlTemplates: Array<string>;
  defaultConfigurationTemplates: Array<ConfigurationTemplate>;
  templateVariables: Array<TemplateVariable> = [];
  dockerRegistryUUID?: string;
  versions: Array<ProjectVersion>;
  status: AggregatedDeploymentStatus;
  defaultLifetimeBehaviour?: LifetimeBehaviour;
  namespace: string;

  constructor(public readonly uuid?: string) {
  }

  static from(from: ProjectDTO): Project {
    let project = new Project(from.uuid);
    project.name = from.name;

    project.imageName = from.imageName;
    project.newVersionsDeploymentBehaviour = from.newVersionsDeploymentBehaviour;
    project.urlTemplates = from.urlTemplates || [];
    project.defaultConfigurationTemplates = from.defaultConfigurationTemplates.map(tpl => ConfigurationTemplate.from(tpl)) || [];
    project.templateVariables = from.templateVariables || [];
    project.dockerRegistryUUID = from.dockerRegistryUUID;
    project.versions = from.versions.map(version => ProjectVersion.from(version));
    project.status = from.status;
    project.defaultLifetimeBehaviour = from.defaultLifetimeBehaviour;
    project.namespace = from.namespace;

    return project;
  }

  static fromProjectExport(from: ProjectExportDTO): Project {
    const version = from?.exportMetadata?.version;
    if (version === undefined || version === null) {
      throw new Error(`Project export configuration is invalid.`);
    }
    if (version !== SUPPORTED_VERSION) {
      throw new Error(`Project export version ${version} is not supported.`);
    }

    const project = new Project();

    project.name = from.name;
    project.imageName = from.imageName;
    project.newVersionsDeploymentBehaviour = from.newVersionsDeploymentBehaviour;
    project.dockerRegistryUUID = from.dockerRegistryUUID;
    project.status = from.status;
    project.defaultLifetimeBehaviour = from.defaultLifetimeBehaviour;

    project.urlTemplates = from.urlTemplates;
    // remove potentially existing ids
    project.defaultConfigurationTemplates = (from.defaultConfigurationTemplates as Array<ConfigurationTemplateDTO> ?? [])
      .map(({id, ...configurationTemplate}) => ConfigurationTemplate.from(configurationTemplate as ConfigurationTemplateDTO));
    project.templateVariables = (from.templateVariables as Array<TemplateVariable> ?? [])
      .map(({id, ...templateVariable}) => templateVariable as TemplateVariable);

    return project;
  }

  /**
   * Informs, whether this is a new project (thus a project that has never been persisted so far).
   *
   * @returns {boolean}
   */
  public isNew(): boolean {
    return !this.uuid;
  }

  public isOrphan(): boolean {
    return !this.dockerRegistryUUID;
  }
}
