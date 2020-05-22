import {Component, EventEmitter, Input, OnInit, Output, ViewEncapsulation} from "@angular/core";
import {FormControl, NG_VALUE_ACCESSOR} from "@angular/forms";
import {MatDialog} from "@angular/material/dialog";
import {MatSnackBar} from "@angular/material/snack-bar";
import {FileReaderService} from "../../form/upload/file-reader.service";
import {ConfirmDialog} from "../../util/confirm-dialog/confirm-dialog.component";
import {ConfigurationTemplate} from "../configuration-template";
import IStandaloneEditorConstructionOptions = monaco.editor.IStandaloneEditorConstructionOptions;

export class ConfigurationTemplateEditorModel {
  constructor(public template?: ConfigurationTemplate, public defaultTemplate?: ConfigurationTemplate) {
  }

  public get name(): string {
    return this.effectiveTemplate.name;
  }

  public set name(name: string) {
    this.effectiveTemplate.name = name;
  }

  public get description(): string {
    return this.effectiveTemplate.description;
  }

  public set description(description: string) {
    this.effectiveTemplate.description = description;
  }

  public get effectiveTemplate(): ConfigurationTemplate {
    return this.template ? this.template : this.defaultTemplate;
  }

  public isOverwriting(): boolean {
    return !!this.template && !!this.defaultTemplate;
  }

  public isOriginal(): boolean {
    return this.template && !this.defaultTemplate;
  }

  public setContent(content: string): void {
    if (!this.template) {
      this.template = ConfigurationTemplate.clone(this.defaultTemplate);
    }
    this.template.content = content;
  }

  public resetTemplate(): void {
    this.template = undefined;
  }
}

@Component({
  selector: 'template-editor',
  templateUrl: './template-editor.html',
  styleUrls: ['./template-editor.scss'],
  encapsulation: ViewEncapsulation.None,
  providers: [
    {provide: NG_VALUE_ACCESSOR, useExisting: TemplateEditorComponent, multi: true}
  ]
})
export class TemplateEditorComponent implements OnInit {
  @Input()
  public templates: Array<ConfigurationTemplate> = [];
  @Input()
  public defaultTemplates: Array<ConfigurationTemplate> = [];
  @Input()
  public label: string = 'Configuration Template';
  @Input()
  public readonly: boolean = false;
  @Output()
  public templatesChange: EventEmitter<Array<ConfigurationTemplate>> = new EventEmitter();
  @Output()
  public templatesValid: EventEmitter<boolean> = new EventEmitter<boolean>();
  public configurationTemplatesModels: Array<ConfigurationTemplateEditorModel> = [];
  public selectedTab = new FormControl(0);

  private _fileReaderService: FileReaderService;
  private _skipTextOverwrite: Date = null;

  public readonly editorOptions: IStandaloneEditorConstructionOptions = {
    theme: 'vs-light',
    renderLineHighlight: "gutter",
    language: 'yaml',
    fontSize: 12,
    scrollBeyondLastLine: false,
    contextmenu: false,
    minimap: {
      enabled: false
    }
  };

  constructor(private snackBar: MatSnackBar, private dialog: MatDialog) {
    this._fileReaderService = new FileReaderService();
  }

  ngOnInit(): void {
    // custom templates
    for (let template of this.templates) {
      this.configurationTemplatesModels.push(new ConfigurationTemplateEditorModel(template));
    }

    if (!this.defaultTemplates) {
      return;
    }

    for (let template of this.defaultTemplates) {
      let tpl = this.configurationTemplatesModels.find(tpl => tpl.name === template.name);
      if (tpl) {
        tpl.defaultTemplate = template;
      } else {
        this.configurationTemplatesModels.push(new ConfigurationTemplateEditorModel(null, template));
      }
    }
  }

  public async onConfigUpload($event: FileList | File[]) {
    const files = await this._fileReaderService.readAll($event);

    for (let file of files) {
      if (typeof file.content !== 'string') {
        this.snackBar.open(`Could not upload file ${file.name}`, null, {
          duration: 1000
        });
        return;
      }

      const template = this.createNewTemplate();
      template.content = file.content;
      template.name = file.name;
      this.addNewTemplate(template);
    }
  }

  public onAddNewTemplate() {
    const template = this.createNewTemplate();
    this.addNewTemplate(template);
  }

  public onTextOverwrite(content: string, template: ConfigurationTemplateEditorModel) {
    if (this._skipTextOverwrite && this._skipTextOverwrite.getTime() + 50 < new Date().getTime() || content === template.effectiveTemplate.content) {
      this._skipTextOverwrite = null;
      return;
    }

    template.setContent(content);
    this.emitTemplates();
  }

  public removeTemplateWithConfirmation(template: ConfigurationTemplateEditorModel) {
    const dialogRef = this.dialog.open(ConfirmDialog, {
      width: '250px',
      data: {
        message: '',
        title: 'Do you really want to delete this template?'
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.configurationTemplatesModels.splice(this.configurationTemplatesModels.indexOf(template), 1);
        this.emitTemplates();
      }
    });
  }

  public useDefaultProjectConfigurationTemplate(template: ConfigurationTemplateEditorModel): void {
    template.resetTemplate();
    this.emitTemplates();

    this._skipTextOverwrite = new Date();
  }

  private addNewTemplate(template: ConfigurationTemplate) {
    let configurationTemplateEditorModel = new ConfigurationTemplateEditorModel(template);
    this.configurationTemplatesModels.push(configurationTemplateEditorModel);
    template.name = template.name || `new_${this.configurationTemplatesModels.indexOf(configurationTemplateEditorModel) + 1}.yaml`;
    setTimeout(() => {
      this.selectedTab.setValue(this.configurationTemplatesModels.length - 1);
    }, 0);
    this.emitTemplates();
  }

  private emitTemplates() {
    this.templatesChange.emit(this.configurationTemplatesModels
      .filter(template => !!template.template)
      .map(template => template.template));
  }

  private createNewTemplate(): ConfigurationTemplate {
    return ConfigurationTemplate.from({
      id: null,
      name: '',
      content: '',
      description: ''
    });
  }

  private getIllegalTemplateNamesFor(template: ConfigurationTemplateEditorModel): Array<string> {
    return this.configurationTemplatesModels
      .filter(t => t !== template)
      .map(t => t.name);
  }

  private checkValidNames(): void {
    const names = this.configurationTemplatesModels
      .map(ct => ct.name);
    let counts = [];
    let namesValid = true;
    for (let name of names) {
      if (name && counts[name] === undefined) {
        counts[name] = 1;
      } else {
        namesValid = false;
      }
    }
    this.templatesValid.emit(namesValid);
  }

}
