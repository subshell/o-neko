import {Component, EventEmitter, Input, OnInit, Output, ViewEncapsulation} from '@angular/core';
import {UntypedFormControl, NG_VALUE_ACCESSOR} from '@angular/forms';
import {MatDialog} from '@angular/material/dialog';
import {MatSnackBar} from '@angular/material/snack-bar';
import {FileReaderService} from '../../form/upload/file-reader.service';
import {ConfirmDialog} from '../../util/confirm-dialog/confirm-dialog.component';
import {ConfigurationTemplate} from '../configuration-template';
import {EditConfigurationTemplateDialogComponent} from './edit-configuration-template-dialog/edit-configuration-template-dialog.component';
import {Select, Store} from "@ngxs/store";
import {ThemingState} from "../../store/theming/theming.state";
import {Observable} from "rxjs";
import {FileDownloadService} from '../../util/file-download.service';
import {TranslateService} from "@ngx-translate/core";
import {RestService} from "../../rest/rest.service";
import {HelmRegistry} from "../../registries/helm/helm-registry";
import {HelmCharts, HelmChartVersion} from "../../registries/helm-charts";

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
  public selectedTab = new UntypedFormControl(0);

  private _fileReaderService: FileReaderService;
  private _skipTextOverwrite: Date = null;

  @Select(ThemingState.isDarkMode) isDarkTheme$: Observable<boolean>;

  public registryNameByRegistryId: {[name: string]: string} = {};
  public chartRegistries: Observable<Array<HelmRegistry>>;
  public chartsByRegistry: {[registry: string]: HelmCharts} = {};

  public editorOptions: any = {
    theme: 'vs-light',
    renderLineHighlight: 'gutter',
    language: 'yaml',
    fontSize: 12,
    scrollBeyondLastLine: false,
    contextmenu: false,
    minimap: {
      enabled: false
    },
    tabSize: 2
  };

  public get currentTemplateModel(): ConfigurationTemplateEditorModel {
    return this.configurationTemplatesModels[this.selectedTab.value];
  }

  public get currentHelmCharts(): HelmCharts {
    return this.chartsByRegistry[this.currentTemplateModel.effectiveTemplate.helmRegistryId];
  }

  public get currentHelmChartNames(): Array<string> {
    if (!this.currentHelmCharts || !this.currentHelmCharts.charts) {
      return [];
    }
    return Object.keys(this.currentHelmCharts.charts);
  }

  public get currentHelmChartVersions(): Array<HelmChartVersion> {
    if (!this.currentHelmCharts) {
      return [];
    }
    return this.currentHelmCharts.charts[this.currentTemplateModel.effectiveTemplate.chartName];
  }

  constructor(private readonly snackBar: MatSnackBar,
              private readonly dialog: MatDialog,
              private store: Store,
              private translate: TranslateService,
              private rest: RestService) {
    this._fileReaderService = new FileReaderService();
    this.isDarkTheme$.subscribe(isDark => {
      this.editorOptions = {
        ...this.editorOptions,
        theme: isDark ? 'vs-dark' : 'vs-light'
      };
    });
    this.chartRegistries = this.rest.helm().getAllHelmRegistries();
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

    this.loadHelmCharts();
  }

  private loadHelmCharts() {
    this.chartRegistries.subscribe(registries => {
      registries.forEach(registry => {
        this.rest.helm().getHelmChartsByRegistry(registry).subscribe(charts => {
          this.chartsByRegistry[registry.getId()] = charts;
          this.registryNameByRegistryId[registry.getId()] = registry.name;
        });
      });
    })
  }

  public removeRegistryPart(chartName: string) {
    return chartName.substr(this.registryNameByRegistryId[this.currentHelmCharts.registryId].length + 1, chartName.length);
  }

  public async onConfigUpload($event: FileList | File[]) {
    const files = await this._fileReaderService.readAll($event);

    for (let file of files) {
      if (typeof file.content !== 'string') {
        this.snackBar.open(this.translate.instant('components.templateEditor.uploadFailedError', {filename: file.name}), null, {
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

  public hasTemplates(): boolean {
    return this.configurationTemplatesModels.length > 0;
  }

  public helmChartSettingsChanged() {
    this.emitValidity();
    this.emitTemplates();
  }

  public onTextOverwrite(content: string, template: ConfigurationTemplateEditorModel) {
    if (this._skipTextOverwrite && this._skipTextOverwrite.getTime() + 50 < new Date().getTime() || content === template.effectiveTemplate.content) {
      this._skipTextOverwrite = null;
      return;
    }

    template.setContent(content);
    this.emitValidity();
    this.emitTemplates();
  }

  public removeTemplateWithConfirmation(template: ConfigurationTemplateEditorModel) {
    const dialogRef = this.dialog.open(ConfirmDialog, {
      width: '250px',
      data: {
        message: '',
        title: this.translate.instant('components.templateEditor.confirmDeletionOfTemplate')
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        // select another tab if the latest one is active and is going to be deleted.
        if (this.selectedTab.value === this.configurationTemplatesModels.length - 1 &&  this.selectedTab.value !== 0) {
          this.selectedTab.setValue(this.selectedTab.value - 1);
        }

        this.configurationTemplatesModels.splice(this.configurationTemplatesModels.indexOf(template), 1);

        if (!this.hasTemplates()) {
          // ensure at least one empty template
          //const template = this.createNewTemplate();
          //this.addNewTemplate(template);
        }

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
    this.emitValidity();
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
      description: '',
      chartName: '',
      chartVersion: '',
      helmRegistryId: ''
    });
  }

  public openEditConfigurationTemplateDialog(currentTemplateModel: ConfigurationTemplateEditorModel) {
    const dialogRef = this.dialog.open(EditConfigurationTemplateDialogComponent, {
      width: '250px',
      data: {
        model: currentTemplateModel,
        models: this.configurationTemplatesModels
      }
    });

    dialogRef.afterClosed().subscribe(({valid, filename, description}) => {
      if (valid) {
        this.currentTemplateModel.name = filename;
        this.currentTemplateModel.description = description;
        this.emitValidity();
      }
    });
  }

  public onDownloadCurrentFile(currentTemplateModel: ConfigurationTemplateEditorModel): void {
    FileDownloadService.downloadFle(currentTemplateModel.template.content, `${currentTemplateModel.name}.yaml`, 'text/yaml');
  }

  private isValid(): boolean {
    return this.templates.every(tpl => tpl.isValid());
  }

  private emitValidity() {
    this.templatesValid.emit(this.isValid());
  }
}
