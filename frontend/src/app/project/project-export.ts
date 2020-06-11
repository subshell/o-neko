import {AggregatedDeploymentStatus, DeploymentBehaviour, LifetimeBehaviour, TemplateVariable} from './project';
import {ConfigurationTemplate} from '../deployable/configuration-template';

export interface ProjectExportMetadataDTO {
  version: string;
  exportedAt: string;
}

export interface ProjectExportConfigurationTemplate {
  content: string;
  name: string;
  description: string;
}

export interface ProjectExportTemplateVariable {
  name: string;
  label: string;
  values: Array<string>;
  useValues: boolean;
  defaultValue: string;
  showOnDashboard: boolean;
}

export interface ProjectExportDTO {
  name: string;
  imageName: string;
  newVersionsDeploymentBehaviour: DeploymentBehaviour;
  defaultConfigurationTemplates: Array<ProjectExportConfigurationTemplate>;
  templateVariables: ProjectExportTemplateVariable[];
  dockerRegistryUUID?: string;
  status: AggregatedDeploymentStatus;
  defaultLifetimeBehaviour?: LifetimeBehaviour;
  exportMetadata: ProjectExportMetadataDTO;
}
