import {Component, EventEmitter, Input, OnInit, Output, ViewEncapsulation} from '@angular/core';
import {TemplateVariable} from "../project";
import {ProjectVersion} from "../project-version";

export interface ProjectVersionVariableActionChangeEvent {
  version: ProjectVersion
}

@Component({
  selector: 'project-version-variable-actions',
  templateUrl: './project-version-variable-actions.component.html',
  styleUrls: ['./project-version-variable-actions.component.scss'],
  encapsulation: ViewEncapsulation.None
})
export class ProjectVersionVariableActionsComponent implements OnInit {

  @Input()
  public hasEditPermission: boolean;

  @Input()
  public version: ProjectVersion;

  @Output()
  public onVariableChange: EventEmitter<ProjectVersionVariableActionChangeEvent> = new EventEmitter<ProjectVersionVariableActionChangeEvent>();

  public get availableTemplateVariables(): Array<TemplateVariable> {
    return this.version.availableTemplateVariables.filter(templateVariable => templateVariable.showOnDashboard);
  }

  public ngOnInit(): void {
    for (let availableTemplateVariable of this.version.availableTemplateVariables) {
      this.version.templateVariables[availableTemplateVariable.name] = this.version.templateVariables[availableTemplateVariable.name] || availableTemplateVariable.defaultValue;
    }
  }

  public emitOnChange() {
    this.onVariableChange.emit({
      version: this.version
    });
  }
}
