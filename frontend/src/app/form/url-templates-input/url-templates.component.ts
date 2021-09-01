import {Component, EventEmitter, Input, OnInit, Output, ViewChild} from "@angular/core";
import {MatInput} from "@angular/material/input";

@Component({
  selector: 'on-url-templates-input',
  templateUrl: './url-templates.component.html',
  styleUrls: ['./url-templates.component.scss']
})
export class UrlTemplatesComponent implements OnInit {

  @Input("urlTemplates")
  public _urlTemplates: Array<string> = [];
  public urlTemplates: Array<string> = [];

  @Input("readOnlyUrlTemplates")
  public _readOnlyTemplates: Array<string> = [];
  public readOnlyTemplates: Array<string> = [];

  @Output()
  public templatesChange = new EventEmitter<Array<string>>();

  @ViewChild("input", {static: true})
  public input: MatInput;
  textInput: string;

  ngOnInit(): void {
    this.urlTemplates = Array.of(...this._urlTemplates);
    this.readOnlyTemplates = Array.of(...this._readOnlyTemplates);
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

}
