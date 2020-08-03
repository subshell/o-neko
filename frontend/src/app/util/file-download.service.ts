export class FileDownloadService {
  public static downloadJSON(obj: any, filename: string = 'file.json'): void {
    const json = JSON.stringify(obj, undefined, 2);
    const blob = new Blob([json], {type: 'application/json'});
    FileDownloadService.downloadBlob(blob, filename);
  }

  public static downloadFle(text: string, filename: string = 'file.json', fileType: string = 'application/text'): void {
    const blob = new Blob([text], {type: fileType});
    FileDownloadService.downloadBlob(blob, filename);
  }

  private static downloadBlob(blob: Blob, filename: string) {
    const url = URL.createObjectURL(blob);

    const anchorElement = document.createElement('a');
    document.body.appendChild(anchorElement);
    anchorElement.style.display = 'none';
    anchorElement.href = url;
    anchorElement.download = filename;
    anchorElement.click();
    document.body.removeChild(anchorElement);

    window.URL.revokeObjectURL(url);
  }
}
