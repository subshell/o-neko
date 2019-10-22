import {cloneDeep, map} from "lodash";
import {ConfigurationTemplate, ConfigurationTemplateDTO} from "../deployable/configuration-template";
import {Deployment, DeploymentDTO, DesiredState} from "../deployable/deployment";
import {Namespace} from "../namespace/namespace";

export interface MeshComponentDTO {
  id?: string;
  name: string;
  projectId: string;
  projectVersionId: string;
  templateVariables?: { [key: string]: string };
  configurationTemplates: Array<ConfigurationTemplateDTO>;
  outdated: boolean;
  urls: Array<string>;
  deployment: DeploymentDTO;
  desiredState: DesiredState;
}

export class MeshComponent implements MeshComponentDTO {
  name: string;
  namespace: Namespace;
  projectId: string;
  projectVersionId: string;
  templateVariables?: { [key: string]: string };
  configurationTemplates: Array<ConfigurationTemplate>;
  outdated: boolean;
  urls: Array<string>;
  deployment: Deployment;
  desiredState: DesiredState;

  constructor(public readonly id?: string) {
    this.configurationTemplates = [];
    this.urls = [];
    this.templateVariables = {};
  }

  static from(from: MeshComponentDTO): MeshComponent {
    let component = new MeshComponent(from.id);
    component.name = from.name;
    component.projectId = from.projectId;
    component.projectVersionId = from.projectVersionId;
    component.templateVariables = cloneDeep(from.templateVariables);
    component.configurationTemplates = map(from.configurationTemplates, ct => ConfigurationTemplate.from(ct));
    component.outdated = from.outdated;
    component.urls = from.urls;
    component.deployment = Deployment.from(from.deployment);
    component.desiredState = from.desiredState;
    return component;
  }

  public isNew(): boolean {
    return !this.id;
  }
}
