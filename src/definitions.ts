export interface GoogleDrivePluginPlugin {
  echo(options: { value: string }): Promise<{ value: string }>;
}
