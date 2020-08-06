import {Component, EventEmitter, Input, Output} from '@angular/core';
import {TranslateService} from "@ngx-translate/core";

@Component({
  selector: 'file-upload',
  templateUrl: './file-upload.component.html',
  styleUrls: ['./file-upload.component.scss']
})
export class FileUploadComponent {

  @Input() displayType: 'button' | 'menu-item' | 'dnd'  = 'button';
  @Input() disabled: boolean = false;
  @Input() multiple: boolean = true;
  @Input() label: string = 'upload';
  @Input() accept: string = '.yaml, .yml';

  @Output() public filesCallback = new EventEmitter<FileList | File[]>();

  constructor(translate: TranslateService) {
    this.label = translate.instant('components.forms.fileUpload.upload');
  }


  public filesSet({files}) {
    if (files && files[0]) {
      this.filesCallback.emit(files);
    }
  }
}
