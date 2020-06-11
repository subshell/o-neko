import {Component, EventEmitter, Input, Output} from '@angular/core';

@Component({
  selector: 'file-upload',
  templateUrl: './file-upload.component.html',
  styleUrls: ['./file-upload.component.scss']
})
export class FileUploadComponent {

  @Input() displayType: string = 'button';
  @Input() disabled: boolean = false;
  @Input() multiple: boolean = true;
  @Input() label: string = 'upload';
  @Input() accept: string = '.yaml, .yml';

  @Output() public filesCallback = new EventEmitter<FileList | File[]>();

  public filesSet({files}) {
    if (files && files[0]) {
      this.filesCallback.emit(files);
    }
  }
}
