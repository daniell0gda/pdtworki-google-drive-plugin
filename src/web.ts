import {WebPlugin} from '@capacitor/core';

import type {GoogleDrivePluginPlugin} from './definitions';

export class GoogleDrivePluginWeb extends WebPlugin implements GoogleDrivePluginPlugin {
  async echo(options: { value: string }): Promise<{ value: string }> {
    console.log('ECHO', options);
    return options;
  }

  async storeAppData(options: { appData: string, authToken: string, syncState:string }): Promise<{ status: string }> {
    console.log('STORE RECIPES', options);
    return {status: 'dummy'};
  }

  async fetchAppData(options: { authToken: string }): Promise<{ appData: string, status: string }> {
    console.log('FETCH RECIPES', options);
    return {
      status: 'dummy',
      appData: '{}'
    };
  }

  async fetchSyncData(options: { authToken: string }): Promise<{
    syncState: string,
    status: string,
    newAccessToken: string}> {
    console.log('FETCH RECIPES', options);
    return {
      status: 'dummy',
      syncState: '{}',
      newAccessToken: ''
    };
  }

  async hasAppDataOnDrive(options: { authToken: string }): Promise<{ result: boolean, status: string }> {
    console.log('FETCH RECIPES', options);
    return {
      status: 'dummy',
      result: true
    };
  }
}
