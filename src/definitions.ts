export interface GoogleDrivePluginPlugin {
  echo(options: { value: string }): Promise<{ value: string }>;
  storeAppData(options: {
    appData: string,
    authToken: string,
    appName:string,
    syncState:string
  }): Promise<{ status: string }>;
  fetchAppData(options: { authToken: string, appName:string }): Promise<{ appData: string, status: string }>;
  fetchSyncData(options: { authToken: string, appName:string }): Promise<{ syncState: string, status: string }>;
  hasAppDataOnDrive(options: { authToken: string }): Promise<{ result: boolean, status: string }>;
}
