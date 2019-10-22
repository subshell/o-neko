import {isArray} from 'lodash';
import {UploadedFile} from "./uploaded-file";

export class FileReaderService {

  constructor() {
  }

  private static fileListToArray(fileList: FileList | File[]): File[] {
    if (isArray(fileList)) {
      return fileList;
    }

    const files = [];
    for (let i = 0; i < fileList.length; i++) {
      files.push(fileList.item(i));
    }
    return files;
  }

  public read(file: File): Promise<UploadedFile> {
    return this.readAll([file]).then(files => files[0]);
  }

  public readAll(fileList: FileList | File[]): Promise<UploadedFile[]> {
    const files = FileReaderService.fileListToArray(fileList);

    return Promise.all(files.map(file => {
      const reader = new FileReader();
      reader.readAsText(file);
      return new Promise<UploadedFile>((res) => {
        reader.onload = () => res(new UploadedFile(reader.result, file.type, file.name));
      });
    }));
  }
}
