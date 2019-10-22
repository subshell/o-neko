export interface ConfigurationTemplateDTO {
  id: string
  content: string
  name: string
  description: string
}

export class ConfigurationTemplate implements ConfigurationTemplateDTO {
  id: string;
  content: string;
  name: string;
  description: string;

  public static from(templateDTO: ConfigurationTemplateDTO): ConfigurationTemplate {
    const template = new ConfigurationTemplate();

    template.id = templateDTO.id;
    template.content = templateDTO.content;
    template.description = templateDTO.description;
    template.name = templateDTO.name;

    return template;
  }

  public static clone(template: ConfigurationTemplate): ConfigurationTemplate {
    let tpl = ConfigurationTemplate.from(template);
    tpl.id = null;
    return tpl;
  }
}
