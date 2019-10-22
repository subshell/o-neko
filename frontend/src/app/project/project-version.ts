import {cloneDeep} from "lodash";
import * as moment from "moment";
import {ConfigurationTemplate} from "../deployable/configuration-template";
import {Deployment, DeploymentDTO, DesiredState} from "../deployable/deployment";
import {Namespace, NamespaceDTO} from "../namespace/namespace";
import {DeploymentBehaviour, LifetimeBehaviour, TemplateVariable} from "./project";

export interface ProjectVersionDTO {
  uuid: string;
  name: string;
  deploymentBehaviour: DeploymentBehaviour;
  templateVariables?: { [key: string]: string };
  availableTemplateVariables: TemplateVariable[];
  deployment: DeploymentDTO;
  urls: Array<string>;
  configurationTemplates: Array<ConfigurationTemplate>;
  outdated: boolean
  lifetimeBehaviour?: LifetimeBehaviour;
  namespace: NamespaceDTO;
  implicitNamespace: NamespaceDTO;
  desiredState: DesiredState;
  imageUpdatedDate: string | Date;
}

export class ProjectVersion implements ProjectVersionDTO {
  uuid: string;
  name: string;
  deploymentBehaviour: DeploymentBehaviour;
  templateVariables?: { [key: string]: string };
  availableTemplateVariables: TemplateVariable[];
  deployment: Deployment;
  urls: Array<string>;
  configurationTemplates: Array<ConfigurationTemplate>;
  outdated: boolean;
  lifetimeBehaviour?: LifetimeBehaviour;
  namespace: Namespace;
  implicitNamespace: Namespace;
  desiredState: DesiredState;
  imageUpdatedDate: Date;

  public get hasCustomTemplates(): boolean {
    return this.configurationTemplates && this.configurationTemplates.length > 0;
  }

  get formattedImageUpdatedDate(): string {
    if (this.imageUpdatedDate === null) {
      return null;
    }

    let now = new Date();
    let imageupdateDateMoment = moment(this.imageUpdatedDate);
    if (moment(now).isSame(imageupdateDateMoment, 'day')) {
      return imageupdateDateMoment.format('HH:mm:ss');
    } else if (moment(now).isSame(imageupdateDateMoment, 'month')) {
      return imageupdateDateMoment.format('Do MMM HH:mm:ss');
    }
    return imageupdateDateMoment.format('DD/MM/YY HH:mm:ss');
  }

  static from(from: ProjectVersionDTO): ProjectVersion {
    let version = new ProjectVersion();
    version.uuid = from.uuid;
    version.name = from.name;
    version.deploymentBehaviour = from.deploymentBehaviour;
    version.templateVariables = cloneDeep(from.templateVariables);
    version.availableTemplateVariables = cloneDeep(from.availableTemplateVariables);
    version.deployment = Deployment.from(from.deployment);
    version.urls = from.urls;
    version.configurationTemplates = from.configurationTemplates;
    version.outdated = from.outdated;
    version.lifetimeBehaviour = from.lifetimeBehaviour;
    version.namespace = Namespace.from(from.namespace);
    version.implicitNamespace = Namespace.from(from.implicitNamespace);
    version.desiredState = from.desiredState;
    version.imageUpdatedDate = from.imageUpdatedDate ? new Date(from.imageUpdatedDate) : null;
    return version;
  }

  public mergeFrom(from: ProjectVersionDTO): void {
    this.uuid = from.uuid;
    this.name = from.name;
    this.deploymentBehaviour = from.deploymentBehaviour;
    this.templateVariables = cloneDeep(from.templateVariables);
    this.availableTemplateVariables = cloneDeep(from.availableTemplateVariables);
    this.deployment = Deployment.from(from.deployment);
    this.urls = from.urls;
    this.configurationTemplates = from.configurationTemplates;
    this.outdated = from.outdated;
    this.lifetimeBehaviour = from.lifetimeBehaviour;
    this.desiredState = from.desiredState;
    this.imageUpdatedDate = from.imageUpdatedDate ? new Date(from.imageUpdatedDate) : null;
  }

}
