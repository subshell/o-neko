export interface ConfigurationTemplateDTO {
  id: string
  content: string
  name: string
  description: string
  chartName: string
  chartVersion: string
  helmRegistryId: string
}

export class ConfigurationTemplate implements ConfigurationTemplateDTO {
  id: string;
  content: string;
  name: string;
  description: string;
  chartName: string;
  chartVersion: string;
  helmRegistryId: string;

  public static from(templateDTO: ConfigurationTemplateDTO): ConfigurationTemplate {
    const template = new ConfigurationTemplate();

    template.id = templateDTO.id;
    template.content = templateDTO.content;
    template.description = templateDTO.description;
    template.name = templateDTO.name;
    template.chartName = templateDTO.chartName;
    template.chartVersion = templateDTO.chartVersion;
    template.helmRegistryId = templateDTO.helmRegistryId;

    return template;
  }

  public static clone(template: ConfigurationTemplate): ConfigurationTemplate {
    let tpl = ConfigurationTemplate.from(template);
    tpl.id = null;
    return tpl;
  }
}
