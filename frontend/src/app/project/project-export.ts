import {AggregatedDeploymentStatus, DeploymentBehaviour, LifetimeBehaviour} from './project';

export interface ProjectExportMetadataDTO {
  version: number;
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
  urlTemplates: Array<string>;
  defaultConfigurationTemplates: Array<ProjectExportConfigurationTemplate>;
  templateVariables: ProjectExportTemplateVariable[];
  dockerRegistryUUID?: string;
  status: AggregatedDeploymentStatus;
  defaultLifetimeBehaviour?: LifetimeBehaviour;
  exportMetadata: ProjectExportMetadataDTO;
}

export const SUPPORTED_VERSION = 0;
