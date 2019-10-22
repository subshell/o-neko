import {ConfigurationTemplate} from "./configuration-template";

export interface EffectiveDeployableConfiguration {
  name: string;
  configurationTemplates: Array<ConfigurationTemplate>;
  availableTemplateVariables: { [key: string]: string };
}
