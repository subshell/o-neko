import {Component, EventEmitter, Input, OnInit, Output, ViewChild} from "@angular/core";
import {MatLegacyInput as MatInput} from "@angular/material/legacy-input";

@Component({
  selector: 'on-url-templates-input',
  templateUrl: './url-templates.component.html',
  styleUrls: ['./url-templates.component.scss']
})
export class UrlTemplatesComponent implements OnInit {

  @Input("urlTemplates")
  public _urlTemplates: Array<string> = [];
  public urlTemplates: Array<string> = [];

  @Input("inheritedUrlTemplates")
  public _inheritedUrlTemplates: Array<string> = [];
  public inheritedUrlTemplates: Array<string> = [];

  @Output()
  public templatesChange = new EventEmitter<Array<string>>();

  @ViewChild("input", {static: true})
  public input: MatInput;
  textInput: string;

  ngOnInit(): void {
    this.urlTemplates = Array.of(...this._urlTemplates);
    this.inheritedUrlTemplates = Array.of(...this._inheritedUrlTemplates);
  }

  emitChanges(): void {
    this.templatesChange.emit(this.urlTemplates);
  }

  deleteUrl(template: string) {
    this.urlTemplates = this.urlTemplates.filter(tpl => tpl != template);
    this.emitChanges();
  }

  public addUrl(): void {
    this.urlTemplates.push(this.textInput);
    this.input.value = "";
    this.emitChanges();
  }

  trackByIndex(index: number, el: any): number {
    return index;
  }

}
