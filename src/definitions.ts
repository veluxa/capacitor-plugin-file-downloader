export interface FileDownloaderPlugin {
  download(options: { url: string, filename: string }): Promise<any>;
}
