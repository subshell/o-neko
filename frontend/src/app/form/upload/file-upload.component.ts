import {Component, EventEmitter, Input, Output} from "@angular/core";

@Component({
  selector: 'file-upload',
  templateUrl: './file-upload.component.html',
  styleUrls: ['./file-upload.component.scss']
})
export class FileUploadComponent {

  @Input() disabled: boolean = false;
  @Input() multiple: boolean = true;

  @Output() public filesCallback = new EventEmitter<FileList | File[]>();

  public filesSet(input) {
    const files = input.files;
    if (files && files[0]) {
      this.filesCallback.emit(files);
    }
  }

}
